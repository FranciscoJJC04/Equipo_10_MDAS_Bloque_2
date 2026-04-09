package es.uco.pw.pw2526.controller.Embarcacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;
import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;
import es.uco.pw.pw2526.model.repository.PatronRepository;

import es.uco.pw.pw2526.model.domain.patron.Patron;

import org.springframework.format.annotation.DateTimeFormat;

@Controller
class AsignarPatronaEmbarcacionController {
    private ModelAndView modelAndView = new ModelAndView();
    private final EmbarcacionRepository embarcacionRepository;
    private final PatronRepository patronRepository;

    public AsignarPatronaEmbarcacionController(EmbarcacionRepository embarcacionRepository,
            PatronRepository patronRepository) {
        this.embarcacionRepository = embarcacionRepository;
        this.patronRepository = patronRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.embarcacionRepository.setSQLQueriesFileName(sqlQueriesFileName);
        this.patronRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @GetMapping("/AsignarPatronEmbarcacion")
    public ModelAndView getAsignarPatronEmbarcacionView() {
        this.modelAndView.setViewName("embarcaciones/AsignarPatronEmbarcacionView");
        this.modelAndView.addObject("asignacion", new Embarcacion());
        // añadir listas de patrones y embarcaciones al modelo
        List<?> patrones = patronRepository.obtenerPatrones();
        this.modelAndView.addObject("patrones", patrones);

        // listar todas las embarcaciones por tipo y combinarlas
        List<Embarcacion> embarcaciones = new ArrayList<>();
        for (TipoEmbarcacion t : TipoEmbarcacion.values()) {
            if (t == TipoEmbarcacion.NONE)
                continue;
            List<Embarcacion> list = embarcacionRepository.listarPorTipo(t);
            if (list != null)
                embarcaciones.addAll(list);
        }
        this.modelAndView.addObject("embarcaciones", embarcaciones);
        return modelAndView;
    }

    @PostMapping("/AsignarPatronEmbarcacion")
    public ModelAndView addPatronAEmbarcacion(
            @RequestParam("fechaAsignacion") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam("embarcacion.matricula") String matricula,
            @RequestParam("patron.dni_patron") String dniPatron) {

        Embarcacion embarcacion = new Embarcacion();
        embarcacion.setMatricula(matricula);

        Patron patron = new Patron();
        patron.setDni_patron(dniPatron);

        // Verificación de solapamiento
        boolean haySolapamiento = embarcacionRepository.existeSolapamientoEmbarcacion(matricula, fechaInicio, fechaFin);
        if (haySolapamiento) {
            // Puedes redirigir a una vista específica o usar la misma de fallo
            return buildResponse(false, fechaInicio, fechaFin, embarcacion, patron);
        }

        // Si no hay solapamiento, se procede con la asignación
        boolean success = embarcacionRepository.addPatronAEmbarcacion(fechaInicio, fechaFin, embarcacion, patron);
        return buildResponse(success, fechaInicio, fechaFin, embarcacion, patron);
    }

    private ModelAndView buildResponse(boolean success, LocalDate fechaInicio, LocalDate fechaFin,
            Embarcacion embarcacion, Patron patron) {
        ModelAndView mv = new ModelAndView();
        String nextPage = success ? "embarcacion/AsignarPatronEmbarcacionViewSuccess"
                : "embarcacion/AsignarPatronEmbarcacionViewFail";
        mv.setViewName(nextPage);
        mv.addObject("asignacion", embarcacion);
        mv.addObject("patron", patron);
        mv.addObject("fechaInicio", fechaInicio);
        mv.addObject("fechaFin", fechaFin);
        return mv;
    }

}
