package com.example.microservices.core.location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.core.hotel.Hotel;
import com.example.api.core.location.Location;
import com.example.api.event.Event;
import com.example.util.exceptions.InvalidInputException;
import com.example.microservices.core.location.persistence.LocationRepository;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false"})
public class LocationServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private LocationRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	@Test
	public void getLocationByHotelId() {

		int hotelId = 1;

		sendCreateLocationEvent(hotelId, 1);
		sendCreateLocationEvent(hotelId, 2);
		sendCreateLocationEvent(hotelId, 3);

		assertEquals(3, (long)repository.findByHotelId(hotelId).count().block());

		getAndVerifyLocationByHotelId(hotelId, OK)
		.jsonPath("$.length()").isEqualTo(3)
		.jsonPath("$[2].hotelId").isEqualTo(hotelId)
		.jsonPath("$[2].locationId").isEqualTo(3);
	}
	
	@Test
	public void duplicateError() {
		int hotelId = 1;
		int locationId = 1;

		sendCreateLocationEvent(hotelId, locationId);

		assertEquals(1, (long)repository.count().block());

		try {
			sendCreateLocationEvent(hotelId, locationId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Hotel Id: 1, Location Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, (long)repository.count().block());
	}

	@Test
	public void deleteLocation() {
		int hotelId = 1;
		int locationId = 1;

		sendCreateLocationEvent(hotelId, locationId);
		assertEquals(1, (long)repository.findByHotelId(hotelId).count().block());

		sendDeleteLocationEvent(hotelId);
		assertEquals(0, (long)repository.findByHotelId(hotelId).count().block());

		sendDeleteLocationEvent(hotelId);
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

	private void sendCreateLocationEvent(int hotelId, int locationId) {
		Location location = new Location(hotelId, locationId, "Country"+locationId,"Town","Address", "SA");
		Event<Integer, Hotel> event = new Event(CREATE, hotelId, location);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteLocationEvent(int hotelId) {
		Event<Integer, Hotel> event = new Event(DELETE, hotelId, null);
		input.send(new GenericMessage<>(event));
	}

}