package es.uco.pw.pw2526.controller.socio;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import es.uco.pw.pw2526.model.domain.inscripcion.Inscripcion;
import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.domain.socio.TipoInscripcion;
import es.uco.pw.pw2526.model.repository.SocioRepository;


@Controller
public class ListarInscripcionesPorTipoController 
{
    SocioRepository socioRepository;

    public ListarInscripcionesPorTipoController(SocioRepository socioRepository) 
    {
        this.socioRepository = socioRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.socioRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @GetMapping("/listarInscripcionesPorTipo")
    public ModelAndView listarPorTipo(@RequestParam("tipo") TipoInscripcion tipo) {
    // Llamamos al repositorio para obtener las inscripciones filtradas
    List<Inscripcion> inscripciones = socioRepository.obtenerInscripcionesPorTipo(tipo);

    // Creamos el modelo y le pasamos la vista correspondiente
    ModelAndView model = new ModelAndView("socio/ObtenerIncripcionesPorTipoView.html");
    model.addObject("inscripciones", inscripciones);
    model.addObject("tipo", tipo);
    return model;
}



}
