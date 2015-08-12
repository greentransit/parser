package org.greentransit.parser.my.data;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
	public Map<Integer, List<MSchedule>> stopSchedules;

	public MSpec(List<MStop> stops, List<MRoute> routes, List<MTrip> trips, List<MTripStop> tripStops, List<MServiceDate> serviceDates, //
			Map<Integer, List<MSchedule>> routeSchedules, // Map<Integer, List<MSchedule>> routeSchedules) {
			Map<Integer, List<MSchedule>> stopSchedules) {
		this.stops = stops;
		this.routes = routes;
		this.trips = trips;
		this.tripStops = tripStops;
		this.serviceDates = serviceDates;
		this.stopSchedules = stopSchedules;
	}

	public static final Pattern CLEAN_SLASHES = Pattern.compile("(\\w)[\\s]*[/][\\s]*(\\w)");
	public static final String CLEAN_SLASHES_REPLACEMENT = "$1 / $2";

	private static final CharSequenceTranslator ESCAPE = new LookupTranslator(new String[][] { { "\'", "\'\'" }, { "_", "" } });

	public static String escape(String string) {
		return ESCAPE.translate(string);
	}

	// cleanup route name, stop name...
	public static String cleanLabel(String result) {
		// remove double white-spaces
		result = result.replaceAll("\\s+", " ");
		// cLean-Up tHe caPItalIsaTIon
		result = WordUtils.capitalize/* Fully */(result, ' ', '-', '/', '\'', '(');
		return result.trim();
	}

	public static String removeStartWith(String string, String[] removeChars, int keepLast) {
		if (string == null || string.length() == 0) {
			return string;
		}
		if (removeChars != null) {
			for (String removeChar : removeChars) {
				if (string.startsWith(removeChar)) {
					return string.substring(removeChar.length() - keepLast);
				}
			}
		}
		return string;
	}

	public static String replaceAll(String string, String[] replaceChars, String replacement) {
		if (string == null || string.length() == 0) {
			return string;
		}
		if (replaceChars != null) {
			for (String replaceChar : replaceChars) {
				string = string.replace(replaceChar, replacement);
			}
		}
		return string;
	}

	public static void mainTest(String[] args) {
		String string = "D'Iberville";
		System.out.println(string + " : " + escape(string));
	}
}
