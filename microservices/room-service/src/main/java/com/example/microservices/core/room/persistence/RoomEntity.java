package com.example.microservices.core.room.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection="rooms")
@CompoundIndex(name = "hot-roo-id", unique = true, def = "{'hotelId': 1, 'roomId' : 1}")
public class RoomEntity {

    @Id
    private String id;

    @Version
    private Integer version;
    
    private int hotelId;
    private int roomId;

    private int roomNumber;
    private int beds;
    private float price;

    public RoomEntity() {
    }

    public RoomEntity(
		int hotelId,
    	int roomId,
    	int roomNumber,
    	int beds,
        float price) {
    	
    	this.hotelId = hotelId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.beds = beds;
        this.price = price;
    }

    @Override
    public String toString() {
        return format("RoomEntity: %s/%d", hotelId, roomId);
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public int getHotelId() {
		return hotelId;
	}

	public void setHotelId(int hotelId) {
		this.hotelId = hotelId;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(int roomNumber) {
		this.roomNumber = roomNumber;
	}

	public int getBeds() {
		return beds;
	}

	public void setBeds(int beds) {
		this.beds = beds;
	}

    public float getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}
}