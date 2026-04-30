package es.uco.pw.pw2526.model.domain.reserva;

import java.time.LocalDate;

/**
 * Representa una reserva de embarcación realizada por un socio.
 * <p>
 * Contiene los datos básicos de la reserva: identificador, fecha,
 * socio solicitante, matrícula de la embarcación, patrón (si procede),
 * número de pasajeros, importe calculado y una descripción opcional.
 * </p>
 */
public class Reserva 
{
  
    private int id;
    private LocalDate fecha;
    private String dniSocio;      
    private String matricula;      
    private String dniPatron;      
    private int numeroPasajeros;
    private double importeTotal;
    private String descripcionReserva;

    /**
     * Crea una instancia por defecto con valores iniciales neutros.
     * Los campos numéricos se inicializan a 0 y las cadenas a cadena vacía.
     */
    public Reserva() 
    {
        this.id = 0;
        this.fecha = null;
        this.dniSocio = "";
        this.matricula = "";
        this.dniPatron = "";
        this.numeroPasajeros = 0;
        this.importeTotal = 0.0;
        this.descripcionReserva = "";
    }

    /**
     * Crea una instancia con todos los campos principales.
     *
     * @param id identificador de la reserva
     * @param fecha fecha asociada a la reserva
     * @param dniSocio DNI del socio solicitante
     * @param matricula matrícula de la embarcación
     * @param numeroPasajeros número de pasajeros
     * @param importeTotal importe total de la reserva
     * @param descripcionReserva texto descriptivo opcional
     */
    public Reserva(int id, LocalDate fecha, String dniSocio, String matricula,int numeroPasajeros,double importeTotal,String descripcionReserva) 
    {
        this.id = id;
        this.fecha = fecha;
        this.dniSocio = dniSocio;
        this.matricula = matricula;
        this.numeroPasajeros = numeroPasajeros;
        this.importeTotal = importeTotal;
        this.descripcionReserva = descripcionReserva;
    }

    /**
     * Obtiene el identificador de la reserva.
     *
     * @return identificador numérico
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador de la reserva.
     *
     * @param id identificador numérico
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene la fecha de la reserva.
     *
     * @return fecha asociada a la reserva
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha de la reserva.
     *
     * @param fecha fecha a asignar
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el DNI del socio que realizó la reserva.
     *
     * @return DNI del socio
     */
    public String getDniSocio() {
        return dniSocio;
    }

    /**
     * Establece el DNI del socio solicitante.
     *
     * @param dniSocio cadena con el DNI
     */
    public void setDniSocio(String dniSocio) {
        this.dniSocio = dniSocio;
    }

    /**
     * Obtiene la matrícula de la embarcación reservada.
     *
     * @return matrícula como cadena
     */
    public String getMatricula() {
        return matricula;
    }

    /**
     * Establece la matrícula de la embarcación.
     *
     * @param matricula cadena con la matrícula
     */
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    /**
     * Obtiene el número de pasajeros asociados a la reserva.
     *
     * @return número de pasajeros
     */
    public int getNumPasajeros() {
        return numeroPasajeros;
    }

    /**
     * Establece el número de pasajeros para la reserva.
     *
     * @param numeroPasajeros número de pasajeros
     */
    public void setNumPasajeros(final int numeroPasajeros) {
        this.numeroPasajeros = numeroPasajeros;
    }

    /**
     * Obtiene el importe total de la reserva.
     *
     * @return importe en unidades monetarias
     */
    public double getImporteTotal() {
        return importeTotal;
    }

    /**
     * Establece el importe total de la reserva.
     *
     * @param importeTotal importe a asignar
     */
    public void setImporteTotal(final double importeTotal) {
        this.importeTotal = importeTotal;
    }

    /**
     * Obtiene la descripción asociada a la reserva.
     *
     * @return texto descriptivo (puede ser vacío)
     */
    public String getDescripcionReserva() {
        return descripcionReserva;
    }

    /**
     * Establece la descripción de la reserva.
     *
     * @param descripcionReserva texto descriptivo
     */
    public void setDescripcionReserva(final String descripcionReserva) {
        this.descripcionReserva = descripcionReserva;
    }

    @Override
    public String toString() {
        return "Reserva [id=" + id + ", fecha=" + fecha + ", dniSocio=" + dniSocio + ", matricula=" + matricula
                + ", dniPatron=" + dniPatron + ", numeroPasajeros=" + numeroPasajeros + ", importeTotal="
                + importeTotal + ", descripcionReserva=" + descripcionReserva + "]";
    }
    
}
