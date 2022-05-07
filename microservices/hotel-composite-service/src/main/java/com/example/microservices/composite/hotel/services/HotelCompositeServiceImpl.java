package com.example.microservices.composite.hotel.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.example.api.composite.hotel.*;
import com.example.api.core.room.Room;
import com.example.api.core.hotel.Hotel;
import com.example.api.core.review.Review;
import com.example.api.core.location.Location;
import com.example.util.http.ServiceUtil;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class HotelCompositeServiceImpl implements HotelCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(HotelCompositeServiceImpl.class);
	
	private final ServiceUtil serviceUtil;
    private final HotelCompositeIntegration integration;

    @Autowired
    public HotelCompositeServiceImpl(ServiceUtil serviceUtil, HotelCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }
    
    @Override
    public void createCompositeHotel(HotelAggregate body) {
        try {

            LOG.debug("createCompositeHotel: creates a new composite entity for hotelId: {}", body.getHotelId());

            Hotel hotel = new Hotel(body.getHotelId(), body.getTitle(), body.getDescription(), body.getImage(),
            						body.getCreatedOn(), null);
            integration.createHotel(hotel);

            if (body.getLocation() != null) {
                body.getLocation().forEach(r -> {
                    Location location = new Location(body.getHotelId(), r.getLocationId(), r.getCountry(), r.getTown(), r.getAddress(), null);
                    integration.createLocation(location);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getHotelId(), r.getReviewId(), r.getRating(), r.getDescription(),
                    						   r.getCreatedOn(), null);
                    integration.createReview(review);
                });
            }
            
            if (body.getRoom() != null) {
                body.getRoom().forEach(r -> {
                    Room room = new Room(body.getHotelId(), r.getRoomId(), r.getRoomNumber(), r.getBeds(), r.getPrice(), null);
                    integration.createRoom(room);
                });
            }

            LOG.debug("createCompositeHotel: composite entities created for hotelId: {}", body.getHotelId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeHotel failed: {}", re.toString());
            throw re;
        }
    }
    
    @Override
    public Mono<HotelAggregate> getCompositeHotel(int hotelId) {
        return Mono.zip(
            values -> createHotelAggregate((Hotel) values[0], (List<Location>) values[1], (List<Review>) values[2], (List<Room>) values[3], serviceUtil.getServiceAddress()),
            integration.getHotel(hotelId),
            integration.getLocation(hotelId).collectList(),
            integration.getReviews(hotelId).collectList(),
            integration.getRoom(hotelId).collectList())
            .doOnError(ex -> LOG.warn("getCompositeHotel failed: {}", ex.toString()))
            .log();
    }
    
    @Override
    public void deleteCompositeHotel(int hotelId) {
    	try {
            LOG.debug("deleteCompositeHotel: Deletes a hotel aggregate for hotelId: {}", hotelId);

            integration.deleteHotel(hotelId);
            integration.deleteLocations(hotelId);
            integration.deleteReviews(hotelId);
            integration.deleteRooms(hotelId);

            LOG.debug("deleteCompositeHotel: aggregate entities deleted for hotelId: {}", hotelId);

        } catch (RuntimeException re) {
            LOG.warn("deleteCompositeHotel failed: {}", re.toString());
            throw re;
        }
    }
	
	private HotelAggregate createHotelAggregate(Hotel hotel, List<Location> location, List<Review> reviews, List<Room> rooms, String serviceAddress) {

        // 1. Setup hotel info
        int hotelId = hotel.getHotelId();
        String title = hotel.getTitle();
        String description = hotel.getDescription();
        String image = hotel.getImage();
        Date createdOn = hotel.getCreatedOn();

        // 2. Copy summary location info, if available
        List<LocationSummary> locationSummaries = (location == null) ? null :
        	location.stream()
                .map(r -> new LocationSummary(r.getLocationId(), r.getCountry(), r.getTown(), r.getAddress()))
                .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getRating(), r.getDescription(), r.getCreatedOn()))
                .collect(Collectors.toList());
        
        // 4. Copy summary rooms info, if available
        List<RoomSummary> roomSummaries = (rooms == null)  ? null :
        	rooms.stream()
                .map(r -> new RoomSummary(r.getRoomId(), r.getRoomNumber(), r.getBeds(), r.getPrice()))
                .collect(Collectors.toList());

        // 5. Create info regarding the involved microservices addresses
        String hotelAddress = hotel.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String locationAddress = (location != null && location.size() > 0) ? location.get(0).getServiceAddress() : "";
        String roomAddress = (rooms != null && rooms.size() > 0) ? rooms.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, hotelAddress, reviewAddress, locationAddress, roomAddress);

        return new HotelAggregate(hotelId, title, description, image, createdOn,
        						  locationSummaries, reviewSummaries, roomSummaries, serviceAddresses);
    }

}