package microservices.core.hotel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.api.core.hotel.Hotel;
import com.example.microservices.core.hotel.persistence.HotelRepository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.sql.Date;
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class HotelServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private HotelRepository repository;

	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getHotelById() {
		int hotelId = 1;
		postAndVerifyHotel(hotelId, OK);
		assertTrue(repository.findByHotelId(hotelId).isPresent());
		getAndVerifyHotel(hotelId, OK)
        .jsonPath("$.hotelId").isEqualTo(hotelId);
	}
	
	@Test
	public void duplicateError() {
		int hotelId = 1;
		postAndVerifyHotel(hotelId, OK);
		assertTrue(repository.findByHotelId(hotelId).isPresent());
		postAndVerifyHotel(hotelId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/hotel")
			.jsonPath("$.message").isEqualTo("Duplicate key, Hotel Id: " + hotelId);
	}

	@Test
	public void deleteHotel() {
		int hotelId = 1;
		postAndVerifyHotel(hotelId, OK);
		assertTrue(repository.findByHotelId(hotelId).isPresent());
		deleteAndVerifyHotel(hotelId, OK);
		assertFalse(repository.findByHotelId(hotelId).isPresent());
		deleteAndVerifyHotel(hotelId, OK);
	}

	@Test
	public void getHotelInvalidParameterString() {
		getAndVerifyHotel("/no-integer", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/hotel/no-integer")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getHotelNotFound() {
		int hotelIdNotFound = 13;
		getAndVerifyHotel(hotelIdNotFound, NOT_FOUND)
        .jsonPath("$.path").isEqualTo("/hotel/" + hotelIdNotFound)
        .jsonPath("$.message").isEqualTo("No hotel found for hotelId: " + hotelIdNotFound);
	}

	@Test
	public void getHotelInvalidParameterNegativeValue() {
        int hotelIdInvalid = -1;
        getAndVerifyHotel(hotelIdInvalid, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/hotel/" + hotelIdInvalid)
        .jsonPath("$.message").isEqualTo("Invalid hotelId: " + hotelIdInvalid);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyHotel(int hotelId, HttpStatus expectedStatus) {
		return getAndVerifyHotel("/" + hotelId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyHotel(String hotelIdPath, HttpStatus expectedStatus) {
		return client.get()
			.uri("/hotel" + hotelIdPath)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyHotel(int hotelId, HttpStatus expectedStatus) {
		Hotel hotel = new Hotel(hotelId, "n","d","i", Date.valueOf("2021-08-12"), "SA");
		return client.post()
			.uri("/hotel")
			.body(just(hotel), Hotel.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyHotel(int hotelId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/hotel/" + hotelId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
