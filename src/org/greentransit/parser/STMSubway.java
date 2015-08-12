package org.greentransit.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.greentransit.parser.gtfs.GAgencyTools;
import org.greentransit.parser.gtfs.GReader;
import org.greentransit.parser.gtfs.data.GCalendar;
import org.greentransit.parser.gtfs.data.GCalendarDate;
import org.greentransit.parser.gtfs.data.GRoute;
import org.greentransit.parser.gtfs.data.GSpec;
import org.greentransit.parser.gtfs.data.GStop;
import org.greentransit.parser.gtfs.data.GStopTime;
import org.greentransit.parser.gtfs.data.GTrip;
import org.greentransit.parser.my.MGenerator;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MTrip;

//http://www.stm.info/fichiers/gtfs/gtfs_stm.zip
public class STMSubway implements GAgencyTools {

	public static final String ROUTE_ID_FILTER = null;
	public static final String ROUTE_TYPE_FILTER = "1"; // subway only
	public static final String SERVICE_ID_FILTER = "15U";
	public static final String STOP_ID_FILTER = null;
	public static final int THREAD_POOL_SIZE = 4;

	public static void main(String[] args) {
		new STMSubway().start(args);
	}

	public void start(String[] args) {
		System.out.printf("Generating STM subway data...\n");
		long start = System.currentTimeMillis();
		GSpec gtfs = GReader.readGtfsZipFile(args[0], this);
		gtfs.services = GReader.extractServices(gtfs);
		gtfs.tripStops = GReader.extractTripStops(gtfs);
		if (args.length >= 5 && "true".equals(args[4])) {
			GReader.generateStopTimesFromFrequencies(gtfs);
		}
		Map<Integer, GSpec> gtfsByMRouteId = GReader.splitByRouteId(gtfs, this);
		MSpec mSpec = MGenerator.generateMSpec(gtfsByMRouteId, gtfs.stops, this);
		MGenerator.dumpFiles(mSpec, args[1], args[2]);
		System.out.printf("Generating STM subway data... DONE in %s.\n", MGenerator.getPrettyDuration(System.currentTimeMillis() - start));
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
		return "FFFFFF"; // 000000 doesn't contrast well
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


	@Override
	public void setTripHeadsign(MTrip mTrip, GTrip gTrip/* , Collection<MTripStop> mTripStops */) {
		String tripHeadsignLC = gTrip.trip_headsign.toLowerCase(Locale.ENGLISH);
		String stationName = cleanTripHeadsign(gTrip.trip_headsign);
		int directionId = -1;
		if (tripHeadsignLC.contains("angrignon")) { // green
			directionId = 0;
		} else if (tripHeadsignLC.contains("honoré-beaugrand")) { // green
			directionId = 1;
		} else if (tripHeadsignLC.contains("côte-vertu")) { // orange
			directionId = 0;
		} else if (tripHeadsignLC.contains("montmorency") || tripHeadsignLC.contains("henri-bourassa")) { // orange
			directionId = 1;
		} else if (tripHeadsignLC.contains("berri-uqam") || tripHeadsignLC.contains("berri-uqàm")) { // yellow
			directionId = 0;
		} else if (tripHeadsignLC.contains("longueuil-université") || tripHeadsignLC.contains("longueuil–université")) { // De Sherbrooke")) { // yellow
			directionId = 1;
		} else if (tripHeadsignLC.contains("saint-michel")) { // blue
			directionId = 0;
		} else if (tripHeadsignLC.contains("snowdon")) { // blue
			directionId = 1;
		} else {
			System.out.println("Unexpected station: " + tripHeadsignLC + " (headsign: " + gTrip.trip_headsign + ")");
			System.exit(-1);
		}
		mTrip.setHeadsignString(stationName, directionId);
	}

	private static final Pattern UQAM = Pattern.compile("(uq[a|à]m)", Pattern.CASE_INSENSITIVE);
	private static final String UQAM_REPLACEMENT = "UQÀM";

	private static final Pattern U_DE_S = Pattern.compile("(universit[e|é](\\-| )de(\\-| )sherbrooke)", Pattern.CASE_INSENSITIVE);
	private static final String U_DE_S_REPLACEMENT = "UdeS";

	private static final Pattern STATION = Pattern.compile("(station)", Pattern.CASE_INSENSITIVE);

	public static final Pattern SAINT = Pattern.compile("(saint)", Pattern.CASE_INSENSITIVE);
	public static final String SAINT_REPLACEMENT = "St";

	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		tripHeadsign = UQAM.matcher(tripHeadsign).replaceAll(UQAM_REPLACEMENT);
		tripHeadsign = U_DE_S.matcher(tripHeadsign).replaceAll(U_DE_S_REPLACEMENT);
		tripHeadsign = STATION.matcher(tripHeadsign).replaceAll(" ");
		tripHeadsign = SAINT.matcher(tripHeadsign).replaceAll(SAINT_REPLACEMENT);
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static List<String> MMHB = Arrays.asList(new String[] { "montmorency", "henri-bourassa" });
	private static String MMHB_HV = "Montmorency / Henri-Bourassa";

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		if (MMHB.contains(mTrip.getHeadsignValue()) && MMHB.contains(mTripToMerge.getHeadsignValue())) {
			mTrip.setHeadsignString(MMHB_HV, mTrip.getHeadsignId());
			return true;
		}
		return mTrip.mergeHeadsignValue(mTripToMerge);
	}


	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (SERVICE_ID_FILTER != null && !gTrip.service_id.contains(SERVICE_ID_FILTER)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (SERVICE_ID_FILTER != null && !gCalendarDates.service_id.contains(SERVICE_ID_FILTER)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (SERVICE_ID_FILTER != null && !gCalendar.service_id.contains(SERVICE_ID_FILTER)) {
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


	@Override
	public String cleanStopName(String stopName) {
		stopName = STATION.matcher(stopName).replaceAll(" ");
		stopName = SAINT.matcher(stopName).replaceAll(SAINT_REPLACEMENT);
		return MSpec.cleanLabel(stopName);
	}

	@Override
	public boolean excludeStop(GStop gStop) {
		if (STOP_ID_FILTER != null && !gStop.stop_id.contains(STOP_ID_FILTER)) {
			return true;
		}
		return false;
	}

	@Override
	public int getDepartureTime(GStopTime gStopTime) {
		return Integer.valueOf(gStopTime.departure_time.replaceAll(":", ""));
	}
}
