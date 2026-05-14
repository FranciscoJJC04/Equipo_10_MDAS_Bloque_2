package es.uco.pw.pw2526.client;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import es.uco.pw.pw2526.model.domain.inscripcion.Inscripcion;
import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.domain.socio.TipoInscripcion;

public class DemoClientSocios {

    private static final String BASE_API_URL = "http://localhost:8080/api/socio";

    public static void main(String[] args) {
        sendGetRequests();
        sendPostRequests();
        sendPatchRequests();
        sendPutRequest();
        sendDeleteRequest();
    }

    private static void sendGetRequests() {
        RestTemplate restTemplate = new RestTemplate();
        String dniSocioConsulta = "21872274A";

        mostrarTodosLosSocios(restTemplate);
        mostrarSocioPorDni(restTemplate, dniSocioConsulta);
        mostrarInscripcionPorDni(restTemplate, dniSocioConsulta);
        mostrarInscripcionesFamiliares(restTemplate);
        mostrarInscripcionesIndividuales(restTemplate);
    }

    private static void mostrarTodosLosSocios(RestTemplate restTemplate) {
        ResponseEntity<Socio[]> responseSocios = restTemplate.getForEntity(BASE_API_URL, Socio[].class);
        System.out.println("==== REQUEST 1: GET all socios ====");
        if (responseSocios.getHeaders().getDate() > 0) {
            Date responseDate = new Date(responseSocios.getHeaders().getDate());
            System.out.println("Response date: " + responseDate);
        }
        Socio[] socios = responseSocios.getBody();
        if (socios == null) {
            return;
        }
        for (Socio socioItem : socios) {
            System.out.println(socioItem);
            System.out.println("--------------");
        }
    }

    private static void mostrarSocioPorDni(RestTemplate restTemplate, String dni) {
        Socio socio = restTemplate.getForObject(BASE_API_URL + "/{dni}", Socio.class, dni);
        System.out.println("==== REQUEST 2: GET socio with dni ====");
        System.out.println(socio);
    }

    private static void mostrarInscripcionPorDni(RestTemplate restTemplate, String dni) {
        try {
            Inscripcion inscripcion = restTemplate.getForObject(
                    BASE_API_URL + "/inscripciones/{dni}",
                    Inscripcion.class,
                    dni);
            System.out.println("==== REQUEST 3: GET inscripcion por DNI ====");
            System.out.println(inscripcion);
        } catch (HttpClientErrorException e) {
            System.out.println("No se encontro inscripcion para el DNI: " + dni);
            System.out.println("Error: " + e.getResponseBodyAsString());
        }
    }

    private static void mostrarInscripcionesFamiliares(RestTemplate restTemplate) {
        try {
            ResponseEntity<Inscripcion[]> response = restTemplate.getForEntity(
                    BASE_API_URL + "/inscripciones/familiares", Inscripcion[].class);
            System.out.println("==== REQUEST 4: GET inscripciones familiares ====");
            printInscripciones(response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println("Error al obtener inscripciones familiares: " + e.getResponseBodyAsString());
        }
    }

    private static void mostrarInscripcionesIndividuales(RestTemplate restTemplate) {
        try {
            ResponseEntity<Inscripcion[]> response = restTemplate.getForEntity(
                    BASE_API_URL + "/inscripciones/individuales", Inscripcion[].class);
            System.out.println("==== REQUEST 6: GET inscripciones individuales ====");
            printInscripciones(response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println("Error al obtener inscripciones individuales: " + e.getResponseBodyAsString());
        }
    }

    private static void printInscripciones(Inscripcion[] inscripciones) {
        if (inscripciones == null) {
            return;
        }
        for (Inscripcion inscripcion : inscripciones) {
            System.out.println(inscripcion);
            System.out.println("--------------");
        }
    }

    private static void sendPostRequests() {
        RestTemplate restTemplate = new RestTemplate();
        crearSocioValido(restTemplate);
        crearSocioInvalido(restTemplate);
        crearSocioFamiliar(restTemplate);
    }

    private static void crearSocioValido(RestTemplate restTemplate) {
        LocalDate fechaNacimientoNuevoSocio = LocalDate.of(1997, 7, 22);
        Socio socioNuevo = new Socio("24556677K", "Ignacio", "Torres Marquez", fechaNacimientoNuevoSocio,
                "Av. del Mar 30, Cordoba", true, 300, LocalDate.now(), 32381, TipoInscripcion.INDIVIDUAL);

        try {
            ResponseEntity<Socio> apiResponse = restTemplate.postForEntity(BASE_API_URL, socioNuevo, Socio.class);
            System.out.println("==== REQUEST 7: POST socio (valid) ====");
            System.out.println("Status code: " + apiResponse.getStatusCode());
            System.out.println("Response body:\n" + apiResponse.getBody());
        } catch (HttpClientErrorException exception) {
            System.out.println(exception);
        }
    }

    private static void crearSocioInvalido(RestTemplate restTemplate) {
        LocalDate fechaNacimiento = LocalDate.of(1997, 7, 22);
        Socio socioNuevo = new Socio("44556677D", "Carlos", "Raigon Serrano", fechaNacimiento,
                "Av. del Mar 20, Cordoba", false, 150, LocalDate.now(), 32381, TipoInscripcion.INDIVIDUAL);

        System.out.println("==== REQUEST 8: POST socio (invalid) ====");
        try {
            restTemplate.postForEntity(BASE_API_URL, socioNuevo, Socio.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception);
        }
    }

    private static void crearSocioFamiliar(RestTemplate restTemplate) {
        LocalDate fechaNacimiento = LocalDate.of(1999, 11, 29);
        Socio socioConInscripcionFamiliar = new Socio("61729564N", "Almudena", "Sanchez", fechaNacimiento,
                "Camino los Naranjeros, 13", false, 250, LocalDate.now(), 32382, TipoInscripcion.FAMILIAR);

        System.out.println("==== REQUEST 9: POST socio (usando una inscripcion familiar existente) ====");
        try {
            ResponseEntity<Socio> apiResponse = restTemplate.postForEntity(
                    BASE_API_URL + "/familiar/{idInscripcion}",
                    socioConInscripcionFamiliar,
                    Socio.class,
                    32382);
            System.out.println("Status code: " + apiResponse.getStatusCode());
            System.out.println("Response body: " + apiResponse.getBody());
        } catch (HttpClientErrorException exception) {
            System.out.println(exception);
        }
    }

    private static RestTemplate createPatchCompatibleRestTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

    private static void sendPatchRequests() {
        RestTemplate rest = createPatchCompatibleRestTemplate();
        actualizarDatosSocio(rest, "21872463L");
        vincularSocioAFamiliar(rest, "24801274Q", "12872571B");
        desvincularSocioFamiliar(rest, "32672571Z");
    }

    private static void actualizarDatosSocio(RestTemplate rest, String dniToUpdate) {
        Socio updatedSocio = new Socio();
        updatedSocio.setNombre("Luis Javier");
        updatedSocio.setApellidos("Gomez Luque");
        updatedSocio.setFechaNacimiento(LocalDate.of(1980, 5, 12));
        updatedSocio.setDireccion("Calle Nueva 123, Sevilla");
        updatedSocio.setTituloPatron(false);
        updatedSocio.setCuotaInscripcion(300.0);
        updatedSocio.setFechaInscripcion(LocalDate.now());

        try {
            System.out.println("==== REQUEST : PATCH socio ====");
            ResponseEntity<Socio> response = rest.exchange(
                    BASE_API_URL + "/{dni}",
                    HttpMethod.PATCH,
                    new HttpEntity<>(updatedSocio),
                    Socio.class,
                    dniToUpdate);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Socio with DNI " + dniToUpdate + " updated successfully.");
                Socio socioAfterUpdate = rest.getForObject(BASE_API_URL + "/{dni}", Socio.class, dniToUpdate);
                System.out.println("Updated Socio: " + socioAfterUpdate);
                return;
            }
            if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
                System.out.println("No changes were made. Socio with DNI " + dniToUpdate + " already had the same data.");
                return;
            }
            System.out.println("Unexpected response. HTTP: " + response.getStatusCode());
        } catch (HttpClientErrorException e) {
            System.out.println("Error updating socio with DNI " + dniToUpdate + ": " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("General error: " + e.getMessage());
        }
    }

    private static void vincularSocioAFamiliar(RestTemplate rest, String dniTitular, String dniNuevo) {
        try {
            System.out.println("==== REQUEST PATCH 2: VINCULAR SOCIO A INSCRIPCION FAMILIAR ====");
            ResponseEntity<Void> response = rest.exchange(
                    BASE_API_URL + "/inscripciones/familiar/{dniTitular}/{dniNuevo}",
                    HttpMethod.PATCH,
                    HttpEntity.EMPTY,
                    Void.class,
                    dniTitular,
                    dniNuevo);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Socio " + dniNuevo + " vinculado correctamente a la inscripcion de " + dniTitular);
                return;
            }
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                System.out.println("No se pudo vincular: inscripcion o socio no encontrado.");
                return;
            }
            System.out.println("Error al vincular socio. HTTP: " + response.getStatusCode());
        } catch (RestClientException e) {
            System.out.println("Error al vincular socio: " + e.getMessage());
        }
    }

    private static void desvincularSocioFamiliar(RestTemplate rest, String dniDesvincular) {
        try {
            System.out.println("==== REQUEST PATCH 3: DESVINCULAR SOCIO DE INSCRIPCION FAMILIAR ====");
            ResponseEntity<Void> response = rest.exchange(
                    BASE_API_URL + "/inscripciones/familiar/desvincular/{dni}",
                    HttpMethod.PATCH,
                    HttpEntity.EMPTY,
                    Void.class,
                    dniDesvincular);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Socio " + dniDesvincular + " desvinculado correctamente de su inscripcion.");
                return;
            }
            System.out.println("Error al desvincular socio. HTTP: " + response.getStatusCode());
        } catch (RestClientException e) {
            System.out.println("Error al desvincular socio: " + e.getMessage());
        }
    }

    private static void sendPutRequest() {
        RestTemplate rest = new RestTemplate();

        int idToUpdate = 32376;
        Inscripcion updatedInscripcion = new Inscripcion();
        updatedInscripcion.setId(idToUpdate);
        updatedInscripcion.setTipo(TipoInscripcion.FAMILIAR);

        try {
            rest.put(BASE_API_URL + "/inscripciones/{id}", updatedInscripcion, idToUpdate);
            System.out.println("==== REQUEST PUT: UPDATE inscripcion ====");
            System.out.println("Inscripcion con ID " + idToUpdate + " actualizada a FAMILIAR.");
        } catch (RestClientException e) {
            System.out.println("Error al actualizar inscripcion: " + e.getMessage());
        }
    }

    static void sendDeleteRequest() {
        RestTemplate rest = new RestTemplate();
        cancelarInscripcion(rest, "12345678W");
        eliminarSocioSinInscripcion(rest, "12345678W");
    }

    private static void cancelarInscripcion(RestTemplate rest, String dniTitular) {
        try {
            System.out.println("==== REQUEST DELETE: CANCELAR INSCRIPCION POR DNI ====");
            ResponseEntity<Void> delResp = rest.exchange(
                    BASE_API_URL + "/inscripciones/{dni}",
                    HttpMethod.DELETE,
                    HttpEntity.EMPTY,
                    Void.class,
                    dniTitular);

            if (delResp.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Inscripcion del socio con DNI " + dniTitular + " cancelada correctamente.");
                return;
            }
            if (delResp.getStatusCode() == HttpStatus.NOT_FOUND) {
                System.out.println("No existe inscripcion para el socio con DNI " + dniTitular);
                return;
            }
            System.out.println("Error al cancelar inscripcion. HTTP: " + delResp.getStatusCode());
        } catch (RestClientException exception) {
            System.out.println("Error al cancelar inscripcion: " + exception.getMessage());
        }
    }

    private static void eliminarSocioSinInscripcion(RestTemplate rest, String dniEliminar) {
        try {
            System.out.println("==== REQUEST DELETE 2: ELIMINAR SOCIO SIN INSCRIPCION ====");
            ResponseEntity<Void> response = rest.exchange(
                    BASE_API_URL + "/sin-inscripcion/{dni}",
                    HttpMethod.DELETE,
                    HttpEntity.EMPTY,
                    Void.class,
                    dniEliminar);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                System.out.println("Socio " + dniEliminar + " eliminado correctamente.");
                return;
            }
            System.out.println("No se pudo eliminar socio. HTTP: " + response.getStatusCode());
        } catch (RestClientException e) {
            System.out.println("Error al eliminar socio: " + e.getMessage());
        }
    }
}
