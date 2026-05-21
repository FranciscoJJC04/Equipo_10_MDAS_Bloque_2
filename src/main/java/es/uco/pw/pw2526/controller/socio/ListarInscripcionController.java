package es.uco.pw.pw2526.controller.socio;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.inscripcion.Inscripcion;
import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.repository.SocioRepository;


@Controller
public class ListarInscripcionController 
{
      SocioRepository socioRepository;

    public ListarInscripcionController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
        // SQL initialization centralized in SqlQueriesInitializer
    }

    
    @GetMapping("/verInscripciones")
    public ModelAndView obtenerInscripciones() 
    {
    List<Inscripcion> inscripciones = socioRepository.obtenerInscripciones();
    ModelAndView model = new ModelAndView("socio/ObtenerIncripcionesView.html");
    model.addObject("inscripciones", inscripciones);
    return model;
    }

    
}
