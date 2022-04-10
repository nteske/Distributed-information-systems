package com.example.api.core.location;

public class Location {
    private int hotelId;
    private int locationId;

    private String country;
    private String town;
    private String address;

    private String serviceAddress;

    public Location() {
        hotelId = 0;
        locationId = 0;
        country = null;
        town = null;
        address = null;
        serviceAddress = null;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public Location(
    	int hotelId,
    	int locationId,
    	String country,
    	String town,
        String address,
    	String serviceAddress) {
    	
        this.hotelId = hotelId;
        this.locationId = locationId;
        this.country = country;
        this.town = town;
        this.address = address;
        this.serviceAddress = serviceAddress;
    }

    public int getHotelId() {
		return hotelId;
	}

	public int getLocationId() {
		return locationId;
	}

	public String getCountry() {
		return country;
	}

	public String getTown() {
		return town;
	}

	public String getAddress() {
		return address;
	}

	public String getServiceAddress() {
        return serviceAddress;
    }
}
