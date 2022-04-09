package api.core.hotel;

import java.sql.Date;

public class Hotel {
    private final int hotelId;
    private final String title;
	private final String description;
	private final String image;
    private final Date createdOn;	
    private final String serviceAddress;

    public Hotel() {
        hotelId = 0;
        title = null;
        description = null;
        image = null;
        createdOn = null;
        serviceAddress = null;
    }

    public Hotel(
		int hotelId,
		String title,
        String description,
        String image,
		Date createdOn,
		String serviceAddress) {
    	
    	this.hotelId = hotelId;
    	this.title = title;
        this.description = description;
        this.image = image;
    	this.createdOn = createdOn;
    	this.serviceAddress = serviceAddress;
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
    
    public String getServiceAddress() {
        return serviceAddress;
    }
}
