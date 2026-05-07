package es.uco.pw.pw2526.controller.Alquiler;

import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;

import es.uco.pw.pw2526.model.repository.AlquilerRepository;
import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;
import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
/**
 * Controlador que busca embarcaciones disponibles en un rango de fechas.
 * <p>
 * Itera por tipo de embarcación y filtra aquellas que no tienen
 * alquileres solapados en el intervalo proporcionado. Devuelve una vista
 * con la lista de embarcaciones disponibles y las fechas utilizadas.
 * </p>
 */
public class BuscarEmbaracionPorFechaDisponibleController {

    /** Repositorio para operaciones sobre alquileres (comprobación de solapamientos). */
    private final AlquilerRepository alquilerRepository;
    /** Repositorio para operaciones sobre embarcaciones. */
    private final EmbarcacionRepository embarcacionRepository;

    /**
     * Constructor con inyección de repositorios.
     *
     * @param alquilerRepository    repositorio de alquileres
     * @param embarcacionRepository repositorio de embarcaciones
     */
    public BuscarEmbaracionPorFechaDisponibleController(AlquilerRepository alquilerRepository,
            EmbarcacionRepository embarcacionRepository) {
        this.alquilerRepository = alquilerRepository;
        this.embarcacionRepository = embarcacionRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        try {
            this.alquilerRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception ignored) {
        }
        try {
            this.embarcacionRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception ignored) {
        }
    }

    private boolean estaDisponible(Embarcacion embarcacion, java.time.LocalDate inicio, java.time.LocalDate fin) {
        Integer solapamientos = alquilerRepository.countAlquileresSolapados(embarcacion.getMatricula(), inicio, fin);
        return solapamientos != null && solapamientos == 0;
    }

    private List<Embarcacion> obtenerEmbarcacionesDisponibles(java.time.LocalDate inicio, java.time.LocalDate fin) {
        List<Embarcacion> disponibles = new java.util.ArrayList<>();
        for (TipoEmbarcacion tipo : TipoEmbarcacion.values()) {
            if (tipo == TipoEmbarcacion.NONE) {
                continue;
            }
            List<Embarcacion> lista = embarcacionRepository.listarPorTipo(tipo);
            if (lista == null) {
                continue;
            }
            for (Embarcacion embarcacion : lista) {
                if (estaDisponible(embarcacion, inicio, fin)) {
                    disponibles.add(embarcacion);
                }
            }
        }
        return disponibles;
    }

    @GetMapping("/ListarEmbarcacionesDisponiblesPorFecha")
    /**
     * Maneja la petición GET para listar embarcaciones disponibles en el
     * rango [inicio, fin].
     *
     * @param inicio fecha de inicio (ISO date), opcional
     * @param fin    fecha de fin (ISO date), opcional
     * @return ModelAndView con la vista y las embarcaciones disponibles
     */
    public ModelAndView listarEmbaracionesDisponiblesPorFecha(
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate inicio,
            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fin) {

        ModelAndView mv = new ModelAndView("alquiler/ListarEmbarcacionesDisponiblesPorFechaView.html");

        // Si no se han pasado las fechas, devolvemos página con lista vacía (el
        // formulario puede pedirlas)
        if (inicio == null || fin == null) {
            mv.addObject("embarcaciones", new java.util.ArrayList<>());
            return mv;
        }

        // Validación básica
        if (inicio.isAfter(fin)) {
            mv.addObject("error", "La fecha de inicio no puede ser posterior a la de fin.");
            mv.addObject("embarcaciones", new java.util.ArrayList<>());
            return mv;
        }

        mv.addObject("embarcaciones", obtenerEmbarcacionesDisponibles(inicio, fin));
        mv.addObject("inicio", inicio);
        mv.addObject("fin", fin);
        return mv;
    }

}
