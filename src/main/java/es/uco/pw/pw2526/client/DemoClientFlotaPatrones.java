package es.uco.pw.pw2526.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import es.uco.pw.pw2526.model.domain.embarcacion.Embarcacion;
import es.uco.pw.pw2526.model.domain.embarcacion.TipoEmbarcacion;
import es.uco.pw.pw2526.model.domain.patron.Patron;

/**
 * Cliente de demostración para interactuar con la API de flota y patrones.
 * <p>
 * Este cliente realiza solicitudes GET, POST, PATCH y DELETE a la API para gestionar embarcaciones y patrones.
 * </p>
 */
public class DemoClientFlotaPatrones {

    /**
     * Método principal que ejecuta las solicitudes GET, POST, PATCH y DELETE.
     *
     * @param args argumentos de línea de comandos (no utilizados en este caso)
     */
    public static void main(String[] args) {
        sendGetRequests();
        sendPostRequests();
        sendPatchRequests();
        sendDeleteRequests();
    }

    /**
     * Realiza solicitudes GET para obtener la lista de embarcaciones y patrones.
     * <p>
     * Las solicitudes incluyen obtener la lista completa de embarcaciones, embarcaciones por tipo,
     * y la lista de patrones registrados.
     * </p>
     */
    private static void sendGetRequests() {
        RestTemplate rest = new RestTemplate();
        String baseURL = "http://localhost:8080/api/flota-patron";

        // 1. Obtener la lista completa de embarcaciones (GET)
        ResponseEntity<Embarcacion[]> responseEmbarcaciones = rest.getForEntity(baseURL + "/embarcaciones",
                Embarcacion[].class);
        List<Embarcacion> listEmbarcaciones = List.of(responseEmbarcaciones.getBody());
        System.out.println("==== GET Embarcaciones ====");
        for (Embarcacion embarcacion : listEmbarcaciones) {
            System.out.println(embarcacion);
        }

        // 2. Obtener la lista de embarcaciones por tipo (GET)
        String tipo = "VELERO";
        ResponseEntity<Embarcacion[]> responseEmbarcacionesTipo = rest
                .getForEntity(baseURL + "/embarcaciones/tipo/{tipo}", Embarcacion[].class, tipo);
        List<Embarcacion> listEmbarcacionesTipo = List.of(responseEmbarcacionesTipo.getBody());
        System.out.println("==== GET Embarcaciones por tipo ====");
        for (Embarcacion embarcacion : listEmbarcacionesTipo) {
            System.out.println(embarcacion);
        }

        // 3. Obtener la lista de patrones (GET)
        ResponseEntity<Patron[]> responsePatrones = rest.getForEntity(baseURL + "/patrones", Patron[].class);
        List<Patron> listPatrones = List.of(responsePatrones.getBody());
        System.out.println("==== GET Patrones ====");
        for (Patron patron : listPatrones) {
            System.out.println(patron);
        }
    }

    /**
     * Realiza solicitudes POST para crear una nueva embarcación o patrón.
     * <p>
     * El método crea una embarcación y un patrón utilizando solicitudes POST a la API.
     * </p>
     */
    private static void sendPostRequests() {
        RestTemplate rest = new RestTemplate();
        String baseURL = "http://localhost:8080/api/flota-patron";

        // POST para crear una nueva embarcación
        Embarcacion nuevaEmbarcacion = new Embarcacion("10000", TipoEmbarcacion.VELERO, "Caglos", 4);
        ResponseEntity<String> responseEmbarcacion = rest.postForEntity(baseURL + "/embarcaciones", nuevaEmbarcacion,
                String.class);
        System.out.println("==== POST Crear Embarcación ====");
        System.out.println("Respuesta: " + responseEmbarcacion.getBody());

        // POST para crear un nuevo patrón
        LocalDate fechaNacimiento = LocalDate.parse("1985-02-25");
        Patron nuevoPatron = new Patron("10000A", "Augsburguer", "Lopez", fechaNacimiento);
        ResponseEntity<String> responsePatron = rest.postForEntity(baseURL + "/patrones", nuevoPatron, String.class);
        System.out.println("==== POST Crear Patrón ====");
        System.out.println("Respuesta: " + responsePatron.getBody());
    }

    /**
     * Realiza solicitudes PATCH para actualizar los datos de una embarcación o patrón.
     * <p>
     * El método actualiza los datos de una embarcación y un patrón mediante solicitudes PATCH.
     * También permite vincular o desvincular patrones de embarcaciones.
     * </p>
     */
    private static void sendPatchRequests() {
        RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        String baseURL = "http://localhost:8080";

        // Actualizar los datos de una embarcación (menos la matrícula)
        String matriculaToUpdate = "10000"; // Ejemplo de matrícula
        Embarcacion updatedEmbarcacion = new Embarcacion();
        updatedEmbarcacion.setTipo(TipoEmbarcacion.VELERO);
        updatedEmbarcacion.setNombre("Lolita");
        updatedEmbarcacion.setNumPlazas(4);

        try {
            // La URL y el método PATCH para embarcación
            rest.patchForObject(baseURL + "/api/flota-patron/embarcaciones/{matricula}", updatedEmbarcacion,
                    Embarcacion.class, matriculaToUpdate);
            System.out.println("==== REQUEST : PATCH embarcación ====");
            System.out.println("Embarcación con matrícula " + matriculaToUpdate + " actualizada correctamente.");

            // Verificar la actualización
            Embarcacion embarcacionAfterUpdate = rest.getForObject(
                    baseURL + "/api/flota-patron/embarcaciones/{matricula}", Embarcacion.class, matriculaToUpdate);
            System.out.println("Embarcación actualizada: " + embarcacionAfterUpdate);
        } catch (HttpClientErrorException e) {
            System.out.println("Error actualizando embarcación con matrícula " + matriculaToUpdate + ": "
                    + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("Error general: " + e.getMessage());
        }

        // Actualizar los datos de un patrón (menos el DNI)
        String dniPatronToUpdate = "10000A"; // Ejemplo de DNI del patrón
        Patron updatedPatron = new Patron();
        updatedPatron.setNombre("Francisco");
        updatedPatron.setApellido("Franco");
        updatedPatron.setFecha_nacimiento(LocalDate.parse("1980-01-15"));

        try {
            // La URL y el método PATCH para patrón
            rest.patchForObject(baseURL + "/api/flota-patron/patrones/{dni}", updatedPatron, Patron.class,
                    dniPatronToUpdate);
            System.out.println("==== REQUEST : PATCH patrón ====");
            System.out.println("Patrón con DNI " + dniPatronToUpdate + " actualizado correctamente.");

            // Verificar la actualización
            Patron patronAfterUpdate = rest.getForObject(baseURL + "/api/flota-patron/patrones/{dni}", Patron.class,
                    dniPatronToUpdate);
            System.out.println("Patrón actualizado: " + patronAfterUpdate);
        } catch (HttpClientErrorException e) {
            System.out.println(
                    "Error actualizando patrón con DNI " + dniPatronToUpdate + ": " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("Error general: " + e.getMessage());
        }
        // Vincular un patrón a una embarcación (PATCH)
        String matricula = "456"; // Ejemplo de matrícula
        String dniPatron = "22799768G"; // Ejemplo de DNI del patrón
        String fechaInicio = "2025-01-01"; // Fecha de inicio
        String fechaFin = "2025-12-31"; // Fecha de fin (opcional)

        try {
            // La URL y el método PATCH para vincular patrón a embarcación
            rest.patchForObject(baseURL
                    + "/api/flota-patron/embarcaciones/{matricula}/patron/{dniPatron}?fechaInicio={fechaInicio}&fechaFin={fechaFin}",
                    null, String.class, matricula, dniPatron, fechaInicio, fechaFin);
            System.out.println("==== REQUEST : PATCH vincular patrón ====");
            System.out.println("Patrón con DNI " + dniPatron + " vinculado a la embarcación con matrícula " + matricula
                    + " correctamente.");
        } catch (HttpClientErrorException e) {
            System.out.println("Error al vincular patrón con DNI " + dniPatron + " y matrícula " + matricula + ": "
                    + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("Error general: " + e.getMessage());
        }
        // Desvincular un patrón de una embarcación (PATCH)
        try {
            // La URL y el método PATCH para desvincular patrón de embarcación
            rest.patchForObject(baseURL + "/api/flota-patron/embarcacion/{matricula}/desvincularPatron/{dniPatron}",
                    null, String.class, matricula, dniPatron);
            System.out.println("==== REQUEST : PATCH desvincular patrón ====");
            System.out.println("Patrón con DNI " + dniPatron + " desvinculado de la embarcación con matrícula "
                    + matricula + " correctamente.");
        } catch (HttpClientErrorException e) {
            System.out.println("Error al desvincular patrón con DNI " + dniPatron + " y matrícula " + matricula + ": "
                    + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("Error general: " + e.getMessage());
        }
    }

    /**
     * Elimina embarcación o patrón (DELETE).
     * <p>
     * Realiza solicitudes DELETE para eliminar embarcaciones y patrones mediante su matrícula o DNI.
     * </p>
     */
    private static void sendDeleteRequests() {
        RestTemplate rest = new RestTemplate();
        String baseURL = "http://localhost:8080/api/flota-patron";

        // Eliminar embarcación (DELETE)
        rest.delete(baseURL + "/embarcaciones/{matricula}", "10000");
        System.out.println("Embarcación eliminada");

        // Eliminar patrón (DELETE)
        rest.delete(baseURL + "/patrones/{dni}", "10000A");
        System.out.println("Patrón eliminado");
    }
}
