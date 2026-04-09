package es.uco.pw.pw2526.controller.Embarcacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;

import java.util.List;
import java.util.Map;

/**
 * Controlador encargado de listar las asignaciones de embarcaciones.
 * <p>Gestiona la petición GET a la ruta <code>/listarAsignaciones</code> y 
 * devuelve la vista correspondiente con los datos obtenidos desde el repositorio.</p>
 */
@Controller
public class ListarAsignacionesController {

    private final EmbarcacionRepository embarcacionRepository;

    public ListarAsignacionesController(EmbarcacionRepository embarcacionRepository) {
        this.embarcacionRepository = embarcacionRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.embarcacionRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @GetMapping("/listarAsignaciones")
    public ModelAndView listarAsignaciones() {
        List<Map<String, Object>> asignaciones = embarcacionRepository.listarAsignaciones();
        ModelAndView mv = new ModelAndView("embarcaciones/listarAsignacionesView");
        mv.addObject("asignaciones", asignaciones);
        return mv;
    }
}
