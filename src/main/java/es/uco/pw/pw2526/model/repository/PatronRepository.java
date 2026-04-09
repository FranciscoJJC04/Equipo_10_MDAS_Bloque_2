package es.uco.pw.pw2526.model.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import es.uco.pw.pw2526.model.domain.patron.Patron;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para operaciones sobre patrones.
 * <p>
 * Este repositorio provee métodos para insertar, comprobar existencia, listar y actualizar
 * patrones en la base de datos utilizando {@link JdbcTemplate}.
 * </p>
 * 
 * @see JdbcTemplate
 * @see Patron
 */
@Repository
public class PatronRepository extends AbstractRepository {

    /**
     * Constructor del repositorio que inyecta la plantilla JDBC de Spring.
     *
     * @param jdbcTemplate plantilla JDBC usada para ejecutar las consultas SQL
     */
    public PatronRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Inserta un nuevo patrón en la base de datos.
     *
     * Este método recibe un objeto {@link Patron}, verifica que no esté vacío ni tenga
     * valores nulos o vacíos en su campo obligatorio "DNI", y lo inserta en la base de datos.
     *
     * @param patron objeto {@link Patron} con los datos a insertar
     * @return {@code true} si la inserción fue exitosa, {@code false} en caso de error
     *         o si el patrón ya existe en la base de datos.
     */
    public boolean addPatron(Patron patron) {
        try {
            // Validación de entrada
            if (patron == null || patron.getDni_patron() == null || patron.getDni_patron().isBlank()) {
                return false;
            }

            // Comprobar existencia del patrón
            if (existsByDni(patron.getDni_patron())) {
                System.err.println("Error: El patrón con DNI " + patron.getDni_patron() + " ya está registrado.");
                return false; // El patrón ya existe
            }

            // Ejecutar la consulta SQL para insertar el patrón
            String query = sqlQueries.getProperty("insertar-patron");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        patron.getDni_patron(),
                        patron.getNombre(),
                        patron.getApellido(),
                        patron.getFecha_nacimiento());
                return result > 0; // Verificar si se insertó correctamente
            } else {
                return false;
            }
        } catch (DataAccessException exception) {
            System.err.println("Unable to insert patrons in the database");
            return false;
        }
    }

    /**
     * Comprueba si existe un patrón con el DNI dado.
     *
     * Este método consulta la base de datos para verificar si ya existe un patrón con
     * el mismo DNI.
     *
     * @param dni DNI a comprobar
     * @return {@code true} si existe un patrón con ese DNI, {@code false} si no existe
     *         o si ocurre un error.
     */
    public boolean existsByDni(String dni) {
        try {
            if (dni == null || dni.isBlank()) {
                return false;
            }

            // Consultar si el patrón existe con el DNI proporcionado
            String query = sqlQueries.getProperty("existe-patron");
            if (query != null) {
                Integer count = jdbcTemplate.queryForObject(query, Integer.class, dni);
                return count != null && count > 0; // Si existe al menos un patrón con ese DNI
            }
            return false;
        } catch (DataAccessException ex) {
            // Loggear el error para su seguimiento
            System.err.println("Error al comprobar la existencia del patrón: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Devuelve la lista de patrones registrados.
     *
     * Este método consulta la base de datos y devuelve una lista con todos los patrones
     * registrados. Si ocurre un error o la consulta no está disponible, devuelve {@code null}.
     *
     * @return lista de {@link Patron} o {@code null} si la consulta falla.
     */
    public List<Patron> obtenerPatrones() {
        try {
            // Consultar la base de datos para obtener la lista de patrones
            String query = sqlQueries.getProperty("listar-patrones");
            if (query != null) {
                List<Patron> result = jdbcTemplate.query(query, new RowMapper<Patron>() {
                    public Patron mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        // Mapear los resultados de la consulta a objetos Patron
                        return new Patron(
                                rs.getString("dni_patron"),
                                rs.getString("nombre"),
                                rs.getString("apellido"),
                                rs.getDate("fecha_nacimiento").toLocalDate());
                    }
                });
                return result;
            } else {
                return null;
            }
        } catch (DataAccessException exception) {
            System.err.println("Unable to find patrons");
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Actualiza los datos de un patrón existente en la base de datos.
     *
     * Este método actualiza los datos de un patrón en la base de datos. Se requiere que el patrón
     * exista previamente, por lo que se realiza una validación para asegurar que el patrón con el DNI
     * proporcionado está registrado antes de proceder con la actualización.
     *
     * @param patron objeto {@link Patron} con los nuevos datos (incluye DNI)
     * @return {@code true} si la actualización afectó a al menos una fila, {@code false}
     *         en caso de error o si el patrón no existe.
     */
    public boolean updatePatron(Patron patron) {
        try {
            // Validar que el patrón no sea nulo y que el DNI sea válido
            if (patron == null || patron.getDni_patron() == null || patron.getDni_patron().isBlank()) {
                return false;
            }

            // Verificar existencia del patrón antes de actualizar
            if (!existsByDni(patron.getDni_patron())) {
                System.err.println("No existe el patrón con DNI " + patron.getDni_patron());
                return false; // No existe el patrón
            }

            // Ejecutar la consulta SQL para actualizar los datos del patrón
            String query = sqlQueries.getProperty("actualizar-patron");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        patron.getNombre(),
                        patron.getApellido(),
                        patron.getFecha_nacimiento(),
                        patron.getDni_patron());
                return result > 0; // Verificar si la actualización fue exitosa
            } else {
                System.err.println("No se encontró la query 'actualizar-patron' en sql.properties");
                return false;
            }
        } catch (DataAccessException e) {
            System.err.println("Error al actualizar patrón: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un patrón está vinculado a una embarcación.
     *
     * Este método consulta la base de datos para comprobar si el patrón con el DNI dado
     * está vinculado a alguna embarcación.
     *
     * @param dni DNI del patrón a verificar
     * @return {@code true} si el patrón está vinculado a una embarcación, {@code false}
     *         en caso contrario o si ocurre un error.
     */
    public boolean isPatronLinkedToEmbarcacion(String dni) {
        try {
            String query = sqlQueries.getProperty("check-patron-linked-to-embarcacion");
            if (query != null) {
                Integer count = jdbcTemplate.queryForObject(query, Integer.class, dni);
                return count != null && count > 0;
            }
            return false;
        } catch (DataAccessException ex) {
            System.err.println("Error al comprobar si el patrón está vinculado a una embarcación: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Elimina un patrón de la base de datos.
     *
     * Este método elimina un patrón usando su DNI. Antes de eliminar, el patrón debe existir en la base de datos.
     *
     * @param dni DNI del patrón a eliminar
     * @return {@code true} si la eliminación fue exitosa, {@code false} en caso de error
     */
    public boolean deletePatron(String dni) {
        try {
            String query = sqlQueries.getProperty("delete-patron");
            if (query != null) {
                int result = jdbcTemplate.update(query, dni);
                return result > 0;
            }
            return false;
        } catch (DataAccessException ex) {
            System.err.println("Error al eliminar patrón: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Busca un patrón por su DNI.
     *
     * Este método consulta la base de datos y devuelve un patrón con el DNI proporcionado.
     *
     * @param dni DNI del patrón a buscar
     * @return un objeto {@link Patron} si se encuentra un patrón con ese DNI, o {@code null}
     *         si no se encuentra o si ocurre un error.
     */
    public Patron findByDni(String dni) {
        try {
            if (dni == null || dni.isBlank()) {
                return null;
            }

            String query = sqlQueries.getProperty("buscar-patron-por-dni");
            if (query == null) {
                System.err.println("No se encontró la query 'buscar-patron-por-dni' en sql.properties");
                return null;
            }

            List<Patron> patrones = jdbcTemplate.query(query, new RowMapper<Patron>() {
                @Override
                public Patron mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Patron p = new Patron();
                    p.setDni_patron(rs.getString("dni_patron"));
                    p.setNombre(rs.getString("nombre"));
                    p.setApellido(rs.getString("apellido"));
                    java.sql.Date fechaNacimiento = rs.getDate("fecha_nacimiento");
                    if (fechaNacimiento != null) {
                        p.setFecha_nacimiento(fechaNacimiento.toLocalDate());
                    }
                    return p;
                }
            }, dni);

            return patrones.isEmpty() ? null : patrones.get(0);
        } catch (DataAccessException e) {
            System.err.println("Error al buscar patrón por DNI: " + e.getMessage());
            return null;
        }
    }
}
