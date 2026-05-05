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

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Alquiler() {
        this.idAlquiler = 0;
        this.matricula = "";
        this.numPasajeros = 0;
        this.importeTotal = 0.0;
        this.dniSocio = "";
        this.fechaInicio = LocalDate.now();
        this.fechaFin = LocalDate.now();
    }

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

    public int getIdAlquiler() {
        return idAlquiler;
    }

    public void setIdAlquiler(final int idAlquiler) {
        this.idAlquiler = idAlquiler;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(final String matricula) {
        this.matricula = matricula;
    }

    public int getNumPasajeros() {
        return numPasajeros;
    }

    public void setNumPasajeros(final int numPasajeros) {
        this.numPasajeros = numPasajeros;
    }

    public double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(final double importeTotal) {
        this.importeTotal = importeTotal;
    }

    public String getDniSocio() {
        return dniSocio;
    }

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
