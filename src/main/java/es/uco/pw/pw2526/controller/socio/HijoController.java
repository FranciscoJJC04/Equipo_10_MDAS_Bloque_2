package es.uco.pw.pw2526.controller.socio;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.repository.SocioRepository;

/**
 * Controlador encargado de gestionar la adición de hijos a socios existentes.
 * <p>Gestiona las peticiones GET y POST a la ruta <code>/addHijo</code>,
 * mostrando el formulario de registro y procesando la creación del hijo en el repositorio.</p>
 */
@Controller
public class HijoController {

    private final SocioRepository socioRepository;

    public HijoController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
        this.socioRepository.setSQLQueriesFileName("sql.properties");
    }

    /** Muestra formulario para añadir hijo */
    @GetMapping("/addHijo")
    public ModelAndView mostrarFormulario(@RequestParam("dniTitular") String dniTitular) {
        ModelAndView model = new ModelAndView("socio/addHijoView");
        model.addObject("dniTitular", dniTitular);
        model.addObject("hijo", new Socio());
        return model;
    }

    /** Procesa el formulario de creación de hijo */
    @PostMapping("/addHijo")
    public ModelAndView registrarHijo(@RequestParam("dniTitular") String dniTitular,
            @ModelAttribute("hijo") Socio hijo) {

        boolean creado = socioRepository.addHijo(dniTitular, hijo);

        if (creado) {
            return new ModelAndView("redirect:/gestionFamiliar?dni=" + dniTitular);
        } else {
            ModelAndView error = new ModelAndView("inscripcionFamiliarFail");
            error.addObject("mensaje", " No se pudo registrar el hijo. Verifica los datos.");
            return error;
        }
    }
}
