package es.uco.pw.pw2526.controller.socio;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import es.uco.pw.pw2526.model.repository.SocioRepository;

/**
 * Controlador encargado de conceder el título de patrón a los socios.
 * <p>Gestiona las peticiones GET y POST a la ruta <code>/concederTitulo</code>,
 * mostrando el formulario y procesando la concesión del título en el repositorio.</p>
 */
@Controller
public class ConcederTituloController {

    private final SocioRepository socioRepository;

    public ConcederTituloController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
        // SQL initialization centralized in SqlQueriesInitializer
    }

    @GetMapping("/concederTitulo")
    public ModelAndView mostrarFormulario() {
        ModelAndView model = new ModelAndView("socio/concederTituloView");
        // Reutilizar la funcionalidad que obtiene todos los socios
        try {
            java.util.List<?> socios = socioRepository.obtenerSociosSinPatron();
            model.addObject("socios", socios);
        } catch (Exception e) {
            // No detener el render si hay error al cargar socios; la vista seguirá
            // mostrando el formulario
            System.err.println("Aviso: no se pudieron cargar los socios para el select: " + e.getMessage());
        }
        return model;
    }

    @PostMapping("/concederTitulo")
    public ModelAndView submitConcederTitulo(@RequestParam("dni") String dni) 
    {
        ModelAndView mv = new ModelAndView("socio/concederTituloView");
        // Asegurarnos de que la lista de socios esté disponible también después del POST
        try {
            java.util.List<?> socios = socioRepository.obtenerSociosSinPatron();
            mv.addObject("socios", socios);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar los socios tras el POST: " + e.getMessage());
        }
        try {
            if (dni == null || dni.isBlank()) {
                mv.addObject("status", "ERROR");
                mv.addObject("message", "DNI vacío");
                return mv;
            }

            boolean exists = socioRepository.existsByDni(dni);
            if (!exists) {
                mv.addObject("status", "ERROR");
                mv.addObject("message", "No existe un socio con ese DNI");
                return mv;
            }
            // Esta vista solo concede el título (no revoca). Si ya lo tiene, no hacemos
            // nada.
            Boolean current = socioRepository.hasTituloPatron(dni);
            if (current != null && current) {
                mv.addObject("status", "NO_CHANGE");
                mv.addObject("message", "El socio ya tiene el título de patrón.");
                return mv;
            }

            boolean updated = socioRepository.setTituloPatron(dni, true);
            if (updated) {
                mv.addObject("status", "OK");
                mv.addObject("message", "Operación realizada con éxito.");
            } else {
                mv.addObject("status", "ERROR");
                mv.addObject("message", "No se pudo actualizar el título.");
            }
            return mv;
        } catch (Exception e) {
            mv.addObject("status", "ERROR");
            mv.addObject("message", "Error interno: " + e.getMessage());
            return mv;
        }
    }

}
