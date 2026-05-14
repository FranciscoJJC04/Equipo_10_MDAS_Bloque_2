package es.uco.pw.pw2526.controller.socio;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.facade.GestionFamiliarFacade;
import es.uco.pw.pw2526.facade.GestionFamiliarResult;
import es.uco.pw.pw2526.model.domain.socio.Socio;
/**
 * Controlador encargado de gestionar la inscripción de socios familiares.
 * <p>Proporciona la vista de formulario para crear un nuevo socio de tipo familiar
 * y permite gestionar los familiares asociados (cónyuge e hijos).</p>
 */
@Controller
public class InscripcionFamiliarController {

    private final GestionFamiliarFacade gestionFamiliarFacade;

    public InscripcionFamiliarController(GestionFamiliarFacade gestionFamiliarFacade) {
        this.gestionFamiliarFacade = gestionFamiliarFacade;
    }

    /** Muestra el formulario de inscripción familiar */
    @GetMapping("/nuevaInscripcionFamiliar")
    public ModelAndView mostrarFormulario() {
        ModelAndView model = new ModelAndView("socio/inscripcionfamiliarView");
        model.addObject("socio", new Socio());
        return model;
    }

    /** Procesa el formulario y crea un nuevo socio de tipo familiar */
    @PostMapping("/nuevaInscripcionFamiliar")
    public ModelAndView registrarFamiliar(@ModelAttribute("socio") Socio socio) {
        boolean creado = gestionFamiliarFacade.registrarInscripcionFamiliar(socio);

        if (creado) {
            // Redirige a la vista de gestión familiar con el DNI del socio recién creado
            return new ModelAndView("redirect:/gestionFamiliar?dni=" + socio.getDni());
        } else {
            ModelAndView error = new ModelAndView("socio/inscripcionFamiliarFail");
            error.addObject("mensaje", "No se pudo registrar el socio familiar. Revisa los datos o el DNI (puede existir ya).");
            return error;
        }
    }

    /** Muestra la vista para gestionar familiares (conyuge e hijos) */
    // Muestra la vista para gestionar familiares (conyuge e hijos)
    @GetMapping("/gestionFamiliar")
    public ModelAndView gestionarFamiliar(@RequestParam("dni") String dni) {
        ModelAndView model = new ModelAndView("socio/gestionfamiliarView");

        GestionFamiliarResult resultado = gestionFamiliarFacade.obtenerGestionFamiliar(dni);
        if (!resultado.isEncontrado()) {
            model.setViewName("inscripcionFamiliarFail");
            model.addObject("mensaje", "No se encontró el socio con DNI " + resultado.getDniBuscado());
            return model;
        }

        model.addObject("socio", resultado.getSocio());
        model.addObject("conyuge", resultado.getConyuge() != null ? resultado.getConyuge() : null);
        model.addObject("hijos", resultado.getHijo() != null ? resultado.getHijo() : null);

        return model;
    }
}