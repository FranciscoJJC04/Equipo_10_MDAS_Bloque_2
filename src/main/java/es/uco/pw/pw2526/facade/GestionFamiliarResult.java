package es.uco.pw.pw2526.facade;

import es.uco.pw.pw2526.model.domain.socio.Socio;

/**
 * Resultado de la fachada de gestión familiar.
 * <p>
 * Agrupa el socio titular, su cónyuge y su hijo para que el controlador reciba
 * la información ya preparada sin repetir consultas ni lógica de ensamblado.
 * </p>
 */
public class GestionFamiliarResult {

    private final boolean encontrado;
    private final String dniBuscado;
    private final Socio socio;
    private final Socio conyuge;
    private final Socio hijo;

    private GestionFamiliarResult(boolean encontrado, String dniBuscado, Socio socio, Socio conyuge, Socio hijo) {
        this.encontrado = encontrado;
        this.dniBuscado = dniBuscado;
        this.socio = socio;
        this.conyuge = conyuge;
        this.hijo = hijo;
    }

    public static GestionFamiliarResult encontrado(Socio socio, Socio conyuge, Socio hijo) {
        String dni = socio != null ? socio.getDni() : null;
        return new GestionFamiliarResult(true, dni, socio, conyuge, hijo);
    }

    public static GestionFamiliarResult noEncontrado(String dniBuscado) {
        return new GestionFamiliarResult(false, dniBuscado, null, null, null);
    }

    public boolean isEncontrado() {
        return encontrado;
    }

    public String getDniBuscado() {
        return dniBuscado;
    }

    public Socio getSocio() {
        return socio;
    }

    public Socio getConyuge() {
        return conyuge;
    }

    public Socio getHijo() {
        return hijo;
    }
}