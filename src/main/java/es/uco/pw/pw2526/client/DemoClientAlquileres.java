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
        // Refactor Semana 1 (nombrado): nombres más descriptivos para evitar ambigüedad.
        RestTemplate restTemplate = new RestTemplate();
        String baseApiUrl = "http://localhost:8080/api/alquileres";

        // 1. GET lista completa de alquileres
        ResponseEntity<Alquiler[]> responseAll = restTemplate.getForEntity(baseApiUrl, Alquiler[].class);
        System.out.println("==== GET Alquileres ====");
        if (responseAll.getBody() != null)
            for (Alquiler alquiler : responseAll.getBody()) System.out.println(alquiler);

        // 2. GET alquileres futuros
        String fechaReferencia = "2025-01-01";
        ResponseEntity<Alquiler[]> responseFuture = restTemplate.getForEntity(baseApiUrl + "/futuros/{fecha}", Alquiler[].class, fechaReferencia);
        System.out.println("==== GET Alquileres futuros ====");
        if (responseFuture.getBody() != null)
            for (Alquiler alquiler : responseFuture.getBody()) System.out.println(alquiler);

        // 3. GET alquiler por ID
        int idAlquilerConsulta = 6;
        ResponseEntity<Alquiler> responseOne = restTemplate.getForEntity(baseApiUrl + "/{id}", Alquiler.class, idAlquilerConsulta);
        System.out.println("==== GET Alquiler por ID ====");
        System.out.println(responseOne.getBody());

        // 4. GET embarcaciones disponibles
        ResponseEntity<String[]> responseEmbarcacionesDisponibles = restTemplate.getForEntity(
            baseApiUrl + "/embarcaciones-disponibles?inicio=2025-01-01&fin=2025-01-10",
                String[].class
        );
        System.out.println("==== GET Embarcaciones disponibles ====");
        if (responseEmbarcacionesDisponibles.getBody() != null)
            for (String matriculaDisponible : responseEmbarcacionesDisponibles.getBody()) System.out.println(matriculaDisponible);
    }

    /**
     * Envía peticiones POST a la API para crear nuevos alquileres.
     * 
     * @param alquiler el objeto {@link Alquiler} a crear.
     */
    private static void sendPostRequests() {
        // Refactor Semana 1 (nombrado): evitamos nombres genéricos como nuevo/rest/baseURL.
        RestTemplate restTemplate = new RestTemplate();
        String baseApiUrl = "http://localhost:8080/api/alquileres";

        // 5. POST para crear un nuevo alquiler
        Alquiler nuevoAlquiler = new Alquiler();
        nuevoAlquiler.setMatricula("123");
        nuevoAlquiler.setDniSocio("11872274X");
        nuevoAlquiler.setNumPasajeros(3);
        nuevoAlquiler.setFechaInicio(LocalDate.parse("2025-12-31"));
        nuevoAlquiler.setFechaFin(LocalDate.parse("2026-01-02"));

        System.out.println("==== POST Crear Alquiler ====");
        ResponseEntity<String> createResponse = restTemplate.postForEntity(baseApiUrl, nuevoAlquiler, String.class);
        System.out.println(createResponse.getBody());
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
        // Refactor Semana 1 (nombrado): nombre de variable alineado al dominio (DNI socio no titular).
        RestTemplate restTemplate = createPatchCompatibleRestTemplate();
        String baseApiUrl = "http://localhost:8080/api/alquileres";

        // 6. PATCH para vincular socio no titular a un alquiler
        int idAlquiler = 15;
        String dniSocioNoTitular = "10799764v";

        System.out.println("==== PATCH Vincular Socio No Titular ====");
        ResponseEntity<String> responseVincular = restTemplate.exchange(
            baseApiUrl + "/{id}/vincular?dni={dni}",
                HttpMethod.PATCH,
                null,
                String.class,
                idAlquiler,
            dniSocioNoTitular
        );
        System.out.println(responseVincular.getBody());

        // 7. PATCH para desvincular socio de un alquiler
        System.out.println("==== PATCH Desvincular Socio No Titular ====");
        ResponseEntity<String> responseDesvincular = restTemplate.exchange(
            baseApiUrl + "/{id}/desvincular?dni={dni}",
            HttpMethod.PATCH,
            null,
            String.class,
            idAlquiler,
            dniSocioNoTitular
        );

        System.out.println(responseDesvincular.getBody());
    }

    /**
     * Envía peticiones DELETE a la API para cancelar un alquiler futuro.
     * 
     * @param idCancelar el ID del alquiler a cancelar.
     */
    private static void sendDeleteRequests() {
        // Refactor Semana 1 (nombrado): id más explícito sobre su propósito.
        RestTemplate restTemplate = new RestTemplate();
        String baseApiUrl = "http://localhost:8080/api/alquileres";

        // 8. DELETE para cancelar un alquiler futuro
        int idAlquilerCancelar = 67; // Cada vez que se pruebe, se debe incrementar el ID del alquiler

        System.out.println("==== DELETE Cancelar Alquiler Futuro ====");
        ResponseEntity<String> responseDelete = restTemplate.exchange(
            baseApiUrl + "/{id}",
                HttpMethod.DELETE,
                null,
                String.class,
            idAlquilerCancelar
        );
        System.out.println(responseDelete.getBody());
    }
}