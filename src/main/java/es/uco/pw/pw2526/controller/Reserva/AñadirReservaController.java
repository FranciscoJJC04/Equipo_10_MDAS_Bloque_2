package es.uco.pw.pw2526.controller.Reserva;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import es.uco.pw.pw2526.model.domain.reserva.Reserva;
import es.uco.pw.pw2526.model.repository.ReservaRepository;
import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;
import es.uco.pw.pw2526.model.repository.SocioRepository;
import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;

/**
 * Controlador encargado de gestionar la funcionalidad de añadir nuevas reservas.
 * <p>Proporciona la vista de formulario para introducir datos de una reserva
 * y maneja la inserción en el repositorio.</p>
 */
@Controller
public class AñadirReservaController {

    private ModelAndView modelAndView = new ModelAndView();
    private ReservaRepository reservaRepository;
    private EmbarcacionRepository embarcacionRepository;
    private SocioRepository socioRepository;

    public AñadirReservaController(ReservaRepository reservaRepository, EmbarcacionRepository embarcacionRepository,
            SocioRepository socioRepository) {
        this.reservaRepository = reservaRepository;
        this.embarcacionRepository = embarcacionRepository;
        this.socioRepository = socioRepository;
        // SQL initialization centralized in SqlQueriesInitializer
    }

    private ModelAndView crearRespuestaFallida(String error) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("reserva/crearReservaViewFail");
        mv.addObject("error", error);
        populateSociosYEmbarcaciones(mv);
        return mv;
    }

    private void cargarSocios(ModelAndView mv) {
        try {
            List<?> socios = socioRepository.obtenerSocios();
            mv.addObject("socios", socios);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar los socios: " + e.getMessage());
            mv.addObject("socios", new ArrayList<>());
        }
    }

    private void cargarAsignaciones(ModelAndView mv) {
        try {
            List<Map<String, Object>> asignaciones = embarcacionRepository.listarAsignaciones();
            mv.addObject("asignaciones", asignaciones != null ? asignaciones : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar las asignaciones: " + e.getMessage());
            mv.addObject("asignaciones", new ArrayList<>());
        }
    }

    @GetMapping("/addReserva")
    public ModelAndView getAddReservaView() {
        this.modelAndView.setViewName("reserva/crearReservaView");
        this.modelAndView.addObject("newReserva", new Reserva());
        populateSociosYEmbarcaciones(this.modelAndView);
        return modelAndView;
    }

    @PostMapping("/addReserva")
    public ModelAndView postAddReserva(@ModelAttribute("newReserva") Reserva newReserva) {
        if (newReserva == null) {
            return crearRespuestaFallida("Datos de reserva vacíos.");
        }

        String matricula = newReserva.getMatricula();
        java.time.LocalDate fecha = newReserva.getFecha();

        if (fecha == null) {
            return crearRespuestaFallida("Fecha obligatoria.");
        }

        if (matricula == null || matricula.trim().isEmpty()) {
            return crearRespuestaFallida("Matrícula obligatoria.");
        }

        Integer solapamientos = reservaRepository.countReservasSolapados(matricula, fecha, fecha);
        if (solapamientos > 0) {
            return crearRespuestaFallida("La embarcación no está disponible en ese rango de fechas.");
        }

        Integer plazas = reservaRepository.obtenerPlazas(matricula);
        if (plazas == null) {
            return crearRespuestaFallida("No se encontró la embarcación.");
        }

        Integer numPasajeros = 1; // Puedes ajustar esto según la necesidad (como un campo de formulario)

        if (numPasajeros > plazas) {
            return crearRespuestaFallida("Número de pasajeros supera la capacidad (" + plazas + ").");
        }

        double importe = 40.0 * numPasajeros; // Asumimos 15€ por cada pasajero
        newReserva.setFecha(fecha);

        boolean reservaGuardada = reservaRepository.addReserva(newReserva);
        if (!reservaGuardada) {
            return crearRespuestaFallida("Error guardando la reserva en la base de datos.");
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("reserva/crearReservaViewSuccess");
        mv.addObject("success", "Reserva creada correctamente. Importe: " + importe + " €");
        return mv;
    }

    private void populateSociosYEmbarcaciones(ModelAndView mv) {
        cargarSocios(mv);
        cargarAsignaciones(mv);
    }
}
