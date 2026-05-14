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

    private String obtenerQueryInsertReserva() {
        String query = sqlQueries.getProperty("insertar-reserva");
        if (query == null) {
            System.err.println(" No se encontró la query 'insertar-reserva' en sql.properties");
        }
        return query;
    }

    private Double calcularImporteReserva(Reserva reserva) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(reserva.getFecha(), reserva.getFecha()) + 1;
        if (dias <= 0) {
            System.err.println(" Número de días inválido: " + dias);
            return null;
        }
        return 40.0 * reserva.getNumPasajeros() * (double) dias;
    }

    private boolean validarPlazasReserva(Reserva reserva) {
        Integer plazas = obtenerPlazas(reserva.getMatricula());

        if (plazas == null) {
            System.err.println(" No se pudo obtener el número de plazas para la embarcación " + reserva.getMatricula());
            return false;
        }

        if (reserva.getNumPasajeros() > plazas) {
            System.err.println(" Número de pasajeros excede las plazas disponibles (" + reserva.getNumPasajeros()
                    + "/" + plazas + ")");
            return false;
        }
        return true;
    }

    private boolean insertarReserva(String insertReservaQuery, Reserva reserva) {
        java.sql.Date sqlFecha = java.sql.Date.valueOf(reserva.getFecha());
        int insertedRows = jdbcTemplate.update(insertReservaQuery,
                reserva.getId(),
                sqlFecha,
                reserva.getDniSocio(),
                reserva.getMatricula(),
                reserva.getImporteTotal(),
                reserva.getNumPasajeros(),
                reserva.getDescripcionReserva());
        return insertedRows > 0;
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
            String insertReservaQuery = obtenerQueryInsertReserva();
            if (insertReservaQuery == null) {
                return false;
            }

            Double importe = calcularImporteReserva(reserva);
            if (importe == null) {
                return false;
            }
            reserva.setImporteTotal(importe);

            if (!validarPlazasReserva(reserva)) {
                return false;
            }
            return insertarReserva(insertReservaQuery, reserva);

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
            String checkReservaRangeQuery = sqlQueries.getProperty("check-reserva-range");
            if (checkReservaRangeQuery == null) {
                System.err.println(" No se encontró la query 'check-reserva-range' en sql.properties");
                return 0;
            }
            Integer solapamientos = jdbcTemplate.queryForObject(checkReservaRangeQuery, Integer.class, matricula,
                    java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(fin));
            return (solapamientos != null) ? solapamientos : 0;
        } catch (DataAccessException e) {
            System.err.println(" Error contando reservas solapadas: " + e.getMessage());
            return 0;
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
            String selectPlazasQuery = sqlQueries.getProperty("select-embarcacion-num-plazas");

            if (selectPlazasQuery == null) {
                System.err.println("No se encontró la query 'select-embarcacion-num-plazas' en sql.properties");
                return null;
            }

            if (matricula == null || matricula.isEmpty()) {
                System.err.println("La matrícula proporcionada es inválida: " + matricula);
                return null;
            }

            return jdbcTemplate.queryForObject(selectPlazasQuery, Integer.class, matricula);
        } catch (DataAccessException e) {
            System.err.println("Error obteniendo plazas de embarcación: " + e.getMessage());
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
                List<Reserva> reservas = jdbcTemplate.query(query, new RowMapper<Reserva>() {
                    public Reserva mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        Reserva reserva = new Reserva();
                        reserva.setId(rs.getInt("id"));
                        java.sql.Date fechaReserva = rs.getDate("fecha");
                        reserva.setDniSocio(rs.getString("dni_socio"));
                        reserva.setMatricula(rs.getString("matricula"));
                        reserva.setDescripcionReserva(rs.getString("descripcion_reserva"));

                        reserva.setNumPasajeros(rs.getInt("num_pasajeros_reserva"));
                        reserva.setImporteTotal(rs.getDouble("importe_reserva"));

                        if (fechaReserva != null) {
                            reserva.setFecha(fechaReserva.toLocalDate());
                        }

                        return reserva;
                    }
                });
                return reservas;
            } else {
                return null;
            }
        } catch (DataAccessException exception) {
            System.err.println("Unable to find reservas: " + exception.getMessage());
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
            String listarDisponiblesQuery = sqlQueries.getProperty("listar-embarcaciones-disponibles-por-fecha");
            if (listarDisponiblesQuery == null) {
                System.err.println(
                        " No se encontró la query 'listar-embarcaciones-disponibles-por-fecha' en sql.properties");
                return null;
            }
            return jdbcTemplate.queryForList(listarDisponiblesQuery, String.class, java.sql.Date.valueOf(inicio),
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
                        Reserva reserva = new Reserva();
                        reserva.setId(rs.getInt("id"));
                        reserva.setDniSocio(rs.getString("dni_socio"));
                        reserva.setMatricula(rs.getString("matricula"));
                        reserva.setDescripcionReserva(rs.getString("descripcion_reserva"));

                        reserva.setNumPasajeros(rs.getInt("num_pasajeros_reserva"));
                        reserva.setImporteTotal(rs.getDouble("importe_reserva"));

                        java.sql.Date fechaReserva = rs.getDate("fecha");
                        if (fechaReserva != null) {
                            reserva.setFecha(fechaReserva.toLocalDate());
                        }
                        return reserva;
                    }
                });
            } else {
                return null;
            }
        } catch (EmptyResultDataAccessException e) {
            System.err.println("No se encontró la reserva con el ID: " + id);
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
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
                List<Reserva> reservasFuturas = jdbcTemplate.query(query, new RowMapper<Reserva>() {
                    public Reserva mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        Reserva reserva = new Reserva();
                        reserva.setId(rs.getInt("id"));
                        reserva.setDniSocio(rs.getString("dni_socio"));
                        reserva.setMatricula(rs.getString("matricula"));
                        reserva.setDescripcionReserva(rs.getString("descripcion_reserva"));

                        reserva.setNumPasajeros(rs.getInt("num_pasajeros_reserva"));
                        reserva.setImporteTotal(rs.getDouble("importe_reserva"));

                        java.sql.Date fechaReserva = rs.getDate("fecha");
                        if (fechaReserva != null) {
                            reserva.setFecha(fechaReserva.toLocalDate());
                        }
                        return reserva;
                    }
                }, java.sql.Date.valueOf(fecha));
                return reservasFuturas;
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
            String query = sqlQueries.getProperty("update-reserva-fecha");

            if (query == null) {
                System.err.println("No se encontró la query 'update-reserva-fecha' en sql.properties");
                return false;
            }

            java.sql.Date sqlFecha = java.sql.Date.valueOf(reserva.getFecha());

                int updatedRows = jdbcTemplate.update(query,
                    sqlFecha,
                    reserva.getId());

                return updatedRows > 0;
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
            String query = sqlQueries.getProperty("update-reserva-datos");

            if (query == null) {
                System.err.println("No se encontró la query 'update-reserva-datos' en sql.properties");
                return false;
            }

            java.sql.Date sqlFecha = java.sql.Date.valueOf(reserva.getFecha());

                int updatedRows = jdbcTemplate.update(query,
                    reserva.getDescripcionReserva(),
                    reserva.getNumPasajeros(),
                    sqlFecha,
                    reserva.getId());

                return updatedRows > 0;
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

            int deletedRows = jdbcTemplate.update(query, idReserva);

            return deletedRows > 0;
        } catch (DataAccessException e) {
            System.err.println("Error cancelando reserva: " + e.getMessage());
            return false;
        }
    }
}
