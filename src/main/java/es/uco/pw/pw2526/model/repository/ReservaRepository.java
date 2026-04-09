package es.uco.pw.pw2526.model.repository;

import java.sql.Date;
// import java.io.InputStream;
// import java.util.Properties;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;
import es.uco.pw.pw2526.model.domain.patron.Patron;
import es.uco.pw.pw2526.model.domain.reserva.Reserva;
import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;

/**
 * Repositorio para operaciones relacionadas con reservas.
 * <p>Provee métodos para insertar y consultar reservas y plazas de embarcaciones.</p>
 */
@Repository
public class ReservaRepository extends AbstractRepository {

    /**
     * Constructor que inicializa la plantilla JDBC.
     *
     * @param jdbcTemplate plantilla JDBC utilizada para ejecutar consultas
     */
    public ReservaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Inserta una nueva reserva en la base de datos tras validar plazas y
     * calcular el importe total.
     *
     * @param reserva objeto {@link Reserva} con los datos a insertar
     * @return {@code true} si la inserción fue correcta, {@code false} en caso de error
     *         o si la reserva no cumple las condiciones
     */
    public boolean addReserva(Reserva reserva) {
        try {
            String query = sqlQueries.getProperty("insertar-reserva");
            if (query == null) {
                System.err.println(" No se encontró la query 'insertar-reserva' en sql.properties");
                return false;
            }

            // Calcular importe total: 40€ por persona y por día (días inclusivos)
            long dias = java.time.temporal.ChronoUnit.DAYS.between(reserva.getFecha(), reserva.getFecha()) + 1;
            if (dias <= 0) {
                System.err.println(" Número de días inválido: " + dias);
                return false;
            }
            double importe = 40.0 * reserva.getNumPasajeros() * (double) dias;
            reserva.setImporteTotal(importe);
            Integer plazas = obtenerPlazas(reserva.getMatricula());

            if (plazas == null) {
                System.err.println(
                        " No se pudo obtener el número de plazas para la embarcación " + reserva.getMatricula());
                return false;
            }

            if (reserva.getNumPasajeros() > plazas) {
                System.err.println(" Número de pasajeros excede las plazas disponibles (" + reserva.getNumPasajeros()
                        + "/" + plazas + ")");
                return false;
            }

            // Usar java.sql.Date para evitar problemas de mapeo con LocalDate
            java.sql.Date sqlFecha = java.sql.Date.valueOf(reserva.getFecha());
            int result = jdbcTemplate.update(query,
                    reserva.getId(),
                    sqlFecha,
                    reserva.getDniSocio(),
                    reserva.getMatricula(),
                    reserva.getImporteTotal(),
                    reserva.getNumPasajeros(),
                    reserva.getDescripcionReserva());

            return result > 0;

        } catch (DataAccessException e) {
            System.err.println(" Error al insertar reserva: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cuenta el número de reservas que solapan un rango de fechas para una
     * embarcación dada.
     *
     * @param matricula matrícula de la embarcación
     * @param inicio    fecha de inicio (inclusive)
     * @param fin       fecha de fin (inclusive)
     * @return número de reservas solapadas (0 si no hay o en caso de error)
     */
    public Integer countReservasSolapados(String matricula, java.time.LocalDate inicio, java.time.LocalDate fin) {
        try {
            String q = sqlQueries.getProperty("check-reserva-range");
            if (q == null) {
                System.err.println(" No se encontró la query 'check-reserva-range' en sql.properties");
                return 0; // Cambiar a 0 en lugar de null
            }
            Integer solapamientos = jdbcTemplate.queryForObject(q, Integer.class, matricula,
                    java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(fin));
            return (solapamientos != null) ? solapamientos : 0; // Asegurarse de que nunca sea null
        } catch (DataAccessException e) {
            System.err.println(" Error contando reservas solapadas: " + e.getMessage());
            return 0; // Asegurarse de devolver 0 en caso de error
        }
    }

    /**
     * Obtiene el número de plazas de una embarcación por su matrícula.
     *
     * @param matricula matrícula de la embarcación
     * @return número de plazas o {@code null} si no se pudo determinar
     */
    public Integer obtenerPlazas(String matricula) {
        try {
            String q = sqlQueries.getProperty("select-embarcacion-num-plazas");

            if (q == null) {
                System.err.println("No se encontró la query 'select-embarcacion-num-plazas' en sql.properties");
                return null;
            }

            if (matricula == null || matricula.isEmpty()) {
                System.err.println("La matrícula proporcionada es inválida: " + matricula);
                return null;
            }

            return jdbcTemplate.queryForObject(q, Integer.class, matricula);
        } catch (DataAccessException e) {
            System.err.println("Error obteniendo plazas de embarcación: " + e.getMessage());
            e.printStackTrace(); // Esto puede ayudarte a obtener más detalles del error
            return null;
        }
    }

    /**
     * Recupera la lista de reservas existentes.
     *
     * @return lista de {@link Reserva} o {@code null} si no hay consulta
     *         configurada o en caso de error
     */
    public List<Reserva> obtenerReservas() {
        try {
            String query = sqlQueries.getProperty("listar-reservas");
            if (query != null) {
                List<Reserva> result = jdbcTemplate.query(query, new RowMapper<Reserva>() {
                    public Reserva mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        Reserva r = new Reserva();
                        r.setId(rs.getInt("id"));
                        java.sql.Date fi = rs.getDate("fecha");
                        r.setDniSocio(rs.getString("dni_socio"));
                        r.setMatricula(rs.getString("matricula"));
                        r.setDescripcionReserva(rs.getString("descripcion_reserva"));

                        // Asignación de los valores de los pasajeros y el importe
                        r.setNumPasajeros(rs.getInt("num_pasajeros_reserva"));
                        r.setImporteTotal(rs.getDouble("importe_reserva"));

                        // Convertir la fecha SQL a LocalDate
                        if (fi != null) {
                            r.setFecha(fi.toLocalDate());
                        }

                        return r;
                    }
                });
                return result;
            } else {
                return null;
            }
        } catch (DataAccessException exception) {
            System.err.println("Unable to find reservas");
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Lista las matrículas de embarcaciones disponibles entre dos fechas.
     *
     * @param inicio fecha de inicio (inclusive)
     * @param fin    fecha de fin (inclusive)
     * @return lista de matrículas disponibles o {@code null} en caso de
     *         error
     */
    public List<String> listarEmbarcacionesDisponiblesPorFecha(java.time.LocalDate inicio, java.time.LocalDate fin) {
        try {
            String q = sqlQueries.getProperty("listar-embarcaciones-disponibles-por-fecha");
            if (q == null) {
                System.err.println(
                        " No se encontró la query 'listar-embarcaciones-disponibles-por-fecha' en sql.properties");
                return null;
            }
            return jdbcTemplate.queryForList(q, String.class, java.sql.Date.valueOf(inicio),
                    java.sql.Date.valueOf(fin));
        } catch (DataAccessException e) {
            System.err.println(" Error listando matrículas disponibles: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene una reserva específica por su ID.
     *
     * @param id el ID de la reserva a obtener
     * @return la reserva correspondiente al ID, o {@code null} si no existe
     *         o si ocurre un error
     */
    public Reserva obtenerReservaPorId(int id) {
        try {
            String query = sqlQueries.getProperty("obtener-reserva-por-id");
            if (query != null) {
                return jdbcTemplate.queryForObject(query, new Object[]{id}, new RowMapper<Reserva>() {
                    public Reserva mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        Reserva r = new Reserva();
                        r.setId(rs.getInt("id"));
                        r.setDniSocio(rs.getString("dni_socio"));
                        r.setMatricula(rs.getString("matricula"));
                        r.setDescripcionReserva(rs.getString("descripcion_reserva"));

                        // Asignación de los valores num_pasajeros_reserva e importe_reserva
                        r.setNumPasajeros(rs.getInt("num_pasajeros_reserva"));
                        r.setImporteTotal(rs.getDouble("importe_reserva"));

                        java.sql.Date fi = rs.getDate("fecha");
                        if (fi != null) {
                            r.setFecha(fi.toLocalDate());
                        }
                        return r;
                    }
                });
            } else {
                return null;
            }
        } catch (EmptyResultDataAccessException e) {
            System.err.println("No se encontró la reserva con el ID: " + id);
            return null; // O manejarlo según tu lógica (puedes lanzar una excepción personalizada)
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null; // Si ocurre otro error de acceso a datos
        }
    }

    /**
     * Obtiene las reservas futuras a partir de una fecha dada.
     *
     * @param fecha la fecha de referencia para obtener reservas futuras
     * @return lista de reservas futuras o {@code null} si ocurre un error
     */
    public List<Reserva> obtenerReservasFuturas(LocalDate fecha) {
        try {
            String query = sqlQueries.getProperty("listar-reservas-futuras");
            if (query != null) {
                List<Reserva> result = jdbcTemplate.query(query, new RowMapper<Reserva>() {
                    public Reserva mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        Reserva r = new Reserva();
                        r.setId(rs.getInt("id"));
                        r.setDniSocio(rs.getString("dni_socio"));
                        r.setMatricula(rs.getString("matricula"));
                        r.setDescripcionReserva(rs.getString("descripcion_reserva"));

                        // Asignación de los valores num_pasajeros_reserva e importe_reserva
                        r.setNumPasajeros(rs.getInt("num_pasajeros_reserva"));
                        r.setImporteTotal(rs.getDouble("importe_reserva"));

                        java.sql.Date fi = rs.getDate("fecha");
                        if (fi != null) {
                            r.setFecha(fi.toLocalDate());
                        }
                        return r;
                    }
                }, java.sql.Date.valueOf(fecha)); // Pasamos la fecha para filtrar las futuras reservas
                return result;
            } else {
                return null;
            }
        } catch (DataAccessException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza la fecha de una reserva existente.
     *
     * @param reserva el objeto {@link Reserva} con los nuevos datos
     * @return {@code true} si la actualización fue exitosa, {@code false} en
     *         caso contrario
     */
    public boolean updateReservaFecha(Reserva reserva) {
        try {
            // Definir la consulta SQL para actualizar la fecha
            String query = sqlQueries.getProperty("update-reserva-fecha");

            if (query == null) {
                System.err.println("No se encontró la query 'update-reserva-fecha' en sql.properties");
                return false;
            }

            // Usar java.sql.Date para evitar problemas de mapeo con LocalDate
            java.sql.Date sqlFecha = java.sql.Date.valueOf(reserva.getFecha());

            // Ejecutar la consulta de actualización con los parámetros correctos
            int result = jdbcTemplate.update(query,
                    sqlFecha,  // Fecha
                    reserva.getId());  // ID de la reserva

            return result > 0;  // Si result es mayor que 0, la actualización fue exitosa
        } catch (DataAccessException e) {
            System.err.println("Error al actualizar los datos de la reserva: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza los datos de una reserva, como la descripción y el número de
     * pasajeros.
     *
     * @param reserva el objeto {@link Reserva} con los nuevos datos
     * @return {@code true} si la actualización fue exitosa, {@code false} en
     *         caso contrario
     */
    public boolean updateReservaDatos(Reserva reserva) {
        try {
            // Definir la consulta SQL para actualizar la descripción y el número de plazas
            String query = sqlQueries.getProperty("update-reserva-datos");

            if (query == null) {
                System.err.println("No se encontró la query 'update-reserva-datos' en sql.properties");
                return false;
            }

            // Usar java.sql.Date para evitar problemas de mapeo con LocalDate
            java.sql.Date sqlFecha = java.sql.Date.valueOf(reserva.getFecha());

            // Ejecutar la consulta de actualización
            int result = jdbcTemplate.update(query,
                    reserva.getDescripcionReserva(),
                    reserva.getNumPasajeros(),
                    sqlFecha,
                    reserva.getId());

            return result > 0;  // Si se actualizó correctamente, result será mayor que 0
        } catch (DataAccessException e) {
            System.err.println("Error al actualizar los datos de la reserva: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancela una reserva futura por su ID.
     *
     * @param idReserva el ID de la reserva a cancelar
     * @return {@code true} si la cancelación fue exitosa, {@code false} en
     *         caso contrario
     */
    public boolean cancelarReservaFutura(int idReserva) {
        try {
            String query = sqlQueries.getProperty("eliminar-reserva");

            if (query == null) {
                System.err.println("No existe la query 'eliminar-reserva'");
                return false;
            }

            // Ejecutar la consulta de eliminación
            int result = jdbcTemplate.update(query, idReserva);

            return result > 0;  // Si se eliminó correctamente, result será mayor que 0
        } catch (DataAccessException e) {
            System.err.println("Error cancelando reserva: " + e.getMessage());
            return false;
        }
    }
}
