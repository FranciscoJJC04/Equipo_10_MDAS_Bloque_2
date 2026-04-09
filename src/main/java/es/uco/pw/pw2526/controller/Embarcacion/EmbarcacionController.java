package es.uco.pw.pw2526.controller.Embarcacion;

import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.repository.EmbarcacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/flota-patron")
public class EmbarcacionController {

    @Autowired
    private EmbarcacionRepository embarcacionRepository;

    // Obtener embarcación por matrícula (GET)
    @GetMapping("/embarcaciones/{matricula}")
    public ResponseEntity<Embarcacion> getEmbarcacionPorMatricula(@PathVariable String matricula) {
        Embarcacion embarcacion = embarcacionRepository.findByMatricula(matricula);

        if (embarcacion != null) {
            return ResponseEntity.ok(embarcacion);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

   
}
