package it.smartcommunitylab.playandgo.visits.resource;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.smartcommunitylab.playandgo.visits.model.EventModel;
import it.smartcommunitylab.playandgo.visits.model.TrackedInstanceInfo;
import it.smartcommunitylab.playandgo.visits.service.GameEngineClientService;
import it.smartcommunitylab.playandgo.visits.service.LocationVisitValidator;
import it.smartcommunitylab.playandgo.visits.service.LocationVisitValidator.POI;
import it.smartcommunitylab.playandgo.visits.service.LocationVisitValidator.Sfida;
import it.smartcommunitylab.playandgo.visits.service.PlayGoEngineClientService;
import it.smartcommunitylab.playandgo.visits.service.SecurityService;

@Path("/api")
public class VisitsResource {

	private static final Logger logger = LoggerFactory.getLogger(VisitsResource.class);

	@RestClient
	@Inject
	private PlayGoEngineClientService service;

	@Inject
	private LocationVisitValidator validator;

	@Inject
	GameEngineClientService geService;
	
	@Inject
	SecurityService securityService;

	@ConfigProperty(name = "app.exturl")
	private String extUrl;

    @POST
    @Path("/events")
    public void events(@QueryParam String apiKey, EventModel event) {

    	if (!securityService.checkApiKey(apiKey)) throw new SecurityException("Invalid API Key");
    	
    	switch(event.getEventType()) {
    		case "validTrack": {
    			processValidTrackEvent(event);
    			break;
    		}
    		case "register": {
    			processRegistrationEvent(event);
    			break;
    		}
    	}
    }

    @POST
    @Path("/reset")
    public void resetConf(@QueryParam String apiKey) {

    	if (!securityService.checkApiKey(apiKey)) throw new SecurityException("Invalid API Key");
    	validator.reset();
    	
    }
    
    @GET
    @Path("/visited/{campaignId}/{type}/{userId}")
    public Response visited(@PathParam("campaignId") String campaignId, @PathParam("type") String type, @PathParam("userId") String userId) {
    	Optional<Sfida> challenge = validator.getChallenges(campaignId).stream().filter(s -> s.slug.equals(type)).findAny();
    	if (challenge.isPresent()) {
        	try {
    			List<String> visitedPois = geService.getVisited(campaignId, type, userId);
    			String weblink = challenge.get().weblink;
    			if (weblink != null) {
    				weblink += "?visited="+ visitedPois.stream().collect(Collectors.joining(","));
    				return Response.status(302).location(URI.create(weblink)).build();
    			}
    		} catch (Exception e) {
    		}
    	}
    	return Response.noContent().build();
    }
    

	private void processRegistrationEvent(EventModel event) {
		if (geService.isRegistered(event.getCampaignId(), event.getPlayerId())) {
			logger.warn("User {} already registered for campaign {}", event.getPlayerId(), event.getCampaignId());
			return;
		}
		Collection<Sfida> challenges = validator.getChallenges(event.getCampaignId());
		for (Sfida s : challenges) {
			try {
				String weblink = createLink(event.getCampaignId(), s.slug, event.getPlayerId());
				geService.sendVisitChallengesOnCreate(event.getCampaignId(), s.slug, s.nome, weblink, 5.0, 100.0, event.getPlayerId(), new Date());
				geService.sendVisitChallengesOnCreate(event.getCampaignId(), s.slug, s.nome, weblink, 15.0, 200.0, event.getPlayerId(), new Date());
				geService.sendVisitChallengesOnCreate(event.getCampaignId(), s.slug, s.nome, weblink, 25.0, 300.0, event.getPlayerId(), new Date());
			} catch (Exception e) {
				logger.warn("Error processing event: " + event.toString()+" ("+e.getMessage()+")");
			}
		} 
	}

	private String createLink(String campaignId, String slug, String playerId) {
		return extUrl + (extUrl.endsWith("/") ? "" : "/") + "visited/" + campaignId +"/" + slug +"/" + playerId;
	}

	private void processValidTrackEvent(EventModel event) {
		TrackedInstanceInfo info = service.getTrackedInstanceInfo(event.getCampaignId(), event.getPlayerId(), (String)event.getData().get("trackedInstanceId"));
    	if (info != null) {
        	Set<POI> points = validator.checkVisitedPoints(event.getCampaignId(), info.getPolyline(), info.getStartTime());
        	for (POI poi: points) {
        		try {
    				geService.sendLocationVisit(event.getCampaignId(), poi.poiId, poi.poiType, event.getPlayerId(), info.getStartTime());
    			} catch (Exception e) {
    				logger.warn("Error processing event: " + event.toString()+" ("+e.getMessage()+")");
    			}
        	}
    		
    	}
	}
}