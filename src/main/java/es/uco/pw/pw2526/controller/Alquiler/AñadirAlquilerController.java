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

@Controller
/**
 * Controlador responsable de gestionar la creación de alquileres.
 * <p>
 * Proporciona la vista para crear un nuevo alquiler (GET) y procesa el
 * formulario de envío (POST). Valida fechas, disponibilidad, plazas
 * y calcula el importe antes de persistir la entidad mediante el
 * {@link AlquilerRepository}.
 * </p>
 */
public class AñadirAlquilerController {

    /** Reutiliza un ModelAndView para operaciones sencillas sobre vistas. */
    private ModelAndView modelAndView = new ModelAndView();
    /** Repositorio para operaciones sobre alquileres. */
    private AlquilerRepository alquilerRepository;
    /** Repositorio para operaciones sobre embarcaciones. */
    private EmbarcacionRepository embarcacionRepository;
    /** Repositorio para operaciones sobre socios. */
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
        ModelAndView mv = new ModelAndView();
        // Validaciones básicas
        if (newAlquiler == null) {
            mv.setViewName("alquiler/crearAlquilerView");
            mv.addObject("error", "Datos de alquiler vacíos.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        String matricula = newAlquiler.getMatricula();
        String dni = newAlquiler.getDniSocio();
        Integer numPasajeros = newAlquiler.getNumPasajeros();
        java.time.LocalDate inicio = newAlquiler.getFechaInicio();
        java.time.LocalDate fin = newAlquiler.getFechaFin();

        if (inicio == null || fin == null) {
            mv.setViewName("alquiler/crearAlquilerViewFail");
            mv.addObject("error", "Fechas de inicio y fin obligatorias.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }
        if (inicio.isAfter(fin)) {
            mv.setViewName("alquiler/crearAlquilerViewFail");
            mv.addObject("error", "La fecha de inicio no puede ser posterior a la de fin.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
        if (dias <= 0) {
            mv.setViewName("alquiler/crearAlquilerViewFail");
            mv.addObject("error", "Número de días inválido.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }
        if (!isDuracionValidaPorTemporada(inicio, dias)) {
            mv.setViewName("alquiler/crearAlquilerViewFail");
            mv.addObject("error", "Duración no permitida para la temporada (Oct-Abr: 3 días; May-Sep: 7 o 14 días).");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        // 3) Comprobar disponibilidad (usa método en AlquilerRepository que cuente
        // solapamientos)
        Integer solapamientos = alquilerRepository.countAlquileresSolapados(matricula, inicio, fin);
        if (solapamientos == null) {
            mv.setViewName("alquiler/crearAlquilerViewFail.html");
            mv.addObject("error", "Error comprobando disponibilidad.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }
        if (solapamientos > 0) {
            mv.setViewName("alquiler/crearAlquilerViewFail.html");
            mv.addObject("error", "La embarcación no está disponible en ese rango de fechas.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        // 4) Comprobar plazas usando AlquilerRepository
        Integer plazas = alquilerRepository.obtenerPlazas(matricula);
        if (plazas == null) {
            mv.setViewName("alquiler/crearAlquilerViewFail.html");
            mv.addObject("error", "No se encontró la embarcación.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }
        if (numPasajeros > plazas) {
            mv.setViewName("alquiler/crearAlquilerViewFail.html");
            mv.addObject("error", "Número de pasajeros supera la capacidad (" + plazas + ").");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

        // 5) Calcular importe y persistir
        double importe = 20.0 * numPasajeros * (double) dias;
        newAlquiler.setImporteTotal(importe);

        boolean ok = alquilerRepository.addAlquiler(newAlquiler);
        if (!ok) {
            mv.setViewName("alquiler/crearAlquilerViewFail.html");
            mv.addObject("error", "Error guardando el alquiler en la base de datos.");
            populateSociosYEmbarcaciones(mv);
            return mv;
        }

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
        // Socios->listado de socios
        try {
            java.util.List<?> socios = socioRepository.obtenerSociosConPatron();
            mv.addObject("socios", socios);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar los socios tras el POST: " + e.getMessage());
            mv.addObject("socios", new ArrayList<>());
        }

        // Embarcaciones: combinar por tipo->listado de embarcaciones (matriculas)
        try {
            List<Embarcacion> embarcaciones = new ArrayList<>();
            for (TipoEmbarcacion t : TipoEmbarcacion.values()) {
                if (t == TipoEmbarcacion.NONE)
                    continue;
                List<Embarcacion> list = embarcacionRepository.listarPorTipo(t);
                if (list != null)
                    embarcaciones.addAll(list);
            }
            mv.addObject("embarcaciones", embarcaciones);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar las embarcaciones tras el POST: " + e.getMessage());
            mv.addObject("embarcaciones", new ArrayList<Embarcacion>());
            mv.addObject("matriculas", new ArrayList<String>());
        }
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

    /**
     * Construye un {@link ModelAndView} con la vista de resultado según el
     * parámetro {@code success} y añade el objeto alquiler al modelo.
     *
     * @param success indica si la operación fue exitosa
     * @param alquiler objeto alquiler (puede ser null)
     * @return ModelAndView preparado para la vista de respuesta
     */
    private ModelAndView buildResponse(boolean success, Alquiler alquiler) {
        ModelAndView mv = new ModelAndView();
        String nextPage = success ? "/crearAlquilerViewSuccess.html" : "/crearAlquilerViewFail.html";
        mv.setViewName(nextPage);
        mv.addObject("alquiler", alquiler);
        return mv;
    }

}
