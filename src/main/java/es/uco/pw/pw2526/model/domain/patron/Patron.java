package es.uco.pw.pw2526.model.domain.patron;

import java.time.LocalDate;

/**
 * Entidad que representa a una persona con habilitación de patrón.
 *
 * <p>Incluye datos básicos de identificación y fecha de nacimiento.</p>
 */
public class Patron {
    private String dni_patron;
    private String nombre;
    private String apellido;
    private LocalDate fecha_nacimiento;

    /**
     * Constructor por defecto. Inicializa campos con valores por defecto.
     */
    public Patron() {
        this.dni_patron = "";
        this.nombre = "";
        this.apellido = "";
        this.fecha_nacimiento = LocalDate.now();
    }

    /**
     * Constructor completo.
     */
    public Patron(String dni_patron, String nombre, String apellido, LocalDate fecha_nacimiento) {
        this.dni_patron = dni_patron;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fecha_nacimiento = fecha_nacimiento;
    }

    /** Devuelve el identificador nacional del patrón. */
    public String getDni_patron() {
        return dni_patron;
    }

    /** Establece el identificador nacional del patrón. */
    public void setDni_patron(String dni_patron) {
        this.dni_patron = dni_patron;
    }

    /** Devuelve el nombre. */
    public String getNombre() {
        return nombre;
    }

    /** Establece el nombre. */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Devuelve el apellido. */
    public String getApellido() {
        return apellido;
    }

    /** Establece el apellido. */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /** Devuelve la fecha de nacimiento. */
    public LocalDate getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    /** Establece la fecha de nacimiento. */
    public void setFecha_nacimiento(LocalDate fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

    @Override
    public String toString() {
        return "Patron [dni_patron=" + dni_patron + ", nombre=" + nombre + ", apellido=" + apellido
                + ", fecha_nacimiento=" + fecha_nacimiento + "]";
    }

}
