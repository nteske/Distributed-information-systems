package com.example.api.core.review;

import java.util.Date;

public class Review {
    private int hotelId;
    private int reviewId;
    private int rating;
    private String description;
    private Date createdOn;
    private String serviceAddress;

    public Review() {
        hotelId = 0;
        reviewId = 0;
        rating = 0;
        description = null;
        createdOn = null;
        serviceAddress = null;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
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
