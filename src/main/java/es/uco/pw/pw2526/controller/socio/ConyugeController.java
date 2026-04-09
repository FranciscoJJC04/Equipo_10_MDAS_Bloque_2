package es.uco.pw.pw2526.controller.socio;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.repository.SocioRepository;

/**
 * Controlador encargado de gestionar la adición de cónyuges a socios existentes.
 * <p>Gestiona las peticiones GET y POST a la ruta <code>/addConyuge</code>,
 * mostrando el formulario de registro y procesando la creación del cónyuge en el repositorio.</p>
 */
@Controller
public class ConyugeController {

    private final SocioRepository socioRepository;

    public ConyugeController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
        this.socioRepository.setSQLQueriesFileName("sql.properties");
    }

    /** Muestra formulario para añadir cónyuge */
    @GetMapping("/addConyuge")
    public ModelAndView mostrarFormulario(@RequestParam("dniTitular") String dniTitular) {
        ModelAndView model = new ModelAndView("socio/addConyugeView");
        model.addObject("dniTitular", dniTitular);
        model.addObject("conyuge", new Socio());
        return model;
    }

    /** Procesa el formulario de creación de cónyuge */
    @PostMapping("/addConyuge")
    public ModelAndView registrarConyuge(@RequestParam("dniTitular") String dniTitular,
            @ModelAttribute("conyuge") Socio conyuge) {

        boolean creado = socioRepository.addConyuge(dniTitular, conyuge);
        if (creado) {
            return new ModelAndView("redirect:/gestionFamiliar?dni=" + dniTitular);
        } else {
            ModelAndView error = new ModelAndView("socio/inscripcionFamiliarFail");
            error.addObject("mensaje", " No se pudo registrar el cónyuge. Verifica los datos.");
            return error;
        }
    }
}
