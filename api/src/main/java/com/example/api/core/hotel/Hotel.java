package com.example.api.core.hotel;

import java.util.Date;

public class Hotel {
    private int hotelId;
    private String title;
	private String description;
	private String image;
    private Date createdOn;	
    private String serviceAddress;

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

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
