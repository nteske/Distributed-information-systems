package api.core.review;

import java.sql.Date;

public class Review {
    private final int hotelId;
    private final int reviewId;
    private final int rating;
    private final String description;
    private final Date createdOn;
    private final String serviceAddress;

    public Review() {
        hotelId = 0;
        reviewId = 0;
        rating = 0;
        description = null;
        createdOn = null;
        serviceAddress = null;
    }

    public Review(
    	int hotelId,
    	int reviewId,
    	int rating,
    	String description,
    	Date createdOn,
    	String serviceAddress) {
    	
        this.hotelId = hotelId;
        this.reviewId = reviewId;
        this.rating = rating;
        this.description = description;
        this.createdOn = createdOn;
        this.serviceAddress = serviceAddress;
    }

    public int getHotelId() {
		return hotelId;
	}

	public int getReviewId() {
		return reviewId;
	}

    public int getRating() {
		return rating;
	}

	public String getDescription() {
		return description;
	}

    public Date getCreatedOn() {
		return createdOn;
	}

	public String getServiceAddress() {
        return serviceAddress;
    }
}
