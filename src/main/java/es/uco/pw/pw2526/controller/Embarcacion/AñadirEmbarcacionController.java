package es.uco.pw.pw2526.controller.Embarcacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;
import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;

/**
 * Controlador encargado de gestionar la funcionalidad de añadir nuevas embarcaciones.
ador encargado de gestionar la funcionalidad de añadir nuevas embarcaciones.
 * <p>Proporcion * <p>Proporciona la vista de formulario para introducira la vista de formulario para introducir datos de una embarcación
 * y mane datos de una embarcación
 * y maneja la inserción enja la inserción en el repositorio. el repositorio.</p>
 */
@Controller
public class AñadirEmbarcacionController {

    private EmbarcacionRepository embarcacionRepository;

    public AñadirEmbarcacionController(EmbarcacionRepository embarcacionRepository) {
        this.embarcacionRepository = embarcacionRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.embarcacionRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @GetMapping("/addEmbarcacion")
    public ModelAndView getAddEmbarcacionView() {
        ModelAndView model = new ModelAndView();
        model.setViewName("embarcaciones/addEmbarcacionView"); // sin .html
        model.addObject("newEmbarcacion", new Embarcacion());
        model.addObject("tipos", TipoEmbarcacion.values());
        return model;
    }

    @PostMapping("/addEmbarcacion")
    public ModelAndView addEmbarcacion(@ModelAttribute Embarcacion embarcacion) {
        boolean success = embarcacionRepository.addEmbarcacion(embarcacion);
        return buildResponse(success, embarcacion);
    }

    private ModelAndView buildResponse(boolean success, Embarcacion embarcacion) {
        ModelAndView mv = new ModelAndView();
        String nextPage = success ? "embarcaciones/addEmbarcacionViewSuccess" : "embarcaciones/addEmbarcacionViewFail"; // sin .html
        mv.setViewName(nextPage);
        mv.addObject("embarcacion", embarcacion);
        return mv;
    }
}
