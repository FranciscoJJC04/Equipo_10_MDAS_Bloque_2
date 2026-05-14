package es.uco.pw.pw2526.model.domain.inscripcion;

import es.uco.pw.pw2526.model.domain.socio.TipoInscripcion;

/**
 * Modelo que representa una inscripción (tipo e identificador).
 */
public class Inscripcion {
    private int idInscripcion;
    private TipoInscripcion tipo;

    public Inscripcion() {
        this.idInscripcion = 0;
        this.tipo = TipoInscripcion.NONE;
    }

    public Inscripcion(int idInscripcion, TipoInscripcion tipo) {
        this.idInscripcion = idInscripcion;
        this.tipo = tipo;
    }

    public int getId() {
        return idInscripcion;
    }

    public void setId(int idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public TipoInscripcion getTipo() {
        return tipo;
    }

    public void setTipo(TipoInscripcion tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Inscripcion [idInscripcion=" + idInscripcion + ", tipo=" + tipo + "]";
    }
}
