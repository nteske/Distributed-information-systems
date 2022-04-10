package microservices.composite.hotel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.sql.Date;

import com.example.api.composite.hotel.HotelAggregate;
import com.example.api.composite.hotel.LocationSummary;
import com.example.api.composite.hotel.ReviewSummary;
import com.example.api.composite.hotel.RoomSummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.api.core.location.Location;
import com.example.api.core.room.Room;
import com.example.api.core.review.Review;
import com.example.api.core.hotel.Hotel;
import com.example.microservices.composite.hotel.services.HotelCompositeIntegration;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import static reactor.core.publisher.Mono.just;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
class HotelCompositeServiceApplicationTests {

	private static final int HOTEL_ID_OK = 1;
	private static final int HOTEL_ID_NOT_FOUND = 2;
	private static final int HOTEL_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

	@MockBean
	private HotelCompositeIntegration compositeIntegration;

	@Before
	public void setUp() {

		when(compositeIntegration.getHotel(HOTEL_ID_OK)).
			thenReturn(new Hotel(HOTEL_ID_OK, "Crystal Hotel", "Place where comfort meets luxury.", "https://placekitten.com/200/300", Date.valueOf("2021-08-13"), "mock-address"));
		when(compositeIntegration.getLocation(HOTEL_ID_OK)).
			thenReturn(singletonList(new Location(HOTEL_ID_OK,1 , "Serbia", "Belgrade", "Internacionalnih Brigada 9", "mock address")));
		when(compositeIntegration.getReviews(HOTEL_ID_OK)).
			thenReturn(singletonList(new Review(HOTEL_ID_OK,1 ,5 , "Great!", Date.valueOf("2021-10-13"), "mock address")));
		when(compositeIntegration.getRoom(HOTEL_ID_OK)).
			thenReturn(singletonList(new Room(HOTEL_ID_OK, 1, 26, 3, 220, "mock address")));
		
		when(compositeIntegration.getHotel(HOTEL_ID_NOT_FOUND)).thenThrow(new NotFoundException("NOT FOUND: " + HOTEL_ID_NOT_FOUND));

		when(compositeIntegration.getHotel(HOTEL_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + HOTEL_ID_INVALID));
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void createCompositeHotel1() {

		HotelAggregate compositeHotel = new HotelAggregate(1, "Some title", "Some Description","Some image",Date.valueOf("2021-08-13"),null,null,null, null);

		postAndVerifyHotel(compositeHotel, OK);
	}

	@Test
	public void createCompositeHotel2() {
		HotelAggregate compositeHotel = new HotelAggregate(1, "Some title", "Some Description","Some image",Date.valueOf("2021-08-13"),
				singletonList(new LocationSummary(1, "Serbia", "Belgrade", "Internacionalnih Brigada 9")),
				singletonList(new ReviewSummary(1, 5 , "Great!", Date.valueOf("2021-10-13"))), 
				singletonList(new RoomSummary(1, 26, 3, 220)), 
						null);

		postAndVerifyHotel(compositeHotel, OK);
	}

	@Test
	public void deleteCompositeHotel() {
		HotelAggregate compositeHotel = new HotelAggregate(1, "Some title", "Some Description","Some image",Date.valueOf("2021-08-13"),
				singletonList(new LocationSummary(1, "Serbia", "Belgrade", "Internacionalnih Brigada 9")),
				singletonList(new ReviewSummary(1,5 , "Great!", Date.valueOf("2021-10-13"))), 
				singletonList(new RoomSummary(1, 26, 3, 220)), 
						null);

		postAndVerifyHotel(compositeHotel, OK);

		deleteAndVerifyHotel(compositeHotel.getHotelId(), OK);
		deleteAndVerifyHotel(compositeHotel.getHotelId(), OK);
	}

	@Test
	public void getHotelById() {
		getAndVerifyHotel(HOTEL_ID_OK, OK)
        .jsonPath("$.hotelId").isEqualTo(HOTEL_ID_OK)
        .jsonPath("$.location.length()").isEqualTo(1)
        .jsonPath("$.reviews.length()").isEqualTo(1)
    	.jsonPath("$.room.length()").isEqualTo(1);
	}

	@Test
	public void getHotelNotFound() {
		getAndVerifyHotel(HOTEL_ID_NOT_FOUND, NOT_FOUND)
        .jsonPath("$.path").isEqualTo("/hotel-composite/" +HOTEL_ID_NOT_FOUND)
        .jsonPath("$.message").isEqualTo("NOT FOUND: " + HOTEL_ID_NOT_FOUND);
	}

	@Test
	public void getHotelInvalidInput() {
		getAndVerifyHotel(HOTEL_ID_INVALID, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/hotel-composite/" + HOTEL_ID_INVALID)
        .jsonPath("$.message").isEqualTo("INVALID: " + HOTEL_ID_INVALID);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyHotel(int hotelId, HttpStatus expectedStatus) {
		return client.get()
			.uri("/hotel-composite/" + hotelId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private void postAndVerifyHotel(HotelAggregate compositeHotel, HttpStatus expectedStatus) {
		client.post()
			.uri("/hotel-composite")
			.body(just(compositeHotel), HotelAggregate.class)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus);
	}

	private void deleteAndVerifyHotel(int hotelId, HttpStatus expectedStatus) {
		client.delete()
			.uri("/hotel-composite/" + hotelId)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus);
	}

}
