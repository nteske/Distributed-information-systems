package com.example.microservices.core.review;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.core.review.Review;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import com.example.api.core.hotel.Hotel;
import com.example.api.event.Event;
import com.example.util.exceptions.InvalidInputException;
import com.example.microservices.core.review.persistence.ReviewRepository;

import static org.junit.Assert.assertEquals;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;
import static org.junit.Assert.fail;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {
	"logging.level.com.example=DEBUG",
	"eureka.client.enabled=false",
	"spring.datasource.url=jdbc:h2:mem:review-db"})
public class ReviewServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();
	}

	@Test
	public void getReviewsByHotelId() {
		int hotelId = 1;
		assertEquals(0, repository.findByHotelId(hotelId).size());
		sendCreateReviewEvent(hotelId, 1);
		sendCreateReviewEvent(hotelId, 2);
		sendCreateReviewEvent(hotelId, 3);
		assertEquals(3, repository.findByHotelId(hotelId).size());
		getAndVerifyReviewsByHotelId(hotelId, OK)
		.jsonPath("$.length()").isEqualTo(3)
		.jsonPath("$[2].hotelId").isEqualTo(hotelId)
		.jsonPath("$[2].reviewId").isEqualTo(3);
	}
	
	@Test
	public void duplicateError() {
		int hotelId = 1;
		int reviewId = 1;

		assertEquals(0, repository.count());
		sendCreateReviewEvent(hotelId, reviewId);

		assertEquals(1, repository.count());
		try {
			sendCreateReviewEvent(hotelId, reviewId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Hotel Id: 1, Review Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteReviews() {
		int hotelId = 1;
		int reviewId = 1;

		sendCreateReviewEvent(hotelId, reviewId);
		assertEquals(1, repository.findByHotelId(hotelId).size());

		sendDeleteReviewEvent(hotelId);
		assertEquals(0, repository.findByHotelId(hotelId).size());

		sendDeleteReviewEvent(hotelId);
	}

	@Test
	public void getReviewsMissingParameter() {
		getAndVerifyReviewsByHotelId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Required int parameter 'hotelId' is not present");
	}

	@Test
	public void getReviewsInvalidParameter() {
		getAndVerifyReviewsByHotelId("?hotelId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getReviewsNotFound() {
		getAndVerifyReviewsByHotelId("?hotelId=213", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getReviewsInvalidParameterNegativeValue() {
		int hotelIdInvalid = -1;
		getAndVerifyReviewsByHotelId("?hotelId=" + hotelIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Invalid hotelId: " + hotelIdInvalid);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyReviewsByHotelId(int hotelId, HttpStatus expectedStatus) {
		return getAndVerifyReviewsByHotelId("?hotelId=" + hotelId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByHotelId(String hotelIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/review" + hotelIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private void sendCreateReviewEvent(int hotelId, int reviewId) {
		Review review = new Review(hotelId, reviewId, 3, "great " + reviewId, new Date(), "SA");
		Event<Integer, Hotel> event = new Event(CREATE, hotelId, review);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteReviewEvent(int hotelId) {
		Event<Integer, Hotel> event = new Event(DELETE, hotelId, null);
		input.send(new GenericMessage<>(event));
	}

}