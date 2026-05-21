package es.uco.pw.pw2526.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;
import es.uco.pw.pw2526.model.repository.AlquilerRepository;
import es.uco.pw.pw2526.util.ValidationUtils;
// SQL initialization handled by SqlQueriesInitializer

/**
 * Controlador REST para gestionar los alquileres de embarcaciones.
 * <p>
 * Este controlador maneja las peticiones HTTP relacionadas con la gestión de alquileres, incluyendo la creación,
 * consulta, actualización (vinculación/desvinculación de socios) y eliminación de alquileres.
 * </p>
 */
@RestController
@RequestMapping(path="/api/alquileres", produces="application/json")
public class AlquilerRestController {

    @Autowired
    private AlquilerRepository alquilerRepository;

    /**
     * Inicializa el controlador configurando el archivo de consultas SQL.
     * 
     * Este método es ejecutado después de la construcción del bean.
     */
    // SQL initialization handled by SqlQueriesInitializer

    private String validarDatosAlquiler(Alquiler alquiler) {
        String error = ValidationUtils.requireNonEmpty(alquiler.getMatricula(), "La matrícula es obligatoria.");
        if (error != null) {
            return error;
        }

        error = ValidationUtils.requireDateRange(
                alquiler.getFechaInicio(),
                alquiler.getFechaFin(),
                "Las fechas de inicio y fin son obligatorias.",
                "La fecha de fin no puede ser anterior a la fecha de inicio.");
        if (error != null) {
            return error;
        }

        return ValidationUtils.requirePositive(alquiler.getNumPasajeros(), "El número de pasajeros debe ser mayor que 0.");
    }

    private boolean esAlquilerFuturo(Alquiler alquiler) {
        return alquiler.getFechaInicio().isAfter(LocalDate.now());
    }

    /**
     * Obtiene todos los alquileres registrados.
     * 
     * @return una respuesta HTTP con el estado 200 (OK) y la lista de alquileres, o 204 (No Content) si no hay alquileres.
     */
    @GetMapping("")
    public ResponseEntity<List<Alquiler>> getAllAlquileres() {
        List<Alquiler> alquileres = alquilerRepository.obtenerAlquileres();
        return ResponseEntity.ok(alquileres);
    }

    /**
     * Obtiene los alquileres futuros, filtrados por fecha.
     * 
     * @param fechaTexto la fecha de inicio a partir de la cual se desean los alquileres futuros.
     * @return una respuesta HTTP con los alquileres futuros o 400 (Bad Request) si la fecha es inválida.
     */
    @GetMapping("/futuros/{fecha}")
    public ResponseEntity<List<Alquiler>> getAlquileresFuturos(@PathVariable("fecha") String fechaTexto) {
        LocalDate fechaReferencia = LocalDate.parse(fechaTexto);
        List<Alquiler> alquileresFuturos = alquilerRepository.obtenerAlquileresFuturos(fechaReferencia);

        if (alquileresFuturos == null || alquileresFuturos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(alquileresFuturos);
    }

    /**
     * Obtiene un alquiler por su ID.
     * 
     * @param id el ID del alquiler a buscar.
     * @return una respuesta HTTP con el alquiler correspondiente o 404 (Not Found) si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Alquiler> getAlquilerById(@PathVariable("id") int id) {
        Alquiler alquiler = alquilerRepository.obtenerAlquilerPorId(id);
        if (alquiler == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(alquiler);
    }

    /**
     * Obtiene las embarcaciones disponibles en un rango de fechas.
     * 
     * @param inicio la fecha de inicio del rango.
     * @param fin la fecha de fin del rango.
     * @return una respuesta HTTP con las embarcaciones disponibles o 400 (Bad Request) si hay un error de formato.
     */
    @GetMapping("/embarcaciones-disponibles")
    public ResponseEntity<List<String>> getEmbarcacionesDisponibles(
            @RequestParam("inicio") String inicio,
            @RequestParam("fin") String fin) {

        LocalDate fechaInicio = LocalDate.parse(inicio);
        LocalDate fechaFin = LocalDate.parse(fin);

        List<String> matriculasDisponibles = alquilerRepository.listarEmbarcacionesDisponiblesPorFecha(fechaInicio, fechaFin);

        if (matriculasDisponibles == null || matriculasDisponibles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(matriculasDisponibles);
    }

    /**
     * Crea un nuevo alquiler.
     * 
     * @param alquiler el alquiler a crear.
     * @return una respuesta HTTP con el estado 201 (Created) si el alquiler se crea correctamente, o 400 (Bad Request) si hay algún error.
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> createAlquiler(@RequestBody Alquiler alquiler) {
        String errorValidacion = validarDatosAlquiler(alquiler);
        if (errorValidacion != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorValidacion);
        }

        boolean alquilerCreado = alquilerRepository.addAlquiler(alquiler);
        if (alquilerCreado) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Alquiler creado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el alquiler.");
        }
    }

    /**
     * Vincula un socio no titular a un alquiler futuro.
     * 
     * @param id el ID del alquiler.
     * @param dniSocio el DNI del socio a vincular.
     * @return una respuesta HTTP con el estado de la vinculación.
     */
    @PatchMapping("/{id}/vincular")
    public ResponseEntity<String> vincularSocioNoTitular(
            @PathVariable("id") int id,
            @RequestParam("dni") String dniSocio) {

        Alquiler alquiler = alquilerRepository.obtenerAlquilerPorId(id);

        if (alquiler == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el alquiler con ID " + id);
        }

        if (!esAlquilerFuturo(alquiler)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Solo se puede vincular socios en alquileres FUTUROS.");
        }

        boolean socioVinculado = alquilerRepository.addSocioAlquiler(dniSocio, id);

        if (socioVinculado) {
            return ResponseEntity.ok("Socio vinculado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No se pudo vincular el socio.");
        }
    }

    /**
     * Desvincula un socio no titular de un alquiler futuro.
     * 
     * @param id el ID del alquiler.
     * @param dniSocio el DNI del socio a desvincular.
     * @return una respuesta HTTP con el estado de la desvinculación.
     */
    @PatchMapping("/{id}/desvincular")
    public ResponseEntity<String> desvincularSocioNoTitular(
            @PathVariable("id") int id,
            @RequestParam("dni") String dniSocio) {

        Alquiler alquiler = alquilerRepository.obtenerAlquilerPorId(id);

        if (alquiler == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el alquiler con ID " + id);
        }

        if (!esAlquilerFuturo(alquiler)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Solo se puede desvincular socios en alquileres FUTUROS.");
        }

        boolean socioDesvinculado = alquilerRepository.desvincularSocioNoTitular(id, dniSocio);

        if (socioDesvinculado) {
            return ResponseEntity.ok("Socio desvinculado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No se pudo desvincular el socio.");
        }
    }

    /**
     * Cancela un alquiler futuro.
     * 
     * @param id el ID del alquiler a cancelar.
     * @return una respuesta HTTP con el estado de la cancelación.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelarAlquiler(@PathVariable("id") int id) {

        Alquiler alquiler = alquilerRepository.obtenerAlquilerPorId(id);

        if (alquiler == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El alquiler no existe.");
        }

        if (!esAlquilerFuturo(alquiler)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Solo se pueden cancelar alquileres FUTUROS.");
        }

        boolean alquilerCancelado = alquilerRepository.cancelarAlquilerFuturo(id);

        if (alquilerCancelado) {
            return ResponseEntity.ok("Alquiler cancelado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cancelar el alquiler.");
        }
    }
}