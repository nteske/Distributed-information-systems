package com.example.api.composite.hotel;

public class RoomSummary {

    private final int roomId;
    private final int roomNumber;
    private final int beds;
	private final float price;

    public RoomSummary() {
        this.roomId = 0;
        this.roomNumber = 0;
        this.beds = 0;
        this.price = 0;
    }

    public RoomSummary(int roomId, int roomNumber, int beds, float price) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.beds = beds;
        this.price = price;
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
}
