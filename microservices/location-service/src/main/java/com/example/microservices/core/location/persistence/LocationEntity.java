package com.example.microservices.core.location.persistence;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="location")
@CompoundIndex(name = "hot-loc-id", unique = true, def = "{'hotelId': 1, 'locationId' : 1}")
public class LocationEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    private int hotelId;
    private int locationId;


    private String country;
    private String town;
    private String address;

    public LocationEntity() {
    }

    public LocationEntity(
		int hotelId,
    	int locationId,
    	String country,
    	String town,
    	String address) {

    	this.hotelId = hotelId;
        this.locationId = locationId;
        this.country = country;
        this.town = town;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
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

	public void setId(String id) {
        this.id = id;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
}