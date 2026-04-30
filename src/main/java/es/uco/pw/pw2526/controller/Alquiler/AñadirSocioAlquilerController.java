package es.uco.pw.pw2526.controller.Alquiler;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.stereotype.Controller;
import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;
import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.repository.AlquilerRepository;
import es.uco.pw.pw2526.model.repository.SocioRepository;

@Controller
/**
 * Controlador para añadir socios a un alquiler existente.
 * <p>
 * Proporciona la vista para seleccionar un socio y asociarlo a un alquiler
 * (GET) y procesa el formulario de asociación (POST). Realiza las
 * comprobaciones de capacidad antes de delegar la inserción al repositorio.
 * </p>
 */
public class AñadirSocioAlquilerController {
    /** Repositorio para operaciones sobre alquileres. */
    private final AlquilerRepository alquilerRepository;
    /** Repositorio para operaciones sobre socios. */
    private final SocioRepository socioRepository;
    /** Instancia reutilizable de ModelAndView para respuestas simples. */
    private final ModelAndView modelAndView;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param alquilerRepository repositorio de alquileres
     * @param socioRepository    repositorio de socios
     */
    public AñadirSocioAlquilerController(AlquilerRepository alquilerRepository, SocioRepository socioRepository) {
        this.alquilerRepository = alquilerRepository;
        this.socioRepository = socioRepository;
        this.modelAndView = new ModelAndView();
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.alquilerRepository.setSQLQueriesFileName(sqlQueriesFileName);
        try {
            this.socioRepository.setSQLQueriesFileName(sqlQueriesFileName);
        } catch (Exception ignored) {
        }
    }

    @GetMapping("/addSociosAlquiler")
    /**
     * Muestra la vista para añadir un socio a un alquiler.
     *
     * @return ModelAndView con la plantilla de selección de socio
     */
    public ModelAndView getAddSocioView() {
        this.modelAndView.setViewName("alquiler/addSociosAlquilerView.html");
        this.modelAndView.addObject("newSocio", new Socio());
        // Añadir la lista de socios para poblar el desplegable
        try {
            java.util.List<?> socios = socioRepository.obtenerSocios();
            this.modelAndView.addObject("socios", socios);
        } catch (Exception e) {
            System.err.println("Aviso: no se pudieron cargar los socios en GET: " + e.getMessage());
            this.modelAndView.addObject("socios", new java.util.ArrayList<>());
        }

        return modelAndView;
    }

    @PostMapping("/addSociosAlquiler")
    /**
     * Procesa el formulario que añade un socio a un alquiler.
     * <p>
     * Comprueba la capacidad disponible y delega en el repositorio para
     * asociar el socio al alquiler.
     * </p>
     *
     * @param idAlquiler identificador del alquiler
     * @param dniSocio   DNI del socio a añadir
     * @return ModelAndView con éxito o fallo de la operación
     */
    public ModelAndView procesarFormulario(@RequestParam("idAlquiler") int idAlquiler,
        @RequestParam("dniSocio") String dniSocio) {
        // comprobar plazas y socios actuales para aplicar la restricción: solo se
        // pueden añadir hasta (plazas - 1)
        Integer plazas = alquilerRepository.getPlazasByAlquiler(idAlquiler);
        Integer actuales = alquilerRepository.countSociosEnAlquiler(idAlquiler);

        if (plazas == null) {
            ModelAndView mv = new ModelAndView("alquiler/addSociosAlquilerViewFail");
            mv.addObject("error", "No se pudo determinar la capacidad del alquiler.");
            return mv;
        }

        int maxSocios = Math.max(0, plazas - 1);
        int actualesInt = (actuales == null) ? 0 : actuales.intValue();

        if (actualesInt >= maxSocios) {
            ModelAndView mv = new ModelAndView("alquiler/addSociosAlquilerViewFail");
            mv.addObject("error",
                    "No se puede añadir más socios: capacidad alcanzada (" + actualesInt + "/" + maxSocios + ").");
            return mv;
        }

        // Refactor de nombrado: boolean descriptivo de resultado.
        boolean socioAnadidoAlAlquiler = alquilerRepository.addSocioAlquiler(dniSocio, idAlquiler);

        if (socioAnadidoAlAlquiler) {
            ModelAndView mv = new ModelAndView("alquiler/addSociosAlquilerViewSuccess");
            mv.addObject("success", "Socio añadido correctamente al alquiler.");
            return mv;
        } else {
            ModelAndView mv = new ModelAndView("alquiler/addSociosAlquilerViewFail");
            mv.addObject("error", "No se pudo añadir el socio al alquiler (error al insertar).");
            return mv;
        }
    }

}
