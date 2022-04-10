package com.example.api.composite.hotel;

public class ServiceAddresses {
    private final String cmp;
    private final String hot;
    private final String rev;
    private final String loc;
    private final String roo;

    public ServiceAddresses() {
        cmp = null;
        hot = null;
        rev = null;
        loc = null;
        roo = null;
    }

    public ServiceAddresses(
    	String compositeAddress,
    	String hotelAddress,
    	String reviewAddress,
    	String locationAddress,
    	String roomAddress) {
    	
        this.cmp = compositeAddress;
        this.hot = hotelAddress;
        this.rev = reviewAddress;
        this.loc = locationAddress;
        this.roo = roomAddress;
    }

	public String getCmp() {
		return cmp;
	}

	public String getHot() {
		return hot;
	}

	public String getRev() {
		return rev;
	}

	public String getLoc() {
		return loc;
	}

	public String getRoo() {
		return roo;
	}
}
