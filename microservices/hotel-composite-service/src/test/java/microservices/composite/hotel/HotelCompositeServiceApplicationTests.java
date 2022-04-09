package microservices.composite.hotel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import api.core.location.Location;
import api.core.room.Room;
import api.core.review.Review;
import api.core.hotel.Hotel;
import com.example.microservices.composite.hotel.services.HotelCompositeIntegration;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;

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
	public void getHotelById() {

        client.get()
            .uri("/hotel-composite/" + HOTEL_ID_OK)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.hotelId").isEqualTo(HOTEL_ID_OK)
            .jsonPath("$.trivia.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1)
        	.jsonPath("$.crazyCredits.length()").isEqualTo(1);
	}

	@Test
	public void getHotelNotFound() {

        client.get()
            .uri("/hotel-composite/" + HOTEL_ID_NOT_FOUND)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/hotel-composite/" + HOTEL_ID_NOT_FOUND)
            .jsonPath("$.message").isEqualTo("NOT FOUND: " + HOTEL_ID_NOT_FOUND);
	}

	@Test
	public void getHotelInvalidInput() {

        client.get()
            .uri("/hotel-composite/" + HOTEL_ID_INVALID)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/hotel-composite/" + HOTEL_ID_INVALID)
            .jsonPath("$.message").isEqualTo("INVALID: " + HOTEL_ID_INVALID);
	}
}
