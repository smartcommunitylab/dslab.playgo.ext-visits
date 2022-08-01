package it.smartcommunitylab.playandgo.visits.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrackedInstanceInfo {
	
	public enum TravelValidity {
		VALID, INVALID, PENDING
	}

	private String trackedInstanceId;
	private String clientId;
	private String multimodalId;
	private Date startTime;
	private Date endTime;
	private String modeType;
	private double distance = 0.0; // meters
	private TravelValidity validity;
	private List<CampaignTripInfo> campaigns = new ArrayList<>();
	private String polyline;
	
	public TravelValidity getValidity() {
		return validity;
	}
	public void setValidity(TravelValidity validity) {
		this.validity = validity;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public List<CampaignTripInfo> getCampaigns() {
		return campaigns;
	}
	public void setCampaigns(List<CampaignTripInfo> campaigns) {
		this.campaigns = campaigns;
	}
	public String getTrackedInstanceId() {
		return trackedInstanceId;
	}
	public void setTrackedInstanceId(String trackedInstanceId) {
		this.trackedInstanceId = trackedInstanceId;
	}
	public String getMultimodalId() {
		return multimodalId;
	}
	public void setMultimodalId(String multimodalId) {
		this.multimodalId = multimodalId;
	}
	public String getModeType() {
		return modeType;
	}
	public void setModeType(String modeType) {
		this.modeType = modeType;
	}
	public String getPolyline() {
		return polyline;
	}
	public void setPolyline(String polyline) {
		this.polyline = polyline;
	}
	public String getClientId() {
		return clientId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
}
