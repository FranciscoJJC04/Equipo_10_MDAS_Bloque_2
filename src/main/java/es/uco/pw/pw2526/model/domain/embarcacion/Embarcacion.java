package es.uco.pw.pw2526.model.domain.embarcacion;

import es.uco.pw.pw2526.model.domain.patron.Patron;

/**
 * Modelo de dominio que representa una embarcación del club.
 */
public class Embarcacion {
    private String matricula;
    private TipoEmbarcacion tipo;
    private String nombre;
    private int numPlazas;

    public Embarcacion() {
        this.matricula = "";
        this.tipo = TipoEmbarcacion.NONE;
        this.nombre = "";
        this.numPlazas = 0;
    }

    public Embarcacion(String matricula, TipoEmbarcacion tipo, String nombre, int numPlazas) {
        this.matricula = matricula;
        this.tipo = tipo;
        this.nombre = nombre;
        this.numPlazas = numPlazas;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public TipoEmbarcacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoEmbarcacion tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumPlazas() {
        return numPlazas;
    }

    public void setNumPlazas(int numPlazas) {
        this.numPlazas = numPlazas;
    }

    @Override
    public String toString() {
        return "Embarcacion [matricula=" + matricula + ", tipo=" + tipo + ", nombre=" + nombre + ", numPlazas="
                + numPlazas + "]";
    }

    public void setPatron(Patron patron) {
        // Asociar patrón a la embarcación (se guarda solo su DNI en repositorio)
        if (patron == null) {
            return;
        }
        // Este método no persiste información en este objeto de dominio en el diseño actual,
        // pero se deja disponible para asignaciones en memoria si otras capas lo requieren.
    }
    

    

}
