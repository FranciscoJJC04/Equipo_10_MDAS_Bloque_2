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
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        try {
            this.reservaRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception ignored) {
        }
        try {
            this.embarcacionRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception ignored) {
        }
        try {
            this.socioRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception ignored) {
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
        ModelAndView mv = new ModelAndView();
        // Validaciones básicas
        if (newReserva == null) {
            mv.setViewName("reserva/crearReservaViewFail");
            mv.addObject("error", "Datos de reserva vacíos.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        String matricula = newReserva.getMatricula();
        java.time.LocalDate fecha = newReserva.getFecha();

        if (fecha == null) {
            mv.setViewName("reserva/crearReservaViewFail");
            mv.addObject("error", "Fecha obligatoria.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        // Validar matrícula recibida
        if (matricula == null || matricula.trim().isEmpty()) {
            mv.setViewName("reserva/crearReservaViewFail");
            mv.addObject("error", "Matrícula obligatoria.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        // Comprobar disponibilidad de la embarcación
        Integer solapamientos = reservaRepository.countReservasSolapados(matricula, fecha, fecha);
        if (solapamientos > 0) {
            mv.setViewName("reserva/crearReservaViewFail");
            mv.addObject("error", "La embarcación no está disponible en ese rango de fechas.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        // Comprobar plazas disponibles
        Integer plazas = reservaRepository.obtenerPlazas(matricula);
        if (plazas == null) {
            mv.setViewName("reserva/crearReservaViewFail");
            mv.addObject("error", "No se encontró la embarcación.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        Integer numPasajeros = 1; // Puedes ajustar esto según la necesidad (como un campo de formulario)

        if (numPasajeros > plazas) {
            mv.setViewName("reserva/crearReservaViewFail");
            mv.addObject("error", "Número de pasajeros supera la capacidad (" + plazas + ").");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        // Calcular importe (esto es un ejemplo, ajusta según la tarifa)
        double importe = 40.0 * numPasajeros; // Asumimos 15€ por cada pasajero
        newReserva.setFecha(fecha); // Establecer la fecha

        // Guardar la reserva
        // Refactor de nombrado: evitar booleano genérico.
        boolean reservaGuardada = reservaRepository.addReserva(newReserva);
        if (!reservaGuardada) {
            mv.setViewName("reserva/crearReservaViewFail");
            mv.addObject("error", "Error guardando la reserva en la base de datos.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        mv.setViewName("reserva/crearReservaViewSuccess");
        mv.addObject("success", "Reserva creada correctamente. Importe: " + importe + " €");
        return mv;
    }

    private void populateSociosYEmbarcaciones(ModelAndView mv) {
        try {
            List<?> socios = socioRepository.obtenerSocios();
            mv.addObject("socios", socios);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar los socios: " + e.getMessage());
            mv.addObject("socios", new ArrayList<>());
        }

        try {
            try {
                List<Map<String, Object>> asignaciones = embarcacionRepository.listarAsignaciones();
                mv.addObject("asignaciones", asignaciones != null ? asignaciones : new ArrayList<>());
            } catch (Exception ex) {
                System.err.println("Aviso: no se pudieron cargar las asignaciones: " + ex.getMessage());
                mv.addObject("asignaciones", new ArrayList<>());
            }

        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar las embarcaciones tras el POST: " + e.getMessage());
            // mv.addObject("embarcaciones", new ArrayList<Embarcacion>());
            mv.addObject("Asignaciones", new ArrayList<Map<String, Object>>());
            mv.addObject("matriculas", new ArrayList<String>());
        }
    }
}
