package com.example.microservices.core.hotel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.api.core.hotel.Hotel;
import com.example.api.event.Event;
import com.example.microservices.core.hotel.persistence.HotelRepository;
import com.example.util.exceptions.InvalidInputException;

import static org.junit.Assert.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false"})
public class HotelServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private HotelRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	@Test
	public void getHotelById() {
		int hotelId = 1;
		assertNull(repository.findByHotelId(hotelId).block());
		assertEquals(0, (long)repository.count().block());
		sendCreateHotelEvent(hotelId);

		assertNotNull(repository.findByHotelId(hotelId).block());
		assertEquals(1, (long)repository.count().block());
		getAndVerifyHotel(hotelId, OK)
        .jsonPath("$.hotelId").isEqualTo(hotelId);
	}
	
	@Test
	public void duplicateError() {
		int hotelId = 1;
		assertNull(repository.findByHotelId(hotelId).block());

		sendCreateHotelEvent(hotelId);
		assertNotNull(repository.findByHotelId(hotelId).block());
		try {
			sendCreateHotelEvent(hotelId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Hotel Id: " + hotelId, iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}
	}

	@Test
	public void deleteHotel() {
		int hotelId = 1;
		sendCreateHotelEvent(hotelId);
		assertNotNull(repository.findByHotelId(hotelId).block());
		sendDeleteHotelEvent(hotelId);
		assertNull(repository.findByHotelId(hotelId).block());
		sendDeleteHotelEvent(hotelId);
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

	private void sendCreateHotelEvent(int hotelId) {
		Hotel hotel = new Hotel(hotelId, "n","d","i",new Date(), "SA");
		Event<Integer, Hotel> event = new Event(CREATE, hotelId, hotel);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteHotelEvent(int hotelId) {
		Event<Integer, Hotel> event = new Event(DELETE, hotelId, null);
		input.send(new GenericMessage<>(event));
	}
}