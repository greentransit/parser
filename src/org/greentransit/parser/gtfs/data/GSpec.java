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
	public Map<String, GCalendarDate> calendarDates;
	public Map<String, GStop> stops;
	public Map<String, GRoute> routes;
	public Map<String, GTrip> trips;
	public List<GStopTime> stopTimes;
	// Map<String, RouteStop> routeStops;

	// NOT IN GTFS
	public Map<String, GTripStop> tripStops;
	public Map<String, GService> services;

	public GSpec(/* List<GtfsAgency> agencies, */Map<String, GCalendarDate> calendarDates, Map<String, GStop> stops, Map<String, GRoute> routes, Map<String, GTrip> trips, List<GStopTime> stopTimes) {
		// this.agencies = agencies;
		this.calendarDates = calendarDates;
		this.stops = stops;
		this.routes = routes;
		this.trips = trips;
		this.stopTimes = stopTimes;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(calendarDates == null ? null : calendarDates.size()).append('\'').append(',') //
				.append('\'').append(stops == null ? null : stops.size()).append('\'').append(',') //
				.append('\'').append(routes == null ? null : routes.size()).append('\'').append(',') //
				.append('\'').append(trips == null ? null : trips.size()).append('\'').append(',') //
				.append('\'').append(stopTimes == null ? null : stopTimes.size()).append('\'').append(',') //
				.append('\'').append(tripStops == null ? null : tripStops.size()).append('\'').append(',') //
				.append('\'').append(services == null ? null : services.size()).append('\'').append(',') //
				.toString();
	}

	// public void setRouteStops(Map<String, RouteStop> routeStops) {
	// this.routeStops = routeStops;
	// }

	// public void setTripStops(Map<String, GTripStop> tripStops) {
	// this.tripStops = tripStops;
	// }
}
