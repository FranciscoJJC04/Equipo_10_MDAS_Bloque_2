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

    private static final String BASE_API_URL = "http://localhost:8080/api/flota-patron";

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
        mostrarEmbarcaciones(rest);
        mostrarEmbarcacionesPorTipo(rest, "VELERO");
        mostrarPatrones(rest);
    }

    private static <T> void printArray(T[] body) {
        if (body == null) {
            return;
        }
        for (T item : body) {
            System.out.println(item);
        }
    }

    private static void mostrarEmbarcaciones(RestTemplate rest) {
        ResponseEntity<Embarcacion[]> responseEmbarcaciones = rest.getForEntity(
                BASE_API_URL + "/embarcaciones",
                Embarcacion[].class);
        System.out.println("==== GET Embarcaciones ====");
        printArray(responseEmbarcaciones.getBody());
    }

    private static void mostrarEmbarcacionesPorTipo(RestTemplate rest, String tipo) {
        ResponseEntity<Embarcacion[]> responseEmbarcacionesTipo = rest.getForEntity(
                BASE_API_URL + "/embarcaciones/tipo/{tipo}",
                Embarcacion[].class,
                tipo);
        System.out.println("==== GET Embarcaciones por tipo ====");
        printArray(responseEmbarcacionesTipo.getBody());
    }

    private static void mostrarPatrones(RestTemplate rest) {
        ResponseEntity<Patron[]> responsePatrones = rest.getForEntity(BASE_API_URL + "/patrones", Patron[].class);
        System.out.println("==== GET Patrones ====");
        printArray(responsePatrones.getBody());
    }

    /**
     * Realiza solicitudes POST para crear una nueva embarcación o patrón.
     * <p>
     * El método crea una embarcación y un patrón utilizando solicitudes POST a la API.
     * </p>
     */
    private static void sendPostRequests() {
        RestTemplate rest = new RestTemplate();

        // POST para crear una nueva embarcación
        Embarcacion nuevaEmbarcacion = new Embarcacion("10000", TipoEmbarcacion.VELERO, "Caglos", 4);
        ResponseEntity<String> responseEmbarcacion = rest.postForEntity(BASE_API_URL + "/embarcaciones", nuevaEmbarcacion,
                String.class);
        System.out.println("==== POST Crear Embarcación ====");
        System.out.println("Respuesta: " + responseEmbarcacion.getBody());

        // POST para crear un nuevo patrón
        LocalDate fechaNacimiento = LocalDate.parse("1985-02-25");
        Patron nuevoPatron = new Patron("10000A", "Augsburguer", "Lopez", fechaNacimiento);
        ResponseEntity<String> responsePatron = rest.postForEntity(BASE_API_URL + "/patrones", nuevoPatron, String.class);
        System.out.println("==== POST Crear Patrón ====");
        System.out.println("Respuesta: " + responsePatron.getBody());
    }

    private static RestTemplate createPatchCompatibleRestTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    private static void actualizarEmbarcacion(RestTemplate rest, String baseURL) {
        String matriculaToUpdate = "10000";
        Embarcacion updatedEmbarcacion = new Embarcacion();
        updatedEmbarcacion.setTipo(TipoEmbarcacion.VELERO);
        updatedEmbarcacion.setNombre("Lolita");
        updatedEmbarcacion.setNumPlazas(4);

        try {
            rest.patchForObject(baseURL + "/api/flota-patron/embarcaciones/{matricula}", updatedEmbarcacion,
                    Embarcacion.class, matriculaToUpdate);
            System.out.println("==== REQUEST : PATCH embarcación ====");
            System.out.println("Embarcación con matrícula " + matriculaToUpdate + " actualizada correctamente.");

            Embarcacion embarcacionAfterUpdate = rest.getForObject(
                    baseURL + "/api/flota-patron/embarcaciones/{matricula}", Embarcacion.class, matriculaToUpdate);
            System.out.println("Embarcación actualizada: " + embarcacionAfterUpdate);
        } catch (HttpClientErrorException e) {
            System.out.println("Error actualizando embarcación con matrícula " + matriculaToUpdate + ": "
                    + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("Error general: " + e.getMessage());
        }
    }

    private static void actualizarPatron(RestTemplate rest, String baseURL) {
        String dniPatronToUpdate = "10000A";
        Patron updatedPatron = new Patron();
        updatedPatron.setNombre("Francisco");
        updatedPatron.setApellido("Franco");
        updatedPatron.setFechaNacimiento(LocalDate.parse("1980-01-15"));

        try {
            rest.patchForObject(baseURL + "/api/flota-patron/patrones/{dni}", updatedPatron, Patron.class,
                    dniPatronToUpdate);
            System.out.println("==== REQUEST : PATCH patrón ====");
            System.out.println("Patrón con DNI " + dniPatronToUpdate + " actualizado correctamente.");

            Patron patronAfterUpdate = rest.getForObject(baseURL + "/api/flota-patron/patrones/{dni}", Patron.class,
                    dniPatronToUpdate);
            System.out.println("Patrón actualizado: " + patronAfterUpdate);
        } catch (HttpClientErrorException e) {
            System.out.println(
                    "Error actualizando patrón con DNI " + dniPatronToUpdate + ": " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("Error general: " + e.getMessage());
        }
    }

    private static void vincularPatron(RestTemplate rest, String baseURL, String matricula, String dniPatron,
            String fechaInicio, String fechaFin) {
        try {
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
    }

    private static void desvincularPatron(RestTemplate rest, String baseURL, String matricula, String dniPatron) {
        try {
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
     * Realiza solicitudes PATCH para actualizar los datos de una embarcación o patrón.
     * <p>
     * El método actualiza los datos de una embarcación y un patrón mediante solicitudes PATCH.
     * También permite vincular o desvincular patrones de embarcaciones.
     * </p>
     */
    private static void sendPatchRequests() {
        RestTemplate rest = createPatchCompatibleRestTemplate();
        String baseURL = "http://localhost:8080";

        actualizarEmbarcacion(rest, baseURL);
        actualizarPatron(rest, baseURL);

        String matricula = "456";
        String dniPatron = "22799768G";
        vincularPatron(rest, baseURL, matricula, dniPatron, "2025-01-01", "2025-12-31");
        desvincularPatron(rest, baseURL, matricula, dniPatron);
    }

    /**
     * Elimina embarcación o patrón (DELETE).
     * <p>
     * Realiza solicitudes DELETE para eliminar embarcaciones y patrones mediante su matrícula o DNI.
     * </p>
     */
    private static void sendDeleteRequests() {
        RestTemplate rest = new RestTemplate();

        // Eliminar embarcación (DELETE)
        rest.delete(BASE_API_URL + "/embarcaciones/{matricula}", "10000");
        System.out.println("Embarcación eliminada");

        // Eliminar patrón (DELETE)
        rest.delete(BASE_API_URL + "/patrones/{dni}", "10000A");
        System.out.println("Patrón eliminado");
    }
}
