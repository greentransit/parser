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
import org.greentransit.parser.my.data.MInboundType;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MTrip;
import org.greentransit.parser.my.data.MTripStop;

// http://www.amt.qc.ca/xdata/express/google_transit.zip
// TODO enable (all working but only 1 route and no live data)
@Deprecated
public class AMTBus implements GAgencyTools {

	// TODO put with RTL buses (operated by RTL)

	public static final String ROUTE_ID_FILTER = null; // "JANV13";
	public static final String ROUTE_TYPE_FILTER = "3"; // bus only
	public static final String SERVICE_ID_FILTER = null; // "JANV13"; // TODO use calendar
	public static final String STOP_ID_FILTER = SERVICE_ID_FILTER;
	public static final int THREAD_POOL_SIZE = 1;

	public static void main(String[] args) {
		new AMTBus().start(args);

	}

	public void start(String[] args) {
		System.out.printf("Generating AMT bus data...\n");
		long start = System.currentTimeMillis();
		// GTFS parsing
		GSpec gtfs = GReader.readGtfsZipFile(args[0], this);
		gtfs.tripStops = GReader.extractTripStops(gtfs);
		Map<Integer, GSpec> gtfsByMRouteId = GReader.splitByRouteId(gtfs, this);
		// Objects generation
		MSpec mSpec = MGenerator.generateMSpec(gtfsByMRouteId, gtfs.stops, this);
		// Dump to files
		MGenerator.dumpFiles(mSpec, args[1], args[2]);
		System.out.printf("Generating AMT bus data... DONE in %d seconds\n", ((System.currentTimeMillis() - start) / 1000));
		// System.exit(0);
	}

	@Override
	public int getRouteId(GRoute gRoute) {
		return Integer.valueOf(gRoute.route_id);
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		return gRoute.route_short_name;
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		return MSpec.cleanLabel(gRoute.route_long_name);
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
		String[] shapeIdPart = gTrip.shape_id.split("_");
		int direction = Integer.valueOf(shapeIdPart[1]) % 2;
		return Integer.valueOf(shapeIdPart[0] + "0" + direction);
	}

	@Override
	public void setTripHeadsign(MTrip mTrip, GTrip gTrip/* , Collection<MTripStop> mTripStops */) {
		mTrip.setHeadsignInbound(MInboundType.parse(gTrip.direction_id));
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

	public static final String PLACE_CHAR_DE = "de ";
	public static final String PLACE_CHAR_RUE = "rue ";

	// public static final int PLACE_CHAR_DE_LENGTH = PLACE_CHAR_DE.length();
	// public static final String PLACE_CHAR_IN = "/ ";
	// public static final String PLACE_CHAR_IN_DE = PLACE_CHAR_IN + PLACE_CHAR_DE;

	@Override
	public String cleanStopName(String gStopName) {
		// rue de / rue
		String result = gStopName;
		if (result.contains(PLACE_CHAR_DE)) {
			result = result.replace(PLACE_CHAR_DE, "");
		}
		if (result.contains(PLACE_CHAR_RUE)) {
			result = result.replace(PLACE_CHAR_RUE, "");
		}
		// if (result.contains(PLACE_CHAR_IN_DE)) {
		// result = result.replace(PLACE_CHAR_IN_DE, PLACE_CHAR_IN);
		// }
		return MSpec.cleanLabel(result);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return null;
	}

	@Override
	public int getStopId(GStop gStop) {
		return Integer.valueOf(gStop.stop_id.substring(0, gStop.stop_id.length() - 2));
	}

	@Override
	public boolean excludeStop(GStop gStop) {
		if (STOP_ID_FILTER != null && !gStop.stop_id.contains(STOP_ID_FILTER)) {
			return true;
		}
		return false;
	}

	@Override
	public int getThreadPoolSize() {
		return THREAD_POOL_SIZE;
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
