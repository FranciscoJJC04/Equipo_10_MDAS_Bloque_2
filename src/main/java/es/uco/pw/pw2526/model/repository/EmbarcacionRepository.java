package es.uco.pw.pw2526.model.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;
import es.uco.pw.pw2526.model.domain.patron.Patron;

/**
 * Repositorio para operaciones sobre embarcaciones.
 * <p>
 * Este repositorio incluye métodos para insertar, consultar, actualizar y eliminar embarcaciones.
 * También ofrece funcionalidades para la asignación de patrones a embarcaciones y la comprobación de solapamientos en asignaciones.
 * </p>
 */
@Repository
public class EmbarcacionRepository extends AbstractRepository {

    /**
     * Constructor que recibe el {@link JdbcTemplate} proporcionado por Spring.
     *
     * @param jdbcTemplate plantilla JDBC usada para ejecutar las consultas.
     */
    public EmbarcacionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Inserta una nueva embarcación en la base de datos.
     *
     * Este método recibe un objeto {@link Embarcacion} con los datos necesarios y lo inserta en la base de datos.
     *
     * @param embarcacion objeto {@link Embarcacion} con los datos a insertar
     * @return {@code true} si la operación fue exitosa, {@code false} en caso de error
     */
    public boolean addEmbarcacion(Embarcacion embarcacion) {
        try {
            String query = sqlQueries.getProperty("insertar-embarcacion");
            if (query == null) {
                System.err.println("No se encontró la query 'insertar-embarcacion' en sql.properties");
                return false;
            }

            int result = jdbcTemplate.update(query,
                    embarcacion.getMatricula(),
                    embarcacion.getTipo().toString(),
                    embarcacion.getNombre(),
                    embarcacion.getNumPlazas());
            return result > 0;

        } catch (DataAccessException e) {
            System.err.println("Error al insertar embarcación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista las embarcaciones filtradas por tipo.
     *
     * Este método consulta la base de datos y devuelve una lista de embarcaciones del tipo especificado.
     *
     * @param tipo tipo de embarcación a filtrar
     * @return lista de {@link Embarcacion} que coinciden con el tipo, o {@code null} si ocurre un error
     */
    public List<Embarcacion> listarPorTipo(TipoEmbarcacion tipo) {
        try {
            String query = sqlQueries.getProperty("listar-embarcacion-por-tipo");
            if (query == null) {
                System.err.println("No se encontró la query 'listar-embarcacion-por-tipo'");
                return null;
            }

            return jdbcTemplate.query(query, new RowMapper<Embarcacion>() {
                @Override
                public Embarcacion mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new Embarcacion(
                            rs.getString("matricula"),
                            TipoEmbarcacion.valueOf(rs.getString("tipo")),
                            rs.getString("nombre"),
                            rs.getInt("num_plazas"));
                }
            }, tipo.toString());

        } catch (DataAccessException e) {
            System.err.println("Error al listar embarcaciones por tipo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Asigna un patrón a una embarcación durante el intervalo especificado.
     *
     * Este método asigna un patrón a una embarcación para las fechas indicadas.
     * 
     * @param fechaInicio fecha de inicio de la asignación
     * @param fechaFin fecha de fin de la asignación
     * @param embarcacion embarcación a la que se asigna el patrón
     * @param patron patrón asignado
     * @return {@code true} si la asignación se guardó correctamente
     */
    public boolean addPatronAEmbarcacion(LocalDate fechaInicio, LocalDate fechaFin, Embarcacion embarcacion,
            Patron patron) {
        try {
            String query = sqlQueries.getProperty("asignar-patron-embarcacion");
            if (query == null) {
                System.err.println("No se encontró la query 'asignar-patron-embarcacion' en sql.properties");
                return false;
            }

            int result = jdbcTemplate.update(query,
                    java.sql.Date.valueOf(fechaInicio), // fecha_asignacion
                    java.sql.Date.valueOf(fechaFin),
                    embarcacion.getMatricula(),
                    patron.getDni_patron());
            return result > 0;

        } catch (DataAccessException e) {
            System.err.println("Error al asignar patrón a embarcación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Comprueba si existe solapamiento de asignaciones en la embarcación especificada.
     * Si {@code fechaFin} es {@code null} se usa la consulta para asignaciones abiertas.
     *
     * @param matricula matrícula de la embarcación
     * @param fechaInicio fecha de inicio a comprobar
     * @param fechaFin fecha de fin a comprobar (o {@code null})
     * @return {@code true} si existe solapamiento, {@code false} en caso contrario
     */
    public boolean existeSolapamientoEmbarcacion(String matricula, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin == null) {
            String sql = sqlQueries.getProperty("check-embarcacion-solapamiento-open");
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                    matricula, Date.valueOf(fechaInicio));
            return count != null && count > 0;
        } else {
            String sql = sqlQueries.getProperty("check-embarcacion-solapamiento-range");
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                    matricula, Date.valueOf(fechaFin), Date.valueOf(fechaInicio));
            return count != null && count > 0;
        }
    }

    /**
     * Obtiene el listado de asignaciones (patrón ↔ embarcación) tal como lo define la consulta SQL correspondiente.
     *
     * @return lista de mapas (cada mapa representa una fila) o {@code null} si la query no existe o hay un error
     */
    public java.util.List<java.util.Map<String, Object>> listarAsignaciones() {
        try {
            String q = sqlQueries.getProperty("listar-patron-embarcacion");
            if (q == null) {
                System.err.println(" No se encontró la query 'listar-patron-embarcacion' en sql.properties");
                return null;
            }
            return jdbcTemplate.queryForList(q);
        } catch (DataAccessException e) {
            System.err.println("Error listando asignaciones: " + e.getMessage());
            return null;
        }
    }

    /**
     * Devuelve la lista completa de embarcaciones registradas.
     *
     * Este método consulta la base de datos y devuelve todas las embarcaciones registradas.
     *
     * @return lista de {@link Embarcacion} o {@code null} si ocurre un error
     */
    public List<Embarcacion> listarEmbarcaciones() {
        try {
            String query = sqlQueries.getProperty("listar-embarcaciones");
            if (query == null) {
                System.err.println("No se encontró la query 'listar-embarcaciones' en sql.properties");
                return null;
            }

            return jdbcTemplate.query(query, new RowMapper<Embarcacion>() {
                @Override
                public Embarcacion mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new Embarcacion(
                            rs.getString("matricula"),
                            TipoEmbarcacion.valueOf(rs.getString("tipo")),
                            rs.getString("nombre"),
                            rs.getInt("num_plazas"));
                }
            });

        } catch (DataAccessException e) {
            System.err.println("Error al listar embarcaciones: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si una embarcación está vinculada a un alquiler o reserva.
     *
     * Este método consulta la base de datos para comprobar si la embarcación está vinculada a algún alquiler o reserva.
     *
     * @param matricula matrícula de la embarcación
     * @return {@code true} si la embarcación está vinculada a un alquiler o reserva, {@code false} en caso contrario
     */
    public boolean isEmbarcacionLinkedToAnyAlquilerOrReserva(String matricula) {
        try {
            String query = sqlQueries.getProperty("check-embarcacion-linked-to-alquiler-reserva");
            if (query != null) {
                Integer count = jdbcTemplate.queryForObject(query, Integer.class, matricula);
                return count != null && count > 0;
            }
            return false;
        } catch (DataAccessException ex) {
            System.err.println(
                    "Error al comprobar si la embarcación está vinculada a un alquiler o reserva: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Elimina una embarcación de la base de datos.
     *
     * Este método elimina una embarcación usando su matrícula.
     *
     * @param matricula matrícula de la embarcación a eliminar
     * @return {@code true} si la operación fue exitosa, {@code false} en caso de error
     */
    public boolean deleteEmbarcacion(String matricula) {
        try {
            String query = sqlQueries.getProperty("delete-embarcacion");
            if (query != null) {
                int result = jdbcTemplate.update(query, matricula);
                return result > 0;
            }
            return false;
        } catch (DataAccessException ex) {
            System.err.println("Error al eliminar embarcación: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Busca una embarcación por su matrícula.
     *
     * Este método consulta la base de datos y devuelve la embarcación correspondiente a la matrícula proporcionada.
     *
     * @param matricula matrícula de la embarcación a buscar
     * @return un objeto {@link Embarcacion} si se encuentra, o {@code null} si no existe o ocurre un error
     */
    public Embarcacion findByMatricula(String matricula) {
        try {
            if (matricula == null || matricula.isBlank()) {
                return null;
            }

            String query = sqlQueries.getProperty("buscar-embarcacion-por-matricula");
            if (query == null) {
                System.err.println("No se encontró la query 'buscar-embarcacion-por-matricula' en sql.properties");
                return null;
            }

            List<Embarcacion> embarcaciones = jdbcTemplate.query(query, new RowMapper<Embarcacion>() {
                @Override
                public Embarcacion mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Embarcacion e = new Embarcacion();
                    e.setMatricula(rs.getString("matricula"));
                    e.setTipo(TipoEmbarcacion.valueOf(rs.getString("tipo")));
                    e.setNombre(rs.getString("nombre"));
                    e.setNumPlazas(rs.getInt("num_plazas"));
                    return e;
                }
            }, matricula);

            return embarcaciones.isEmpty() ? null : embarcaciones.get(0);
        } catch (DataAccessException e) {
            System.err.println("Error al buscar embarcación por matrícula: " + e.getMessage());
            return null;
        }
    }

    /**
     * Actualiza los datos de una embarcación existente.
     *
     * Este método actualiza los datos de una embarcación en la base de datos, como su tipo, nombre y número de plazas.
     *
     * @param embarcacion objeto {@link Embarcacion} con los nuevos datos
     * @return {@code true} si la operación fue exitosa, {@code false} en caso de error
     */
    public boolean updateEmbarcacion(Embarcacion embarcacion) {
        try {
            String query = sqlQueries.getProperty("actualizar-embarcacion");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        embarcacion.getTipo().toString(),
                        embarcacion.getNombre(),
                        embarcacion.getNumPlazas(),
                        embarcacion.getMatricula());
                return result > 0;
            }
            return false;
        } catch (DataAccessException e) {
            System.err.println("Error al actualizar embarcación: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un patrón de una embarcación.
     *
     * Este método elimina la relación entre un patrón y una embarcación.
     *
     * @param embarcacion embarcación de la cual se desvincula el patrón
     * @param patron patrón a desvincular
     * @return {@code true} si la desvinculación fue exitosa, {@code false} en caso de error
     */
    public boolean removePatronFromEmbarcacion(Embarcacion embarcacion, Patron patron) {
        try {
            String query = sqlQueries.getProperty("remove-patron-from-embarcacion");
            if (query != null) {
                int result = jdbcTemplate.update(query, embarcacion.getMatricula(), patron.getDni_patron());
                return result > 0;
            }
            return false;
        } catch (DataAccessException e) {
            System.err.println("Error al desvincular patrón de embarcación: " + e.getMessage());
            return false;
        }
    }
}
