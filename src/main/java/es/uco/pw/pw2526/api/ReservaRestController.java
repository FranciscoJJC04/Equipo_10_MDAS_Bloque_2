package es.uco.pw.pw2526.api;

import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;
import es.uco.pw.pw2526.model.domain.reserva.Reserva;
import es.uco.pw.pw2526.model.repository.AlquilerRepository;
import es.uco.pw.pw2526.model.repository.ReservaRepository;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar las reservas.
 * <p>Este controlador expone los métodos necesarios para crear, consultar, modificar y cancelar reservas.</p>
 */
@RestController
@RequestMapping(path = "/api/reserva", produces = "application/json")
public class ReservaRestController {

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * Inicializa el controlador, configurando el archivo de consultas SQL.
     */
    @PostConstruct
    public void init() {
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.reservaRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    /**
     * Obtiene la lista completa de reservas.
     *
     * @return una respuesta con la lista de reservas
     */
    @GetMapping("")
    public ResponseEntity<List<Reserva>> getAllReservas() {
        List<Reserva> reservas = reservaRepository.obtenerReservas();
        return ResponseEntity.ok(reservas);
    }

    /**
     * Obtiene la lista de reservas futuras a partir de la fecha actual.
     *
     * @return una respuesta con la lista de reservas futuras
     */
    @GetMapping("/futuros")
    public ResponseEntity<List<Reserva>> getReservasFuturas() {
        try {
            // Refactor de nombrado: nombres consistentes con el dominio.
            LocalDate fechaReferencia = LocalDate.now();
            List<Reserva> reservasFuturas = reservaRepository.obtenerReservasFuturas(fechaReferencia);

            if (reservasFuturas == null || reservasFuturas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservasFuturas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene la información concreta de una reserva dada su ID.
     *
     * @param id el ID de la reserva
     * @return una respuesta con los detalles de la reserva
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reserva> getReservaById(@PathVariable("id") int id) {
        Reserva reserva = reservaRepository.obtenerReservaPorId(id);
        if (reserva == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reserva);
    }

    /**
     * Crea una nueva reserva.
     *
     * @param reserva la reserva a crear
     * @return una respuesta que indica el resultado de la creación
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> createReserva(@RequestBody Reserva reserva) {
        try {
            // Validación de los datos de la reserva
            if (reserva.getMatricula() == null || reserva.getMatricula().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La matrícula es obligatoria.");
            }
            if (reserva.getFecha() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La fecha es obligatoria.");
            }
            if (reserva.getNumPasajeros() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El número de pasajeros debe ser mayor que 0.");
            }

            // Creación de la reserva
            boolean reservaCreada = reservaRepository.addReserva(reserva);
            if (reservaCreada) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Reserva creada correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al crear la reserva.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la reserva.");
        }
    }

    /**
     * Modifica la fecha de una reserva existente.
     *
     * @param id el ID de la reserva
     * @param nuevaFechaTexto la nueva fecha para la reserva
     * @return una respuesta con el resultado de la modificación
     */
    @PatchMapping("/{id}/modificarFecha")
    public ResponseEntity<String> modificarFechaReserva(
            @PathVariable("id") int id,
            @RequestParam("nuevaFecha") String nuevaFechaTexto) {
        try {
            // Obtener la reserva por ID
            Reserva reserva = reservaRepository.obtenerReservaPorId(id);
            if (reserva == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe la reserva con ID " + id);
            }

            // Comprobar si la nueva fecha es válida
            LocalDate nuevaFecha = LocalDate.parse(nuevaFechaTexto);
            if (!nuevaFecha.isAfter(reserva.getFecha())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La nueva fecha debe ser posterior a la fecha original.");
            }

            // Verificar disponibilidad de la embarcación
            List<Reserva> reservasFuturas = reservaRepository.obtenerReservasFuturas(nuevaFecha);
            boolean isDisponible = true;
            for (Reserva reservaFutura : reservasFuturas) {
                if (reservaFutura.getMatricula().equals(reserva.getMatricula())) {
                    isDisponible = false;
                    break;
                }
            }

            if (!isDisponible) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("La embarcación no está disponible en la nueva fecha solicitada.");
            }

            // Actualizar la fecha de la reserva
            reserva.setFecha(nuevaFecha);
            boolean actualizado = reservaRepository.updateReservaFecha(reserva);
            if (actualizado) {
                return ResponseEntity.ok("Fecha de reserva modificada correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("No se pudo actualizar la fecha de la reserva.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al modificar la fecha de la reserva.");
        }
    }

    /**
     * Modifica los datos de una reserva existente.
     *
     * @param id el ID de la reserva
     * @param descripcion nueva descripción de la reserva
     * @param numPlazas nuevo número de plazas para la reserva
     * @return una respuesta con el resultado de la modificación
     */
    @PatchMapping("/{id}/modificarDatos")
    public ResponseEntity<String> modificarDatosReserva(
            @PathVariable("id") int id,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam(value = "numPlazas", required = false) Integer numPlazas) {
        try {
            // Obtener la reserva por ID
            Reserva reserva = reservaRepository.obtenerReservaPorId(id);
            if (reserva == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe la reserva con ID " + id);
            }

            // Modificar la descripción si se proporciona
            if (descripcion != null) {
                reserva.setDescripcionReserva(descripcion);
            }

            // Modificar el número de plazas si se proporciona y es válido
            if (numPlazas != null) {
                Integer plazasDisponibles = reservaRepository.obtenerPlazas(reserva.getMatricula());
                if (plazasDisponibles == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("No se pudo obtener la capacidad de la embarcación.");
                }
                if (numPlazas > plazasDisponibles) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("El número de plazas no puede exceder la capacidad máxima de la embarcación.");
                }
                reserva.setNumPasajeros(numPlazas);
            }

            // Actualizar la reserva en la base de datos
            boolean actualizado = reservaRepository.updateReservaDatos(reserva);
            if (actualizado) {
                return ResponseEntity.ok("Datos de la reserva modificados correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se pudo modificar los datos de la reserva.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al modificar los datos de la reserva.");
        }
    }
    

    /**
     * Cancela una reserva futura.
     *
     * @param id el ID de la reserva a cancelar
     * @return una respuesta con el resultado de la cancelación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelarReserva(@PathVariable("id") int id) {
        try {
            // Obtener la reserva por ID
            Reserva reserva = reservaRepository.obtenerReservaPorId(id);
            if (reserva == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("La reserva no existe.");
            }

            // Verificar si la fecha de la reserva es futura
            if (!reserva.getFecha().isAfter(LocalDate.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Solo se pueden cancelar reservas FUTURAS.");
            }

            // Llamar al repositorio para cancelar la reserva
            boolean reservaCancelada = reservaRepository.cancelarReservaFutura(id);
            if (reservaCancelada) {
                return ResponseEntity.ok("Reserva cancelada correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al cancelar la reserva.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cancelar la reserva.");
        }
    }
}
