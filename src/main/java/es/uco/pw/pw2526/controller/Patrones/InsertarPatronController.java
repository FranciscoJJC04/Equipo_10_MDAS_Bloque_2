package es.uco.pw.pw2526.controller.Patrones;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.patron.Patron;
import es.uco.pw.pw2526.model.repository.PatronRepository;


/**
 * Controlador para insertar patrones. Proporciona vistas para mostrar el
 * formulario de inserción y procesar la creación de un nuevo patrón.
 */
@Controller
public class InsertarPatronController {

    private ModelAndView modelAndView = new ModelAndView();
    PatronRepository patronRepository;
        
    public InsertarPatronController(PatronRepository patronRepository){
        this.patronRepository = patronRepository;
    }

    @GetMapping("/addPatron")
    public ModelAndView getAddPatronView() {
        this.modelAndView.setViewName("patron/addPatronView.html");
        this.modelAndView.addObject("newPatron", new Patron());
        return modelAndView;
    }

    @PostMapping("/addPatron")
    public ModelAndView addPatron(@ModelAttribute Patron newPatron) {
        if (newPatron == null || newPatron.getDniPatron() == null || newPatron.getDniPatron().isBlank()) {
            boolean success = patronRepository.addPatron(newPatron);
            return buildResponse(success, newPatron);
        }

        if (patronRepository.existsByDni(newPatron.getDniPatron())) {
            return buildResponse(false, newPatron);
        }

        boolean success = patronRepository.addPatron(newPatron);
        return buildResponse(success, newPatron);
    }

    private ModelAndView buildResponse(boolean success, Patron patron) {
        ModelAndView mv = new ModelAndView();
        String nextPage = success ? "patron/addPatronViewSuccess" : "patron/addPatronViewFail";
        mv.setViewName(nextPage);
        mv.addObject("patron", patron);
        return mv;
    }

}




