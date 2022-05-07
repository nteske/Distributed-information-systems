package com.example.api.composite.hotel;

import java.util.Date;

public class ReviewSummary {

    private final int reviewId;
    private final int rating;
    private final String description;
    private final Date createdOn;

    public ReviewSummary() {
        this.reviewId = 0;
        this.rating = 0;
        this.description = null;
        this.createdOn = null;
    }

    public ReviewSummary(int reviewId, int rating , String description, Date createdOn) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.description = description;
        this.createdOn = createdOn;
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

}
