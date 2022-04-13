package microservices.core.review;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.core.review.Review;
import com.example.microservices.core.review.persistence.ReviewRepository;

import static org.junit.Assert.assertEquals;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.sql.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {
"spring.datasource.url=jdbc:h2:mem:review-db"})
class ReviewServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewRepository repository;

	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getReviewsByHotelId() {
		int hotelId = 1;
		assertEquals(0, repository.findByHotelId(hotelId).size());
		postAndVerifyReview(hotelId, 1, OK);
		postAndVerifyReview(hotelId, 2, OK);
		postAndVerifyReview(hotelId, 3, OK);
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
		postAndVerifyReview(hotelId, reviewId, OK)
			.jsonPath("$.hotelId").isEqualTo(hotelId)
			.jsonPath("$.reviewId").isEqualTo(reviewId);

		assertEquals(1, repository.count());
		postAndVerifyReview(hotelId, reviewId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Duplicate key, Hotel Id: 1, Review Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteReviews() {
		int hotelId = 1;
		int reviewId = 1;

		postAndVerifyReview(hotelId, reviewId, OK);
		assertEquals(1, repository.findByHotelId(hotelId).size());

		deleteAndVerifyReviewsByHotelId(hotelId, OK);
		assertEquals(0, repository.findByHotelId(hotelId).size());

		deleteAndVerifyReviewsByHotelId(hotelId, OK);
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

	private WebTestClient.BodyContentSpec postAndVerifyReview(int hotelId, int reviewId, HttpStatus expectedStatus) {
		Review review = new Review(hotelId, reviewId, 3, "Great "+reviewId, Date.valueOf("2021-08-12"), "SA");
		return client.post()
			.uri("/review")
			.body(just(review), Review.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByHotelId(int hotelId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/review?hotelId=" + hotelId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}

}