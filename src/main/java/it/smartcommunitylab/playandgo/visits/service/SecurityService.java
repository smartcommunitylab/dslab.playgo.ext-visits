package it.smartcommunitylab.playandgo.visits.service;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SecurityService {

	@ConfigProperty(name = "security.apikey")
	private String apiKey;

	public boolean checkApiKey(String key) {
		return apiKey.equals(key);
	}
}
