package org.greentransit.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import org.greentransit.parser.my.data.MDirectionType;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MTrip;

// http://www.stm.info/fichiers/gtfs/gtfs_stm.zip
public class STMBus implements GAgencyTools {

	public static final String ROUTE_ID_FILTER = null;
	public static final String ROUTE_TYPE_FILTER = "3"; // bus only
	public static final String SERVICE_ID_FILTER = "17M";
	public static final String STOP_ID_FILTER = null;
	public static final int THREAD_POOL_SIZE = 4;
	public static final List<String> EXCLUDED_ROUTE_IDS = null;

	public static void main(String[] args) {
		new STMBus().start(args);
	}

	public void start(String[] args) {
		System.out.printf("Generating STM bus data...\n");
		long start = System.currentTimeMillis();
		GSpec gtfs = GReader.readGtfsZipFile(args[0], this);
		gtfs.services = GReader.extractServices(gtfs);
		gtfs.tripStops = GReader.extractTripStops(gtfs);
		if (args.length >= 4 && "true".equals(args[4])) {
			GReader.generateStopTimesFromFrequencies(gtfs);
		}
		Map<Integer, GSpec> gtfsByMRouteId = GReader.splitByRouteId(gtfs, this);
		MSpec mSpec = MGenerator.generateMSpec(gtfsByMRouteId, gtfs.stops, this);
		MGenerator.dumpFiles(mSpec, args[1], args[2]);
		System.out.printf("Generating STM bus data... DONE in %s.\n", MGenerator.getPrettyDuration(System.currentTimeMillis() - start));
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
		String result = gRoute.route_long_name;
		result = result.replace("( ", "(");
		result = result.replace(" )", ")");
		result = result.replace("(nuit)", "");
		result = result.replace("/", " / ");
		return MSpec.cleanLabel(result);
	}

	public static final String COLOR_GREEEN = "007339";
	public static final String COLOR_BLACK = "000000";
	public static final String COLOR_BLUE = "0060AA";


	public static final List<Integer> ROUTES_10MIN = Arrays.asList(new Integer[] { //
			18, 24, 32, 33, 44, 45, 48, 49, 51, 55, 64, 67, 69, 80, 90, 97, // 0
					103, 105, 106, 121, 136, 139, 141, 161, 165, 171, 187, 193, // 1
					211, // 2
					406, 470 }); // 4


	@Override
	public String getRouteColor(GRoute gRoute) {
		int routeId = getRouteId(gRoute);
		if (routeId >= 700) {
			return COLOR_BLUE;
		}
		if (routeId >= 400) {
			return COLOR_GREEEN;
		}
		if (routeId >= 300) {
			return COLOR_BLACK;
		}
		return COLOR_BLUE;
		// if (routeId >= 700) {
		// return COLOR_SHUTTLE;
		// } else if (routeId >= 400) {
		// return COLOR_EXPRESS;
		// } else if (routeId >= 300) {
		// return COLOR_NIGHT;
		// } else if (ROUTES_10MIN.contains(routeId)) {
		// return COLOR_10MIN;
		// } else {
		// return COLOR_LOCAL;
		// }
	}

	private static final String COLOR_WHITE = "FFFFFF";

	@Override
	public String getRouteTextColor(GRoute gRoute) {
		return COLOR_WHITE;
	}

	@Override
	public boolean excludeRoute(GRoute gRoute) {
		if (ROUTE_TYPE_FILTER != null && !gRoute.route_type.equals(ROUTE_TYPE_FILTER)) {
			return true;
		}
		if (ROUTE_ID_FILTER != null && !gRoute.route_id.startsWith(ROUTE_ID_FILTER)) {
			return true;
		}
		if (EXCLUDED_ROUTE_IDS != null && EXCLUDED_ROUTE_IDS.contains(gRoute.route_id)) {
			return true;
		}
		return false;
	}

	@Override
	public void setTripHeadsign(MTrip mTrip, GTrip gTrip) {
		// mTrip.headsignType = MyTrip.HEADSIGN_TYPE_DIRECTION;
		// mTrip.headsignString = getDirection(gTrip);
		// mTripStops could be use in last resort to enter the last stop ID
		// using => getLastStopId(mTripStops)
		mTrip.setHeadsignDirection(getDirection(gTrip));
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		return mTrip.mergeHeadsignValue(mTripToMerge);
	}

	public static MDirectionType getDirection(GTrip gTrip) {
		return MDirectionType.parse(gTrip.trip_headsign.substring(gTrip.trip_headsign.length() - 1));
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

	private static final String PLACE_CHAR_DE = "de ";
	private static final int PLACE_CHAR_DE_LENGTH = PLACE_CHAR_DE.length();

	private static final String PLACE_CHAR_DES = "des ";
	private static final int PLACE_CHAR_DES_LENGTH = PLACE_CHAR_DES.length();

	private static final String PLACE_CHAR_DU = "du ";
	private static final int PLACE_CHAR_DU_LENGTH = PLACE_CHAR_DU.length();

	private static final String PLACE_CHAR_LA = "la ";
	private static final int PLACE_CHAR_LA_LENGTH = PLACE_CHAR_LA.length();

	private static final String PLACE_CHAR_LE = "le ";
	private static final int PLACE_CHAR_LE_LENGTH = PLACE_CHAR_LE.length();

	private static final String PLACE_CHAR_L = "l'";
	private static final int PLACE_CHAR_L_LENGTH = PLACE_CHAR_L.length();

	private static final String PLACE_CHAR_D = "d'";
	private static final int PLACE_CHAR_D_LENGTH = PLACE_CHAR_D.length();

	private static final String PLACE_CHAR_IN = "/ ";
	private static final String PLACE_CHAR_IN_DE = PLACE_CHAR_IN + PLACE_CHAR_DE;
	private static final String PLACE_CHAR_IN_DES = PLACE_CHAR_IN + PLACE_CHAR_DES;
	private static final String PLACE_CHAR_IN_DU = PLACE_CHAR_IN + PLACE_CHAR_DU;
	private static final String PLACE_CHAR_IN_LA = PLACE_CHAR_IN + PLACE_CHAR_LA;
	private static final String PLACE_CHAR_IN_LE = PLACE_CHAR_IN + PLACE_CHAR_LE;
	private static final String PLACE_CHAR_IN_L = PLACE_CHAR_IN + PLACE_CHAR_L;
	private static final String PLACE_CHAR_IN_D = PLACE_CHAR_IN + PLACE_CHAR_D;

	private static final String PLACE_CHAR_PARENTHESE = "(";
	private static final String PLACE_CHAR_PARENTHESE_DE = PLACE_CHAR_PARENTHESE + PLACE_CHAR_DE;
	private static final String PLACE_CHAR_PARENTHESE_DES = PLACE_CHAR_PARENTHESE + PLACE_CHAR_DES;
	private static final String PLACE_CHAR_PARENTHESE_DU = PLACE_CHAR_PARENTHESE + PLACE_CHAR_DU;
	private static final String PLACE_CHAR_PARENTHESE_LA = PLACE_CHAR_PARENTHESE + PLACE_CHAR_LA;
	private static final String PLACE_CHAR_PARENTHESE_LE = PLACE_CHAR_PARENTHESE + PLACE_CHAR_LE;
	private static final String PLACE_CHAR_PARENTHESE_L = PLACE_CHAR_PARENTHESE + PLACE_CHAR_L;
	private static final String PLACE_CHAR_PARENTHESE_D = PLACE_CHAR_PARENTHESE + PLACE_CHAR_D;

	private static final String PLACE_CHAR_PARENTHESE_STATION = PLACE_CHAR_PARENTHESE + "station ";
	private static final String PLACE_CHAR_PARENTHESE_STATION_BIG = PLACE_CHAR_PARENTHESE + "Station ";

	@Override
	public String cleanStopName(String result) {
		if (result.startsWith(PLACE_CHAR_DE)) {
			result = result.substring(PLACE_CHAR_DE_LENGTH);
		} else if (result.startsWith(PLACE_CHAR_DES)) {
			result = result.substring(PLACE_CHAR_DES_LENGTH);
		} else if (result.startsWith(PLACE_CHAR_DU)) {
			result = result.substring(PLACE_CHAR_DU_LENGTH);
		}
		if (result.startsWith(PLACE_CHAR_LA)) {
			result = result.substring(PLACE_CHAR_LA_LENGTH);
		} else if (result.startsWith(PLACE_CHAR_LE)) {
			result = result.substring(PLACE_CHAR_LE_LENGTH);
		} else if (result.startsWith(PLACE_CHAR_L)) {
			result = result.substring(PLACE_CHAR_L_LENGTH);
		} else if (result.startsWith(PLACE_CHAR_D)) {
			result = result.substring(PLACE_CHAR_D_LENGTH);
		}
		// if (result.contains(PLACE_CHAR_IN_DE_LA)) {
		// result = result.replace(PLACE_CHAR_IN_DE_LA, PLACE_CHAR_IN);
		// } else
		if (result.contains(PLACE_CHAR_IN_DE)) {
			result = result.replace(PLACE_CHAR_IN_DE, PLACE_CHAR_IN);
		} else if (result.contains(PLACE_CHAR_IN_DES)) {
			result = result.replace(PLACE_CHAR_IN_DES, PLACE_CHAR_IN);
		} else if (result.contains(PLACE_CHAR_IN_DU)) {
			result = result.replace(PLACE_CHAR_IN_DU, PLACE_CHAR_IN);
		}
		if (result.contains(PLACE_CHAR_IN_LA)) {
			result = result.replace(PLACE_CHAR_IN_LA, PLACE_CHAR_IN);
		} else if (result.contains(PLACE_CHAR_IN_LE)) {
			result = result.replace(PLACE_CHAR_IN_LE, PLACE_CHAR_IN);
		} else if (result.contains(PLACE_CHAR_IN_L)) {
			result = result.replace(PLACE_CHAR_IN_L, PLACE_CHAR_IN);
		} else if (result.contains(PLACE_CHAR_IN_D)) {
			result = result.replace(PLACE_CHAR_IN_D, PLACE_CHAR_IN);
		}
		
		if (result.contains(PLACE_CHAR_PARENTHESE_DE)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_DE, PLACE_CHAR_PARENTHESE);
		} else if (result.contains(PLACE_CHAR_PARENTHESE_DES)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_DES, PLACE_CHAR_PARENTHESE);
		} else if (result.contains(PLACE_CHAR_PARENTHESE_DU)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_DU, PLACE_CHAR_PARENTHESE);
		}
		if (result.contains(PLACE_CHAR_PARENTHESE_LA)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_LA, PLACE_CHAR_PARENTHESE);
		} else if (result.contains(PLACE_CHAR_PARENTHESE_LE)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_LE, PLACE_CHAR_PARENTHESE);
		} else if (result.contains(PLACE_CHAR_PARENTHESE_L)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_L, PLACE_CHAR_PARENTHESE);
		} else if (result.contains(PLACE_CHAR_PARENTHESE_D)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_D, PLACE_CHAR_PARENTHESE);
		}
		if (result.contains(PLACE_CHAR_PARENTHESE_STATION)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_STATION, PLACE_CHAR_PARENTHESE);
		}
		if (result.contains(PLACE_CHAR_PARENTHESE_STATION_BIG)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_STATION_BIG, PLACE_CHAR_PARENTHESE);
		}
		return MSpec.cleanLabel(result);
	}

	@Override
	public int getStopId(GStop gStop) {
		return Integer.valueOf(getStopCode(gStop));
	}

	@Override
	public String getStopCode(GStop gStop) {
		return gStop.stop_code;
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
