package microservices.composite.hotel.services;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import api.composite.hotel.*;
import api.core.hotel.Hotel;
import api.core.review.Review;
import api.core.room.Room;
import api.core.location.Location;
import util.exceptions.NotFoundException;
import util.http.ServiceUtil;

@RestController
public class HotelCompositeServiceImpl implements HotelCompositeService {
    
	private final ServiceUtil serviceUtil;
    private  HotelCompositeIntegration integration;

    @Autowired
    public HotelCompositeServiceImpl(ServiceUtil serviceUtil, HotelCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }
	
	@Override
	public HotelAggregate getHotel(int hotelId) {
		Hotel hotel = integration.getHotel(hotelId);
        if (hotel == null) throw new NotFoundException("No hotel found for hotelId: " + hotelId);

        List<Location> location = integration.getLocation(hotelId);

        List<Review> reviews = integration.getReviews(hotelId);
        
        List<Room> room = integration.getRoom(hotelId);

        return createHotelAggregate(hotel, location, reviews, room, serviceUtil.getServiceAddress());
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
