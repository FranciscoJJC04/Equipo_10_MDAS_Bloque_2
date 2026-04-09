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

    /**
     * Método principal para ejecutar las solicitudes HTTP de demostración.
     */
    public static void main(String[] args) {
        sendGetRequests();
        sendPostRequests();
        sendPatchRequestFecha(); // Renombrado para evitar duplicados
        sendPatchRequestDatos(); // Renombrado para evitar duplicados
        sendDeleteRequest();
    }

    /**
     * Realiza solicitudes GET para obtener reservas.
     * <p>Obtiene todas las reservas, las reservas futuras y una reserva específica por ID.</p>
     */
    private static void sendGetRequests() {
        RestTemplate rest = new RestTemplate();
        String baseURL = "http://localhost:8080/api/reserva";

        // Obtener todas las reservas
        System.out.println("==== REQUEST 1: GET all reservas ====");
        ResponseEntity<Reserva[]> responseAll = rest.getForEntity(baseURL, Reserva[].class);
        if (responseAll.getBody() != null)
            for (Reserva r : responseAll.getBody()) System.out.println(r);

        // Obtener reservas futuras
        System.out.println("==== REQUEST 2: GET futuras reservas ====");
        ResponseEntity<Reserva[]> responseFuture = rest.getForEntity(baseURL + "/futuros", Reserva[].class);
        if (responseFuture.getBody() != null) {
            for (Reserva r : responseFuture.getBody()) {
                System.out.println(r);
            }
        }

        // Obtener una reserva específica por ID
        System.out.println("==== REQUEST 3: GET reserva by ID ====");
        int id = 9;
        ResponseEntity<Reserva> responseOne = rest.getForEntity(baseURL + "/{id}", Reserva.class, id);
        System.out.println(responseOne.getBody());
    }

    /**
     * Realiza una solicitud POST para crear una nueva reserva.
     * <p>Se configura una nueva reserva y se envía a la API para su creación.</p>
     */
    private static void sendPostRequests() {
        RestTemplate rest = new RestTemplate();
        String baseURL = "http://localhost:8080/api/reserva";

        // Crear una nueva reserva
        Reserva nuevo = new Reserva();
        nuevo.setId(40);  // El id será asignado por la base de datos, si es autoincremental, ponlo a 0 o null si la base lo gestiona.
        nuevo.setMatricula("madfv");
        nuevo.setDniSocio("21872274A");
        nuevo.setNumPasajeros(3);
        nuevo.setFecha(LocalDate.parse("2025-02-01"));

        double precioPorPasajero = 40.0;
        double importeReserva = precioPorPasajero * nuevo.getNumPasajeros();
        nuevo.setImporteTotal(importeReserva);  // Establecer el importe de la reserva
        String descripcion = "Reserva para alquiler de embarcación";
        nuevo.setDescripcionReserva(descripcion);

        System.out.println("==== REQUEST 4: POST crear reserva ====");
        ResponseEntity<String> response = rest.postForEntity(baseURL, nuevo, String.class);
        System.out.println(response.getBody());

        
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
    private static void sendPatchRequestFecha() {
        RestTemplate rest = createPatchCompatibleRestTemplate();
        String baseURL = "http://localhost:8080/api/reserva";

        int idReserva = 75;
        String nuevaFecha = "2029-12-12"; // Nueva fecha solicitada

        System.out.println("==== 5: PATCH Modificar Fecha Reserva ====");
        ResponseEntity<String> response = rest.exchange(
                baseURL + "/{id}/modificarFecha?nuevaFecha={nuevaFecha}",
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
    private static void sendPatchRequestDatos() {
        RestTemplate rest = createPatchCompatibleRestTemplate();
        String baseURL = "http://localhost:8080/api/reserva";

        int idReserva = 9; // ID de la reserva que quieres modificar
        String nuevaDescripcion = "Nueva descripción para la reserva";
        int nuevoNumPlazas = 1; // Nuevo número de plazas

        System.out.println("==== 6: PATCH Modificar Datos de la Reserva ====");
        ResponseEntity<String> response = rest.exchange(
                baseURL + "/{id}/modificarDatos?descripcion={descripcion}&numPlazas={numPlazas}",
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
        RestTemplate rest = createPatchCompatibleRestTemplate();
        String baseURL = "http://localhost:8080/api/reserva";


        

        // 7. DELETE Cancelar reserva invalido
        int idReserva = 76; // ID de la reserva que quieres cancelar

        System.out.println("==== 7: DELETE Cancelar Reserva (INVALIDO) ====");
        ResponseEntity<String> response = rest.exchange(
                baseURL + "/{id}",
                HttpMethod.DELETE,
                null,
                String.class,
                idReserva
        );
        System.out.println(response.getBody());
    }

    
}
