package api.composite.hotel;

import java.sql.Date;
import java.util.List;

public class HotelAggregate {
    private final int hotelId;
    private final String title;
	private final String description;
	private final String image;
    private final Date createdOn;
    private final List<LocationSummary> location;
    private final List<ReviewSummary> reviews;
	private final List<RoomSummary> room;
    private final ServiceAddresses serviceAddresses;

    public HotelAggregate(
        int hotelId,
        String title,
        String description,
        String image,
		Date createdOn,
        List<LocationSummary> location,
		List<ReviewSummary> reviews,
        List<RoomSummary> room,
		ServiceAddresses serviceAddresses) {

        this.hotelId = hotelId;
        this.title = title;
        this.description = description;
        this.image = image;
        this.createdOn = createdOn;
        this.location = location;
        this.reviews = reviews;
		this.room = room;
        this.serviceAddresses = serviceAddresses;
    }

    public int getHotelId() {
        return hotelId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
    	return description;
    }
    
    public String getImage() {
    	return image;
    }

    public Date getCreatedOn() {
        return createdOn;
    }
	

    public List<LocationSummary> getLocation() {
        return location;
    }

    public List<ReviewSummary> getReviews() {
        return reviews;
    }
	
	public List<RoomSummary> getRoom() {
        return room;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
}
