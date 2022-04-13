package microservices.core.room;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.api.core.room.Room;
import com.example.microservices.core.room.persistence.RoomRepository;

import static org.junit.Assert.assertEquals;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class RoomServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private RoomRepository repository;


	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getRoomsByHotelId() {

		int hotelId = 1;

		postAndVerifyRoom(hotelId, 1, OK);
		postAndVerifyRoom(hotelId, 2, OK);
		postAndVerifyRoom(hotelId, 3, OK);

		assertEquals(3, repository.findByHotelId(hotelId).size());

		getAndVerifyRoomsByHotelId(hotelId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].hotelId").isEqualTo(hotelId)
			.jsonPath("$[2].roomId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {
		int hotelId = 1;
		int roomId = 1;

		postAndVerifyRoom(hotelId, roomId, OK)
			.jsonPath("$.hotelId").isEqualTo(hotelId)
			.jsonPath("$.roomId").isEqualTo(roomId);

		assertEquals(1, repository.count());

		postAndVerifyRoom(hotelId, roomId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/room")
			.jsonPath("$.message").isEqualTo("Duplicate key, Hotel Id: 1, Room Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteRooms() {
		int hotelId = 1;
		int roomId = 1;

		postAndVerifyRoom(hotelId, roomId, OK);
		assertEquals(1, repository.findByHotelId(hotelId).size());

		deleteAndVerifyRoomsByHotelId(hotelId, OK);
		assertEquals(0, repository.findByHotelId(hotelId).size());

		deleteAndVerifyRoomsByHotelId(hotelId, OK);
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

	private WebTestClient.BodyContentSpec postAndVerifyRoom(int hotelId, int roomId, HttpStatus expectedStatus) {
		Room room = new Room(hotelId, roomId,315,roomId,233, "SA");
		return client.post()
			.uri("/room")
			.body(just(room), Room.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyRoomsByHotelId(int hotelId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/room?hotelId=" + hotelId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}