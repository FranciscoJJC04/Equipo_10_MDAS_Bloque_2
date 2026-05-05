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
            if (patron == null || patron.getDniPatron() == null || patron.getDniPatron().isBlank()) {
                return false;
            }

            if (existsByDni(patron.getDniPatron())) {
                System.err.println("Error: El patrón con DNI " + patron.getDniPatron() + " ya está registrado.");
                return false;
            }

            String query = sqlQueries.getProperty("insertar-patron");
            if (query != null) {
                int insertedRows = jdbcTemplate.update(query,
                        patron.getDniPatron(),
                        patron.getNombre(),
                        patron.getApellido(),
                        patron.getFechaNacimiento());
                return insertedRows > 0;
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

            String query = sqlQueries.getProperty("existe-patron");
            if (query != null) {
                Integer count = jdbcTemplate.queryForObject(query, Integer.class, dni);
                return count != null && count > 0;
            }
            return false;
        } catch (DataAccessException ex) {
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
            String query = sqlQueries.getProperty("listar-patrones");
            if (query != null) {
                List<Patron> patrones = jdbcTemplate.query(query, new RowMapper<Patron>() {
                    public Patron mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        return new Patron(
                                rs.getString("dni_patron"),
                                rs.getString("nombre"),
                                rs.getString("apellido"),
                                rs.getDate("fecha_nacimiento").toLocalDate());
                    }
                });
                return patrones;
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
            if (patron == null || patron.getDniPatron() == null || patron.getDniPatron().isBlank()) {
                return false;
            }

            if (!existsByDni(patron.getDniPatron())) {
                System.err.println("No existe el patrón con DNI " + patron.getDniPatron());
                return false;
            }

            String query = sqlQueries.getProperty("actualizar-patron");
            if (query != null) {
                int updatedRows = jdbcTemplate.update(query,
                        patron.getNombre(),
                        patron.getApellido(),
                        patron.getFechaNacimiento(),
                        patron.getDniPatron());
                return updatedRows > 0;
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
                int deletedRows = jdbcTemplate.update(query, dni);
                return deletedRows > 0;
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
                    Patron patronEncontrado = new Patron();
                    patronEncontrado.setDniPatron(rs.getString("dni_patron"));
                    patronEncontrado.setNombre(rs.getString("nombre"));
                    patronEncontrado.setApellido(rs.getString("apellido"));
                    java.sql.Date fechaNacimiento = rs.getDate("fecha_nacimiento");
                    if (fechaNacimiento != null) {
                        patronEncontrado.setFechaNacimiento(fechaNacimiento.toLocalDate());
                    }
                    return patronEncontrado;
                }
            }, dni);

            return patrones.isEmpty() ? null : patrones.get(0);
        } catch (DataAccessException e) {
            System.err.println("Error al buscar patrón por DNI: " + e.getMessage());
            return null;
        }
    }
}
