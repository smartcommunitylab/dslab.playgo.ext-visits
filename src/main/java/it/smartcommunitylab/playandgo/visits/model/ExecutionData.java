package it.smartcommunitylab.playandgo.visits.model;

import java.util.Date;
import java.util.Map;

public class ExecutionData {
    public String gameId;
    public String actionId;
    public String playerId;
    public Map<String, Object> data;
    /*
     * date can be serialized either as millis timestamp or as ISO 8601 date string representation
     */
    private Date executionMoment;

    public ExecutionData() {}

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
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

    public Date getExecutionMoment() {
        return executionMoment;
    }

    public void setExecutionMoment(Date executionMoment) {
        this.executionMoment = executionMoment;
    }

}