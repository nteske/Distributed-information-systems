package microservices.core.location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.core.location.Location;
import com.example.microservices.core.location.persistence.LocationRepository;
import static org.junit.Assert.assertEquals;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.sql.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class LocationServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private LocationRepository repository;

	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getLocationByHotelId() {

		int hotelId = 1;

		postAndVerifyLocation(hotelId, 1, OK);
		postAndVerifyLocation(hotelId, 2, OK);
		postAndVerifyLocation(hotelId, 3, OK);

		assertEquals(3, repository.findByHotelId(hotelId).size());

		getAndVerifyLocationByHotelId(hotelId, OK)
		.jsonPath("$.length()").isEqualTo(3)
		.jsonPath("$[2].hotelId").isEqualTo(hotelId)
		.jsonPath("$[2].locationId").isEqualTo(3);
	}
	
	@Test
	public void duplicateError() {
		int hotelId = 1;
		int locationId = 1;

		postAndVerifyLocation(hotelId, locationId, OK)
			.jsonPath("$.hotelId").isEqualTo(hotelId)
			.jsonPath("$.locationId").isEqualTo(locationId);

		assertEquals(1, repository.count());

		postAndVerifyLocation(hotelId, locationId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/location")
			.jsonPath("$.message").isEqualTo("Duplicate key, Hotel Id: 1, Location Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteLocation() {
		int hotelId = 1;
		int locationId = 1;

		postAndVerifyLocation(hotelId, locationId, OK);
		assertEquals(1, repository.findByHotelId(hotelId).size());

		deleteAndVerifyLocationByHotelId(hotelId, OK);
		assertEquals(0, repository.findByHotelId(hotelId).size());

		deleteAndVerifyLocationByHotelId(hotelId, OK);
	}

	@Test
	public void getLocationMissingParameter() {
		getAndVerifyLocationByHotelId("", BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/location")
		.jsonPath("$.message").isEqualTo("Required int parameter 'hotelId' is not present");
	}

	@Test
	public void getLocationInvalidParameter() {
		getAndVerifyLocationByHotelId("?hotelId=no-integer", BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/location")
		.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getLocationNotFound() {
		getAndVerifyLocationByHotelId("?hotelId=113", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getLocationInvalidParameterNegativeValue() {
		int hotelIdInvalid = -1;
		getAndVerifyLocationByHotelId("?hotelId=" + hotelIdInvalid, UNPROCESSABLE_ENTITY)
		.jsonPath("$.path").isEqualTo("/location")
		.jsonPath("$.message").isEqualTo("Invalid hotelId: " + hotelIdInvalid);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyLocationByHotelId(int hotelId, HttpStatus expectedStatus) {
		return getAndVerifyLocationByHotelId("?hotelId=" + hotelId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyLocationByHotelId(String hotelIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/location" + hotelIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyLocation(int hotelId, int locationId, HttpStatus expectedStatus) {
		Location location = new Location(hotelId, locationId, "Country", "Town " + locationId, "Address", "SA");
		return client.post()
			.uri("/location")
			.body(just(location), Location.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyLocationByHotelId(int hotelId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/location?hotelId=" + hotelId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}

}