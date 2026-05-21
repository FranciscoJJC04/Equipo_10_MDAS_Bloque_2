package es.uco.pw.pw2526.controller.Patrones;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import es.uco.pw.pw2526.model.domain.patron.Patron;
import es.uco.pw.pw2526.model.repository.PatronRepository;

/**
 * Controlador para actualizar datos de un patrón.
 */
@Controller
public class UpdatePatronController {

    private final PatronRepository patronRepository;

    public UpdatePatronController(PatronRepository patronRepository) {
        this.patronRepository = patronRepository;
    }

    @GetMapping("/updatePatron")
    public ModelAndView mostrarFormulario(@RequestParam(value = "dni", required = false) String dni) {
        ModelAndView mv = new ModelAndView("patron/updatePatronView");

        if (dni != null && !dni.isBlank()) {
            // Buscar patrón existente
            try {
                Patron patron = patronRepository.obtenerPatrones().stream()
                    .filter(p -> p.getDniPatron().equals(dni))
                    .findFirst()
                    .orElse(null);

                if (patron != null) {
                    mv.addObject("patron", patron);
                } else {
                    mv.addObject("error", "No se encontró el patrón con ese DNI.");
                }
            } catch (Exception e) {
                mv.addObject("error", "Error al cargar el patrón: " + e.getMessage());
            }
        }

        return mv;
    }

    @PostMapping("/updatePatron")
    public ModelAndView actualizarPatron(@ModelAttribute Patron patronActualizado) {
        ModelAndView mv = new ModelAndView("updatePatronView");

        if (patronActualizado == null || patronActualizado.getDniPatron() == null || patronActualizado.getDniPatron().isBlank()) {
            mv.addObject("status", "ERROR");
            mv.addObject("message", "DNI no válido.");
            return mv;
        }

        boolean success = patronRepository.updatePatron(patronActualizado);

        if (success) {
            mv.addObject("status", "OK");
            mv.addObject("message", "Patrón actualizado correctamente.");
        } else {
            mv.addObject("status", "ERROR");
            mv.addObject("message", "No se pudo actualizar el patrón.");
        }

        mv.addObject("patron", patronActualizado);
        return mv;
    }
}
