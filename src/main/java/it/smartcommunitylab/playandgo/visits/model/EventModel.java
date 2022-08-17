
package it.smartcommunitylab.playandgo.visits.model;

import java.util.Map;

public class EventModel {

	private String campaignId;
	private String playerId;
	private String eventType;
	
	private Long timestamp;
	
	private Map<String, Object> data;
	
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "EventModel [campaignId=" + campaignId + ", playerId=" + playerId + ", eventType=" + eventType
				+ ", data=" + data + "]";
	}
	
}
