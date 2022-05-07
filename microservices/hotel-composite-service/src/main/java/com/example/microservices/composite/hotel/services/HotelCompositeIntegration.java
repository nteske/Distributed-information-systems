package com.example.microservices.composite.hotel.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.api.core.room.*;
import com.example.api.core.hotel.*;
import com.example.api.core.review.*;
import com.example.api.core.location.*;
import com.example.api.event.Event;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.HttpErrorInfo;

import java.io.IOException;

import static reactor.core.publisher.Flux.empty;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;

@EnableBinding(HotelCompositeIntegration.MessageSources.class)
@Component
public class HotelCompositeIntegration implements HotelService, LocationService, ReviewService, RoomService {

    private static final Logger LOG = LoggerFactory.getLogger(HotelCompositeIntegration.class);

    private final String hotelServiceUrl = "http://hotel";
    private final String locationServiceUrl = "http://location";
    private final String reviewServiceUrl = "http://review";
    private final String roomServiceUrl = "http://room";
    
    private final ObjectMapper mapper;
    private final WebClient.Builder webClientBuilder;
    
    private WebClient webClient;
    
    private MessageSources messageSources;

    public interface MessageSources {

        String OUTPUT_HOTELS = "output-hotels";
        String OUTPUT_LOCATIONS = "output-location";
        String OUTPUT_REVIEWS = "output-reviews";
        String OUTPUT_ROOMS = "output-rooms";

        @Output(OUTPUT_HOTELS)
        MessageChannel outputHotels();

        @Output(OUTPUT_LOCATIONS)
        MessageChannel outputLocation();

        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();
        
        @Output(OUTPUT_ROOMS)
        MessageChannel outputRooms();
    }

    @Autowired
    public HotelCompositeIntegration(
    	WebClient.Builder webClientBuilder,
        ObjectMapper mapper,
        MessageSources messageSources
    ) {
    	this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.messageSources = messageSources;
    }
    
    @Override
    public Hotel createHotel(Hotel body) {
    	messageSources.outputHotels().send(MessageBuilder.withPayload(new Event(CREATE, body.getHotelId(), body)).build());
        return body;
    }

    @Override
    public Mono<Hotel> getHotel(int hotelId) {
    	String url = hotelServiceUrl + "/hotel/" + hotelId;
        LOG.debug("Will call the getHotel API on URL: {}", url);
        return getWebClient().get().uri(url).retrieve().bodyToMono(Hotel.class).log().onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }
    
    @Override
    public void deleteHotel(int hotelId) {
    	messageSources.outputHotels().send(MessageBuilder.withPayload(new Event(DELETE, hotelId, null)).build());
    }
    
    @Override
    public Location createLocation(Location body) {
    	messageSources.outputLocation().send(MessageBuilder.withPayload(new Event(CREATE, body.getHotelId(), body)).build());
        return body;
    }
    
    @Override
    public Flux<Location> getLocation(int hotelId) {
    	String url = locationServiceUrl + "/location?hotelId=" + hotelId;
    	LOG.debug("Will call the getLocation API on URL: {}", url);
    	// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Location.class).log().onErrorResume(error -> empty());
    }
    
    @Override
    public void deleteLocations(int hotelId) {
    	messageSources.outputLocation().send(MessageBuilder.withPayload(new Event(DELETE, hotelId, null)).build());
    }
    
    @Override
    public Review createReview(Review body) {
    	messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(CREATE, body.getHotelId(), body)).build());
        return body;
    }

    @Override
    public Flux<Review> getReviews(int hotelId) {
    	String url = reviewServiceUrl + "/review?hotelId=" + hotelId;
    	LOG.debug("Will call the getReviews API on URL: {}", url);
        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Review.class).log().onErrorResume(error -> empty());
    }
    
    @Override
    public void deleteReviews(int hotelId) {
    	messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(DELETE, hotelId, null)).build());
    }
    
    @Override
    public Room createRoom(Room body) {
    	messageSources.outputRooms().send(MessageBuilder.withPayload(new Event(CREATE, body.getHotelId(), body)).build());
        return body;
    }

    @Override
	public Flux<Room> getRoom(int hotelId) {
    	String url = roomServiceUrl + "/room?hotelId=" + hotelId;
    	LOG.debug("Will call the getRooms API on URL: {}", url);
    	// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Room.class).log().onErrorResume(error -> empty());
	}
    
    @Override
    public void deleteRooms(int hotelId) {
    	messageSources.outputRooms().send(MessageBuilder.withPayload(new Event(DELETE, hotelId, null)).build());
    }
    
    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }
    
    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }
        WebClientResponseException wcre = (WebClientResponseException)ex;
        switch (wcre.getStatusCode()) {
        case NOT_FOUND:
            return new NotFoundException(getErrorMessage(wcre));
        case UNPROCESSABLE_ENTITY :
            return new InvalidInputException(getErrorMessage(wcre));
        default:
            LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
            LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
            return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

}