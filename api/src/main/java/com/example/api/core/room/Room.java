package api.core.room;

public class Room {
    private final int hotelId;
    private final int roomId;
    private final int roomNumber;
    private final int beds;
	private final float price;
    private final String serviceAddress;

    public Room() {
    	hotelId = 0;
    	roomId = 0;
    	roomNumber = 0;
        beds = 0;
        price = 0;
        serviceAddress = null;
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
