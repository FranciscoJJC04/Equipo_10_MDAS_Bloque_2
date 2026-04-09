package es.uco.pw.pw2526.controller.socio;

import java.util.List;
import java.util.Collections;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.repository.SocioRepository;
/**
 * Controlador encargado de gestionar la inscripción de socios familiares.
 * <p>Proporciona la vista de formulario para crear un nuevo socio de tipo familiar
 * y permite gestionar los familiares asociados (cónyuge e hijos).</p>
 */
@Controller
public class InscripcionFamiliarController {

    private final SocioRepository socioRepository;

    public InscripcionFamiliarController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
        this.socioRepository.setSQLQueriesFileName("sql.properties");
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
        boolean creado = socioRepository.nuevaInscripcionFamiliar(socio);

        if (creado) {
            // Redirige a la vista de gestión familiar con el DNI del socio recién creado
            return new ModelAndView("redirect:/gestionFamiliar?dni=" + socio.getDni());
        } else {
            ModelAndView error = new ModelAndView("socio/inscripcionFamiliarFail");
            error.addObject("mensaje", "❌ No se pudo registrar el socio familiar. Revisa los datos o el DNI (puede existir ya).");
            return error;
        }
    }

    /** Muestra la vista para gestionar familiares (conyuge e hijos) */
    // Muestra la vista para gestionar familiares (conyuge e hijos)
    @GetMapping("/gestionFamiliar")
    public ModelAndView gestionarFamiliar(@RequestParam("dni") String dni) {
        ModelAndView model = new ModelAndView("socio/gestionfamiliarView");

        // Obtén el socio por su DNI
        Socio socio = socioRepository.findByDni(dni);

        // Verifica si el socio existe
        if (socio == null) {
            model.setViewName("inscripcionFamiliarFail");
            model.addObject("mensaje", "No se encontró el socio con DNI " + dni);
            return model;
        }

        // Obtén el cónyuge asociado al socio
        Socio conyuge = socioRepository.obtenerSocioConyuge(socio.getIdInscripcion());

        // Obtén el hijo asociado al socio
        Socio hijo = socioRepository.obtenerSocioHijo(socio.getIdInscripcion());

        // Pasar los datos al modelo para la vista
        model.addObject("socio", socio);
        model.addObject("conyuge", conyuge != null ? conyuge : null);
        model.addObject("hijos", hijo != null ? hijo : null); // Pasar el hijo

        return model;
    }
}