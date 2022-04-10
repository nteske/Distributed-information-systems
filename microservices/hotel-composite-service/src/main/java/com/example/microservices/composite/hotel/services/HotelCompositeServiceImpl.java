package com.example.microservices.composite.hotel.services;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.api.composite.hotel.*;
import com.example.api.core.hotel.Hotel;
import com.example.api.core.review.Review;
import com.example.api.core.room.Room;
import com.example.api.core.location.Location;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

@RestController
public class HotelCompositeServiceImpl implements HotelCompositeService {
    
    private static final Logger LOG = LoggerFactory.getLogger(HotelCompositeServiceImpl.class);


	private final ServiceUtil serviceUtil;
    private  HotelCompositeIntegration integration;

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

            LOG.debug("createCompositeHotel: composite entites created for hotelId: {}", body.getHotelId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeHotel failed", re);
            throw re;
        }
    }
	
	@Override
	public HotelAggregate getCompositeHotel(int hotelId) {
        LOG.debug("getCompositeHotel: lookup a hotel aggregate for hotelId: {}", hotelId);

		Hotel hotel = integration.getHotel(hotelId);
        if (hotel == null) throw new NotFoundException("No hotel found for hotelId: " + hotelId);

        List<Location> location = integration.getLocation(hotelId);

        List<Review> reviews = integration.getReviews(hotelId);
        
        List<Room> room = integration.getRoom(hotelId);

        LOG.debug("getCompositeHotel: aggregate entity found for hotelId: {}", hotelId);

        return createHotelAggregate(hotel, location, reviews, room, serviceUtil.getServiceAddress());
	}

        
    @Override
    public void deleteCompositeHotel(int hotelId) {

        LOG.debug("deleteCompositeHotel: Deletes a movie aggregate for hotelId: {}", hotelId);

        integration.deleteHotel(hotelId);

        integration.deleteLocations(hotelId);

        integration.deleteReviews(hotelId);
        
        integration.deleteRooms(hotelId);

        LOG.debug("deleteCompositeHotel: aggregate entities deleted for hotelId: {}", hotelId);
    }
	
	private HotelAggregate createHotelAggregate(Hotel hotel, List<Location> location, List<Review> reviews, List<Room> room, String serviceAddress) {

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
        
        // 4. Copy summary room info, if available
        List<RoomSummary> roomSummaries = (room == null)  ? null :
        	room.stream()
                .map(r -> new RoomSummary(r.getRoomId(), r.getRoomNumber(), r.getBeds(), r.getPrice()))
                .collect(Collectors.toList());

        // 5. Create info regarding the involved microservices addresses
        String hotelAddress = hotel.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String locationAddress = (location != null && location.size() > 0) ? location.get(0).getServiceAddress() : "";
        String roomAddress = (room != null && room.size() > 0) ? room.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, hotelAddress, reviewAddress, locationAddress, roomAddress);

        return new HotelAggregate(hotelId, title, description, image, createdOn, locationSummaries, reviewSummaries, roomSummaries, serviceAddresses);
    }

}
