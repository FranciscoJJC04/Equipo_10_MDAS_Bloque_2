package es.uco.pw.pw2526.model.domain.socio;

import java.time.LocalDate;

/**
 * Modelo de dominio que representa a un socio del club.
 *
 * Contiene datos personales y la información de su inscripción.
 */
public class Socio {

    private String dni;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String direccion;
    private boolean tituloPatron;
    private double cuotaInscripcion;
    private LocalDate fechaInscripcion;
    private int idInscripcion;
    private TipoInscripcion tipo;

    public Socio() {
        this.dni = "";
        this.nombre = "";
        this.apellidos = "";
        this.fechaNacimiento = LocalDate.now();
        this.direccion = "";
        this.tituloPatron = false;
        this.cuotaInscripcion = 0.0;
        this.fechaInscripcion = LocalDate.now();
        this.idInscripcion = 0;
        this.tipo = TipoInscripcion.NONE;
    }

    public Socio(String dni, String nombre, String apellidos, LocalDate fechaNacimiento, String direccion,
            boolean tituloPatron, double cuotaInscripcion, LocalDate fechaInscripcion, int idInscripcion, 
            TipoInscripcion tipo) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.tituloPatron = tituloPatron;
        this.cuotaInscripcion = cuotaInscripcion;
        this.fechaInscripcion = fechaInscripcion;
        this.idInscripcion = idInscripcion;
        this.tipo = tipo;
        this.tipo=tipo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isTituloPatron() {
        return tituloPatron;
    }

    public void setTituloPatron(boolean tituloPatron) {
        this.tituloPatron = tituloPatron;
    }

    public double getCuotaInscripcion() {
        return cuotaInscripcion;
    }

    public void setCuotaInscripcion(double cuotaInscripcion) {
        this.cuotaInscripcion = cuotaInscripcion;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public int getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(int idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public String getTipo() {
        if (tipo == TipoInscripcion.INDIVIDUAL) {
            return "Individual";
        } else if (tipo == TipoInscripcion.FAMILIAR) {
            return "Familiar";
        } else {
            return "None";
        }
    }
    /**
     * Establece el tipo de inscripción a partir de una cadena.
     * Acepta valores como "Individual" o "Familiar" (case-insensitive).
     *
     * @param tipo cadena que representa el tipo de inscripción
     */
    public void setTipo(String tipo) {
        if (tipo.equalsIgnoreCase("Individual")) {
            this.tipo = TipoInscripcion.INDIVIDUAL;
        } else if (tipo.equalsIgnoreCase("Familiar")) {
            this.tipo = TipoInscripcion.FAMILIAR;
        } else {
            this.tipo = TipoInscripcion.NONE;
        }
    }

    @Override
    public String toString() 
    {
        return "Socio [dni=" + dni + ", nombre=" + nombre + ", apellidos=" + apellidos + ", fechaNacimiento="
                + fechaNacimiento + ", direccion=" + direccion + ", tituloPatron=" + tituloPatron + ", cuotaInscripcion="
                + cuotaInscripcion + ", fechaInscripcion=" + fechaInscripcion + ", idInscripcion=" + idInscripcion
                + "]";
    }
}   