package es.uco.pw.pw2526.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import es.uco.pw.pw2526.model.domain.reserva.Reserva;

import java.time.LocalDate;

/**
 * Cliente de demostración para interactuar con la API de reservas.
 * <p>Este cliente realiza solicitudes HTTP a la API de reservas para obtener, crear, modificar y cancelar reservas.</p>
 */
public class DemoClientReserva {

    private static final String BASE_API_URL = "http://localhost:8080/api/reserva";

    /**
     * Método principal para ejecutar las solicitudes HTTP de demostración.
     */
    public static void main(String[] args) {
        sendGetRequests();
        sendPostRequests();
        sendPatchRequestForDate();
        sendPatchRequestForDetails();
        sendDeleteRequest();
    }

    /**
     * Realiza solicitudes GET para obtener reservas.
     * <p>Obtiene todas las reservas, las reservas futuras y una reserva específica por ID.</p>
     */
    private static void sendGetRequests() {
        RestTemplate restTemplate = new RestTemplate();
        mostrarTodasLasReservas(restTemplate);
        mostrarReservasFuturas(restTemplate);
        mostrarReservaPorId(restTemplate, 9);
    }

    private static void printReservas(Reserva[] reservas) {
        if (reservas == null) {
            return;
        }
        for (Reserva reserva : reservas) {
            System.out.println(reserva);
        }
    }

    private static void mostrarTodasLasReservas(RestTemplate restTemplate) {
        System.out.println("==== REQUEST 1: GET all reservas ====");
        ResponseEntity<Reserva[]> responseAll = restTemplate.getForEntity(BASE_API_URL, Reserva[].class);
        printReservas(responseAll.getBody());
    }

    private static void mostrarReservasFuturas(RestTemplate restTemplate) {
        System.out.println("==== REQUEST 2: GET futuras reservas ====");
        ResponseEntity<Reserva[]> responseFuture = restTemplate.getForEntity(BASE_API_URL + "/futuros", Reserva[].class);
        printReservas(responseFuture.getBody());
    }

    private static void mostrarReservaPorId(RestTemplate restTemplate, int idReservaConsultada) {
        System.out.println("==== REQUEST 3: GET reserva by ID ====");
        ResponseEntity<Reserva> responseOne = restTemplate.getForEntity(
                BASE_API_URL + "/{id}",
                Reserva.class,
                idReservaConsultada);
        System.out.println(responseOne.getBody());
    }

    /**
     * Realiza una solicitud POST para crear una nueva reserva.
     * <p>Se configura una nueva reserva y se envía a la API para su creación.</p>
     */
    private static void sendPostRequests() {
        RestTemplate restTemplate = new RestTemplate();

        // Crear una nueva reserva
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setId(40);  // El id será asignado por la base de datos, si es autoincremental, ponlo a 0 o null si la base lo gestiona.
        nuevaReserva.setMatricula("madfv");
        nuevaReserva.setDniSocio("21872274A");
        nuevaReserva.setNumPasajeros(3);
        nuevaReserva.setFecha(LocalDate.parse("2025-02-01"));

        double precioPorPasajero = 40.0;
        double importeReserva = precioPorPasajero * nuevaReserva.getNumPasajeros();
        nuevaReserva.setImporteTotal(importeReserva);  // Establecer el importe de la reserva
        String descripcionReserva = "Reserva para alquiler de embarcación";
        nuevaReserva.setDescripcionReserva(descripcionReserva);

        System.out.println("==== REQUEST 4: POST crear reserva ====");
        ResponseEntity<String> createResponse = restTemplate.postForEntity(BASE_API_URL, nuevaReserva, String.class);
        System.out.println(createResponse.getBody());

        
    }

    /**
     * Crea una plantilla RestTemplate compatible con solicitudes PATCH.
     *
     * @return una instancia de RestTemplate compatible con PATCH
     */
    private static RestTemplate createPatchCompatibleRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        return new RestTemplate(requestFactory);
    }

    /**
     * Realiza una solicitud PATCH para modificar la fecha de una reserva existente.
     * <p>Envía una nueva fecha para una reserva especificada por ID.</p>
     */
    private static void sendPatchRequestForDate() {
        RestTemplate restTemplate = createPatchCompatibleRestTemplate();

        int idReserva = 75;
        String nuevaFecha = "2029-12-12"; // Nueva fecha solicitada

        System.out.println("==== 5: PATCH Modificar Fecha Reserva ====");
        ResponseEntity<String> response = restTemplate.exchange(
            BASE_API_URL + "/{id}/modificarFecha?nuevaFecha={nuevaFecha}",
                HttpMethod.PATCH,
                null,
                String.class,
                idReserva,
                nuevaFecha
        );
        System.out.println(response.getBody());
    }

    /**
     * Realiza una solicitud PATCH para modificar los datos de una reserva existente.
     * <p>Envía nuevos datos, como la descripción y el número de plazas, para una reserva especificada por ID.</p>
     */
    private static void sendPatchRequestForDetails() {
        RestTemplate restTemplate = createPatchCompatibleRestTemplate();

        int idReserva = 9; // ID de la reserva que quieres modificar
        String nuevaDescripcion = "Nueva descripción para la reserva";
        int nuevoNumPlazas = 1; // Nuevo número de plazas

        System.out.println("==== 6: PATCH Modificar Datos de la Reserva ====");
        ResponseEntity<String> response = restTemplate.exchange(
            BASE_API_URL + "/{id}/modificarDatos?descripcion={descripcion}&numPlazas={numPlazas}",
                HttpMethod.PATCH,
                null,
                String.class,
                idReserva,
                nuevaDescripcion,
                nuevoNumPlazas
        );
        System.out.println(response.getBody());
    }


    
    /**
     * Realiza una solicitud DELETE para cancelar una reserva.
     * <p>Envía una solicitud para cancelar la reserva especificada por ID.</p>
     */
    private static void sendDeleteRequest() {
        RestTemplate restTemplate = createPatchCompatibleRestTemplate();


        

        // 7. DELETE Cancelar reserva invalido
        int idReserva = 76; // ID de la reserva que quieres cancelar

        System.out.println("==== 7: DELETE Cancelar Reserva (INVALIDO) ====");
        ResponseEntity<String> response = restTemplate.exchange(
            BASE_API_URL + "/{id}",
                HttpMethod.DELETE,
                null,
                String.class,
                idReserva
        );
        System.out.println(response.getBody());
    }

    
}
