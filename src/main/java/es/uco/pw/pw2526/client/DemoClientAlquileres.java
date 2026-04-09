package es.uco.pw.pw2526.client;

import java.time.LocalDate;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import es.uco.pw.pw2526.model.domain.alquiler.Alquiler;

/**
 * Clase cliente para interactuar con la API de alquileres.
 * 
 * Esta clase utiliza {@link RestTemplate} para realizar peticiones HTTP a la API REST de alquileres.
 * Los métodos permiten realizar operaciones de obtener, crear, actualizar y eliminar alquileres.
 */
public class DemoClientAlquileres {

    /**
     * Método principal que ejecuta todas las operaciones de la API.
     * Realiza peticiones GET, POST, PATCH y DELETE a la API de alquileres.
     * 
     * @param args los argumentos de línea de comandos (no utilizados en este caso)
     */
    public static void main(String[] args) {
        sendGetRequests();
        sendPostRequests();
        sendPatchRequests();
        sendDeleteRequests();
    }

    /**
     * Envía peticiones GET a la API para obtener diferentes tipos de información sobre alquileres.
     * Incluye la lista completa de alquileres, alquileres futuros, alquiler por ID y embarcaciones disponibles.
     */
    private static void sendGetRequests() {
        RestTemplate rest = new RestTemplate();
        String baseURL = "http://localhost:8080/api/alquileres";

        // 1. GET lista completa de alquileres
        ResponseEntity<Alquiler[]> responseAll = rest.getForEntity(baseURL, Alquiler[].class);
        System.out.println("==== GET Alquileres ====");
        if (responseAll.getBody() != null)
            for (Alquiler a : responseAll.getBody()) System.out.println(a);

        // 2. GET alquileres futuros
        String fecha = "2025-01-01";
        ResponseEntity<Alquiler[]> responseFuture = rest.getForEntity(baseURL + "/futuros/{fecha}", Alquiler[].class, fecha);
        System.out.println("==== GET Alquileres futuros ====");
        if (responseFuture.getBody() != null)
            for (Alquiler a : responseFuture.getBody()) System.out.println(a);

        // 3. GET alquiler por ID
        int id = 6;
        ResponseEntity<Alquiler> responseOne = rest.getForEntity(baseURL + "/{id}", Alquiler.class, id);
        System.out.println("==== GET Alquiler por ID ====");
        System.out.println(responseOne.getBody());

        // 4. GET embarcaciones disponibles
        ResponseEntity<String[]> responseDisp = rest.getForEntity(
                baseURL + "/embarcaciones-disponibles?inicio=2025-01-01&fin=2025-01-10",
                String[].class
        );
        System.out.println("==== GET Embarcaciones disponibles ====");
        if (responseDisp.getBody() != null)
            for (String m : responseDisp.getBody()) System.out.println(m);
    }

    /**
     * Envía peticiones POST a la API para crear nuevos alquileres.
     * 
     * @param alquiler el objeto {@link Alquiler} a crear.
     */
    private static void sendPostRequests() {
        RestTemplate rest = new RestTemplate();
        String baseURL = "http://localhost:8080/api/alquileres";

        // 5. POST para crear un nuevo alquiler
        Alquiler nuevo = new Alquiler();
        nuevo.setMatricula("123");
        nuevo.setDniSocio("11872274X");
        nuevo.setNumPasajeros(3);
        nuevo.setFechaInicio(LocalDate.parse("2025-12-31"));
        nuevo.setFechaFin(LocalDate.parse("2026-01-02"));

        System.out.println("==== POST Crear Alquiler ====");
        ResponseEntity<String> response = rest.postForEntity(baseURL, nuevo, String.class);
        System.out.println(response.getBody());
    }

    /**
     * Crea un {@link RestTemplate} compatible con el método PATCH.
     * 
     * @return un {@link RestTemplate} configurado con {@link HttpComponentsClientHttpRequestFactory}.
     */
    private static RestTemplate createPatchCompatibleRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        return new RestTemplate(requestFactory);
    }

    /**
     * Envía peticiones PATCH a la API para actualizar la vinculación o desvinculación de socios en alquileres.
     * 
     * @throws RestClientException en caso de error en la ejecución de las peticiones PATCH.
     */
    private static void sendPatchRequests() {
        RestTemplate rest = createPatchCompatibleRestTemplate();
        String baseURL = "http://localhost:8080/api/alquileres";

        // 6. PATCH para vincular socio no titular a un alquiler
        int idAlquiler = 15;
        String nuevoSocio = "10799764v";

        System.out.println("==== PATCH Vincular Socio No Titular ====");
        ResponseEntity<String> responseVincular = rest.exchange(
                baseURL + "/{id}/vincular?dni={dni}",
                HttpMethod.PATCH,
                null,
                String.class,
                idAlquiler,
                nuevoSocio
        );
        System.out.println(responseVincular.getBody());

        // 7. PATCH para desvincular socio de un alquiler
        System.out.println("==== PATCH Desvincular Socio No Titular ====");
        ResponseEntity<String> responseDesvincular = rest.exchange(
            baseURL + "/{id}/desvincular?dni={dni}",
            HttpMethod.PATCH,
            null,
            String.class,
            idAlquiler,
            nuevoSocio
        );

        System.out.println(responseDesvincular.getBody());
    }

    /**
     * Envía peticiones DELETE a la API para cancelar un alquiler futuro.
     * 
     * @param idCancelar el ID del alquiler a cancelar.
     */
    private static void sendDeleteRequests() {
        RestTemplate rest = new RestTemplate();
        String baseURL = "http://localhost:8080/api/alquileres";

        // 8. DELETE para cancelar un alquiler futuro
        int idCancelar = 67; // Cada vez que se pruebe, se debe incrementar el ID del alquiler

        System.out.println("==== DELETE Cancelar Alquiler Futuro ====");
        ResponseEntity<String> responseDelete = rest.exchange(
                baseURL + "/{id}",
                HttpMethod.DELETE,
                null,
                String.class,
                idCancelar
        );
        System.out.println(responseDelete.getBody());
    }
}