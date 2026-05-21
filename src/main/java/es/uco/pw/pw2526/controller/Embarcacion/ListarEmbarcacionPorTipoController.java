package es.uco.pw.pw2526.controller.Embarcacion;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;
import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;

/**
 * Controlador encargado de listar embarcaciones filtradas por tipo.
 * <p>Gestiona la petición GET a la ruta <code>/listarEmbarcacionesPorTipo</code> y 
 * devuelve la vista correspondiente con los datos obtenidos desde el repositorio.</p>
 */
@Controller
public class ListarEmbarcacionPorTipoController {

    private EmbarcacionRepository embarcacionRepository;

    public ListarEmbarcacionPorTipoController(EmbarcacionRepository embarcacionRepository) {
        this.embarcacionRepository = embarcacionRepository;
    }

    @GetMapping("/listarEmbarcacionesPorTipo")
    public ModelAndView listarPorTipo(@RequestParam("tipo") TipoEmbarcacion tipo) {
        List<Embarcacion> embarcaciones = embarcacionRepository.listarPorTipo(tipo);
        ModelAndView model = new ModelAndView("embarcaciones/listarEmbarcacionesPorTipoView");
        model.addObject("embarcaciones", embarcaciones);
        model.addObject("tipo", tipo);
        return model;
    }
}
