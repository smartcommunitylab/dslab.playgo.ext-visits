package it.smartcommunitylab.playandgo.visits.model;

import java.util.Date;

public class Campaign {
	public static enum Type {
		company, city, school, personal
	};
	
	private String campaignId;
	private Type type;
	private String territoryId;
	private Date dateFrom;
	private Date dateTo;
	private Boolean active = Boolean.FALSE;
	private int startDayOfWeek = 1; //Monday is 1 and Sunday is 7
	private String gameId;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getTerritoryId() {
		return territoryId;
	}

	public void setTerritoryId(String territoryId) {
		this.territoryId = territoryId;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public int getStartDayOfWeek() {
		return startDayOfWeek;
	}

	public void setStartDayOfWeek(int startDayOfWeek) {
		this.startDayOfWeek = startDayOfWeek;
	}


	public boolean currentlyActive() {
		Date now = new Date();
		return !Boolean.FALSE.equals(getActive()) && 
				(getDateFrom() == null || !getDateFrom().after(now)) &&
				(getDateTo() == null || !getDateTo().before(now));
	}
	
	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

}
