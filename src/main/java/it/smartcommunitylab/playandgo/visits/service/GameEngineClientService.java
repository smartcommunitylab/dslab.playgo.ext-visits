package it.smartcommunitylab.playandgo.visits.service;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.logging.Log;
import it.smartcommunitylab.playandgo.visits.model.Campaign;
import it.smartcommunitylab.playandgo.visits.model.ChallengeAssignment;
import it.smartcommunitylab.playandgo.visits.model.ExecutionData;

@ApplicationScoped
public class GameEngineClientService {

	private HttpClient httpClient;
	
	@Inject
	@RestClient
	PlayGoEngineClientService engineClient;

	private static ObjectMapper mapper = new ObjectMapper(); {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	private Map<String, Campaign> campaignMap = new HashMap<>();
	
	@ConfigProperty(name = "ge.endpoint")
	private String geEndpoint;
	@ConfigProperty(name = "ge.username")
	private String geUsername;
	@ConfigProperty(name = "ge.password")
	private String gePassword;

	@PostConstruct
	public void init() {
		httpClient = HttpClient.newBuilder()
				.authenticator(new Authenticator() {
					@Override
				    protected PasswordAuthentication getPasswordAuthentication() {
				      return new PasswordAuthentication(
				        geUsername, 
				        gePassword.toCharArray());
				    }
				})
	            .version(HttpClient.Version.HTTP_2)
	            .build();
	}
	
	public void sendLocationVisit(String campaignId, String poiId, String type, String playerId, Date date) throws Exception {
		ExecutionData ed = new ExecutionData();
		ed.setGameId(getCampaign(campaignId).getGameId());
		ed.setPlayerId(playerId);
		ed.setActionId("point_interest_reached");
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("poi", poiId);
		data.put("typePoi", type);
		ed.setData(data);
		ed.setExecutionMoment(date);			

		String content = mapper.writeValueAsString(ed);
		
		HttpResponse<String> response = httpClient.send(HttpRequest
				.newBuilder()
				.uri(URI.create(geEndpoint + "/gengine/execute"))
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(content))
                .build(), 
                HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			Log.error("Error communication with GE: " + response.body());
			throw new Exception(response.body());
		}
		
	}
	
	public void sendVisitChallengesOnCreate(String campaignId, String type, String name, String weblink, Double target, Double bonus, String playerId, Date date) throws Exception {
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("bonusPointType", "green leaves");
		data.put("bonusScore", bonus);
		data.put("target", target);
		data.put("typePoi", type);
		data.put("category", name);
		data.put("weblink", weblink);
		data.put("periodName", "weekly");
		
		ChallengeAssignment challenge = new ChallengeAssignment();
		long now = System.currentTimeMillis();
		LocalDateTime ldt = LocalDateTime.now().plusDays(7).with(ChronoField.DAY_OF_WEEK, 1).truncatedTo(ChronoUnit.DAYS).minusSeconds(1);
		Date end = new Date(ldt.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli());		
		
		challenge.setStart(new Date(now));
		challenge.setEnd(end);

		challenge.setModelName("visitPointInterest");
		challenge.setInstanceName("'visitPointInterest" + Long.toHexString(now) + "-" + Integer.toHexString((playerId + campaignId).hashCode()));
		
		challenge.setData(data);
		
		String content = mapper.writeValueAsString(challenge);
		
		HttpResponse<String> response = httpClient.send(HttpRequest
				.newBuilder()
				.uri(URI.create(geEndpoint + "/data/game/" + getCampaign(campaignId).getGameId() + "/player/" + playerId + "/challenges"))
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(content))
                .build(), 
                HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			Log.error("Error communication with GE: " + response.body());
			throw new Exception(response.body());
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getVisited(String campaignId, String slug, String userId) throws Exception {
		Campaign c = getCampaign(campaignId);
		if (c != null) {
			HttpResponse<String> response = httpClient.send(HttpRequest
					.newBuilder()
					.uri(URI.create(geEndpoint + "/data/game/" + c.getGameId() + "/player/" + userId))
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.GET()
	                .build(), 
	                HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				Log.error("Error communication with GE: " + response.body());
				return Collections.emptyList();
			}
			String body = response.body();
			Map<String, ?> v = mapper.readValue(body, Map.class);
			v = (Map<String, ?>)v.get("state");
			List<Map<String, ?>> collections = (List<Map<String, ?>>) v.get("BadgeCollectionConcept");
			for (Map<String, ?> o : collections) {
				if (slug.equals(o.get("name"))) {
					List<String> list = (List<String>) o.get("badgeEarned");
					return list;
				}
			}
		}
		return Collections.emptyList();
	}
	
	public boolean isRegistered(String campaignId, String playerId) {
		Campaign c = getCampaign(campaignId);
		if (c == null) {
			return true;
		}
		HttpResponse<String> response = null;
		try {
			response = httpClient.send(HttpRequest
					.newBuilder()
					.uri(URI.create(geEndpoint + "/data/game/" + c.getGameId() + "/player/" + playerId +"/challenges"))
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.GET()
			        .build(), 
			        HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				Log.error("Error communication with GE: " + response.body());
				return true;
			}
			String body = response.body();
			List<?> v = mapper.readValue(body, List.class);
			return v.size() > 0;
		} catch (Exception e) {
			Log.error("Error communication with GE: " + response.body(), e);
			return true;
		}
	}

	
	protected Campaign getCampaign(String id) {
		if (!campaignMap.containsKey(id)) {
			campaignMap.put(id, engineClient.getCampaign(id));
		}
		return campaignMap.get(id);
	}

}
