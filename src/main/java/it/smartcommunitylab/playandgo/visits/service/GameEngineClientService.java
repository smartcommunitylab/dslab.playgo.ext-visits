package it.smartcommunitylab.playandgo.visits.service;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.logging.Log;
import it.smartcommunitylab.playandgo.visits.model.Campaign;
import it.smartcommunitylab.playandgo.visits.model.ExecutionData;

@ApplicationScoped
public class GameEngineClientService {

	private HttpClient httpClient;
	
	@Inject
	@RestClient
	PlayGoEngineClientService engineClient;

	private ObjectMapper mapper = new ObjectMapper();

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
	
	protected Campaign getCampaign(String id) {
		if (!campaignMap.containsKey(id)) {
			campaignMap.put(id, engineClient.getCampaign(id));
		}
		return campaignMap.get(id);
	}
}
