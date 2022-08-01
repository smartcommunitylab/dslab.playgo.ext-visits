package it.smartcommunitylab.playandgo.visits.model;

public class Geolocation {

	private Double latitude;
	private Double longitude;

	public Geolocation() {
	}
	
    public Geolocation(Double latitude, Double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}
    

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

	
	
  
    @Override
    public String toString() {
    	return latitude + "," + longitude;
    }
    
}

