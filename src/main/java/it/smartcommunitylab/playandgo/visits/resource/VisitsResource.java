package it.smartcommunitylab.playandgo.visits.resource;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import it.smartcommunitylab.playandgo.visits.model.TrackedInstanceInfo;
import it.smartcommunitylab.playandgo.visits.service.GameEngineClientService;
import it.smartcommunitylab.playandgo.visits.service.LocationVisitValidator;
import it.smartcommunitylab.playandgo.visits.service.LocationVisitValidator.POI;
import it.smartcommunitylab.playandgo.visits.service.PlayGoEngineClientService;

@Path("/api")
public class VisitsResource {

	@RestClient
	@Inject
	private PlayGoEngineClientService service;

	@Inject
	private LocationVisitValidator validator;

	@Inject
	GameEngineClientService geService;
	
    @GET
    @Path("/visits")
    public void hello(@QueryParam String campaignId, @QueryParam String playerId, @QueryParam String trackedInstanceId) {
    	
    	TrackedInstanceInfo info = service.getTrackedInstanceInfo(campaignId, playerId, trackedInstanceId);
    	Set<POI> points = validator.checkVisitedPoints(campaignId, info.getPolyline(), info.getStartTime());
    	for (POI poi: points) {
    		try {
				geService.sendLocationVisit(campaignId, poi.poiId, poi.poiType, playerId, info.getStartTime());
			} catch (Exception e) {
				// suppress
			}
    	}
    }
}