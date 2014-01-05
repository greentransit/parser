package org.greentransit.parser;

import java.util.List;
import java.util.Map;

import org.greentransit.parser.gtfs.GAgencyTools;
import org.greentransit.parser.gtfs.GReader;
import org.greentransit.parser.gtfs.data.GCalendarDate;
import org.greentransit.parser.gtfs.data.GRoute;
import org.greentransit.parser.gtfs.data.GSpec;
import org.greentransit.parser.gtfs.data.GStop;
import org.greentransit.parser.gtfs.data.GStopTime;
import org.greentransit.parser.gtfs.data.GTrip;
import org.greentransit.parser.my.MGenerator;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MTrip;
import org.greentransit.parser.my.data.MTripStop;

//http://www.stm.info/fichiers/gtfs/gtfs_stm.zip
public class STMSubway implements GAgencyTools {

	public static final String ROUTE_ID_FILTER = null; // "5"; //
	public static final String ROUTE_TYPE_FILTER = "1"; // subway only
	public static final String SERVICE_ID_FILTER = "14J"; // TODO use calendar
	public static final String STOP_ID_FILTER = null;
	public static final int THREAD_POOL_SIZE = 1; // 4;

	public static void main(String[] args) {
		new STMSubway().start(args);
	}

	public void start(String[] args) {
		System.out.printf("Generating STM subway data...\n");
		long start = System.currentTimeMillis();
		// GTFS parsing
		GSpec gtfs = GReader.readGtfsZipFile(args[0], this);
		gtfs.services = GReader.extractServices(gtfs);
		gtfs.tripStops = GReader.extractTripStops(gtfs);
		Map<Integer, GSpec> gtfsByMRouteId = GReader.splitByRouteId(gtfs, this);
		// Objects generation
		MSpec mSpec = MGenerator.generateMSpec(gtfsByMRouteId, gtfs.stops, this);
		// Dump to files
		MGenerator.dumpFiles(mSpec, args[1], args[2]);

		System.out.printf("Generating STM subway data... DONE in %d seconds\n", ((System.currentTimeMillis() - start) / 1000));
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
		return "";// not used gRoute.route_short_name;
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		return MSpec.cleanLabel(gRoute.route_long_name); // French names are the names!
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
		// return gTrip.route_id + "-" + stationName.substring(0, 2).toUpperCase(Locale.ENGLISH);
		// manual because fix and to group Orange line towards Henri-Bourassa & Montmorency
		String stationName = cleanStopName(gTrip.trip_headsign);
		switch (Integer.valueOf(gTrip.route_id)) {
		case 1: // GREEN
			if (stationName.equalsIgnoreCase("Angrignon")) {
				return 101; // "1-AA";
			} else if (stationName.equalsIgnoreCase("Honoré-Beaugrand")) {
				return 102; // "1-HB";
			}
			break;
		case 2: // ORANGE
			if (stationName.equalsIgnoreCase("Côte-Vertu")) {
				return 201; // "2-CV";
			} else if (stationName.equalsIgnoreCase("Henri-Bourassa") || stationName.equalsIgnoreCase("Montmorency")) {
				return 202; // "2-MM";
			}
			break;
		case 4: // YELLOW
			if (stationName.equalsIgnoreCase("Berri-UQAM")) {
				return 401; // "4-BU";
			} else if (stationName.equalsIgnoreCase("Longueuil-Université de Sherbrooke")) {
				return 402; // "4-LU";
			}
			break;
		case 5: // BLUE
			if (stationName.equalsIgnoreCase("Saint-Michel")) {
				return 501; // "5-SM";
			} else if (stationName.equalsIgnoreCase("Snowdon")) {
				return 502; // "5-SS";
			}
			break;
		}
		System.out.println("Subway direction " + gTrip.trip_headsign + " unexpected!");
		System.exit(-1);
		return 0;
	}

	@Override
	public void setTripHeadsign(MTrip mTrip, GTrip gTrip/* , Collection<MTripStop> mTripStops */) {
		// Station ABC => ABC
		String stationName = cleanStopName(gTrip.trip_headsign);
		// Henri-Bourassa == Montmorency
		if (stationName.equalsIgnoreCase("Henri-Bourassa")) {
			stationName = "Montmorency";
		}
		int directionId = -1;
		if (stationName.equals("Angrignon")) { // green
			directionId = 0;
		} else if (stationName.equals("Honoré-Beaugrand")) { // green
			directionId = 1;
		} else if (stationName.equals("Côte-Vertu")) { // orange
			directionId = 0;
		} else if (stationName.equals("Montmorency")) { // orange
			directionId = 1;
		} else if (stationName.equals("Berri-UQAM")) { // yellow
			directionId = 0;
		} else if (stationName.equals("Longueuil-Université De Sherbrooke")) { // yellow
			directionId = 1;
		} else if (stationName.equals("Saint-Michel")) { // blue
			directionId = 0;
		} else if (stationName.equals("Snowdon")) { // blue
			directionId = 1;
		} else {
			System.out.println("Unexpected station: " + stationName);
			System.exit(-1);
		}
		mTrip.setHeadsignString(stationName, directionId);
	}

	@Override
	public int mergeTrip(MTripStop ts1, MTripStop ts2, List<MTripStop> l1, List<MTripStop> l2, int i1, int i2) {
		System.out.println("Have to resolve: " + ts1.tripIdString + "," + ts1.stopId + "," + ts2.stopId);
		System.out.println("l1:" + l1.toString());
		System.out.println("l2:" + l2.toString());
		System.exit(-1);
		return 0;
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
		return Integer.valueOf(gStop.stop_id);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return ""; // gStop.stop_id;
	}

	// public static final String PLACE_CHAR_PARENTHESE = "(";
	public static final String PLACE_CHAR_STATION = /* PLACE_CHAR_PARENTHESE + */"station ";
	public static final int PLACE_CHAR_STATION_LENGTH = PLACE_CHAR_STATION.length();
	public static final String PLACE_CHAR_STATION_BIG = /* PLACE_CHAR_PARENTHESE + */"Station ";
	public static final int PLACE_CHAR_STATION_BIG_LENGTH = PLACE_CHAR_STATION_BIG.length();

	@Override
	public String cleanStopName(String gStopName) {
		String result = gStopName;
		if (result.startsWith(PLACE_CHAR_STATION)) {
			result = result.substring(PLACE_CHAR_STATION_LENGTH);
		}
		if (result.startsWith(PLACE_CHAR_STATION_BIG)) {
			result = result.substring(PLACE_CHAR_STATION_BIG_LENGTH);
		}
		return MSpec.cleanLabel(result);
	}

	@Override
	public boolean excludeStop(GStop gStop) {
		if (STOP_ID_FILTER != null && !gStop.stop_id.contains(STOP_ID_FILTER)) {
			return true;
		}
		return false;
	}

	public static void mainTest(String[] args) {
		System.out.println("'" + new STMSubway().cleanStopName("Station ABC") + "'");
	}

	@Override
	public int getDepartureTime(GStopTime gStopTime) {
		return Integer.valueOf(gStopTime.departure_time.replaceAll(":", ""));
	}

	@Override
	public int getCalendarDate(GCalendarDate gCalendarDate) {
		return Integer.valueOf(gCalendarDate.date);
	}
}
