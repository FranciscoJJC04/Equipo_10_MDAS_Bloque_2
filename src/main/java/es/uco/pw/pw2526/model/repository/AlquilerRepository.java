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



/** Repositorio mínimo para insertar alquileres (solo creación por ahora) */
@Repository
public class AlquilerRepository extends AbstractRepository 
{

    public AlquilerRepository(JdbcTemplate jdbcTemplate) 
    {
        this.jdbcTemplate = jdbcTemplate;
        this.setSQLQueriesFileName("db/sql.properties");
    }

    public boolean addAlquiler(Alquiler alquiler) 
    {
        try 
        {
            String query = sqlQueries.getProperty("insertar-alquiler");
            if (query == null) 
            {
                System.err.println("No se encontró la query 'insertar-alquiler' en sql.properties");
                return false;
            }
            
            // Calcular importe total: 20€ por persona y por día (días inclusivos)
            long dias = java.time.temporal.ChronoUnit.DAYS.between(alquiler.getFechaInicio(), alquiler.getFechaFin()) + 1;
            if (dias <= 0) {
                System.err.println(" Número de días inválido: " + dias);
                return false;
            }
            double importe = 20.0 * alquiler.getNumPasajeros() * (double) dias;
            alquiler.setImporteTotal(importe);

       

        int result = jdbcTemplate.update(query,
            alquiler.getMatricula(),
            alquiler.getNumPasajeros(),
            importe,
            alquiler.getDniSocio(),
            alquiler.getFechaInicio(),
            alquiler.getFechaFin());
                  

            return result > 0;

        } 
        catch (DataAccessException e) 
        {
            System.err.println("Error al insertar alquiler: " + e.getMessage());
            return false;
        }
    }

    /** Cuenta alquileres que se solapan con el rango [inicio, fin] para la matrícula indicada */
    public Integer countAlquileresSolapados(String matricula, java.time.LocalDate inicio, java.time.LocalDate fin) {
        try {
            String q = sqlQueries.getProperty("check-alquiler-range");
            if (q == null) {
                System.err.println("No se encontró la query 'check-alquiler-range' en sql.properties");
                return null;
            }
            return jdbcTemplate.queryForObject(q, Integer.class, matricula, java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(fin));
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
            String q = sqlQueries.getProperty("select-embarcacion-num-plazas");
            if (q == null) {
                System.err.println("No se encontró la query 'select-embarcacion-num-plazas' en sql.properties");
                return null;
            }
            Integer plazas = jdbcTemplate.queryForObject(q, Integer.class, matricula);
            return plazas;
        } catch (DataAccessException e) {
            System.err.println(" Error obteniendo plazas de embarcación: " + e.getMessage());
            return null;
        }
    }

    public boolean addSocioAlquiler(String dniSocio, int id_alquiler) {
    try {
        // Verificar si el socio ya está en el alquiler
        String qExiste = sqlQueries.getProperty("existe-socio-en-alquiler");
        if (qExiste != null) {
            Integer existe = jdbcTemplate.queryForObject(qExiste, Integer.class, dniSocio, id_alquiler);
            if (existe != null && existe > 0) {
                System.err.println(" El socio ya está asociado al alquiler " + id_alquiler);
                return false;
            }
        }

        // Insertar el socio en la tabla socio_alquiler
        String queryInsert = sqlQueries.getProperty("insertar-socio-alquiler");
        if (queryInsert == null) {
            System.err.println(" No se encontró la query 'insertar-socio-alquiler'");
            return false;
        }

        int result = jdbcTemplate.update(queryInsert, dniSocio, id_alquiler);
        return result > 0;

    } catch (DataAccessException e) {
        System.err.println(" Error al añadir socio al alquiler: " + e.getMessage());
        return false;
    }
}


    /** Devuelve el número de plazas de la embarcación asociada al alquiler (o null si no se puede obtener) */
    public Integer getPlazasByAlquiler(int id_alquiler) {
        try {
            String qPlazas = sqlQueries.getProperty("select-embarcacion-num-plazas-by-alquiler");
            if (qPlazas == null) return null;
            return jdbcTemplate.queryForObject(qPlazas, Integer.class, id_alquiler);
        } catch (DataAccessException ex) {
            System.err.println("Error obteniendo plazas by alquiler: " + ex.getMessage());
            return null;
        }
    }

    /** Cuenta los socios ya asociados al alquiler */
    public Integer countSociosEnAlquiler(int id_alquiler) {
        try {
            String qCount = sqlQueries.getProperty("count-socios-por-alquiler");
            if (qCount == null) return null;
            return jdbcTemplate.queryForObject(qCount, Integer.class, id_alquiler);
        } catch (DataAccessException ex) {
            System.err.println("Error contando socios por alquiler: " + ex.getMessage());
            return null;
        }
    }

    /** Obtiene el valor num_pasajeros del alquiler (o null en caso de error) */
    public Integer getNumPasajerosByAlquiler(int id_alquiler) {
        try {
            String q = sqlQueries.getProperty("select-num-pasajeros-by-alquiler");
            if (q == null) {
                System.err.println(" No se encontró la query 'select-num-pasajeros-by-alquiler' en sql.properties");
                return null;
            }
            return jdbcTemplate.queryForObject(q, Integer.class, id_alquiler);
        } catch (DataAccessException ex) {
            System.err.println("Error obteniendo num_pasajeros por alquiler: " + ex.getMessage());
            return null;
        }
    }

    /** Obtiene la lista de socios asociados a un alquiler (detalles completos de socio). */
    public List<Socio> obtenerSociosPorAlquiler(int id_alquiler) {
        try {
            String q = sqlQueries.getProperty("listar-socios-por-alquiler");
            if (q == null) {
                System.err.println(" No se encontró la query 'listar-socios-por-alquiler' en sql.properties");
                return null;
            }
            List<Socio> result = jdbcTemplate.query(q, new org.springframework.jdbc.core.RowMapper<Socio>() {
                public Socio mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
                    Socio s = new Socio();
                    s.setDni(rs.getString("dni"));
                    s.setNombre(rs.getString("nombre"));
                    s.setApellidos(rs.getString("apellidos"));
                    java.sql.Date fechaNacimiento = rs.getDate("fecha_nacimiento");
                    if (fechaNacimiento != null) s.setFechaNacimiento(fechaNacimiento.toLocalDate());
                    s.setDireccion(rs.getString("direccion"));
                    s.setTituloPatron(rs.getBoolean("titulo_patron"));
                    java.sql.Date fechaIns = rs.getDate("fecha_inscripcion");
                    if (fechaIns != null) s.setFechaInscripcion(fechaIns.toLocalDate());
                    return s;
                }
            }, id_alquiler);
            return result;
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
                List<Alquiler> result = jdbcTemplate.query(query, new RowMapper<Alquiler>(){
                public Alquiler mapRow(ResultSet rs, int rowNumber) throws SQLException{
                    Alquiler a = new Alquiler();
                    // Mapear el id del alquiler y otros campos relevantes
                    try {
                        // id_alquiler en la BBDD
                        int id = rs.getInt("id_alquiler");
                        a.setIdAlquiler(id);
                    } catch (SQLException ex) {
                        // si no existe la columna, no romper: dejar id por defecto (0)
                    }
                    a.setDniSocio(rs.getString("dni_socio"));
                    a.setMatricula(rs.getString("matricula"));
                    // mapear num_pasajeros y importe_total si existen
                    try {
                        a.setNumPasajeros(rs.getInt("num_pasajeros"));
                    } catch (SQLException ex) {
                        // ignorar si la columna no existe
                    }
                    try {
                        a.setImporteTotal(rs.getDouble("importe_total"));
                    } catch (SQLException ex) {
                        // ignorar si la columna no existe
                    }
                    java.sql.Date fi = rs.getDate("fecha_inicio");
                    java.sql.Date ff = rs.getDate("fecha_fin");
                    if (fi != null) {
                        a.setFechaInicio(fi.toLocalDate());
                    }
                    if (ff != null) {
                        a.setFechaFin(ff.toLocalDate());
                    }
                    return a;
                }
                });
                return result;
            }
            else
                return null;
        }catch(DataAccessException exception){
            System.err.println("Unable to find students");
            exception.printStackTrace();
            return null;
        }
    }


/** Lista matrículas de embarcaciones disponibles en el rango [inicio, fin] (ambas incluidas).
 *  Devuelve null en caso de error (como el resto de este repo) o la lista de matriculas.
 */
public List<String> listarEmbaracionesDisponiblesPorFecha(java.time.LocalDate inicio, java.time.LocalDate fin) {
    try {
        String q = sqlQueries.getProperty("listar-embarcaciones-disponibles-por-fecha");
        if (q == null) {
            System.err.println(" No se encontró la query 'listar-embarcaciones-disponibles-por-fecha' en sql.properties");
            return null;
        }
        return jdbcTemplate.queryForList(q, String.class, java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(fin));
    } catch (org.springframework.dao.DataAccessException e) {
        System.err.println(" Error listando matriculas disponibles: " + e.getMessage());
        return null;
    }
}

//P2 añadido
public Alquiler obtenerAlquilerPorId(int id_alquiler) {
    try {
        String q = sqlQueries.getProperty("select-alquiler-by-id");
        if (q == null) {
            System.err.println(" No se encontró la query 'select-alquiler-by-id' en sql.properties");
            return null;
        }

        return jdbcTemplate.queryForObject(q, new RowMapper<Alquiler>() {
            @Override
            public Alquiler mapRow(ResultSet rs, int rowNum) throws SQLException {
                Alquiler a = new Alquiler();
                a.setIdAlquiler(rs.getInt("id_alquiler"));
                a.setDniSocio(rs.getString("dni_socio"));
                a.setMatricula(rs.getString("matricula"));
                a.setNumPasajeros(rs.getInt("num_pasajeros"));
                a.setImporteTotal(rs.getDouble("importe_total"));

                java.sql.Date fi = rs.getDate("fecha_inicio");
                java.sql.Date ff = rs.getDate("fecha_fin");
                if (fi != null) a.setFechaInicio(fi.toLocalDate());
                if (ff != null) a.setFechaFin(ff.toLocalDate());

                return a;
            }
        }, id_alquiler);

    } catch (DataAccessException e) {
        System.err.println(" Error obteniendo alquiler por ID: " + e.getMessage());
        return null;
    }
}
public List<Alquiler> obtenerAlquileresFuturos(java.time.LocalDate fechaReferencia) {
    try {
        String q = sqlQueries.getProperty("listar-alquileres-futuros");
        if (q == null) {
            System.err.println(" No se encontró la query 'listar-alquileres-futuros' en sql.properties");
            return null;
        }

        return jdbcTemplate.query(q, new RowMapper<Alquiler>() {
            @Override
            public Alquiler mapRow(ResultSet rs, int rowNum) throws SQLException {
                Alquiler a = new Alquiler();
                a.setIdAlquiler(rs.getInt("id_alquiler"));
                a.setDniSocio(rs.getString("dni_socio"));
                a.setMatricula(rs.getString("matricula"));
                a.setNumPasajeros(rs.getInt("num_pasajeros"));
                a.setImporteTotal(rs.getDouble("importe_total"));

                java.sql.Date fi = rs.getDate("fecha_inicio");
                java.sql.Date ff = rs.getDate("fecha_fin");
                if (fi != null) a.setFechaInicio(fi.toLocalDate());
                if (ff != null) a.setFechaFin(ff.toLocalDate());

                return a;
            }
        }, java.sql.Date.valueOf(fechaReferencia));

    } catch (DataAccessException e) {
        System.err.println(" Error obteniendo alquileres futuros: " + e.getMessage());
        return null;
    }
}

//P2 semana 2
public boolean vincularSocioNoTitular(int idAlquiler, String dniSocio) {
    try {
        String query = sqlQueries.getProperty("insertar-socio-alquiler");
        if (query == null) {
            System.err.println("No existe la query 'insertar-socio-alquiler'");
            return false;
        }

        // insert into socio_alquiler (dni_socio, id_alquiler)
        int result = jdbcTemplate.update(query, dniSocio, idAlquiler);
        return result > 0;

    } catch (DataAccessException e) {
        System.err.println("Error vinculando socio: " + e.getMessage());
        return false;
    }
}

public boolean desvincularSocioNoTitular(int idAlquiler, String dniSocio) {
    try {
        String query = sqlQueries.getProperty("eliminar-socio-alquiler");
        if (query == null) {
            System.err.println("No existe la query 'eliminar-socio-alquiler'");
            return false;
        }

        // delete from socio_alquiler where dni_socio = ? and id_alquiler = ?
        int result = jdbcTemplate.update(query, dniSocio, idAlquiler);
        return result > 0;

    } catch (DataAccessException e) {
        System.err.println("Error desvinculando socio: " + e.getMessage());
        return false;
    }
}

public boolean cancelarAlquilerFuturo(int idAlquiler) {
    try {
        String query = sqlQueries.getProperty("eliminar-alquiler");
        if (query == null) {
            System.err.println("No existe la query 'eliminar-alquiler'");
            return false;
        }

        // delete from alquiler where id_alquiler = ?
        int result = jdbcTemplate.update(query, idAlquiler);
        return result > 0;

    } catch (DataAccessException e) {
        System.err.println("Error cancelando alquiler: " + e.getMessage());
        return false;
    }
}




}
