package es.uco.pw.pw2526.client;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import es.uco.pw.pw2526.model.domain.inscripcion.Inscripcion;
import es.uco.pw.pw2526.model.domain.socio.Socio;
import es.uco.pw.pw2526.model.domain.socio.TipoInscripcion;

public class DemoClientSocios {

	/**
     * Método principal que ejecuta todas las pruebas REST en orden.
     * @param args argumentos de línea de comandos (no usados).
     */
	public static void main(String[] args) {
		sendGetRequests();
		sendPostRequests();
		sendPatchRequests();
		sendPutRequest();
		sendDeleteRequest();
	}

	 /**
     * Envía varias peticiones GET para:
     * - Listar todos los socios.
     * - Obtener un socio por DNI.
     * - Obtener inscripción por DNI de socio.
     * - Listar inscripciones familiares.
     * - Listar inscripciones individuales.
     */
	private static void sendGetRequests()
	{
		// Refactor Semana 1 (nombrado): nombres de variables orientados al dominio y su contenido.
		RestTemplate restTemplate = new RestTemplate();
		String baseApiUrl = "http://localhost:8080";

		// Listado de todos los socios
		ResponseEntity<Socio[]> responseSocios = restTemplate.getForEntity(baseApiUrl + "/api/socio", Socio[].class);
		List<Socio> socios = Arrays.asList(responseSocios.getBody());
		System.out.println("==== REQUEST 1: GET all socios ====");
		Date responseDate = new Date(responseSocios.getHeaders().getDate());
		System.out.println("Response date: " + responseDate);
		for(Socio socioItem: socios){
			System.out.println(socioItem);
			System.out.println("--------------");
		}

		// Request to retrive one student
        String dniSocioConsulta = "21872274A";
		Socio socio = restTemplate.getForObject(baseApiUrl + "/api/socio/{dni}", Socio.class, dniSocioConsulta);
		System.out.println("==== REQUEST 2: GET socio with dni ====");
		System.out.println(socio.toString());

		//Obtener la información de una inscripción a partir del dni de un socio
		try {
			Inscripcion inscripcion = restTemplate.getForObject(
			baseApiUrl + "/api/socio/inscripciones/{dni}", 
			Inscripcion.class, 
			dniSocioConsulta
		);
		System.out.println("==== REQUEST 3: GET inscripción por DNI ====");
		System.out.println(inscripcion);
		} catch (HttpClientErrorException e) 
		{
			System.out.println("No se encontró inscripción para el DNI: " + dniSocioConsulta);
			System.out.println("Error: " + e.getResponseBodyAsString());
		}

	// Listado de inscripciones familiares
    try {
		ResponseEntity<Inscripcion[]> responseInscripcionesFamiliares =
			restTemplate.getForEntity(baseApiUrl + "/api/socio/inscripciones/familiares", Inscripcion[].class);
		List<Inscripcion> inscripcionesFamiliares = Arrays.asList(responseInscripcionesFamiliares.getBody());

        System.out.println("==== REQUEST 4: GET inscripciones familiares ====");
		for (Inscripcion inscripcionFamiliar : inscripcionesFamiliares) {
		    System.out.println(inscripcionFamiliar);
            System.out.println("--------------");
        }
    } catch (HttpClientErrorException e) {
        System.out.println("Error al obtener inscripciones familiares: " + e.getResponseBodyAsString());
    }

	// Listado de inscripciones individuales
	try {
		ResponseEntity<Inscripcion[]> responseInscripcionesIndividuales =
				restTemplate.getForEntity(baseApiUrl + "/api/socio/inscripciones/individuales", Inscripcion[].class);
		List<Inscripcion> inscripcionesIndividuales = Arrays.asList(responseInscripcionesIndividuales.getBody());

		System.out.println("==== REQUEST 6: GET inscripciones individuales ====");
		for (Inscripcion inscripcionIndividual : inscripcionesIndividuales) {
			System.out.println(inscripcionIndividual);
			System.out.println("--------------");
		}
	} catch (HttpClientErrorException e) {
		System.out.println("Error al obtener inscripciones individuales: " + e.getResponseBodyAsString());
	}

}

	 /**
	 * Envía varias peticiones POST para:
	 * - Crear un nuevo socio (válido).
	 * - Crear un nuevo socio (inválido).
	 * - Crear un nuevo socio asociándolo a una inscripción familiar ya existente.
	 */
	private static void sendPostRequests()
	{
		// Refactor Semana 1 (nombrado): nombres pronunciables y sin abreviaturas ambiguas.
		RestTemplate restTemplate = new RestTemplate();
		String baseApiUrl = "http://localhost:8080";

		// // POST a new socio (valid)
		LocalDate fechaNacimientoNuevoSocio = LocalDate.of(1997, 07, 22);
		Socio socioNuevo = new Socio("24556677K", "Ignacio", "Torres Marquez",fechaNacimientoNuevoSocio,
				"Av. del Mar 30, Córdoba", true, 300, LocalDate.now(), 32381, es.uco.pw.pw2526.model.domain.socio.TipoInscripcion.INDIVIDUAL);
		ResponseEntity<Socio> apiResponse;
		
		try{
			apiResponse = restTemplate.postForEntity(baseApiUrl + "/api/socio", socioNuevo, Socio.class);	
			System.out.println("==== REQUEST 7: POST socio (valid) ====");
			System.out.println("Status code: " + apiResponse.getStatusCode());
			System.err.println("Response body:\n" + apiResponse.getBody());
		}catch(HttpClientErrorException exception){
			System.out.println(exception);
		}

		// POST a student (invalid)
		socioNuevo.setDni("44556677D");
		socioNuevo.setNombre("Carlos");
		socioNuevo.setApellidos("Raigon Serrano");
		socioNuevo.setFechaNacimiento(fechaNacimientoNuevoSocio);
		socioNuevo.setDireccion("Av. del Mar 20, Córdoba");
		socioNuevo.setTituloPatron(false);
		socioNuevo.setFechaInscripcion(LocalDate.now());
		socioNuevo.setCuotaInscripcion(150);
		

		System.out.println("==== REQUEST 8: POST socio (invalid) ====");
		try{
			apiResponse = restTemplate.postForEntity(baseApiUrl + "/api/socio", socioNuevo, Socio.class);
		}catch(HttpClientErrorException exception){
			System.out.println(exception);
		}

// 		//Crear un nuevo socio asociándolo a una inscripción familiar ya existente
			LocalDate fechaNacimientoSocioFamiliar = LocalDate.of(1999, 11, 29);
			Socio socioConInscripcionFamiliar = new Socio("61729564Ñ", "Almudena", "Sanchez", fechaNacimientoSocioFamiliar,
					"Camino los Naranjeros, 13", false, 250, LocalDate.now(), 32382,
					es.uco.pw.pw2526.model.domain.socio.TipoInscripcion.FAMILIAR);
			System.out.println("==== REQUEST 9: POST socio (usando una inscripcion familiar existente) ====");
			try {
					apiResponse = restTemplate.postForEntity(
					baseApiUrl + "/api/socio/familiar/{idInscripcion}",
					socioConInscripcionFamiliar,
					Socio.class,
					32382 // id de inscripción familiar existente
				);
				System.out.println("Status code: " + apiResponse.getStatusCode());
				System.out.println("Response body: " + apiResponse.getBody());
			} catch (HttpClientErrorException exception) {
				System.out.println(exception);
			}

 }
// 	 /**
// 	 * Envía varias peticiones PATCH para:
// 	 * - Actualizar los datos de un socio existente (excepto el DNI).
// 	 * - Vincular un socio existente a una inscripción familiar ya creada.
// 	 * - Desvincular un socio de su inscripción familiar.
// 	 */
	private static void sendPatchRequests()
	{

		RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
		String baseURL = "http://localhost:8080";

		//Actualizar los datos de un socio existente menos el DNI
		//El nombre original era Luis Angel, con el cambio será Luis Javier
		//Se pueden cambiar culaquiera de los datos excepto el DNI
		String dniToUpdate = "21872463L";
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
				baseURL + "/api/socio/{dni}",
				HttpMethod.PATCH,
				new HttpEntity<>(updatedSocio),
				Socio.class,
				dniToUpdate
			);

			if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT) {
				System.out.println("Socio with DNI " + dniToUpdate + " updated successfully.");
				
				// Verify the update
				Socio socioAfterUpdate = rest.getForObject(baseURL + "/api/socio/{dni}", Socio.class, dniToUpdate);
				System.out.println("Updated Socio: " + socioAfterUpdate);
			} else if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
				System.out.println("No changes were made. Socio with DNI " + dniToUpdate + " already had the same data.");
			} else {
				System.out.println("Unexpected response. HTTP: " + response.getStatusCode());
			}
		} catch (HttpClientErrorException e) {
			System.out.println("Error updating socio with DNI " + dniToUpdate + ": " + e.getResponseBodyAsString());
		} catch (RestClientException e) {
			System.out.println("General error: " + e.getMessage());
		}

	// // Vincular un socio existente a una inscripción familiar ya creada
	// // ==== PATCH 2: Vincular un socio existente a una inscripción familiar ====
    String dniTitular = "24801274Q";
    String dniNuevo = "12872571B";

		try {
			System.out.println("==== REQUEST PATCH 2: VINCULAR SOCIO A INSCRIPCION FAMILIAR ====");
			ResponseEntity<Void> response = rest.exchange(
				baseURL + "/api/socio/inscripciones/familiar/{dniTitular}/{dniNuevo}",
				HttpMethod.PATCH,
				HttpEntity.EMPTY,
				Void.class,
				dniTitular,
				dniNuevo
			);

			if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
				System.out.println("Socio " + dniNuevo + " vinculado correctamente a la inscripción de " + dniTitular);
			} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
				System.out.println("No se pudo vincular: inscripción o socio no encontrado.");
			} else {
				System.out.println("Error al vincular socio. HTTP: " + response.getStatusCode());
			}
		} catch (RestClientException e) {
			System.out.println("Error al vincular socio: " + e.getMessage());
		}
		
		//==== PATCH 3: Desvincular socio de inscripción familiar ====
		String dniDesvincular = "32672571Z";

    try {
        System.out.println("==== REQUEST PATCH 3: DESVINCULAR SOCIO DE INSCRIPCION FAMILIAR ====");
        ResponseEntity<Void> response = rest.exchange(
            baseURL + "/api/socio/inscripciones/familiar/desvincular/{dni}",
            HttpMethod.PATCH,
            HttpEntity.EMPTY,
            Void.class,
            dniDesvincular
        );

        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            System.out.println("Socio " + dniDesvincular + " desvinculado correctamente de su inscripción.");
        } else {
            System.out.println("Error al desvincular socio. HTTP: " + response.getStatusCode());
        }
    } catch (RestClientException e) {
        System.out.println("Error al desvincular socio: " + e.getMessage());
    }


	}

// /**
//  * PUT: Actualiza el tipo de una inscripción existente (por ID).
//  */

private static void sendPutRequest() {
		RestTemplate rest = new RestTemplate();
		String baseURL = "http://localhost:8080/api/socio";

		int idToUpdate = 32376; // inscripción individual existente

		Inscripcion updatedInscripcion = new Inscripcion();
		updatedInscripcion.setId(idToUpdate);
		updatedInscripcion.setTipo(TipoInscripcion.FAMILIAR); // convertir a familiar

		try {
			// PUT request
			rest.put(baseURL + "/inscripciones/{id}", updatedInscripcion, idToUpdate);
			System.out.println("==== REQUEST PUT: UPDATE inscripcion ====");
			System.out.println("Inscripción con ID " + idToUpdate + " actualizada a FAMILIAR.");

		} catch (RestClientException e) {
			System.out.println("Error al actualizar inscripción: " + e.getMessage());
		}
}

/**
 * Realiza una solicitud DELETE para cancelar una inscripción de un socio.
 * <p>Envía una solicitud para cancelar la inscripción asociada al DNI del socio titular.</p>
 */
static void sendDeleteRequest() {
    RestTemplate rest = new RestTemplate();
    String baseURL = "http://localhost:8080/api/socio";
     String dniTitular = "12345678W";

	// Cancelar inscripción por DNI del socio titular,hacer su inscripcion Null y su cuota a 0
	try {
		System.out.println("==== REQUEST DELETE: CANCELAR INSCRIPCION POR DNI ====");
		ResponseEntity<Void> delResp = rest.exchange(
			baseURL + "/inscripciones/{dni}",
			HttpMethod.DELETE,
			HttpEntity.EMPTY,
			Void.class,
			dniTitular
		);

		if (delResp.getStatusCode() == HttpStatus.NO_CONTENT) {
			System.out.println("Inscripción del socio con DNI " + dniTitular + " cancelada correctamente.");
		} else if (delResp.getStatusCode() == HttpStatus.NOT_FOUND) {
			System.out.println("No existe inscripción para el socio con DNI " + dniTitular);
		} else {
			System.out.println("Error al cancelar inscripción. HTTP: " + delResp.getStatusCode());
		}
	} catch (RestClientException exception) {
		System.out.println("Error al cancelar inscripción: " + exception.getMessage());
	}


	// ==== DELETE: Eliminar socio sin inscripción ====
	//En este delete se elimina el socio que en el delete anterior se le ha cancelado la inscripción
	String dniEliminar = "12345678W";

	try {
		System.out.println("==== REQUEST DELETE 2: ELIMINAR SOCIO SIN INSCRIPCION ====");
		ResponseEntity<Void> response = rest.exchange(
			baseURL + "/sin-inscripcion/{dni}",
			HttpMethod.DELETE,
			HttpEntity.EMPTY,
			Void.class,
			dniEliminar
		);

		if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
			System.out.println("Socio " + dniEliminar + " eliminado correctamente.");
		} else {
			System.out.println("No se pudo eliminar socio. HTTP: " + response.getStatusCode());
		}
	} catch (RestClientException e) {
		System.out.println("Error al eliminar socio: " + e.getMessage());
	}


}


}