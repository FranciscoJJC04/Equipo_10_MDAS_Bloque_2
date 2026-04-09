package es.uco.pw.pw2526.controller.Alquiler;

import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;
import es.uco.pw.pw2526.model.repository.AlquilerRepository;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controlador encargado de listar los alquileres registrados en el sistema.
 * <p>Gestiona la petición GET a la ruta <code>/ListarAlquileres</code> y 
 * devuelve la vista correspondiente con los datos obtenidos desde el repositorio.</p>
 */
@Controller
public class ListarAlquilerController {

    AlquilerRepository alquilerRepository;

    public ListarAlquilerController(AlquilerRepository alquilerRepository) {
        this.alquilerRepository = alquilerRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.alquilerRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    
    @GetMapping("/ListarAlquileres")
    public ModelAndView obtenerAlquileres() {
        List<Alquiler> alquileres = alquilerRepository.obtenerAlquileres();
        ModelAndView model = new ModelAndView("alquiler/ListarAlquilerView");
        model.addObject("alquiler", alquileres);
        return model;
    }

}