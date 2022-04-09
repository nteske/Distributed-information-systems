package api.composite.hotel;

public class LocationSummary {
	
	private final int locationId;
    private final String country;
    private final String town;
    private final String address;

    public LocationSummary(int locationId, String country, String town, String address) {
        this.locationId = locationId;
        this.country = country;
        this.town = town;
        this.address = address;
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
}
