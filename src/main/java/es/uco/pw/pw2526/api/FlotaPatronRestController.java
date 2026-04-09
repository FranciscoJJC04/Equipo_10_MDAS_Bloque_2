package es.uco.pw.pw2526.api;

import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;
import es.uco.pw.pw2526.model.domain.patron.Patron;
import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;
import es.uco.pw.pw2526.model.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar embarcaciones y patrones.
 * <p>
 * Proporciona endpoints para obtener, crear, actualizar y eliminar embarcaciones y patrones,
 * así como vincular y desvincular patrones de embarcaciones.
 * </p>
 */
@RestController
@RequestMapping(path = "/api/flota-patron", produces = "application/json")
public class FlotaPatronRestController {

    @Autowired
    private EmbarcacionRepository embarcacionRepository;

    @Autowired
    private PatronRepository patronRepository;

    /**
     * Método que inicializa las consultas SQL utilizadas por los repositorios.
     * <p>
     * Este método es llamado después de la construcción del controlador, 
     * y establece el archivo de propiedades con las consultas SQL.
     * </p>
     */
    @PostConstruct
    public void init() {
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.patronRepository.setSQLQueriesFileName(sqlQueriesFileName);
        this.embarcacionRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    // Rutas GET

    /**
     * Obtiene la lista completa de embarcaciones.
     * 
     * @return la lista de embarcaciones, o código de estado 204 si no hay embarcaciones.
     */
    @GetMapping("/embarcaciones")
    public ResponseEntity<List<Embarcacion>> getAllEmbarcaciones() {
        List<Embarcacion> embarcaciones = embarcacionRepository.listarEmbarcaciones();
        return ResponseEntity.ok(embarcaciones);
    }

    /**
     * Obtiene una lista de embarcaciones filtradas por tipo.
     * 
     * @param tipo tipo de embarcación a filtrar
     * @return la lista de embarcaciones de ese tipo, o código de estado 204 si no se encuentran.
     */
    @GetMapping("/embarcaciones/tipo/{tipo}")
    public ResponseEntity<List<Embarcacion>> getEmbarcacionesPorTipo(@PathVariable("tipo") TipoEmbarcacion tipo) {
        List<Embarcacion> embarcaciones = embarcacionRepository.listarPorTipo(tipo);
        if (embarcaciones == null || embarcaciones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(embarcaciones);
    }

    /**
     * Obtiene la lista completa de patrones.
     * 
     * @return la lista de patrones.
     */
    @GetMapping("/patrones")
    public ResponseEntity<List<Patron>> getAllPatrones() {
        List<Patron> patrones = patronRepository.obtenerPatrones();
        return ResponseEntity.ok(patrones);
    }

    // Rutas POST

    /**
     * Crea una nueva embarcación.
     * 
     * @param embarcacion la embarcación a crear.
     * @return una respuesta con el estado de la creación.
     */
    @PostMapping(path = "/embarcaciones", consumes = "application/json")
    public ResponseEntity<String> createEmbarcacion(@RequestBody Embarcacion embarcacion) {
        try {
            if (embarcacion.getMatricula() == null || embarcacion.getMatricula().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La matrícula es obligatoria.");
            }
            if (embarcacion.getTipo() == null || embarcacion.getTipo() == TipoEmbarcacion.NONE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El tipo de embarcación es obligatorio.");
            }
            if (embarcacion.getNumPlazas() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El número de plazas debe ser mayor que 0.");
            }

            boolean success = embarcacionRepository.addEmbarcacion(embarcacion);
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Embarcación creada con éxito.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear la embarcación.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear embarcación.");
        }
    }

    /**
     * Crea un nuevo patrón.
     * 
     * @param patron el patrón a crear.
     * @return una respuesta con el estado de la creación.
     */
    @PostMapping(path = "/patrones", consumes = "application/json")
    public ResponseEntity<String> createPatron(@RequestBody Patron patron) {
        try {
            if (patron.getDni_patron() == null || patron.getDni_patron().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El DNI del patrón es obligatorio.");
            }
            if (patron.getNombre() == null || patron.getNombre().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre del patrón es obligatorio.");
            }
            if (patron.getApellido() == null || patron.getApellido().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El apellido del patrón es obligatorio.");
            }
            if (patron.getFecha_nacimiento() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La fecha de nacimiento del patrón es obligatoria.");
            }

            boolean success = patronRepository.addPatron(patron);
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Patrón creado con éxito.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el patrón.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear patrón.");
        }
    }

    // Rutas PATCH para actualización de embarcación

    /**
     * Actualiza los datos de una embarcación.
     * 
     * @param matricula la matrícula de la embarcación a actualizar.
     * @param requestEmbarcacion los nuevos datos de la embarcación.
     * @return la embarcación actualizada o un error si no se encuentra.
     */
    @PatchMapping(path = "/embarcaciones/{matricula}", consumes = "application/json")
    public ResponseEntity<Embarcacion> patchEmbarcacion(@PathVariable String matricula,
            @RequestBody Embarcacion requestEmbarcacion) {
        try {
            // Buscar embarcación actual por matrícula
            Embarcacion currentEmbarcacion = this.embarcacionRepository.findByMatricula(matricula);
            if (currentEmbarcacion != null) {

                // Aseguramos que la matrícula no se cambia
                requestEmbarcacion.setMatricula(currentEmbarcacion.getMatricula());

                // Actualizar solo los campos no nulos
                if (requestEmbarcacion.getTipo() != null) {
                    currentEmbarcacion.setTipo(requestEmbarcacion.getTipo());
                }
                if (requestEmbarcacion.getNombre() != null) {
                    currentEmbarcacion.setNombre(requestEmbarcacion.getNombre());
                }
                if (requestEmbarcacion.getNumPlazas() != 0) {
                    currentEmbarcacion.setNumPlazas(requestEmbarcacion.getNumPlazas());
                }

                // Guardar embarcación actualizada
                boolean resultOk = embarcacionRepository.updateEmbarcacion(currentEmbarcacion);
                if (resultOk) {
                    return ResponseEntity.ok(currentEmbarcacion);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(currentEmbarcacion);
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            System.err.println("Error en PATCH embarcación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(requestEmbarcacion);
        }
    }

    // Rutas PATCH para actualización de patrón

    /**
     * Actualiza los datos de un patrón.
     * 
     * @param dni el DNI del patrón a actualizar.
     * @param requestPatron los nuevos datos del patrón.
     * @return el patrón actualizado o un error si no se encuentra.
     */
    @PatchMapping(path = "/patrones/{dni}", consumes = "application/json")
    public ResponseEntity<Patron> patchPatron(@PathVariable String dni, @RequestBody Patron requestPatron) {
        try {
            // Buscar patrón actual por DNI
            Patron currentPatron = this.patronRepository.findByDni(dni);
            if (currentPatron != null) {

                // Aseguramos que el DNI no se cambia
                requestPatron.setDni_patron(currentPatron.getDni_patron());

                // Actualizar solo los campos no nulos
                if (requestPatron.getNombre() != null) {
                    currentPatron.setNombre(requestPatron.getNombre());
                }
                if (requestPatron.getApellido() != null) {
                    currentPatron.setApellido(requestPatron.getApellido());
                }
                if (requestPatron.getFecha_nacimiento() != null) {
                    currentPatron.setFecha_nacimiento(requestPatron.getFecha_nacimiento());
                }

                // Guardar patrón actualizado
                boolean resultOk = patronRepository.updatePatron(currentPatron);
                if (resultOk) {
                    return ResponseEntity.ok(currentPatron);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(requestPatron);
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            System.err.println("Error en PATCH patrón: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(requestPatron);
        }
    }

    // Rutas PATCH para vincular patrón a embarcación

    /**
     * Vincula un patrón a una embarcación.
     * 
     * @param matricula la matrícula de la embarcación.
     * @param dniPatron el DNI del patrón.
     * @param fechaInicio la fecha de inicio de la asignación.
     * @param fechaFin la fecha de fin de la asignación (opcional).
     * @return una respuesta con el estado de la vinculación.
     */
    @PatchMapping(path = "/embarcaciones/{matricula}/patron/{dniPatron}")
    public ResponseEntity<String> vincularPatronAEmbarcacion(
            @PathVariable("matricula") String matricula,
            @PathVariable("dniPatron") String dniPatron,
            @RequestParam("fechaInicio") String fechaInicio,
            @RequestParam(value = "fechaFin", required = false) String fechaFin) {

        Embarcacion embarcacion = embarcacionRepository.findByMatricula(matricula);
        Patron patron = patronRepository.findByDni(dniPatron);

        if (embarcacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Embarcación no encontrada.");
        }

        if (patron == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patrón no encontrado.");
        }

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = (fechaFin != null) ? LocalDate.parse(fechaFin) : null;

        // Verificar si existe solapamiento con otras asignaciones
        boolean solapamiento = embarcacionRepository.existeSolapamientoEmbarcacion(matricula, inicio, fin);
        if (solapamiento) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La asignación solapa con otra reserva.");
        }

        // Vincular patrón a embarcación
        boolean exito = embarcacionRepository.addPatronAEmbarcacion(inicio, fin, embarcacion, patron);
        if (exito) {
            return ResponseEntity.status(HttpStatus.OK).body("Patrón vinculado a la embarcación con éxito.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al vincular el patrón.");
        }
    }

    // Rutas PATCH para desvincular patrón de embarcación

    /**
     * Desvincula un patrón de una embarcación.
     * 
     * @param matricula la matrícula de la embarcación.
     * @param dniPatron el DNI del patrón.
     * @return una respuesta con el estado de la desvinculación.
     */
    @PatchMapping(path = "/embarcacion/{matricula}/desvincularPatron/{dniPatron}")
    public ResponseEntity<String> desvincularPatronDeEmbarcacion(
            @PathVariable("matricula") String matricula,
            @PathVariable("dniPatron") String dniPatron) {

        Embarcacion embarcacion = embarcacionRepository.findByMatricula(matricula);
        Patron patron = patronRepository.findByDni(dniPatron);

        if (embarcacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Embarcación no encontrada.");
        }

        if (patron == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patrón no encontrado.");
        }

        // Desvincular patrón de la embarcación
        boolean exito = embarcacionRepository.removePatronFromEmbarcacion(embarcacion, patron);
        if (exito) {
            return ResponseEntity.status(HttpStatus.OK).body("Patrón desvinculado de la embarcación con éxito.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al desvincular el patrón.");
        }
    }

    // Rutas DELETE

    /**
     * Elimina una embarcación por su matrícula.
     * 
     * @param matricula la matrícula de la embarcación a eliminar.
     * @return una respuesta con el estado de la eliminación.
     */
    @DeleteMapping("/embarcaciones/{matricula}")
    public ResponseEntity<String> deleteEmbarcacion(@PathVariable String matricula) {
        if (embarcacionRepository.isEmbarcacionLinkedToAnyAlquilerOrReserva(matricula)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La embarcación está vinculada a un alquiler o reserva.");
        }

        boolean success = embarcacionRepository.deleteEmbarcacion(matricula);
        if (success) {
            return ResponseEntity.ok("Embarcación eliminada con éxito.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la embarcación.");
        }
    }

    /**
     * Elimina un patrón por su DNI.
     * 
     * @param dni el DNI del patrón a eliminar.
     * @return una respuesta con el estado de la eliminación.
     */
    @DeleteMapping("/patrones/{dni}")
    public ResponseEntity<String> deletePatron(@PathVariable String dni) {
        if (patronRepository.isPatronLinkedToEmbarcacion(dni)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El patrón está vinculado a una embarcación.");
        }

        boolean success = patronRepository.deletePatron(dni);
        if (success) {
            return ResponseEntity.ok("Patrón eliminado con éxito.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el patrón.");
        }
    }
}
