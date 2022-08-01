package it.smartcommunitylab.playandgo.visits.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import io.quarkus.oidc.client.filter.OidcClientFilter;
import it.smartcommunitylab.playandgo.visits.model.Campaign;
import it.smartcommunitylab.playandgo.visits.model.TrackedInstanceInfo;

@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey="playandgo-engine")
public interface PlayGoEngineClientService {

	@GET
	@Path("/ext/track/{campaignId}/{playerId}/{trackedInstanceId}")
	public TrackedInstanceInfo getTrackedInstanceInfo(@PathParam String campaignId, @PathParam String playerId, @PathParam String trackedInstanceId);
	
	
	@GET
	@Path("/campaign/{campaignId}")
	public Campaign getCampaign(@PathParam String campaignId);

}
