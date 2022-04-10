package com.example.api.core.location;

public class Location {
    private final int hotelId;
    private final int locationId;

    private final String country;
    private final String town;
    private final String address;

    private final String serviceAddress;

    public Location() {
        hotelId = 0;
        locationId = 0;
        country = null;
        town = null;
        address = null;
        serviceAddress = null;
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
