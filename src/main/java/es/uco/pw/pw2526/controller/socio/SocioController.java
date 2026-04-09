package es.uco.pw.pw2526.controller.socio;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import es.uco.pw.pw2526.model.repository.SocioRepository;
import es.uco.pw.pw2526.model.domain.socio.Socio;
import java.util.List;

/**
 * Controlador encargado de gestionar las operaciones relacionadas con los socios.
 * <p>Proporciona funcionalidades para listar todos los socios junto con sus detalles,
 * incluyendo cónyuges e hijos.</p>
 */
@Controller
public class SocioController {

    private final SocioRepository socioRepository;

    public SocioController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
    }

    // Listar todos los socios con sus detalles, conyuges e hijos
    @GetMapping("/listarSocios")
    public ModelAndView listarSocios() {
        ModelAndView model = new ModelAndView("socio/listarSociosView");
        
        List<Socio> socios = socioRepository.obtenerTodosSociosConDetalles();
        
        if (socios != null && !socios.isEmpty()) {
            model.addObject("socios", socios);
        } else {
            model.addObject("mensaje", "No se encontraron socios.");
        }
        
        return model;
    }
}
