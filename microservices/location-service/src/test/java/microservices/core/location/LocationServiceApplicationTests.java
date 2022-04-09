package microservices.core.location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
class LocationServiceApplicationTests {
	@Autowired
	private WebTestClient client;

	@Test
	public void getLocationByHotelId() {

		int hotelId = 1;

		client.get()
			.uri("/location?hotelId=" + hotelId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].hotelId").isEqualTo(hotelId);
	}

	@Test
	public void getLocationMissingParameter() {

		client.get()
			.uri("/location")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/location")
			.jsonPath("$.message").isEqualTo("Required int parameter 'hotelId' is not present");
	}

	@Test
	public void getLocationInvalidParameter() {

		client.get()
			.uri("/location?hotelId=no-integer")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/location")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getLocationNotFound() {

		int hotelIdNotFound = 113;

		client.get()
			.uri("/location?hotelId=" + hotelIdNotFound)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getLocationInvalidParameterNegativeValue() {

		int hotelIdInvalid = -1;

		client.get()
			.uri("/location?hotelId=" + hotelIdInvalid)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/location")
			.jsonPath("$.message").isEqualTo("Invalid hotelId: " + hotelIdInvalid);
	}

}
