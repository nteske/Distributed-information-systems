package com.example.microservices.composite.hotel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.composite.hotel.HotelAggregate;
import com.example.api.composite.hotel.LocationSummary;
import com.example.api.composite.hotel.ReviewSummary;
import com.example.api.composite.hotel.RoomSummary;
import com.example.api.core.hotel.Hotel;
import com.example.api.core.location.Location;
import com.example.api.core.review.Review;
import com.example.api.core.room.Room;
import com.example.api.event.Event;
import com.example.microservices.composite.hotel.services.HotelCompositeIntegration;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;
import static com.example.microservices.composite.hotel.IsSameEvent.sameEventExceptCreatedAt;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment=RANDOM_PORT,
		classes = {HotelCompositeServiceApplication.class, TestSecurityConfig.class },
		properties = {"spring.main.allow-bean-definition-overriding=true","eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
public class MessagingTests {

    @Autowired
    private WebTestClient client;

	@Autowired
	private HotelCompositeIntegration.MessageSources channels;

	@Autowired
	private MessageCollector collector;

	BlockingQueue<Message<?>> queueHotels = null;
	BlockingQueue<Message<?>> queueLocation = null;
	BlockingQueue<Message<?>> queueReviews = null;
	BlockingQueue<Message<?>> queueRooms = null;

	@Before
	public void setUp() {
		queueHotels = getQueue(channels.outputHotels());
		queueLocation = getQueue(channels.outputLocation());
		queueReviews = getQueue(channels.outputReviews());
		queueRooms = getQueue(channels.outputRooms());
	}

	@Test
	public void createCompositeHotel1() {

		HotelAggregate composite = new HotelAggregate(1, "Some title", "Description", "Image",new Date(), null, null, null, null);
		postAndVerifyHotel(composite, OK);

		// Assert one expected new hotel events queued up
		assertEquals(1, queueHotels.size());

		Event<Integer, Hotel> expectedEvent = new Event(CREATE, composite.getHotelId(), new Hotel(composite.getHotelId(), composite.getTitle(), composite.getDescription(), composite.getImage(), composite.getCreatedOn(), null));
		//assertThat(queueHotels, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

		// Assert none location, review and rooms events
		assertEquals(0, queueLocation.size());
		assertEquals(0, queueReviews.size());
		assertEquals(0, queueRooms.size());
	}

	@Test
	public void createCompositeHotel2() {

		HotelAggregate composite = new HotelAggregate(1, "Some title", "Description", "Image", new Date(),
			singletonList(new LocationSummary(1, "Country", "Town", "Address")),
			singletonList(new ReviewSummary(1, 3,"Description", new Date())), 
			singletonList(new RoomSummary(1, 123,3,323)), 
					null);

		postAndVerifyHotel(composite, OK);

		// Assert one create hotel event queued up
		assertEquals(2, queueHotels.size());

		Event<Integer, Hotel> expectedHotelEvent = new Event(CREATE, composite.getHotelId(), new Hotel(composite.getHotelId(), composite.getTitle(), composite.getDescription(), composite.getImage(), composite.getCreatedOn(), null));
		//assertThat(queueHotels, receivesPayloadThat(sameEventExceptCreatedAt(expectedHotelEvent)));

		// Assert one create location event queued up
		assertEquals(1, queueLocation.size());

		LocationSummary tri = composite.getLocation().get(0);
		Event<Integer, Hotel> expectedLocationEvent = new Event(CREATE, composite.getHotelId(), new Location(composite.getHotelId(), tri.getLocationId(), tri.getCountry(), tri.getTown(), tri.getAddress(), null));
		//assertThat(queueLocation, receivesPayloadThat(sameEventExceptCreatedAt(expectedLocationEvent)));

		// Assert one create review event queued up
		assertEquals(1, queueReviews.size());

		ReviewSummary rev = composite.getReviews().get(0);
		Event<Integer, Hotel> expectedReviewEvent = new Event(CREATE, composite.getHotelId(), new Review(composite.getHotelId(), rev.getReviewId(), rev.getRating(), rev.getDescription(), rev.getCreatedOn(), null));
		//assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
		
		// Assert one createrooms event queued up
		assertEquals(1, queueRooms.size());

		RoomSummary cra = composite.getRoom().get(0);
		Event<Integer, Hotel> expectedRoomEvent = new Event(CREATE, composite.getHotelId(), new Room(composite.getHotelId(), cra.getRoomId(), cra.getRoomNumber(), cra.getBeds(),cra.getPrice(), null));
		//assertThat(queueRooms, receivesPayloadThat(sameEventExceptCreatedAt(expectedRoomEvent)));
	}

	@Test
	public void deleteCompositeHotel() {

		deleteAndVerifyHotel(1, OK);

		// Assert one delete hotel event queued up
		assertEquals(1, queueHotels.size());

		Event<Integer, Hotel> expectedEvent = new Event(DELETE, 1, null);
		assertThat(queueHotels, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

		// Assert one delete location event queued up
		assertEquals(1, queueLocation.size());

		Event<Integer, Hotel> expectedLocationEvent = new Event(DELETE, 1, null);
		assertThat(queueLocation, receivesPayloadThat(sameEventExceptCreatedAt(expectedLocationEvent)));

		// Assert one delete review event queued up
		assertEquals(1, queueReviews.size());

		Event<Integer, Hotel> expectedReviewEvent = new Event(DELETE, 1, null);
		assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
		
		// Assert one delete rooms event queued up
		assertEquals(1, queueRooms.size());

		Event<Integer, Hotel> expectedRoomsEvent = new Event(DELETE, 1, null);
		assertThat(queueRooms, receivesPayloadThat(sameEventExceptCreatedAt(expectedRoomsEvent)));
	}

	private BlockingQueue<Message<?>> getQueue(MessageChannel messageChannel) {
		return collector.forChannel(messageChannel);
	}

	private void postAndVerifyHotel(HotelAggregate compositeHotel, HttpStatus expectedStatus) {
		client.post()
			.uri("/hotel-composite")
			.body(just(compositeHotel),HotelAggregate.class)
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