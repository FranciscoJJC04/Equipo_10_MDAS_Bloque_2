package es.uco.pw.pw2526.controller.Patrones;
import org.springframework.http.HttpStatus;

import es.uco.pw.pw2526.model.domain.patron.Patron;
import es.uco.pw.pw2526.model.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/flota-patron")
public class PatronController {

    @Autowired
    private PatronRepository patronRepository;

    // Obtener patrón por DNI (GET)
    @GetMapping("/patrones/{dni}")
    public ResponseEntity<Patron> getPatronPorDni(@PathVariable String dni) {
        Patron patron = patronRepository.findByDni(dni);

        if (patron != null) {
            return ResponseEntity.ok(patron);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    
}
