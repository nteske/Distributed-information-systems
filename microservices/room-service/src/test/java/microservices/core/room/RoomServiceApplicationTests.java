package microservices.core.room;

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
class RoomServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void getRoomsByHotelId() {

		int hotelId = 1;

		client.get()
			.uri("/room?hotelId=" + hotelId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].hotelId").isEqualTo(hotelId);
	}

	@Test
	public void getRoomsMissingParameter() {

		client.get()
			.uri("/room")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/room")
			.jsonPath("$.message").isEqualTo("Required int parameter 'hotelId' is not present");
	}

	@Test
	public void getRoomsInvalidParameter() {

		client.get()
			.uri("/room?hotelId=no-integer")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/room")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getRoomsNotFound() {

		int hotelIdNotFound = 213;

		client.get()
			.uri("/room?hotelId=" + hotelIdNotFound)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getRoomsInvalidParameterNegativeValue() {

		int hotelIdInvalid = -1;

		client.get()
			.uri("/room?hotelId=" + hotelIdInvalid)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/room")
			.jsonPath("$.message").isEqualTo("Invalid hotelId: " + hotelIdInvalid);
	}

}
