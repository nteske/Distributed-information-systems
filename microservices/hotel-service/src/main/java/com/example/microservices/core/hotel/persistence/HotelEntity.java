package com.example.microservices.core.hotel.persistence;
import java.sql.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="hotels")
public class HotelEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private int hotelId;

    private String title;
	private String description;
    private String image;
    private Date createdOn;	

    public HotelEntity() {
    }

    public HotelEntity(
    	int hotelId,
    	String title,
    	String description,
        String image,
        Date createdOn) {

    	this.hotelId = hotelId;
    	this.title = title;
    	this.description = description;
    	this.image = image;
    	this.createdOn = createdOn;
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

	public void setId(String id) {
        this.id = id;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

	public void setReleaseDate(Date createdOn) {
		this.createdOn = createdOn;
	}
}