package es.uco.pw.pw2526.controller.Reserva;

import es.uco.pw.pw2526.model.domain.reserva.Reserva;
import es.uco.pw.pw2526.model.repository.ReservaRepository;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controlador encargado de listar las reservas registradas en el sistema.
 * <p>Gestiona la petición GET a la ruta <code>/ListarReservas</code> y 
 * devuelve la vista correspondiente con los datos obtenidos desde el repositorio.</p>
 */
@Controller
public class ListarReservasController {

    ReservaRepository reservaRepository;

    public ListarReservasController(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.reservaRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @GetMapping("/ListarReservas")
    public ModelAndView obtenerReservas()
    {           
    List<Reserva> reservas = reservaRepository.obtenerReservas();
    ModelAndView model = new ModelAndView("reserva/ListarReservasView");
    model.addObject("reservas", reservas);
        return model;
    }
}
