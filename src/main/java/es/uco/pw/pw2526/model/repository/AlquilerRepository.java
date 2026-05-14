package es.uco.pw.pw2526.model.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import es.uco.pw.pw2526.model.domain.socio.Socio;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;



import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;

@Repository
public class AlquilerRepository extends AbstractRepository {

    public AlquilerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.setSQLQueriesFileName("db/sql.properties");
    }

    private Alquiler mapAlquiler(ResultSet rs) throws SQLException {
        Alquiler alquiler = new Alquiler();
        alquiler.setIdAlquiler(rs.getInt("id_alquiler"));
        alquiler.setDniSocio(rs.getString("dni_socio"));
        alquiler.setMatricula(rs.getString("matricula"));
        alquiler.setNumPasajeros(rs.getInt("num_pasajeros"));
        alquiler.setImporteTotal(rs.getDouble("importe_total"));

        java.sql.Date fechaInicio = rs.getDate("fecha_inicio");
        java.sql.Date fechaFin = rs.getDate("fecha_fin");
        if (fechaInicio != null) {
            alquiler.setFechaInicio(fechaInicio.toLocalDate());
        }
        if (fechaFin != null) {
            alquiler.setFechaFin(fechaFin.toLocalDate());
        }
        return alquiler;
    }

    private String obtenerQueryInsertAlquiler() {
        String query = sqlQueries.getProperty("insertar-alquiler");
        if (query == null) {
            System.err.println("No se encontró la query 'insertar-alquiler' en sql.properties");
        }
        return query;
    }

    private Double calcularImporteAlquiler(Alquiler alquiler) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(alquiler.getFechaInicio(), alquiler.getFechaFin()) + 1;
        if (dias <= 0) {
            System.err.println(" Número de días inválido: " + dias);
            return null;
        }
        return 20.0 * alquiler.getNumPasajeros() * (double) dias;
    }

    private boolean insertarAlquiler(String insertAlquilerQuery, Alquiler alquiler, double importe) {
        int insertedRows = jdbcTemplate.update(insertAlquilerQuery,
                alquiler.getMatricula(),
                alquiler.getNumPasajeros(),
                importe,
                alquiler.getDniSocio(),
                alquiler.getFechaInicio(),
                alquiler.getFechaFin());
        return insertedRows > 0;
    }

    public boolean addAlquiler(Alquiler alquiler) {
        try {
            String insertAlquilerQuery = obtenerQueryInsertAlquiler();
            if (insertAlquilerQuery == null) {
                return false;
            }

            Double importe = calcularImporteAlquiler(alquiler);
            if (importe == null) {
                return false;
            }
            alquiler.setImporteTotal(importe);
            return insertarAlquiler(insertAlquilerQuery, alquiler, importe);
        } catch (DataAccessException e) {
            System.err.println("Error al insertar alquiler: " + e.getMessage());
            return false;
        }
    }

    /** Cuenta alquileres que se solapan con el rango [inicio, fin] para la matrícula indicada */
    public Integer countAlquileresSolapados(String matricula, java.time.LocalDate inicio, java.time.LocalDate fin) {
        try {
            String checkRangeQuery = sqlQueries.getProperty("check-alquiler-range");
            if (checkRangeQuery == null) {
                System.err.println("No se encontró la query 'check-alquiler-range' en sql.properties");
                return null;
            }
            return jdbcTemplate.queryForObject(checkRangeQuery, Integer.class, matricula, java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(fin));
        } catch (DataAccessException e) {
            System.err.println(" Error contando alquileres solapados: " + e.getMessage());
            return null;
        }
    }

    /** Obtiene el número de plazas de la embarcación identificada por la matrícula. 
     *  Retorna null si no existe o en caso de error.
     */
    public Integer obtenerPlazas(String matricula) {
        try {
            String selectPlazasQuery = sqlQueries.getProperty("select-embarcacion-num-plazas");
            if (selectPlazasQuery == null) {
                System.err.println("No se encontró la query 'select-embarcacion-num-plazas' en sql.properties");
                return null;
            }
            Integer plazas = jdbcTemplate.queryForObject(selectPlazasQuery, Integer.class, matricula);
            return plazas;
        } catch (DataAccessException e) {
            System.err.println(" Error obteniendo plazas de embarcación: " + e.getMessage());
            return null;
        }
    }

    public boolean addSocioAlquiler(String dniSocio, int idAlquiler) {
    try {
        String existsSocioAlquilerQuery = sqlQueries.getProperty("existe-socio-en-alquiler");
        if (existsSocioAlquilerQuery != null) {
            Integer existe = jdbcTemplate.queryForObject(existsSocioAlquilerQuery, Integer.class, dniSocio, idAlquiler);
            if (existe != null && existe > 0) {
                System.err.println(" El socio ya está asociado al alquiler " + idAlquiler);
                return false;
            }
        }

        String queryInsert = sqlQueries.getProperty("insertar-socio-alquiler");
        if (queryInsert == null) {
            System.err.println(" No se encontró la query 'insertar-socio-alquiler'");
            return false;
        }

        int insertedRows = jdbcTemplate.update(queryInsert, dniSocio, idAlquiler);
        return insertedRows > 0;

    } catch (DataAccessException e) {
        System.err.println(" Error al añadir socio al alquiler: " + e.getMessage());
        return false;
    }
}


    /** Devuelve el número de plazas de la embarcación asociada al alquiler (o null si no se puede obtener) */
    public Integer getPlazasByAlquiler(int idAlquiler) {
        try {
            String selectPlazasByAlquilerQuery = sqlQueries.getProperty("select-embarcacion-num-plazas-by-alquiler");
            if (selectPlazasByAlquilerQuery == null) return null;
            return jdbcTemplate.queryForObject(selectPlazasByAlquilerQuery, Integer.class, idAlquiler);
        } catch (DataAccessException ex) {
            System.err.println("Error obteniendo plazas by alquiler: " + ex.getMessage());
            return null;
        }
    }

    /** Cuenta los socios ya asociados al alquiler */
    public Integer countSociosEnAlquiler(int idAlquiler) {
        try {
            String countSociosQuery = sqlQueries.getProperty("count-socios-por-alquiler");
            if (countSociosQuery == null) return null;
            return jdbcTemplate.queryForObject(countSociosQuery, Integer.class, idAlquiler);
        } catch (DataAccessException ex) {
            System.err.println("Error contando socios por alquiler: " + ex.getMessage());
            return null;
        }
    }

    /** Obtiene el valor num_pasajeros del alquiler (o null en caso de error) */
    public Integer getNumPasajerosByAlquiler(int idAlquiler) {
        try {
            String selectNumPasajerosQuery = sqlQueries.getProperty("select-num-pasajeros-by-alquiler");
            if (selectNumPasajerosQuery == null) {
                System.err.println(" No se encontró la query 'select-num-pasajeros-by-alquiler' en sql.properties");
                return null;
            }
            return jdbcTemplate.queryForObject(selectNumPasajerosQuery, Integer.class, idAlquiler);
        } catch (DataAccessException ex) {
            System.err.println("Error obteniendo num_pasajeros por alquiler: " + ex.getMessage());
            return null;
        }
    }

    /** Obtiene la lista de socios asociados a un alquiler (detalles completos de socio). */
    public List<Socio> obtenerSociosPorAlquiler(int idAlquiler) {
        try {
            String listarSociosPorAlquilerQuery = sqlQueries.getProperty("listar-socios-por-alquiler");
            if (listarSociosPorAlquilerQuery == null) {
                System.err.println(" No se encontró la query 'listar-socios-por-alquiler' en sql.properties");
                return null;
            }
            List<Socio> sociosPorAlquiler = jdbcTemplate.query(listarSociosPorAlquilerQuery, new org.springframework.jdbc.core.RowMapper<Socio>() {
                public Socio mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
                    Socio socio = new Socio();
                    socio.setDni(rs.getString("dni"));
                    socio.setNombre(rs.getString("nombre"));
                    socio.setApellidos(rs.getString("apellidos"));
                    java.sql.Date fechaNacimiento = rs.getDate("fecha_nacimiento");
                    if (fechaNacimiento != null) socio.setFechaNacimiento(fechaNacimiento.toLocalDate());
                    socio.setDireccion(rs.getString("direccion"));
                    socio.setTituloPatron(rs.getBoolean("titulo_patron"));
                    java.sql.Date fechaIns = rs.getDate("fecha_inscripcion");
                    if (fechaIns != null) socio.setFechaInscripcion(fechaIns.toLocalDate());
                    return socio;
                }
            }, idAlquiler);
            return sociosPorAlquiler;
        } catch (org.springframework.dao.DataAccessException ex) {
            System.err.println("Error obteniendo socios por alquiler: " + ex.getMessage());
            return null;
        }
    }


     public List<Alquiler> obtenerAlquileres()
    {
        try{
            String query = sqlQueries.getProperty("listar-alquileres");
            if(query != null){
                List<Alquiler> alquileres = jdbcTemplate.query(query, new RowMapper<Alquiler>(){
                public Alquiler mapRow(ResultSet rs, int rowNumber) throws SQLException{
                    return mapAlquiler(rs);
                }
                });
                return alquileres;
            }
            else
                return null;
        }catch(DataAccessException exception){
            System.err.println("Unable to find alquileres: " + exception.getMessage());
            return null;
        }
    }


/** Lista matrículas de embarcaciones disponibles en el rango [inicio, fin] (ambas incluidas).
 *  Devuelve null en caso de error (como el resto de este repo) o la lista de matriculas.
 */
public List<String> listarEmbaracionesDisponiblesPorFecha(java.time.LocalDate inicio, java.time.LocalDate fin) {
    try {
        String listarDisponiblesQuery = sqlQueries.getProperty("listar-embarcaciones-disponibles-por-fecha");
        if (listarDisponiblesQuery == null) {
            System.err.println(" No se encontró la query 'listar-embarcaciones-disponibles-por-fecha' en sql.properties");
            return null;
        }
        return jdbcTemplate.queryForList(listarDisponiblesQuery, String.class, java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(fin));
    } catch (org.springframework.dao.DataAccessException e) {
        System.err.println(" Error listando matriculas disponibles: " + e.getMessage());
        return null;
    }
}

public Alquiler obtenerAlquilerPorId(int idAlquiler) {
    try {
        String selectAlquilerByIdQuery = sqlQueries.getProperty("select-alquiler-by-id");
        if (selectAlquilerByIdQuery == null) {
            System.err.println(" No se encontró la query 'select-alquiler-by-id' en sql.properties");
            return null;
        }

        return jdbcTemplate.queryForObject(selectAlquilerByIdQuery, new RowMapper<Alquiler>() {
            @Override
            public Alquiler mapRow(ResultSet rs, int rowNum) throws SQLException {
                return mapAlquiler(rs);
            }
        }, idAlquiler);

    } catch (DataAccessException e) {
        System.err.println(" Error obteniendo alquiler por ID: " + e.getMessage());
        return null;
    }
}

public List<Alquiler> obtenerAlquileresFuturos(java.time.LocalDate fechaReferencia) {
    try {
        String listarFuturosQuery = sqlQueries.getProperty("listar-alquileres-futuros");
        if (listarFuturosQuery == null) {
            System.err.println(" No se encontró la query 'listar-alquileres-futuros' en sql.properties");
            return null;
        }

        return jdbcTemplate.query(listarFuturosQuery, new RowMapper<Alquiler>() {
            @Override
            public Alquiler mapRow(ResultSet rs, int rowNum) throws SQLException {
                return mapAlquiler(rs);
            }
        }, java.sql.Date.valueOf(fechaReferencia));

    } catch (DataAccessException e) {
        System.err.println(" Error obteniendo alquileres futuros: " + e.getMessage());
        return null;
    }
}

public boolean vincularSocioNoTitular(int idAlquiler, String dniSocio) {
    try {
        String insertSocioAlquilerQuery = sqlQueries.getProperty("insertar-socio-alquiler");
        if (insertSocioAlquilerQuery == null) {
            System.err.println("No existe la query 'insertar-socio-alquiler'");
            return false;
        }

        int insertedRows = jdbcTemplate.update(insertSocioAlquilerQuery, dniSocio, idAlquiler);
        return insertedRows > 0;

    } catch (DataAccessException e) {
        System.err.println("Error vinculando socio: " + e.getMessage());
        return false;
    }
}

public boolean desvincularSocioNoTitular(int idAlquiler, String dniSocio) {
    try {
        String deleteSocioAlquilerQuery = sqlQueries.getProperty("eliminar-socio-alquiler");
        if (deleteSocioAlquilerQuery == null) {
            System.err.println("No existe la query 'eliminar-socio-alquiler'");
            return false;
        }

        int deletedRows = jdbcTemplate.update(deleteSocioAlquilerQuery, dniSocio, idAlquiler);
        return deletedRows > 0;

    } catch (DataAccessException e) {
        System.err.println("Error desvinculando socio: " + e.getMessage());
        return false;
    }
}

public boolean cancelarAlquilerFuturo(int idAlquiler) {
    try {
        String deleteAlquilerQuery = sqlQueries.getProperty("eliminar-alquiler");
        if (deleteAlquilerQuery == null) {
            System.err.println("No existe la query 'eliminar-alquiler'");
            return false;
        }

        int deletedRows = jdbcTemplate.update(deleteAlquilerQuery, idAlquiler);
        return deletedRows > 0;

    } catch (DataAccessException e) {
        System.err.println("Error cancelando alquiler: " + e.getMessage());
        return false;
    }
}




}
