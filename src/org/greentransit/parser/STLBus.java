package org.greentransit.parser;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.greentransit.parser.gtfs.GAgencyTools;
import org.greentransit.parser.gtfs.GReader;
import org.greentransit.parser.gtfs.data.GCalendarDate;
import org.greentransit.parser.gtfs.data.GRoute;
import org.greentransit.parser.gtfs.data.GSpec;
import org.greentransit.parser.gtfs.data.GStop;
import org.greentransit.parser.gtfs.data.GStopTime;
import org.greentransit.parser.gtfs.data.GTrip;
import org.greentransit.parser.my.MGenerator;
import org.greentransit.parser.my.data.MDirectionType;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MTrip;
import org.greentransit.parser.my.data.MTripStop;

// http://www.stl.laval.qc.ca/opendata/GTF_STL.zip
public class STLBus implements GAgencyTools {

	public static final String ROUTE_ID_FILTER = "JANV13";
	public static final String ROUTE_TYPE_FILTER = "3"; // bus only
	public static final String SERVICE_ID_FILTER = "JANV13"; // TODO use
															 // calendar
	public static final String STOP_ID_FILTER = SERVICE_ID_FILTER;
	public static final int THREAD_POOL_SIZE = 2;

	public static void main(String[] args) {
		new STLBus().start(args);
	}

	public void start(String[] args) {
		System.out.printf("Generating STL bus data...\n");
		long start = System.currentTimeMillis();
		// GTFS parsing
		GSpec gtfs = GReader.readGtfsZipFile(args[0], this);
		gtfs.tripStops = GReader.extractTripStops(gtfs);
		Map<Integer, GSpec> gtfsByMRouteId = GReader.splitByRouteId(gtfs, this);
		// Objects generation
		MSpec mSpec = MGenerator.generateMSpec(gtfsByMRouteId, gtfs.stops, this);
		// Dump to files
		MGenerator.dumpFiles(mSpec, args[1], args[2]);
		System.out.printf("Generating STL bus data... DONE in %d seconds\n", ((System.currentTimeMillis() - start) / 1000));
		// System.exit(0);
	}

	@Override
	public int getThreadPoolSize() {
		return THREAD_POOL_SIZE;
	}

	@Override
	public int getRouteId(GRoute gRoute) {
		return Integer.valueOf(gRoute.route_short_name);
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		return gRoute.route_short_name;
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		// AOUT1212E,STL,12,Direction
		// Pont-Viau,3,http://www.stl.laval.qc.ca/lang/fr/horaires-et-trajets/?page_id=2361&route_id=12E,Pont-Viau,CC9900,000000
		return MSpec.cleanLabel(gRoute.route_short_name);
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		return gRoute.route_color;
	}

	@Override
	public String getRouteTextColor(GRoute gRoute) {
		return gRoute.route_text_color;
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (ROUTE_TYPE_FILTER != null && !gRoute.route_type.equals(ROUTE_TYPE_FILTER)) {
			return true;
		}
		if (ROUTE_ID_FILTER != null && !gRoute.route_id.startsWith(ROUTE_ID_FILTER)) {
			return true;
		}
		return false;
	}

	// @Override
	public int getTripId(GTrip gTrip) {
		// trip_id AOUT1212E2D10222190529 => 12E
		// route_id AOUT1212E => 12E
		String route = gTrip.route_id.substring(ROUTE_ID_FILTER.length(), gTrip.route_id.length() - 1);
		int direction = getDirection(gTrip).intValue();
		return Integer.valueOf(route + "0" + direction);
	}

	public static void mainTest(String[] args) {
		String filter = "AOUT12";
		String string = "AOUT1212E";
		System.out.println(string.substring(filter.length(), string.length() - 1));
	}

	@Override
	public void setTripHeadsign(MTrip mTrip, GTrip gTrip/*
														 * ,
														 * Collection<MTripStop>
														 * mTripStops
														 */) {
		// TODO use trip head sign from route_long_name?
		mTrip.setHeadsignDirection(getDirection(gTrip));
	}

	public static MDirectionType getDirection(GTrip gTrip) {
		// AOUT1212E => E
		// AOUT1212O => O => W
		return MDirectionType.parse(gTrip.route_id.substring(gTrip.route_id.length() - 1));
	}

	@Override
	public int mergeTrip(MTripStop ts1, MTripStop ts2, List<MTripStop> l1, List<MTripStop> l2, int i1, int i2) {
		int result = 0;
		// TODO?
		System.out.println("Have to resolve: " + ts1.tripIdString + "," + ts1.stopId + "," + ts2.stopId);
		System.out.println("l1:" + l1.toString());
		System.out.println("l2:" + l2.toString());
		System.exit(-1);
		return result;
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (SERVICE_ID_FILTER != null && !gTrip.service_id.contains(SERVICE_ID_FILTER)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean excludeCalendarDates(GCalendarDate gCalendarDates) {
		if (SERVICE_ID_FILTER != null && !gCalendarDates.service_id.contains(SERVICE_ID_FILTER)) {
			return true;
		}
		return false;
	}

	@Override
	public int getStopId(GStop gStop) {
		return Integer.valueOf(gStop.stop_code);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return gStop.stop_code;
	}

	private static final Pattern REMOVE_STOP_CODE_STOP_NAME = Pattern.compile("\\[[0-9]{5}\\]");

	@Override
	public String cleanStopName(String gStopName) {
		String result = gStopName;
		// Terminus Henri-Bourassa Quai:6 [20011] => Terminus Henri-Bourassa
		// Quai:6
		result = REMOVE_STOP_CODE_STOP_NAME.matcher(result).replaceAll("");

		return MSpec.cleanLabel(result);
	}

	@Override
	public boolean excludeStop(GStop gStop) {
		if (STOP_ID_FILTER != null && !gStop.stop_id.contains(STOP_ID_FILTER)) {
			return true;
		}
		return false;
	}

	// public static String cleanStop_id(String stop_id) {
	// return stop_id.substring(stop_id.length() - 5); // keep the last 5
	// characters
	// }
	//
	// public static String cleanRoute_id(String route_id) {
	// return route_id.substring(6); // ignore the 6 firsts digits
	// }

	// private static final Pattern EXTRACT_ROUTE_ID_FROM_TRIP_ID = Pattern
	// .compile("\\d{1,3}[a-zA-Z]{1}");

	// public static String extractRoute_idFromTrip_id(String trip_id) {
	// // ignore the 6 firsts digits
	// Matcher m = EXTRACT_ROUTE_ID_FROM_TRIP_ID.matcher(trip_id.substring(6));
	// if (m.find()) {
	// return m.group();
	// }
	// return null;// ?
	// }

	// // TEST
	// public static void main2(String[] args) {
	// String string = "JANV1212E2D13523180531";
	// System.out.println(extractRoute_idFromTrip_id(string));
	// }
	
	@Override
	public int getDepartureTime(GStopTime gStopTime) {
		return Integer.valueOf(gStopTime.departure_time.replaceAll(":", ""));
	}

}
