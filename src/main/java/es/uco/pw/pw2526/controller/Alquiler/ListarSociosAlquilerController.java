package es.uco.pw.pw2526.controller.Alquiler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.repository.AlquilerRepository;

/**
 * Controlador encargado de listar los socios asociados a un alquiler específico.
 * <p>Gestiona la petición GET a la ruta <code>/listarSociosAlquiler</code> y 
 * devuelve la vista correspondiente con los datos obtenidos desde el repositorio.</p>
 */
@Controller
public class ListarSociosAlquilerController {
    private final AlquilerRepository alquilerRepository;

    public ListarSociosAlquilerController(AlquilerRepository alquilerRepository) {
        this.alquilerRepository = alquilerRepository;
    }

     /**
     * Constructor del controlador.
     * <p>Inicializa el repositorio de alquileres y establece la ruta del fichero
     * de consultas SQL.</p>
     *
     * @param alquilerRepository instancia del repositorio de alquileres
     */
    @GetMapping("/listarSociosAlquiler")
    public ModelAndView listar(@RequestParam("idAlquiler") int idAlquiler) {
        ModelAndView mv = new ModelAndView("alquiler/listarSociosAlquilerView.html");
        try {
            java.util.List<?> socios = alquilerRepository.obtenerSociosPorAlquiler(idAlquiler);
            if (socios == null)
                socios = new java.util.ArrayList<>();
            mv.addObject("socios", socios);
            mv.addObject("idAlquiler", idAlquiler);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar los socios por alquiler: " + e.getMessage());
            mv.addObject("socios", new java.util.ArrayList<>());
            mv.addObject("idAlquiler", idAlquiler);
        }
        return mv;
    }
}
