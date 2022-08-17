package it.smartcommunitylab.playandgo.visits.resource;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.smartcommunitylab.playandgo.visits.model.EventModel;
import it.smartcommunitylab.playandgo.visits.model.TrackedInstanceInfo;
import it.smartcommunitylab.playandgo.visits.service.GameEngineClientService;
import it.smartcommunitylab.playandgo.visits.service.LocationVisitValidator;
import it.smartcommunitylab.playandgo.visits.service.LocationVisitValidator.POI;
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

	private void processRegistrationEvent(EventModel event) {
		// TODO Auto-generated method stub
		
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