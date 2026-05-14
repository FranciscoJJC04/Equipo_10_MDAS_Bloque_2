package es.uco.pw.pw2526.controller.Alquiler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;


import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;

import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;
import es.uco.pw.pw2526.model.repository.AlquilerRepository;
import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;
import es.uco.pw.pw2526.model.repository.SocioRepository;

/**
 * Controlador responsable de gestionar la creación de alquileres.
 * <p>
 * Proporciona la vista para crear un nuevo alquiler (GET) y procesa el
 * formulario de envío (POST). Valida fechas, disponibilidad, plazas
 * y calcula el importe antes de persistir la entidad mediante el
 * {@link AlquilerRepository}.
 * </p>
 */
@Controller
public class AñadirAlquilerController {

    private ModelAndView modelAndView = new ModelAndView();
    private AlquilerRepository alquilerRepository;
    private EmbarcacionRepository embarcacionRepository;
    private SocioRepository socioRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param alquilerRepository    repositorio de alquileres
     * @param embarcacionRepository repositorio de embarcaciones
     * @param socioRepository       repositorio de socios
     */
    public AñadirAlquilerController(AlquilerRepository alquilerRepository, EmbarcacionRepository embarcacionRepository,
            SocioRepository socioRepository) {
        this.alquilerRepository = alquilerRepository;
        this.embarcacionRepository = embarcacionRepository;
        this.socioRepository = socioRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.alquilerRepository.setSQLQueriesFileName(sqlQueriesFileName);
        // If the other repositories expose the same setter, try to set it as well
        // (silent if not supported)
        try {
            this.embarcacionRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception ignored) {
        }
        try {
            this.socioRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception ignored) {
        }
    }

    private ModelAndView crearRespuestaFallida(String vista, String error) {
        ModelAndView mv = new ModelAndView(vista);
        mv.addObject("error", error);
        populateSociosYEmbarcaciones(mv);
        return mv;
    }

    private void cargarSocios(ModelAndView mv) {
        try {
            java.util.List<?> socios = socioRepository.obtenerSociosConPatron();
            mv.addObject("socios", socios);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar los socios tras el POST: " + e.getMessage());
            mv.addObject("socios", new ArrayList<>());
        }
    }

    private void cargarEmbarcaciones(ModelAndView mv) {
        try {
            List<Embarcacion> embarcaciones = new ArrayList<>();
            for (TipoEmbarcacion t : TipoEmbarcacion.values()) {
                if (t == TipoEmbarcacion.NONE) {
                    continue;
                }
                List<Embarcacion> embarcacionesPorTipo = embarcacionRepository.listarPorTipo(t);
                if (embarcacionesPorTipo != null) {
                    embarcaciones.addAll(embarcacionesPorTipo);
                }
            }
            mv.addObject("embarcaciones", embarcaciones);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar las embarcaciones tras el POST: " + e.getMessage());
            mv.addObject("embarcaciones", new ArrayList<Embarcacion>());
            mv.addObject("matriculas", new ArrayList<String>());
        }
    }

    private String validarDatosInicialesAlquiler(Alquiler alquiler) {
        if (alquiler == null) {
            return "Datos de alquiler vacíos.";
        }
        if (alquiler.getFechaInicio() == null || alquiler.getFechaFin() == null) {
            return "Fechas de inicio y fin obligatorias.";
        }
        if (alquiler.getFechaInicio().isAfter(alquiler.getFechaFin())) {
            return "La fecha de inicio no puede ser posterior a la de fin.";
        }
        return null;
    }

    private String validarDuracionAlquiler(java.time.LocalDate inicio, java.time.LocalDate fin) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
        if (dias <= 0) {
            return "Número de días inválido.";
        }
        if (!isDuracionValidaPorTemporada(inicio, dias)) {
            return "Duración no permitida para la temporada (Oct-Abr: 3 días; May-Sep: 7 o 14 días).";
        }
        return null;
    }

    private String validarDisponibilidadAlquiler(String matricula, java.time.LocalDate inicio, java.time.LocalDate fin) {
        Integer solapamientos = alquilerRepository.countAlquileresSolapados(matricula, inicio, fin);
        if (solapamientos == null) {
            return "Error comprobando disponibilidad.";
        }
        if (solapamientos > 0) {
            return "La embarcación no está disponible en ese rango de fechas.";
        }
        return null;
    }

    private String validarCapacidadAlquiler(String matricula, Integer numPasajeros) {
        Integer plazas = alquilerRepository.obtenerPlazas(matricula);
        if (plazas == null) {
            return "No se encontró la embarcación.";
        }
        if (numPasajeros > plazas) {
            return "Número de pasajeros supera la capacidad (" + plazas + ").";
        }
        return null;
    }

    private double calcularImporteAlquiler(Integer numPasajeros, java.time.LocalDate inicio, java.time.LocalDate fin) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
        return 20.0 * numPasajeros * (double) dias;
    }

    @GetMapping("/addAlquiler")
    /**
     * Muestra la vista de creación de un nuevo alquiler.
     * <p>
     * Añade al modelo un objeto vacío `Alquiler` y las listas necesarias
     * (socios y embarcaciones) para poblar los select de la plantilla.
     * </p>
     *
     * @return ModelAndView que renderiza la plantilla de creación
     */
    public ModelAndView getAddPatronView() {
        this.modelAndView.setViewName("alquiler/crearAlquilerView");
        this.modelAndView.addObject("newAlquiler", new Alquiler());
        // Asegurar que la vista inicial también tenga las listas de socios y
        // embarcaciones
        populateSociosYEmbarcaciones(this.modelAndView);
        return modelAndView;
    }

    @PostMapping("/addAlquiler")
    /**
     * Procesa el envío del formulario de creación de alquiler.
     * <p>
     * Valida fechas, duración por temporada, disponibilidad y plazas. Si
     * todas las comprobaciones son correctas calcula el importe y delega en
     * {@link AlquilerRepository#addAlquiler(Alquiler)} para persistir la
     * reserva.
     * </p>
     *
     * @param newAlquiler objeto vinculado desde el formulario
     * @return ModelAndView con la vista de éxito o fallo y mensajes
     */
    public ModelAndView postAddAlquiler(@ModelAttribute("newAlquiler") Alquiler newAlquiler) {
        String errorInicial = validarDatosInicialesAlquiler(newAlquiler);
        if (errorInicial != null) {
            String vista = "Datos de alquiler vacíos.".equals(errorInicial)
                    ? "alquiler/crearAlquilerView"
                    : "alquiler/crearAlquilerViewFail";
            return crearRespuestaFallida(vista, errorInicial);
        }

        String matricula = newAlquiler.getMatricula();
        Integer numPasajeros = newAlquiler.getNumPasajeros();
        java.time.LocalDate inicio = newAlquiler.getFechaInicio();
        java.time.LocalDate fin = newAlquiler.getFechaFin();

        String errorDuracion = validarDuracionAlquiler(inicio, fin);
        if (errorDuracion != null) {
            return crearRespuestaFallida("alquiler/crearAlquilerViewFail", errorDuracion);
        }

        String errorDisponibilidad = validarDisponibilidadAlquiler(matricula, inicio, fin);
        if (errorDisponibilidad != null) {
            return crearRespuestaFallida("alquiler/crearAlquilerViewFail.html", errorDisponibilidad);
        }

        String errorCapacidad = validarCapacidadAlquiler(matricula, numPasajeros);
        if (errorCapacidad != null) {
            return crearRespuestaFallida("alquiler/crearAlquilerViewFail.html", errorCapacidad);
        }

        double importe = calcularImporteAlquiler(numPasajeros, inicio, fin);
        newAlquiler.setImporteTotal(importe);
        boolean alquilerGuardado = alquilerRepository.addAlquiler(newAlquiler);
        if (!alquilerGuardado) {
            return crearRespuestaFallida("alquiler/crearAlquilerViewFail.html",
                    "Error guardando el alquiler en la base de datos.");
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("alquiler/crearAlquilerViewSuccess.html");
        mv.addObject("success", "Alquiler creado correctamente. Importe: " + importe + " €");
        return mv;
    }

    // Rellena el modelo con la lista combinada de embarcaciones (por tipo) y la
    // lista de socios
    /**
     * Rellena el {@code ModelAndView} con las colecciones necesarias para la
     * vista de creación: lista de socios y listado de embarcaciones.
     * <p>
     * Intenta recuperar las colecciones desde los repositorios y, en caso
     * de error, deja listas vacías para que la vista pueda renderizarse.
     * </p>
     *
     * @param mv modelo a poblar
     */
    private void populateSociosYEmbarcaciones(ModelAndView mv) {
        cargarSocios(mv);
        cargarEmbarcaciones(mv);
    }

    // Método auxiliar para validar duración por temporada
    /**
     * Valida si la duración en días es válida para la temporada de la fecha
     * dada.
     *
     * Reglas:
     * <ul>
     *   <li>Octubre (10) - Abril (4): exactamente 3 días</li>
     *   <li>Mayo (5) - Septiembre (9): 7 o 14 días</li>
     * </ul>
     *
     * @param fechaInicio fecha de inicio
     * @param dias        número de días (incluye ambos extremos)
     * @return {@code true} si la duración es válida según la temporada
     */
    private boolean isDuracionValidaPorTemporada(java.time.LocalDate fechaInicio, long dias) {
        int mes = fechaInicio.getMonthValue();
        // Octubre(10) - Abril(4) => exactamente 3 días
        if (mes >= 10 || mes <= 4) {
            return dias == 3;
        }
        // Mayo(5) - Septiembre(9) => 7 o 14 días
        return dias == 7 || dias == 14;
    }

}
