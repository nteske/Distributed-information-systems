package microservices.core.hotel;

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
class HotelServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void getHotelById() {

		int hotelId = 1;

        client.get()
            .uri("/hotel/" + hotelId)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.hotelId").isEqualTo(hotelId);
	}

	@Test
	public void getHotelInvalidParameterString() {

        client.get()
            .uri("/hotel/no-integer")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(BAD_REQUEST)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/hotel/no-integer")
            .jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getHotelNotFound() {

		int hotelIdNotFound = 13;

        client.get()
            .uri("/hotel/" + hotelIdNotFound)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/hotel/" + hotelIdNotFound)
            .jsonPath("$.message").isEqualTo("No hotel found for hotelId: " + hotelIdNotFound);
	}

	@Test
	public void getHotelInvalidParameterNegativeValue() {

        int hotelIdInvalid = -1;

        client.get()
            .uri("/hotel/" + hotelIdInvalid)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/hotel/" + hotelIdInvalid)
            .jsonPath("$.message").isEqualTo("Invalid hotelId: " + hotelIdInvalid);
	}

}
