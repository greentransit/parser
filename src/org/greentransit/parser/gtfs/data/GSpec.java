package org.greentransit.parser.gtfs.data;

import java.util.List;
import java.util.Map;


/**
 * Holds an entire GTFS specification. See https://developers.google.com/transit/gtfs/reference for details.
 * 
 * @author Mathieu Méa
 */
public class GSpec {
	// List<GtfsAgency> agencies;
	public Map<String, GStop> stops;
	public Map<String, GRoute> routes;
	public Map<String, GTrip> trips;
	public List<GStopTime> stopTimes;
	// Map<String, RouteStop> routeStops;
	public Map<String, GTripStop> tripStops;

	public GSpec(/*List<GtfsAgency> agencies,*/ Map<String, GStop> stops, Map<String, GRoute> routes, Map<String, GTrip> trips, List<GStopTime> stopTimes) {
		// this.agencies = agencies;
		this.stops = stops;
		this.routes = routes;
		this.trips = trips;
		this.stopTimes = stopTimes;
	}

	// public void setRouteStops(Map<String, RouteStop> routeStops) {
	// this.routeStops = routeStops;
	// }

//	public void setTripStops(Map<String, GTripStop> tripStops) {
//		this.tripStops = tripStops;
//	}
}
