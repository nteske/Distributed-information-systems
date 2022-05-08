package com.example.microservices.core.room;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import com.example.api.core.room.Room;
import com.example.api.core.hotel.Hotel;
import com.example.api.event.Event;
import com.example.util.exceptions.InvalidInputException;
import com.example.microservices.core.room.persistence.RoomRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false", "spring.cloud.config.enabled=false", "server.error.include-message=always"})
public class RoomServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private RoomRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;


	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	@Test
	public void getRoomsByHotelId() {

		int hotelId = 1;

		sendCreateRoomEvent(hotelId, 1);
		sendCreateRoomEvent(hotelId, 2);
		sendCreateRoomEvent(hotelId, 3);

		assertEquals(3, (long)repository.findByHotelId(hotelId).count().block());

		getAndVerifyRoomsByHotelId(hotelId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].hotelId").isEqualTo(hotelId)
			.jsonPath("$[2].roomId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {
		int hotelId = 1;
		int roomId = 1;

		sendCreateRoomEvent(hotelId, roomId);

		assertEquals(1, (long)repository.count().block());

		try {
			sendCreateRoomEvent(hotelId, roomId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();

				assertEquals("Duplicate key, Hotel Id: 1, room Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, (long)repository.count().block());
	}

	@Test
	public void deleteRooms() {
		int hotelId = 1;
		int roomId = 1;

		sendCreateRoomEvent(hotelId, roomId);
		assertEquals(1, (long)repository.findByHotelId(hotelId).count().block());

		sendDeleteRoomEvent(hotelId);
		assertEquals(0, (long)repository.findByHotelId(hotelId).count().block());

		sendDeleteRoomEvent(hotelId);
	}

	@Test
	public void getRoomsMissingParameter() {
		getAndVerifyRoomsByHotelId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/room")
			.jsonPath("$.message").isEqualTo("Required int parameter 'hotelId' is not present");
	}

	@Test
	public void getRoomsInvalidParameter() {
		getAndVerifyRoomsByHotelId("?hotelId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/room")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getRoomsNotFound() {
		getAndVerifyRoomsByHotelId("?hotelId=113", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getRoomsInvalidParameterNegativeValue() {
		int hotelIdInvalid = -1;

		getAndVerifyRoomsByHotelId("?hotelId=" + hotelIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/room")
			.jsonPath("$.message").isEqualTo("Invalid hotelId: " + hotelIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRoomsByHotelId(int hotelId, HttpStatus expectedStatus) {
		return getAndVerifyRoomsByHotelId("?hotelId=" + hotelId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRoomsByHotelId(String hotelIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/room" + hotelIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private void sendCreateRoomEvent(int hotelId, int roomId) {
		Room room = new Room(hotelId, roomId, roomId, 33,333, "SA");
		Event<Integer, Hotel> event = new Event(CREATE, hotelId, room);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteRoomEvent(int hotelId) {
		Event<Integer, Hotel> event = new Event(DELETE, hotelId, null);
		input.send(new GenericMessage<>(event));
	}
}