package com.example.microservices.core.review.persistence;

import java.util.Date;

import javax.persistence.*;
import static java.lang.String.format;

@Entity
@Table(name = "reviews", indexes = { @Index(name = "reviews_unique_idx", unique = true, columnList = "hotelId,reviewId") })
public class ReviewEntity {

    @Id @GeneratedValue
    private int id;

    @Version
    private int version;

    private int hotelId;
    private int reviewId;
    private int rating;
    private String description;
    private Date createdOn;

    public ReviewEntity() {
    }

    public ReviewEntity(
		int hotelId,
    	int reviewId,
        int rating,
    	String description,
    	Date createdOn)
    {
    	this.hotelId = hotelId;
        this.reviewId = reviewId;
        this.rating = rating;
        this.description = description;
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return format("ReviewEntity: %s/%d", hotelId, reviewId);
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getHotelId() {
		return hotelId;
	}

	public void setHotelId(int hotelId) {
		this.hotelId = hotelId;
	}

	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

    public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
}