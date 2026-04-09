package es.uco.pw.pw2526.model.domain.alquiler;

import java.time.LocalDate;

/**
 * Entidad que representa un alquiler de embarcación.
 *
 * <p>Contiene información sobre identificador, matrícula, número de
 * pasajeros, importe, socio responsable y rango de fechas.</p>
 */
public class Alquiler {
    private int idAlquiler;
    private String matricula;
    private int numPasajeros;
    private double importeTotal;
    private String dniSocio;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    /** Devuelve la fecha de inicio asociada. */
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    /** Establece la fecha de inicio asociada. */
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /** Devuelve la fecha de fin asociada. */
    public LocalDate getFechaFin() {
        return fechaFin;
    }

    /** Establece la fecha de fin asociada. */
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    /** Constructor por defecto. */
    public Alquiler() {
        this.idAlquiler = 0;
        this.matricula = "";
        this.numPasajeros = 0;
        this.importeTotal = 0.0;
        this.dniSocio = "";
        this.fechaInicio = LocalDate.now();
        this.fechaFin = LocalDate.now();
    }

    /** Constructor con todos los campos inicializados. */
    public Alquiler(int idAlquiler, String matricula, int numPasajeros, double importeTotal, String dniSocio,
            LocalDate fechaInicio, LocalDate fechaFin) {
        this.idAlquiler = idAlquiler;
        this.matricula = matricula;
        this.numPasajeros = numPasajeros;
        this.importeTotal = importeTotal;
        this.dniSocio = dniSocio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    /** Devuelve el identificador. */
    public int getIdAlquiler() {
        return idAlquiler;
    }

    /** Establece el identificador. */
    public void setIdAlquiler(final int idAlquiler) {
        this.idAlquiler = idAlquiler;
    }

    /** Devuelve la matrícula. */
    public String getMatricula() {
        return matricula;
    }

    /** Establece la matrícula. */
    public void setMatricula(final String matricula) {
        this.matricula = matricula;
    }

    /** Devuelve el número de pasajeros previsto. */
    public int getNumPasajeros() {
        return numPasajeros;
    }

    /** Establece el número de pasajeros previsto. */
    public void setNumPasajeros(final int numPasajeros) {
        this.numPasajeros = numPasajeros;
    }

    /** Devuelve el importe total. */
    public double getImporteTotal() {
        return importeTotal;
    }

    /** Establece el importe total. */
    public void setImporteTotal(final double importeTotal) {
        this.importeTotal = importeTotal;
    }

    /** Devuelve el DNI del socio asociado. */
    public String getDniSocio() {
        return dniSocio;
    }

    /** Establece el DNI del socio asociado. */
    public void setDniSocio(final String dniSocio) {
        this.dniSocio = dniSocio;
    }

    @Override
    public String toString() {
        return "Alquiler [idAlquiler=" + idAlquiler + ", matricula=" + matricula + ", numPasajeros=" + numPasajeros
                + ", importeTotal=" + importeTotal + ", dniSocio=" + dniSocio + ", fechaInicio=" + fechaInicio
                + ", fechaFin=" + fechaFin + "]";
    }

}
