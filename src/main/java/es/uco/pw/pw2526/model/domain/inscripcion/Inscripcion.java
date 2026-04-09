package es.uco.pw.pw2526.model.domain.inscripcion;

import es.uco.pw.pw2526.model.domain.socio.TipoInscripcion;

/**
 * Modelo que representa una inscripción (tipo y identificador).
 */
public class Inscripcion 
{
    private int id;
    private TipoInscripcion tipo;

    public Inscripcion()
    {
        this.id = 0;
        this.tipo = TipoInscripcion.NONE;
    }
    public Inscripcion(int id,TipoInscripcion tipo)
    {
        this.id = id;
        this.tipo = tipo;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public TipoInscripcion getTipo() {
        return tipo;
    }
    public void setTipo(TipoInscripcion tipo) {
        this.tipo = tipo;
    }
    @Override
    public String toString() {
        return "Inscripcion [id=" + id + ", tipo=" + tipo + "]";
    }
  
    
    
 

}
