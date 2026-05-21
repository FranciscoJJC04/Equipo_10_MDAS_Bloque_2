package es.uco.pw.pw2526.controller.socio;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.repository.SocioRepository;

/**
 * Controlador encargado de obtener y mostrar la lista de socios registrados en el sistema.
 * <p>Gestiona la petición GET a la ruta <code>/verSocios</code> y devuelve la vista
 * correspondiente con los datos obtenidos desde el repositorio.</p>
 */
@Controller
public class ObtenerSociosController {

    SocioRepository socioRepository;

    public ObtenerSociosController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
        // SQL initialization centralized in SqlQueriesInitializer
    }

    @GetMapping("/verSocios")
    public ModelAndView obtenerSocios() {
        // Student student = studentRepository.findStudentById(id);
        List<Socio> socios = socioRepository.obtenerSocios();
        ModelAndView model = new ModelAndView("socio/obtenerSociosView.html");
        model.addObject("socios", socios);
        return model;
    }
}