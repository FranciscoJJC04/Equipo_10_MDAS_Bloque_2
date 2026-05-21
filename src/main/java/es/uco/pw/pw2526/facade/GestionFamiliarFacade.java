package es.uco.pw.pw2526.facade;

import org.springframework.stereotype.Service;

import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.repository.SocioRepository;

/**
 * Fachada que concentra las operaciones de alta y consulta de la gestión familiar.
 * <p>
 * Oculta a los controladores la coordinación con {@link SocioRepository} y deja
 * expuesta una interfaz simple para registrar familiares o recuperar el estado
 * completo de una inscripción familiar.
 * </p>
 */
@Service
public class GestionFamiliarFacade {

    private final SocioRepository socioRepository;

    public GestionFamiliarFacade(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
    }

    public boolean registrarInscripcionFamiliar(Socio socio) {
        return socioRepository.nuevaInscripcionFamiliar(socio);
    }

    public boolean registrarConyuge(String dniTitular, Socio conyuge) {
        return socioRepository.addConyuge(dniTitular, conyuge);
    }

    public boolean registrarHijo(String dniTitular, Socio hijo) {
        return socioRepository.addHijo(dniTitular, hijo);
    }

    public GestionFamiliarResult obtenerGestionFamiliar(String dniTitular) {
        Socio socio = socioRepository.findByDni(dniTitular);
        if (socio == null) {
            return GestionFamiliarResult.noEncontrado(dniTitular);
        }

        Socio conyuge = socioRepository.obtenerSocioConyuge(socio.getIdInscripcion());
        Socio hijo = socioRepository.obtenerSocioHijo(socio.getIdInscripcion());
        return GestionFamiliarResult.encontrado(socio, conyuge, hijo);
    }
}