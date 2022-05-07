package com.example.microservices.composite.hotel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Date;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false"})
public class HotelCompositeServiceApplicationTests {

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
			thenReturn(Mono.just(new Hotel(HOTEL_ID_OK, "Crystal Hotel", "Place where comfort meets luxury.", "https://placekitten.com/200/300", new Date(), "mock-address")));
		when(compositeIntegration.getLocation(HOTEL_ID_OK)).
			thenReturn(Flux.fromIterable(singletonList(new Location(HOTEL_ID_OK,1 , "Serbia", "Belgrade", "Internacionalnih Brigada 9", "mock address"))));
		when(compositeIntegration.getReviews(HOTEL_ID_OK)).
			thenReturn(Flux.fromIterable(singletonList(new Review(HOTEL_ID_OK,1 ,5 , "Great!", new Date(), "mock address"))));
		when(compositeIntegration.getRoom(HOTEL_ID_OK)).
			thenReturn(Flux.fromIterable(singletonList(new Room(HOTEL_ID_OK, 1, 26, 3, 220, "mock address"))));
		
		when(compositeIntegration.getHotel(HOTEL_ID_NOT_FOUND)).thenThrow(new NotFoundException("NOT FOUND: " + HOTEL_ID_NOT_FOUND));

		when(compositeIntegration.getHotel(HOTEL_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + HOTEL_ID_INVALID));
	}

	@Test
	public void contextLoads() {
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

}
