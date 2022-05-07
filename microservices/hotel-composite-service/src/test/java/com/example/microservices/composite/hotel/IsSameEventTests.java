package com.example.microservices.composite.hotel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import com.example.api.core.hotel.Hotel;
import com.example.api.event.Event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Date;

import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;
import static com.example.microservices.composite.hotel.IsSameEvent.sameEventExceptCreatedAt;

public class IsSameEventTests {

	ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEventObjectCompare() throws JsonProcessingException {

    	// Event #1 and #2 are the same event, but occurs as different times
		// Event #3 and #4 are different events
		Event<Integer, Hotel> event1 = new Event<>(CREATE, 1, new Hotel(1, "Test Title", "Description", "Image", new Date(), "mock-address"));
		Event<Integer, Hotel> event2 = new Event<>(CREATE, 1, new Hotel(1, "Test Title", "Description", "Image", new Date(),"mock-address"));
		Event<Integer, Hotel> event3 = new Event<>(DELETE, 1, null);
		Event<Integer, Hotel> event4 = new Event<>(CREATE, 1, new Hotel(2, "Test Title", "Description", "Image",new Date(), "mock-address"));

		String event1JSon = mapper.writeValueAsString(event1);

		//assertThat(event1JSon, is(sameEventExceptCreatedAt(event2)));
		assertThat(event1JSon, not(sameEventExceptCreatedAt(event3)));
		assertThat(event1JSon, not(sameEventExceptCreatedAt(event4)));
    }
}