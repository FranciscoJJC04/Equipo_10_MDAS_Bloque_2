package es.uco.pw.pw2526.model.domain.patron;

import java.time.LocalDate;

/**
 * Entidad que representa a una persona con habilitación de patrón.
 *
 * <p>Incluye datos básicos de identificación y fecha de nacimiento.</p>
 */
public class Patron {
    private String dniPatron;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;

    /**
     * Constructor por defecto. Inicializa campos con valores por defecto.
     */
    public Patron() {
        this.dniPatron = "";
        this.nombre = "";
        this.apellido = "";
        this.fechaNacimiento = LocalDate.now();
    }

    /**
     * Constructor completo.
     */
    public Patron(String dniPatron, String nombre, String apellido, LocalDate fechaNacimiento) {
        this.dniPatron = dniPatron;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDniPatron() {
        return dniPatron;
    }

    public void setDniPatron(String dniPatron) {
        this.dniPatron = dniPatron;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public String toString() {
        return "Patron [dniPatron=" + dniPatron + ", nombre=" + nombre + ", apellido=" + apellido
                + ", fechaNacimiento=" + fechaNacimiento + "]";
    }

}
