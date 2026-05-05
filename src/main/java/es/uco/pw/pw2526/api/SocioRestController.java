package es.uco.pw.pw2526.api;

import es.uco.pw.pw2526.model.domain.inscripcion.Inscripcion;
import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.domain.socio.TipoInscripcion;
import es.uco.pw.pw2526.model.repository.SocioRepository;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controlador REST para gestionar socios e inscripciones.
 * Expone endpoints CRUD y operaciones específicas sobre inscripciones familiares e individuales.
 */
@RestController()
@RequestMapping(path="/api/socio", produces="application/json")
public class SocioRestController 
{
    SocioRepository socioRepository;

     public SocioRestController(SocioRepository socioRepository){
        this.socioRepository = socioRepository;
        String sqlQueriesFileName = "./src/main/resources/db/sql.properties";
        this.socioRepository.setSQLQueriesFileName(sqlQueriesFileName);
    }

    @GetMapping
    public ResponseEntity<List<Socio>> obtenerSociosApi() {
        List<Socio> socios = socioRepository.obtenerSocios();
        ResponseEntity<List<Socio>> sociosResponse = new ResponseEntity<>(socios, HttpStatus.OK);
        return sociosResponse;
    }

    @GetMapping("/{dni}")
    public ResponseEntity<Socio> obtenerSocioPorDni(@PathVariable String dni) {
        Socio socioEncontrado = socioRepository.findByDni(dni);
        if (socioEncontrado != null) {
            return new ResponseEntity<>(socioEncontrado, HttpStatus.OK);
        }
        return new ResponseEntity<>(socioEncontrado, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/inscripciones/individuales")
    public ResponseEntity<List<Inscripcion>> obtenerInscripcionesIndividuales() {
        List<Inscripcion> inscripciones = socioRepository.obtenerInscripcionesPorTipo(TipoInscripcion.INDIVIDUAL);
        return new ResponseEntity<>(inscripciones, HttpStatus.OK);
    }

    @GetMapping("/inscripciones/familiares")
    public ResponseEntity<List<Inscripcion>> obtenerInscripcionesFamiliares() {
        List<Inscripcion> inscripciones = socioRepository.obtenerInscripcionesPorTipo(TipoInscripcion.FAMILIAR);
        return new ResponseEntity<>(inscripciones, HttpStatus.OK);
    }

    @GetMapping("/inscripciones/{dni}")
    public ResponseEntity<Inscripcion> obtenerInscripcionPorDni(@PathVariable String dni) {
        Inscripcion inscripcion = socioRepository.obtenerInscripcionPorDni(dni);
        if (inscripcion != null) {
            return ResponseEntity.ok(inscripcion);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Socio> crearSocio(@RequestBody Socio socio) {
        boolean existe = socioRepository.existsByDni(socio.getDni());
        if (existe) {
            return new ResponseEntity<>(socio, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        boolean socioCreado = socioRepository.addSocio(socio);
        if (socioCreado) {
            return new ResponseEntity<>(socio, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(socio, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/familiar/{idInscripcion}")
    public ResponseEntity<Socio> crearSocioFamiliar(
            @PathVariable int idInscripcion,
            @RequestBody Socio conyuge) {

        if (socioRepository.existsByDni(conyuge.getDni())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(conyuge);
        }

        boolean conyugeCreado = socioRepository.addConyuge(idInscripcion, conyuge);
        if (conyugeCreado) {
            return ResponseEntity.status(HttpStatus.CREATED).body(conyuge);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(conyuge);
    }

    @PatchMapping(path = "/{dni}", consumes = "application/json")
    public ResponseEntity<Socio> patchSocio(@PathVariable String dni, @RequestBody Socio requestSocio) {
        try {
            Socio currentSocio = this.socioRepository.findByDni(dni);
            if (currentSocio != null) {
                requestSocio.setDni(currentSocio.getDni());

                if (requestSocio.getNombre() != null) {
                    currentSocio.setNombre(requestSocio.getNombre());
                }
                if (requestSocio.getApellidos() != null) {
                    currentSocio.setApellidos(requestSocio.getApellidos());
                }
                if (requestSocio.getFechaNacimiento() != null) {
                    currentSocio.setFechaNacimiento(requestSocio.getFechaNacimiento());
                }
                if (requestSocio.getDireccion() != null) {
                    currentSocio.setDireccion(requestSocio.getDireccion());
                }
                if (requestSocio.getCuotaInscripcion() != 0.0) {
                    currentSocio.setCuotaInscripcion(requestSocio.getCuotaInscripcion());
                }
                if (requestSocio.getFechaInscripcion() != null) {
                    currentSocio.setFechaInscripcion(requestSocio.getFechaInscripcion());
                }
                if (requestSocio.getIdInscripcion() != 0) {
                    currentSocio.setIdInscripcion(requestSocio.getIdInscripcion());
                }
                currentSocio.setTituloPatron(requestSocio.isTituloPatron());

                boolean socioActualizado = socioRepository.updateSocio(currentSocio);
                if (socioActualizado) {
                    return ResponseEntity.ok(currentSocio);
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(requestSocio);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error en PATCH socio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(requestSocio);
        }
    }


/** PUT: Actualizar tipo de inscripción por ID */
@PutMapping("/inscripciones/{id}")
    public ResponseEntity<Inscripcion> updateInscripcion(
            @PathVariable Integer id,
            @RequestBody Inscripcion requestInscripcion) {

        Inscripcion current = socioRepository.findInscripcionById(id);
        if (current == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Cambiar tipo de inscripción
        current.setTipo(requestInscripcion.getTipo());

        boolean tipoActualizado = socioRepository.updateTipoInscripcion(current.getId(), current.getTipo());
        if (tipoActualizado) {
            return ResponseEntity.ok(current);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


/** PATCH: Vincular socio a inscripción familiar */
@PatchMapping("/inscripciones/familiar/{dniTitular}/{dniNuevo}")
public ResponseEntity<Void> vincularSocioAFamiliar(
        @PathVariable String dniTitular,
        @PathVariable String dniNuevo) {

    boolean result = socioRepository.vincularSocioAFamiliar(dniTitular, dniNuevo);

    return result ? ResponseEntity.noContent().build()
                  : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
}

/** PATCH: Desvincular socio de inscripción familiar */
@PatchMapping("/inscripciones/familiar/desvincular/{dni}")
public ResponseEntity<Void> desvincularSocioDeInscripcion(@PathVariable String dni) {
    boolean result = socioRepository.desvincularSocioDeInscripcion(dni);

    if (result) {
        return ResponseEntity.noContent().build();
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

/** DELETE para eliminar socio si no tiene inscripciones asociadas*/
@DeleteMapping("/sin-inscripcion/{dni}")
public ResponseEntity<Void> eliminarSocioSiNoTieneInscripcion(@PathVariable String dni) {
    boolean result = socioRepository.eliminarSocioSiNoTieneInscripcion(dni);

    if (result) {
        return ResponseEntity.noContent().build(); 
    } else {
        return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        
    }
}

/** DELETE: Cancelar inscripción por DNI del titular */
@DeleteMapping("inscripciones/{dni}")
public ResponseEntity<Void> cancelarInscripcionPorDni(@PathVariable String dni) {
    boolean result = socioRepository.cancelarInscripcionPorDni(dni);

    if (result) {
        return ResponseEntity.noContent().build();
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}



}