package es.uco.pw.pw2526.controller.socio;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.repository.SocioRepository;

/**
 * Controlador encargado de gestionar la inserción de nuevos socios.
 * <p>Proporciona la vista de formulario para añadir un socio y procesa la creación
 * en el repositorio, validando los datos y evitando duplicados por DNI.</p>
 */
@Controller
public class InsertarSocioController {

    private ModelAndView modelAndView = new ModelAndView();
    SocioRepository socioRepository;

    public InsertarSocioController(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.socioRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @GetMapping("/addSocio")
    public ModelAndView getAddSocioView() {
        modelAndView.setViewName("socio/addSocioView"); // SIN .html
        this.modelAndView.addObject("newSocio", new Socio());
        return modelAndView;
    }

    @PostMapping("/addSocio")
    public ModelAndView addSocio(@ModelAttribute Socio newSocio) {
        // Comprobación de validación de datos
        if (newSocio == null || newSocio.getDni() == null || newSocio.getDni().isBlank()) {
            return buildResponse(false, newSocio); // Enviar respuesta con error
        }

        // Verificar si el DNI ya existe en la base de datos
        if (socioRepository.existsByDni(newSocio.getDni())) {
            return buildResponse(false, newSocio); // Si existe, devolver error
        }

        // Intentar insertar el nuevo socio
        boolean success = socioRepository.addSocio(newSocio);
        return buildResponse(success, newSocio); // Responder según si la inserción fue exitosa
    }

    private ModelAndView buildResponse(boolean success, Socio socio) {
        ModelAndView mv = new ModelAndView();
        String nextPage = success ? "socio/addSocioViewSuccess" : "socio/addSocioViewFail";
        mv.setViewName(nextPage);
        mv.addObject("socio", socio);
        return mv;
    }

}