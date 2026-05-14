package es.uco.pw.pw2526.model.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import es.uco.pw.pw2526.model.domain.inscripcion.Inscripcion;
import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.domain.socio.TipoInscripcion;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Repositorio para operaciones relacionadas con socios.
 * <p>
 * Provee métodos para obtener, insertar y actualizar socios e
 * inscripciones usando {@link org.springframework.jdbc.core.JdbcTemplate}.
 * </p>
 */
@Repository
public class SocioRepository extends AbstractRepository {
    public SocioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Socio mapSocio(ResultSet rs, boolean incluirCuota, boolean incluirIdInscripcion) throws SQLException {
        Socio socio = new Socio();
        socio.setDni(rs.getString("dni"));
        socio.setNombre(rs.getString("nombre"));
        socio.setApellidos(rs.getString("apellidos"));
        java.sql.Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        if (fechaNacimiento != null) {
            socio.setFechaNacimiento(fechaNacimiento.toLocalDate());
        }
        socio.setDireccion(rs.getString("direccion"));
        socio.setTituloPatron(rs.getBoolean("titulo_patron"));
        if (incluirCuota) {
            socio.setCuotaInscripcion(rs.getDouble("cuota_inscripcion"));
        }
        java.sql.Date fechaInscripcion = rs.getDate("fecha_inscripcion");
        if (fechaInscripcion != null) {
            socio.setFechaInscripcion(fechaInscripcion.toLocalDate());
        }
        if (incluirIdInscripcion) {
            socio.setIdInscripcion(rs.getInt("id_inscripcion"));
        }
        return socio;
    }

    private TipoInscripcion parseTipoInscripcion(String valor, TipoInscripcion valorDefecto) {
        if (valor == null) {
            return valorDefecto;
        }
        String normalizado = valor.trim().toUpperCase();
        try {
            return TipoInscripcion.valueOf(normalizado);
        } catch (IllegalArgumentException ex) {
            for (TipoInscripcion tipoInscripcion : TipoInscripcion.values()) {
                if (tipoInscripcion.name().equalsIgnoreCase(valor.trim())) {
                    return tipoInscripcion;
                }
            }
            return valorDefecto;
        }
    }

    private Inscripcion mapInscripcion(ResultSet rs, TipoInscripcion valorDefecto) throws SQLException {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(rs.getInt("id"));
        inscripcion.setTipo(parseTipoInscripcion(rs.getString("tipo"), valorDefecto));
        return inscripcion;
    }

    private boolean validarSocioFamiliarNuevo(Socio socio, String etiqueta) {
        if (socio == null || socio.getDni() == null || socio.getDni().isBlank()) {
            System.err.println("El " + etiqueta + " es nulo o tiene un DNI vacío");
            return false;
        }
        if (existsByDni(socio.getDni())) {
            System.err.println("El " + etiqueta + " con DNI " + socio.getDni() + " ya existe.");
            return false;
        }
        return true;
    }

    private void completarDatosSocioFamiliar(Socio socio) {
        if (socio.getFechaNacimiento() == null) {
            socio.setFechaNacimiento(java.time.LocalDate.now());
        }
        if (socio.getFechaInscripcion() == null) {
            socio.setFechaInscripcion(java.time.LocalDate.now());
        }
        if (socio.getDireccion() == null || socio.getDireccion().isBlank()) {
            socio.setDireccion("Sin especificar");
        }
    }

    private boolean insertarSocioConCuota(Socio socio, double cuotaInscripcion) {
        String query = sqlQueries.getProperty("insertar-socio");
        if (query == null) {
            System.err.println("No se encontró la query 'insertar-socio'");
            return false;
        }

        int resultado = jdbcTemplate.update(query,
                socio.getDni(),
                socio.getNombre(),
                socio.getApellidos(),
                socio.getFechaNacimiento(),
                socio.getDireccion(),
                socio.isTituloPatron(),
                socio.getIdInscripcion(),
                cuotaInscripcion,
                socio.getFechaInscripcion());
        return resultado > 0;
    }

    private List<Socio> obtenerSociosPorQuery(String queryKey, boolean incluirCuota, boolean incluirIdInscripcion,
            String mensajeError) {
        try {
            String query = sqlQueries.getProperty(queryKey);
            if (query == null) {
                return null;
            }
            return jdbcTemplate.query(query, (rs, rowNumber) -> mapSocio(rs, incluirCuota, incluirIdInscripcion));
        } catch (DataAccessException exception) {
            System.err.println(mensajeError + exception.getMessage());
            exception.printStackTrace();
            return null;
        }
    }

    private boolean addConyugeConInscripcion(int idInscripcion, Socio conyuge) {
        if (!validarSocioFamiliarNuevo(conyuge, "cónyuge")) {
            return false;
        }
        conyuge.setIdInscripcion(idInscripcion);
        completarDatosSocioFamiliar(conyuge);
        return insertarSocioConCuota(conyuge, 250.00);
    }

    /**
     * Obtiene la lista de socios básicos (sin información extensiva).
     *
     * @return lista de {@link Socio} con los campos básicos, o {@code null}
     *         si la consulta no está disponible o ocurre un error.
     */
    public List<Socio> obtenerSocios() {
        return obtenerSociosPorQuery("listar-socios", true, true, "Unable to find socios. Error: ");
    }

    /**
     * Obtiene la lista de socios que no tienen título de patrón.
     *
     * @return lista de {@link Socio} o {@code null} en caso de error.
     */
    public List<Socio> obtenerSociosSinPatron() {
        return obtenerSociosPorQuery(
                "listar-socios-sin-patron",
                false,
                false,
                "Unable to find socios without patron title. Error: ");
    }

    /**
     * Obtiene la lista de socios que sí tienen título de patrón.
     *
     * @return lista de {@link Socio} o {@code null} en caso de error.
     */
    public List<Socio> obtenerSociosConPatron() {
        return obtenerSociosPorQuery(
                "listar-socios-con-patron",
                false,
                false,
                "Unable to find socios with patron title. Error: ");
    }

    /**
     * Inserta una nueva inscripción en la tabla de inscripciones.
     *
     * @param Tipo tipo de inscripción (p.ej. "Individual" o "Familiar").
     * @return id generado de la inscripción, o -1 en caso de error.
     */
    public int addInscripcion(String tipo) {
    try {
        String query = sqlQueries.getProperty("insertar-inscripcion");
        if (query != null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, tipo); // 'INDIVIDUAL' o 'FAMILIAR'
                return ps;
            }, keyHolder);

            return keyHolder.getKey().intValue(); // devuelve el id generado
        }
    } catch (DataAccessException e) {
        System.err.println("Error al insertar inscripción: " + e.getMessage());
    }
    return -1;
}

public List<Inscripcion> obtenerInscripciones() {
    try {
        String query = sqlQueries.getProperty("listar-inscripciones");
        if (query != null) {
            List<Inscripcion> result = jdbcTemplate.query(query,
                    (rs, rowNumber) -> mapInscripcion(rs, null));
            return result;
        } else {
            return null;
        }
    } catch (DataAccessException exception) {
        System.err.println("Unable to list inscripciones: " + exception.getMessage());
        return null;
    }
}

public List<Inscripcion> obtenerInscripcionesPorTipo(TipoInscripcion tipo) {
    try {
        String query = sqlQueries.getProperty("listar-inscripciones-por-tipo");
        if (query == null) {
            System.err.println("No se encontró la query 'listar-inscripciones-por-tipo'");
            return Collections.emptyList();
        }

        return jdbcTemplate.query(query,
                (rs, rowNum) -> mapInscripcion(rs, TipoInscripcion.NONE),
                tipo.name());

    } catch (DataAccessException e) {
        System.err.println("Error al listar inscripciones por tipo: " + e.getMessage());
        return Collections.emptyList();
    }
}



public Inscripcion obtenerInscripcionPorDni(String dni) {
    if (dni == null || dni.isBlank()) {
        return null;
    }

    String query = sqlQueries.getProperty("obtener-inscripcion-por-dni");
    if (query == null) {
        System.err.println("No se encontró la query 'obtener-inscripcion-por-dni' en sql.properties");
        return null;
    }

    try {
        List<Inscripcion> lista = jdbcTemplate.query(query,
                new Object[] { dni },
                (rs, rowNum) -> mapInscripcion(rs, TipoInscripcion.NONE));

        return lista.isEmpty() ? null : lista.get(0);
    } catch (DataAccessException ex) {
        System.err.println("Error al obtener inscripción por DNI: " + dni + " => " + ex.getMessage());
        return null;
    }
}

    /**
     * Inserta un nuevo socio y su inscripción asociada.
     *
     * @param socio objeto {@link Socio} con los datos a insertar
     * @return {@code true} si la inserción fue correcta, {@code false}
     *         en caso contrario
     */
    public boolean addSocio(Socio socio) {
        try {
            if (socio == null || socio.getDni() == null || socio.getDni().isBlank()) {
                return false;
            }

            if (existsByDni(socio.getDni())) {
                System.err.println("Error: El socio con DNI " + socio.getDni() + " ya está registrado.");
                return false; // ya existe
            }

            int inscripcion = addInscripcion(socio.getTipo());

            String query = sqlQueries.getProperty("insertar-socio");
            if (query != null) {
                int result = jdbcTemplate.update(query,
                        socio.getDni(),
                        socio.getNombre(),
                        socio.getApellidos(),
                        socio.getFechaNacimiento(),
                        socio.getDireccion(),
                        socio.isTituloPatron(),
                        inscripcion,
                        300,
                        socio.getFechaInscripcion());

                if (result > 0) {
                    return true; // Socio insertado correctamente
                } else {
                    System.err.println("Error al insertar socio: El resultado de la consulta es 0.");
                    return false; // No se pudo insertar el socio
                }
            }
            return false; // Si la consulta es nula

        } catch (DataAccessException exception) {
            // Capturar excepción detallada
            System.err.println("Unable to insert member in the database: " + exception.getMessage());
            exception.printStackTrace(); // Mostrar detalles de la excepción
            return false;
        }
    }
    /**
     * Actualiza los datos de un socio existente.
     *
     * @param socio objeto {@link Socio} con los datos actualizados
     * @return {@code true} si la actualización fue correcta, {@code false}
     *         en caso contrario
     */
    public boolean updateSocio(Socio socio) {
    try {
        if (socio == null || socio.getDni() == null || socio.getDni().isBlank()) {
            return false;
        }

        if (!existsByDni(socio.getDni())) {
            System.err.println("No existe el socio con DNI " + socio.getDni());
            return false;
        }

        String query = sqlQueries.getProperty("actualizar-socio");
        if (query != null) {
            int result = jdbcTemplate.update(query,
                    socio.getNombre(),
                    socio.getApellidos(),
                    socio.getFechaNacimiento(),
                    socio.getDireccion(),
                    socio.isTituloPatron(),
                    socio.getCuotaInscripcion(),
                    socio.getFechaInscripcion(),
                    socio.getIdInscripcion(),
                    socio.getDni()    
            );
            return result > 0;
        } else {
            System.err.println("No se encontró la query 'actualizar-socio' en sql.properties");
            return false;
        }

    } catch (DataAccessException e) {
        System.err.println("Error al actualizar socio: " + e.getMessage());
        return false;
    }
}

public boolean updateTipoInscripcion(Integer idInscripcion, TipoInscripcion nuevoTipo) {
    try {
        if (idInscripcion == null) {
            return false;
        }

        String query = sqlQueries.getProperty("actualizar-tipo-inscripcion");
        if (query != null) {
            int result = jdbcTemplate.update(query,
                    nuevoTipo.name(),
                    idInscripcion      
            );
            return result > 0;
        } else {
            System.err.println("No se encontró la query 'actualizar-tipo-inscripcion'");
            return false;
        }

    } catch (DataAccessException e) {
        System.err.println("Error al actualizar inscripción: " + e.getMessage());
        return false;
    }
}

public Inscripcion findInscripcionById(Integer id) {
    try {
        String query = sqlQueries.getProperty("buscar-inscripcion-por-id");
        if (query != null) {
            return jdbcTemplate.queryForObject(query, (rs, rowNum) -> {
                Inscripcion inscripcion = new Inscripcion();
                inscripcion.setId(rs.getInt("id"));
                String tipoDb = rs.getString("tipo");
                inscripcion.setTipo(TipoInscripcion.valueOf(tipoDb.trim().toUpperCase()));
                return inscripcion;
            }, id);
        } else {
            System.err.println("No se encontró la query 'buscar-inscripcion-por-id' en sql.properties");
            return null;
        }
    } catch (EmptyResultDataAccessException e) {
        System.err.println("No existe inscripción con ID " + id);
        return null;
    } catch (DataAccessException e) {
        System.err.println("Error al buscar inscripción: " + e.getMessage());
        return null;
    }
}




    /**
     * Comprueba si existe un socio con el DNI indicado.
     *
     * @param dni DNI a comprobar
     * @return {@code true} si existe al menos un socio con ese DNI,
     *         {@code false} en caso de error o si no existe
     */
    public boolean existsByDni(String dni) {
        try {
            if (dni == null || dni.isBlank())
                return false;

            String query = sqlQueries.getProperty("existe-socio");

            if (query != null) {
                Integer count = jdbcTemplate.queryForObject(query, Integer.class, dni);
                return count != null && count > 0; // Si existe al menos un socio con ese DNI
            }
            return false;
        } catch (DataAccessException ex) {
            System.err.println("Error al comprobar la existencia del socio: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Establece o revoca el título de patrón para un socio identificado por su DNI.
     * Devuelve true si se actualizó al menos una fila.
     */
    /**
     * Establece o revoca el título de patrón para un socio.
     *
     * @param dni        DNI del socio
     * @param tieneTitulo {@code true} para asignar el título, {@code false}
     *                    para revocarlo
     * @return {@code true} si se actualizó al menos una fila
     */
    public boolean setTituloPatron(String dni, boolean tieneTitulo) {
        try {
            String query = sqlQueries.getProperty("actualizar-titulo-patron");
            if (query == null) {
                System.err.println(" No se encontró la query 'actualizar-titulo-patron' en sql.properties");
                return false;
            }
            int result = jdbcTemplate.update(query, tieneTitulo, dni);
            return result > 0;
        } catch (DataAccessException e) {
            System.err.println(" Error al actualizar título de patrón: " + e.getMessage());
            return false;
        }
    }

    /**
     * Devuelve si el socio con el DNI indicado tiene el título de patrón.
     *
     * @param dni DNI del socio
     * @return {@link Boolean} true/false si la consulta tuvo éxito, o
     *         {@code null} en caso de error
     */
    public Boolean hasTituloPatron(String dni) {
        try {
            String query = sqlQueries.getProperty("select-titulo-patron");
            if (query == null) {
                System.err.println(" No se encontró la query 'select-titulo-patron' en sql.properties");
                return null;
            }
            Integer value = jdbcTemplate.queryForObject(query, Integer.class, dni);
            if (value == null)
                return null;
            return value != 0;
        } catch (DataAccessException e) {
            System.err.println(" Error al obtener título de patrón: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registra una nueva inscripción de tipo familiar y crea el socio
     * asociado.
     *
     * @param socio datos del socio a crear
     * @return {@code true} si se añadió correctamente, {@code false}
     *         en caso de error
     */
    public boolean nuevaInscripcionFamiliar(Socio socio) {
        try {
            if (socio == null || socio.getDni() == null || socio.getDni().isBlank()) {
                System.err.println(" Error: Socio o DNI nulo");
                return false;
            }

            if (existsByDni(socio.getDni())) {
                System.err.println("El socio con DNI " + socio.getDni() + " ya existe.");
                return false;
            }

            // Crear inscripción de tipo familiar
            int idInscripcion = addInscripcion("familiar");

            String query = sqlQueries.getProperty("insertar-socio");
            if (query == null) {
                System.err.println(" No se encontró la query 'insertar-socio' en sql.properties");
                return false;
            }

            int result = jdbcTemplate.update(query,
                    socio.getDni(),
                    socio.getNombre(),
                    socio.getApellidos(),
                    socio.getFechaNacimiento(),
                    socio.getDireccion(),
                    socio.isTituloPatron(),
                    idInscripcion,
                    300, // misma cuota
                    socio.getFechaInscripcion());

            return result > 0;
        } catch (DataAccessException e) {
            System.err.println(" Error al registrar inscripción familiar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca un socio por su DNI.
     *
     * @param dni DNI a buscar
     * @return {@link Socio} encontrado o {@code null} si no existe o hay error
     */
    public Socio findByDni(String dni) {
        try {
            if (dni == null || dni.isBlank())
                return null;

            String query = sqlQueries.getProperty("buscar-socio-por-dni");
            if (query == null) {
                System.err.println(" No se encontró la query 'buscar-socio-por-dni' en sql.properties");
                return null;
            }

            List<Socio> socios = jdbcTemplate.query(query,
                    (rs, rowNum) -> mapSocio(rs, false, true),
                    dni);

            return socios.isEmpty() ? null : socios.get(0);

        } catch (DataAccessException e) {
            System.err.println(" Error al buscar socio por DNI: " + e.getMessage());
            return null;
        }
    }

    /**
     * Añade un cónyuge a un socio titular.
     *
     * @param dniTitular DNI del titular
     * @param conyuge    datos del cónyuge
     * @return {@code true} si la inserción fue correcta
     */
    public boolean addConyuge(String dniTitular, Socio conyuge) {
        try {
            Socio titular = findByDni(dniTitular);
            if (titular == null) {
                System.err.println("No existe socio titular con DNI " + dniTitular);
                return false;
            }
            return addConyugeConInscripcion(titular.getIdInscripcion(), conyuge);

        } catch (DataAccessException e) {
            System.err.println(" Error al registrar cónyuge: " + e.getMessage());
            return false;
        }
    }

    //Aclaración: esta función es idéntica a la anterior, pero recibe el idInscripcion en vez del dniTitular
    //Necesaria para la P2
    public boolean addConyuge(int idInscripcion, Socio conyuge) {
        return addConyugeConInscripcion(idInscripcion, conyuge);
    }


    /**
     * Añade un hijo a un socio titular.
     *
     * @param dniTitular DNI del titular
     * @param hijo       datos del hijo
     * @return {@code true} si la inserción fue correcta
     */
    public boolean addHijo(String dniTitular, Socio hijo) {
        try {
            if (!validarSocioFamiliarNuevo(hijo, "hijo")) {
                return false;
            }

            Socio titular = findByDni(dniTitular);
            if (titular == null) {
                System.err.println("No existe socio titular con DNI " + dniTitular);
                return false;
            }

            hijo.setIdInscripcion(titular.getIdInscripcion());
            completarDatosSocioFamiliar(hijo);
            return insertarSocioConCuota(hijo, 100.00);

        } catch (DataAccessException e) {
            System.err.println(" Error al registrar hijo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el cónyuge asociado a la inscripción indicada.
     *
     * @param idInscripcion id de la inscripción
     * @return {@link Socio} del cónyuge o {@code null} si no hay ninguno
     */
    public Socio obtenerSocioConyuge(int idInscripcion) {
        try {

            System.out.println("Buscando al cónyuge con id inscripcion --->  " + idInscripcion);
            String query = sqlQueries.getProperty("buscar-conyuge");
            if (query == null) {
                System.err.println(" No se encontró la query 'buscar-socio-por-dni' en sql.properties");
                return null;
            }

            List<Socio> socios = jdbcTemplate.query(query,
                    (rs, rowNum) -> mapSocio(rs, false, false),
                    idInscripcion);

            return socios.isEmpty() ? null : socios.get(0);

        } catch (DataAccessException e) {
            System.err.println(" Error al buscar socio por DNI: " + e.getMessage());
            return null;
        }
    }

    // Obtener el hijo asociado al socio
    /**
     * Obtiene el hijo asociado a la inscripción indicada.
     *
     * @param idInscripcion id de la inscripción
     * @return {@link Socio} del hijo o {@code null} si no hay ninguno
     */
    public Socio obtenerSocioHijo(int idInscripcion) {
        try {
            System.out.println("Buscando al hijo con id inscripcion --->  " + idInscripcion);
            String query = sqlQueries.getProperty("buscar-hijo");
            if (query == null) {
                System.err.println(" No se encontró la query 'buscar-hijo' en sql.properties");
                return null;
            }

            List<Socio> socios = jdbcTemplate.query(query,
                    (rs, rowNum) -> mapSocio(rs, false, false),
                    idInscripcion);

            return socios.isEmpty() ? null : socios.get(0);

        } catch (DataAccessException e) {
            System.err.println(" Error al buscar socio por DNI: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene todos los socios con información adicional (conyuge/hijo)
     * cuando esté disponible.
     *
     * @return lista de {@link Socio} o {@code null} en caso de error
     */
    public List<Socio> obtenerTodosSociosConDetalles() {
        try {
            String query = sqlQueries.getProperty("listar-todos-socios-con-detalles");
            if (query != null) {
                return jdbcTemplate.query(query, new RowMapper<Socio>() {
                    public Socio mapRow(ResultSet rs, int rowNumber) throws SQLException {
                        Socio socio = mapSocio(rs, false, true);
                        
                        // Obtener el conyuge
                        Socio conyuge = obtenerSocioConyuge(socio.getIdInscripcion());
                        // socio.setNombreConyuge(conyuge != null ? conyuge.getNombre() : "No asignado");
                        
                        // Obtener los hijos
                        Socio hijo = obtenerSocioHijo(socio.getIdInscripcion());
                        // socio.setNumHijos(hijo != null ? 1 : 0);

                        return socio;
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error al listar socios: " + e.getMessage());
        }
        return null;
    }

   

public boolean cancelarInscripcionPorDni(String dniTitular) {
    try {
        // 1. Obtener inscripción asociada al titular
        String queryGetInscripcion = sqlQueries.getProperty("obtener-inscripcion-por-dni");
        Inscripcion inscripcion = jdbcTemplate.queryForObject(
            queryGetInscripcion,
            new Object[]{dniTitular},
            (rs, rowNum) -> {
                Inscripcion i = new Inscripcion();
                i.setId(rs.getInt("id"));
                i.setTipo(TipoInscripcion.valueOf(rs.getString("tipo").toUpperCase())); // 👈 conversión segura
                return i;
            }
        );

        if (inscripcion == null) {
            return false; // No existe inscripción para ese DNI
        }

        // 2. Desvincular socios de esa inscripción->convertirla en Null en la tabla socio
        String queryUpdateSocios = sqlQueries.getProperty("desvincular-socios");
        jdbcTemplate.update(queryUpdateSocios, inscripcion.getId());

        // 3. Eliminar inscripción-> delete en la tabla inscripcion
        String queryDeleteInscripcion = sqlQueries.getProperty("eliminar-inscripcion");
        jdbcTemplate.update(queryDeleteInscripcion, inscripcion.getId());

        return true;
    } catch (DataAccessException e) {
        System.err.println("Error al cancelar inscripción: " + e.getMessage());
        return false;
    }
}


public boolean vincularSocioAFamiliar(String dniTitular, String dniNuevo) {
    try {
        // 1. Obtener inscripción del titular
        Inscripcion inscripcion = jdbcTemplate.queryForObject(
            sqlQueries.getProperty("obtener-inscripcion-por-dni"),
            new Object[]{dniTitular},
            (rs, rowNum) -> {
                Inscripcion i = new Inscripcion();
                i.setId(rs.getInt("id"));
                i.setTipo(TipoInscripcion.valueOf(rs.getString("tipo").trim().toUpperCase()));
                return i;
            }
        );

        if (inscripcion == null || inscripcion.getTipo() != TipoInscripcion.FAMILIAR) {
            return false;
        }

        // 2. Actualizar socio nuevo para que apunte a esa inscripción
        int filas = jdbcTemplate.update(
            sqlQueries.getProperty("update-socio-inscripcion"),
            inscripcion.getId(),
            250.0,
            dniNuevo
        );

        return filas > 0;
    } catch (DataAccessException e) {
        System.err.println("Error al vincular socio: " + e.getMessage());
        return false;
    }
}


public boolean desvincularSocioDeInscripcion(String dniSocio) {
    try {
        String query = sqlQueries.getProperty("desvincular-socio-de-inscripcion");
        int filas = jdbcTemplate.update(query, dniSocio);
        return filas > 0;
    } catch (DataAccessException e) {
        System.err.println("Error al desvincular socio: " + e.getMessage());
        return false;
    }
}

public boolean eliminarSocioSiNoTieneInscripcion(String dniSocio) {
    try {
        String query = sqlQueries.getProperty("eliminar-socio-sin-inscripcion");
        int filas = jdbcTemplate.update(query, dniSocio);
        return filas > 0; // true si se eliminó el socio
    } catch (DataAccessException e) {
        System.err.println("Error al eliminar socio: " + e.getMessage());
        return false;
    }
}



}



