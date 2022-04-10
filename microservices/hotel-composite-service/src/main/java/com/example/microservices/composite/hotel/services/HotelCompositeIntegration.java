package com.example.microservices.composite.hotel.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.api.core.location.*;
import com.example.api.core.hotel.*;
import com.example.api.core.review.*;
import com.example.api.core.room.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.HttpErrorInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class HotelCompositeIntegration implements HotelService, RoomService, ReviewService, LocationService {
    
    private static final Logger LOG = LoggerFactory.getLogger(HotelCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String hotelServiceUrl;
    private final String locationServiceUrl;
    private final String reviewServiceUrl;
    private final String roomServiceUrl;

    @Autowired
    public HotelCompositeIntegration(
        RestTemplate restTemplate,
        ObjectMapper mapper,

        @Value("${app.hotel-service.host}") String hotelServiceHost,
        @Value("${app.hotel-service.port}") int    hotelServicePort,

        @Value("${app.location-service.host}") String locationServiceHost,
        @Value("${app.location-service.port}") int    locationServicePort,

        @Value("${app.review-service.host}") String reviewServiceHost,
        @Value("${app.review-service.port}") int    reviewServicePort,
        
        @Value("${app.room-service.host}") String roomServiceHost,
        @Value("${app.room-service.port}") int    roomServicePort
    ) {

        this.restTemplate = restTemplate;
        this.mapper = mapper;

        hotelServiceUrl        = "http://" + hotelServiceHost + ":" + hotelServicePort + "/hotel/";
        locationServiceUrl = "http://" + locationServiceHost + ":" + locationServicePort + "/location";
        reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
        roomServiceUrl    = "http://" + roomServiceHost + ":" + roomServicePort + "/room";
    }

        
    @Override
    public Hotel createHotel(Hotel body) {

        try {
            String url = hotelServiceUrl;
            LOG.debug("Will post a new hotel to URL: {}", url);

            Hotel hotel = restTemplate.postForObject(url, body, Hotel.class);
            LOG.debug("Created a hotel with id: {}", hotel.getHotelId());

            return hotel;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }
    @Override
    public Hotel getHotel(int hotelId) {

        try {
            String url = hotelServiceUrl + "/" + hotelId;
            LOG.debug("Will call the getMHotel API on URL: {}", url);

            Hotel hotel = restTemplate.getForObject(url, Hotel.class);
            LOG.debug("Found a hotel with id: {}", hotel.getHotelId());

            return hotel;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }
    
    @Override
    public void deleteHotel(int hotelId) {
        try {
            String url = hotelServiceUrl + "/" + hotelId;
            LOG.debug("Will call the deleteHotel API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Location createLocation(Location body) {
        try {
        	String url = locationServiceUrl;
            LOG.debug("Will post a new location to URL: {}", url);

            Location location = restTemplate.postForObject(url, body, Location.class);
            LOG.debug("Created a location with id: {}", location.getLocationId());

            return location;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Location> getLocation(int hotelId) {

        try {
            String url = locationServiceUrl + "?hotelId=" + hotelId;

            LOG.debug("Will call the getLocation API on URL: {}", url);
            List<Location> location = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Location>>() {}).getBody();

            LOG.debug("Found {} location for hotel with id: {}", location.size(), hotelId);
            return location;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting location, return zero location: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteLocations(int hotelId) {
        try {
            String url = locationServiceUrl + "?hotelId=" + hotelId;
            LOG.debug("Will call the deleteLocations API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Review createReview(Review body) {
        try {
        	String url = reviewServiceUrl;
            LOG.debug("Will post a new review to URL: {}", url);

            Review review = restTemplate.postForObject(url, body, Review.class);
            LOG.debug("Created a review with id: {}", review.getReviewId());

            return review;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Review> getReviews(int hotelId) {

        try {
            String url = reviewServiceUrl + "?hotelId=" + hotelId;

            LOG.debug("Will call the getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {}).getBody();

            LOG.debug("Found {} reviews for hotel with id: {}", reviews.size(), hotelId);
            return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteReviews(int hotelId) {
        try {
            String url = reviewServiceUrl + "?hotelId=" + hotelId;
            LOG.debug("Will call the deleteReviews API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Room createRoom(Room body) {
        try {
        	String url = roomServiceUrl;
            LOG.debug("Will post a new room to URL: {}", url);

            Room room = restTemplate.postForObject(url, body, Room.class);
            LOG.debug("Created a room with id: {}", room.getRoomId());

            return room;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
	public List<Room> getRoom(int hotelId) {
		
		try {
            String url = roomServiceUrl  + "?hotelId=" + hotelId;

            LOG.debug("Will call the getRoom API on URL: {}", url);
            List<Room> room = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Room>>() {}).getBody();

            LOG.debug("Found {} room for hotel with id: {}", room.size(), hotelId);
            return room;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting room, return zero rooms: {}", ex.getMessage());
            return new ArrayList<>();
        }
	}

    @Override
    public void deleteRooms(int hotelId) {
        try {
            String url = roomServiceUrl + "?hotelId=" + hotelId;
            LOG.debug("Will call the deleteRooms API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (ex.getStatusCode()) {

        case NOT_FOUND:
            return new NotFoundException(getErrorMessage(ex));

        case UNPROCESSABLE_ENTITY :
            return new InvalidInputException(getErrorMessage(ex));

        default:
            LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
            LOG.warn("Error body: {}", ex.getResponseBodyAsString());
            return ex;
        }
    }

    
    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
