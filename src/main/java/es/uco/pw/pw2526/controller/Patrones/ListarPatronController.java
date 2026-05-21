package es.uco.pw.pw2526.controller.Patrones;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.patron.Patron;
import es.uco.pw.pw2526.model.repository.PatronRepository;


/**
 * Controller que lista los patrones disponibles y devuelve la vista
 * correspondiente.
 */
@Controller
public class ListarPatronController 
{

   PatronRepository patronRepository;
        
    public ListarPatronController (PatronRepository patronRepository){
        this.patronRepository = patronRepository;
    }

    @GetMapping("/ListarPatrones")
    public ModelAndView obtenerPatrones()
    {           
        List<Patron> patron = patronRepository.obtenerPatrones();
        ModelAndView model = new ModelAndView("patron/ListarPatronView");
        model.addObject("patron",patron);
        return model;
    }
    
}
