package org.greentransit.parser.my.data;

import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;

/**
 * Holds an entire specification.
 * 
 * @author Mathieu Méa
 */
public class MSpec {
	public List<MStop> stops;
	public List<MRoute> routes;
	public List<MTrip> trips;
	public List<MTripStop> tripStops;
	public List<MServiceDate> serviceDates;
	public /*HashMap<Integer,*/ List<MSchedule> schedules;

	public MSpec(List<MStop> stops, List<MRoute> routes, List<MTrip> trips, List<MTripStop> tripStops, List<MServiceDate> serviceDates, /*HashMap<Integer,*/ List<MSchedule> schedules) {
		this.stops = stops;
		this.routes = routes;
		this.trips = trips;
		this.tripStops = tripStops;
		this.serviceDates = serviceDates;
		this.schedules = schedules;
	}
	
	private static final CharSequenceTranslator ESCAPE = new LookupTranslator(new String[][] { { "\'", "\'\'" }, });
	
	public static String escape(String string) {
		return ESCAPE.translate(string);
	}

	// cleanup route name, stop name...
	public static String cleanLabel(String label) {
		String result = label;
		// remove double white-spaces
		result = result.replaceAll("\\s+", " ");
		// cLean-Up tHe caPItalIsaTIon
		result = WordUtils.capitalize/*Fully*/(result, ' ', '-', '/', '\'');
		return result.trim();
	}
	
	public static void mainTest(String[] args) {
		String string = "D'Iberville";
		System.out.println(string +" : "+ escape(string));
	}
}
