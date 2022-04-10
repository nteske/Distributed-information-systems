package com.example.api.core.room;

public class Room {
    private int hotelId;
    private int roomId;
    private int roomNumber;
    private int beds;
	private float price;
    private String serviceAddress;

    public Room() {
    	hotelId = 0;
    	roomId = 0;
    	roomNumber = 0;
        beds = 0;
        price = 0;
        serviceAddress = null;
    }

    public void setHotelId(int hotelId) {
		this.hotelId = hotelId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public void setRoomNumber(int roomNumber) {
		this.roomNumber = roomNumber;
	}

	public void setBeds(int beds) {
		this.beds = beds;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public Room(
    	int hotelId,
    	int roomId,
    	int roomNumber,
    	int beds,
    	float price,
    	String serviceAddress) {
    	
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.beds = beds;
        this.price = price;
        this.serviceAddress = serviceAddress;
    }

    public int getHotelId() {
		return hotelId;
	}

	public int getRoomId() {
		return roomId;
	}

	public int getRoomNumber() {
		return roomNumber;
	}

	public int getBeds() {
		return beds;
	}

	public float getPrice() {
		return price;
	}

	public String getServiceAddress() {
        return serviceAddress;
    }
}
