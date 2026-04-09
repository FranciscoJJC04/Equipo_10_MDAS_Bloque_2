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

    /** GET: Obtener todos los socios */
    @GetMapping
    public ResponseEntity<List<Socio>>ObtenerSociosApi()
    {
        List<Socio> socios = socioRepository.obtenerSocios();
        ResponseEntity<List<Socio>> response = new ResponseEntity<>(socios, HttpStatus.OK);
        return response;
    }

    /** GET: Obtener socio por dni */
    @GetMapping("/{dni}")
    public ResponseEntity<Socio> getStudentById(@PathVariable String dni){
        Socio student = socioRepository.findByDni(dni);            
        ResponseEntity<Socio> response;
        if(student != null){
            response = new ResponseEntity<>(student, HttpStatus.OK);
        }
        else{
            response = new ResponseEntity<>(student, HttpStatus.NOT_FOUND);
        }
        return response;
    }

    /** GET: Listado de inscripciones individuales */
    @GetMapping("/inscripciones/individuales")
    public ResponseEntity<List<Inscripcion>> obtenerInscripcionesIndividuales() 
    {
        List<Inscripcion> inscripciones = socioRepository.obtenerInscripcionesPorTipo(TipoInscripcion.INDIVIDUAL);
        return new ResponseEntity<>(inscripciones, HttpStatus.OK);
    }

    /** GET: Listado de inscripciones familiares */
    @GetMapping("/inscripciones/familiares")
    public ResponseEntity<List<Inscripcion>> obtenerInscripcionesFamiliares() 
    {
        List<Inscripcion> inscripciones = socioRepository.obtenerInscripcionesPorTipo(TipoInscripcion.FAMILIAR);
        return new ResponseEntity<>(inscripciones, HttpStatus.OK);
    }

    /** GET:Obtener datos de una inscripcion pasando el dni de un socio*/
    @GetMapping("/inscripciones/{dni}")
    public ResponseEntity<Inscripcion> obtenerInscripcionPorDni(@PathVariable String dni) 
{
    Inscripcion inscripcion = socioRepository.obtenerInscripcionPorDni(dni);
    if (inscripcion != null) {
        return ResponseEntity.ok(inscripcion);
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

    /** POST: Crear un nuevo socio */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Socio> crearSocio(@RequestBody Socio socio) 
    {
    ResponseEntity<Socio> response;

    /** Verificar si ya existe un socio con el mismo DNI */
    boolean existe = socioRepository.existsByDni(socio.getDni());
    if (existe) {
        // Si ya existe, devolver error 422 (Unprocessable Entity)
        response = new ResponseEntity<>(socio, HttpStatus.UNPROCESSABLE_ENTITY);
    } else {
        // Intentar insertar el nuevo socio
        boolean resultOk = socioRepository.addSocio(socio);
        if (resultOk) {
            // Si se inserta correctamente, devolver 201 (Created)
            response = new ResponseEntity<>(socio, HttpStatus.CREATED);
        } else {
            // Si hay error interno al insertar, devolver 500
            response = new ResponseEntity<>(socio, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    return response;
    }

    /** POST: Crear un nuevo socio asociándolo a una inscripción familiar ya existente */
    @PostMapping("/familiar/{idInscripcion}")
    public ResponseEntity<Socio> crearSocioFamiliar(
            @PathVariable int idInscripcion,
            @RequestBody Socio conyuge) {

        // Validar si ya existe un socio con ese DNI
        if (socioRepository.existsByDni(conyuge.getDni())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(conyuge);
        }

        // Intentar insertar el nuevo socio asociado al idInscripcion
        boolean resultOk = socioRepository.addConyuge(idInscripcion, conyuge);

        if (resultOk) {
            return ResponseEntity.status(HttpStatus.CREATED).body(conyuge);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(conyuge);
        }
    }

    /** PATCH: Actualizar datos de un socio existente */
@PatchMapping(path="/{dni}", consumes="application/json")
public ResponseEntity<Socio> patchSocio(@PathVariable String dni, @RequestBody Socio requestSocio) {
    Socio response = requestSocio;
    try {
        // Buscar socio actual por DNI
        Socio currentSocio = this.socioRepository.findByDni(dni);
        if (currentSocio != null) {

            // Aseguramos que el DNI no se cambia
            requestSocio.setDni(currentSocio.getDni());

            // Actualizar solo los campos no nulos
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
            // if (requestSocio.getTipo() != null) {
            //     currentSocio.setTipo(requestSocio.getTipo());
            // }
            currentSocio.setTituloPatron(requestSocio.isTituloPatron());

            // Guardar socio actualizado
            boolean resultOk = socioRepository.updateSocio(currentSocio);
            if (resultOk) {
                response = currentSocio;
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(requestSocio);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
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

        boolean resultOk = socioRepository.updateTipoInscripcion(current.getId(), current.getTipo());
        if (resultOk) {
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