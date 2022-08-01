/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package it.smartcommunitylab.playandgo.visits.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.smartcommunitylab.playandgo.visits.model.Geolocation;


/**
 * @author raman
 *
 */
@ApplicationScoped
public class LocationVisitValidator {
	
	private final static int EARTH_RADIUS = 6371; // Earth radius in km.

	private static final Logger logger = LoggerFactory.getLogger(LocationVisitValidator.class);

	private static final double RADIUS = 0.05;

	@ConfigProperty(name = "campaign.visit.source")
	private String campaignVisitSource;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private Map<String, Map<Periodo, List<POI>>> periodMap = new HashMap<>();
	
	private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
	
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		if (!periodMap.isEmpty()) return;
		try {
			Map<String, ?> map = readValue(campaignVisitSource, Map.class);
			map.entrySet().forEach(e -> {
				Map<Periodo, List<POI>> periods = new HashMap<>();
				periodMap.put(e.getKey(), periods);
				try {
					readConfig((String)e.getValue(), periods);
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private <T> T readValue(String url, Class<T> cls) throws Exception {
		HttpResponse<String> send = httpClient.send(
				HttpRequest
					.newBuilder()
					.GET()
					.uri(URI.create(campaignVisitSource))
                    .build(), 
                    HttpResponse.BodyHandlers.ofString());
		
		T value = mapper.readValue(send.body(), cls);
		return value;
	}
	
	@SuppressWarnings("unchecked")
	private void readConfig(String locationConfigUrl, Map<Periodo, List<POI>> periods) throws Exception {
		
		CalendarData data = readValue(locationConfigUrl + "Calendario.json", CalendarData.class);
		if (data.sfide != null) {
			Map<String, List<POI>> poiMap = new HashMap<>();
			for (Sfida sfida : data.sfide) {
				List<POI> allPois = new LinkedList<>();
				for (Categoria cat : sfida.categorie) {
					if (!poiMap.containsKey(cat.nome)) {
						List<POI> poiList = new LinkedList<>();
						Map<String, Object> geoJson = readValue(locationConfigUrl + cat.nome +".json", Map.class);
						List<Object> list = (List<Object>) geoJson.get("features");
						for (Object f : list) {
							Map<String, Object> fm = (Map<String, Object>) f;
							POI poi = new POI();
							poi.poiId = (String) fm.get("id");
							poi.poiType = sfida.slug;
							Map<String, Object> geom = (Map<String, Object>) fm.get("geometry");
							List<Double> coords = (List<Double>) geom.get("coordinates");
							poi.lat = coords.get(1);
							poi.lng = coords.get(0);
							poiList.add(poi);
						}
						poiMap.put(cat.nome, poiList);
					}
					allPois.addAll(poiMap.get(cat.nome));
				}
				sfida.periodo.dalDate = LocalDate.parse(sfida.periodo.dal);
				sfida.periodo.alDate = LocalDate.parse(sfida.periodo.al);
				periods.put(sfida.periodo, allPois);
			}
		}
	}
	
	public Set<POI> checkVisitedPoints(String campaignId, String polyline, Date date) {
		init();
		LocalDate ld = LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
		
		List<Geolocation> trip = decodePoly(polyline);
		Map<Periodo, List<POI>> periods = periodMap.getOrDefault(campaignId, Collections.emptyMap());
		Set<POI> result = new HashSet<>();
		for (Periodo periodo: periods.keySet()) {
			if (periodo.matches(ld)) {
				List<Geolocation> tail = trip.size() > 5 ? trip.subList(trip.size() - 5, trip.size()) : trip;
				List<POI> poiList = periods.get(periodo);
				for (Geolocation point : tail) {
					for (POI poi: poiList) {
						if (!result.contains(poi)) {
							double dist = harvesineDistance(point.getLatitude(), point.getLongitude(), poi.lat, poi.lng);
							if (dist < RADIUS) {
								result.add(poi);
							}
						}
						
					}
				}
			}
		}
		return result;
	}

	private static List<Geolocation> decodePoly(String leg) {
		List<Geolocation> legPositions = new ArrayList<>();
		int index = 0, len = leg.length();
		int lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = leg.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = leg.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			Geolocation onLeg = new Geolocation();
			onLeg.setLatitude((((double) lat / 1E5)));
			onLeg.setLongitude((((double) lng / 1E5)));
			legPositions.add(onLeg);
		}
		return legPositions;
	}
	
	private static double harvesineDistance(double lat1, double lon1, double lat2, double lon2) {
		lat1 = Math.toRadians(lat1);
		lon1 = Math.toRadians(lon1);
		lat2 = Math.toRadians(lat2);
		lon2 = Math.toRadians(lon2);

		double dlon = lon2 - lon1;
		double dlat = lat2 - lat1;

		double a = Math.pow((Math.sin(dlat / 2)), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c;
	}

	
	public static class CalendarData {
		public List<Sfida> sfide;
	}
	
	public static class Sfida {
		public String nome, slug;
		public List<Categoria> categorie;
		public Periodo periodo;
	}
	
	public static class Categoria {
		public String nome;
	}
	
	public static class Periodo {
		public LocalDate dalDate, alDate;
		public String dal, al;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((al == null) ? 0 : al.hashCode());
			result = prime * result + ((dal == null) ? 0 : dal.hashCode());
			return result;
		}

		/**
		 * @param date
		 * @return
		 */
		public boolean matches(LocalDate date) {
			return !date.isBefore(dalDate) && ! date.isAfter(alDate);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Periodo other = (Periodo) obj;
			if (al == null) {
				if (other.al != null)
					return false;
			} else if (!al.equals(other.al))
				return false;
			if (dal == null) {
				if (other.dal != null)
					return false;
			} else if (!dal.equals(other.dal))
				return false;
			return true;
		} 
		
	}
	
	public static class POI {
		public String poiId, poiType;
		public double lat, lng;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((poiId == null) ? 0 : poiId.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			POI other = (POI) obj;
			if (poiId == null) {
				if (other.poiId != null)
					return false;
			} else if (!poiId.equals(other.poiId))
				return false;
			return true;
		}
		
	}
}
