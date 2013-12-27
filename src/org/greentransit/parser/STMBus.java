package org.greentransit.parser;

import java.util.Arrays;
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
import org.greentransit.parser.my.data.MDirectionType;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MTrip;
import org.greentransit.parser.my.data.MTripStop;

// http://www.stm.info/fichiers/gtfs/gtfs_stm.zip
public class STMBus implements GAgencyTools {

	public static final String ROUTE_ID_FILTER = null; // "97"; //
	public static final String ROUTE_TYPE_FILTER = "3"; // bus only
	public static final String SERVICE_ID_FILTER = "13N"; // TODO use calendar
	public static final String STOP_ID_FILTER = null;
	public static final int THREAD_POOL_SIZE = 4;
	public static final List<String> EXCLUDED_ROUTE_IDS = Arrays.asList(new String[] { "809" });

	public static void main(String[] args) {
		new STMBus().start(args);
	}

	public void start(String[] args) {
		System.out.printf("Generating STM bus data...\n");
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

		System.out.printf("Generating STM bus data... DONE in %d seconds\n", ((System.currentTimeMillis() - start) / 1000));
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
		String result = gRoute.route_long_name;
		result = result.replace("( ", "(");
		result = result.replace(" )", ")");
		result = result.replace("(nuit)", "");
		// result = result.replace("/ ", "/");
		// result = result.replace(" /", "/");
		result = result.replace("/", " / "); // double white-spaces removed
											 // later
		return MSpec.cleanLabel(result);
	}

	public static final String COLOR_GREEEN = "007339";
	public static final String COLOR_BLACK = "000000";
	public static final String COLOR_BLUE = "0060AA";
	public static final String COLOR_RED = "ff0000";

	public static final String COLOR_LOCAL = "009ee0";
	public static final String COLOR_10MIN = "97be0d";
	public static final String COLOR_NIGHT = "646567";
	public static final String COLOR_EXPRESS = "e4368a";
	public static final String COLOR_SHUTTLE = "781b7d";

	public static final List<Integer> ROUTES_10MIN = Arrays.asList(new Integer[] { //
			18, 24, 32, 33, 44, 45, 48, 49, 51, 55, 64, 67, 69, 80, 90, 97, // 0
					103, 105, 106, 121, 136, 139, 141, 161, 165, 171, 187, 193, // 1
					211, // 2
					406, 470 }); // 4

	public static final List<Integer> ROUTES_RED = Arrays.asList(new Integer[] { //
			13, 25, 39, 46, 52, 73, 74, 75, // 0
					101, 115, 116, 135, 188, // 1
					213, 216, 218, 219, 225 }); // 2

	@Override
	public String getRouteColor(GRoute gRoute) {
		int routeId = getRouteId(gRoute);
		if (routeId == 747) {
			return COLOR_BLUE;
		}
		if (ROUTES_RED.contains(routeId)) {
			return COLOR_RED;
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

	// @Override
	public int getTripId(GTrip gTrip) {
		return Integer.valueOf(gTrip.route_id + getDirection(gTrip).intValue());
	}

	public String getTripIdString(String routeId, String directionId) {
		return routeId + "-" + MDirectionType.parse(directionId);
	}

	@Override
	public void setTripHeadsign(MTrip mTrip, GTrip gTrip) {
		// mTrip.headsignType = MyTrip.HEADSIGN_TYPE_DIRECTION;
		// mTrip.headsignString = getDirection(gTrip);
		// mTripStops could be use in last resort to enter the last stop ID
		// using => getLastStopId(mTripStops)
		mTrip.setHeadsignDirection(getDirection(gTrip));
	}

	public static MDirectionType getDirection(GTrip gTrip) {
		// 189-W
		return MDirectionType.parse(gTrip.trip_headsign.substring(gTrip.trip_headsign.length() - 1));
	}

	// from http://m.stm.info/bus/arrets/487/W?showAll=true
	private static final List<String> MERGE_BEFORE = Arrays.asList(new String[] { "11-W,51426,51429", "11-E,51427,53833", "33-N,53436,54311",
			"46-W,51576,51577", "48-E,54138,55235", "52-E,50671,50892", "68-W,58256,58317", "68-E,58313,58329", "70-W,59631,60560", "103-W,54161,56318",
			"115-W,56017,56019", "125-W,53007,53016", "139-S,55068,61274", "146-E,53827,54093", "146-W,53827,54093", "166-S,51427,51429", "168-N,56661,60675",
			"186-W,60550,61227", "188-W,54786,54838", "204-W,57505,57568", "204-E,57505,57568", "213-W,55372,60701", "401-N,58088,58106", "409-N,57931,57947",
			"432-S,53102,54063", "439-S,55327,59374", "439-S,55325,61274", "439-N,58667,61274", "440-W,55228,55253", "448-W,52264,60434", "449-S,54244,61273",
			"470-W,58249,61256", "470-E,58286,61257", "470-E,58298,61312", "470-E,60414,61254", "487-W,53425,53716" });
	private static final List<String> MERGE_AFTER_ = Arrays.asList(new String[] { "33-S,53392,53436", "34-E,53065,54325", "46-W,51577,51721",
			"48-W,55260,55313", "52-W,50787,54391", "68-W,58314,58329", "70-E,59631,60577", "115-E,56013,56020", "115-E,54195,54262", "115-W,54195,54262",
			"131-N,54952,60588", "188-E,54789,54838", "201-N,60415,60889", "201-S,57797,60888", "211-E,54181,54267", "213-E,58086,60701", "401-S,58093,58098",
			"401-N,58106,58225", "409-S,57933,57945", "411-E,54181,54304", "432-N,53102,60735", "432-N,53102,53103", "440-E,55066,55266", "448-E,59312,60236",
			"449-N,53224,61273", "449-N,54743,60337", "449-N,52026,54743", "460-W,55839,55843", "460-W,51524,55014", "460-W,51538,51539", "460-E,51541,55016",
			"487-E,53426,53487" });

	// some bus lines have to be merged manually like 33-N
	@Override
	public int mergeTrip(MTripStop ts1, MTripStop ts2, List<MTripStop> l1, List<MTripStop> l2, int i1, int i2) {
		if (Integer.valueOf(ts2.stopId) < Integer.valueOf(ts1.stopId)) {
			return -mergeTrip(ts2, ts1, l2, l1, i2, i1);
		}
		int result = mergeTrip(ts1, ts2);
		if (result != 0) {
			return result;
		}
		result = findMerge(l1, ts2);
		if (result != 0) {
			return result;
		}
		result = -findMerge(l2, ts1);
		if (result != 0) {
			return result;
		}
		System.out.println("Have to resolve: " + ts1.tripIdString + "," + ts1.stopId + "," + ts2.stopId);
		System.out.println("l1:" + l1.toString());
		System.out.println("l2:" + l2.toString());
		System.exit(-1);
		return result;
	}

	private static int findMerge(List<MTripStop> l, MTripStop ts) {
		int result = 0;
		// System.out.print("Comparing " + ts.stopId +" with...");
		for (int i = 0; i < l.size(); i++) {
			MTripStop tsi = l.get(i);
			// System.out.print(tsi.stopId+",");
			if (Integer.valueOf(tsi.stopId) < Integer.valueOf(ts.stopId)) {
				result = mergeTrip(tsi, ts);
			} else {
				result = -mergeTrip(ts, tsi);
			}
			if (result != 0) {
				// System.out.print(">result:"+result+"\n");
				return result;
			}
		}
		// System.out.print("\n");
		return result;
	}

	public static int mergeTrip(MTripStop ts1, MTripStop ts2) {
		String merge = ts1.tripIdString + "," + ts1.stopId + "," + ts2.stopId;
		if (MERGE_BEFORE.contains(merge)) {
			return +1;
		} else if (MERGE_AFTER_.contains(merge)) {
			return -1;
		}
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

	public static final String PLACE_CHAR_DE = "de ";
	public static final int PLACE_CHAR_DE_LENGTH = PLACE_CHAR_DE.length();

	// public static final String PLACE_CHAR_DE_LA = "de la ";
	// public static final int PLACE_CHAR_DE_LA_LENGTH =
	// PLACE_CHAR_DE_LA.length();

	public static final String PLACE_CHAR_DES = "des ";
	public static final int PLACE_CHAR_DES_LENGTH = PLACE_CHAR_DES.length();

	public static final String PLACE_CHAR_DU = "du ";
	public static final int PLACE_CHAR_DU_LENGTH = PLACE_CHAR_DU.length();

	public static final String PLACE_CHAR_LA = "la ";
	public static final int PLACE_CHAR_LA_LENGTH = PLACE_CHAR_LA.length();

	public static final String PLACE_CHAR_L = "l'";
	public static final int PLACE_CHAR_L_LENGTH = PLACE_CHAR_L.length();

	public static final String PLACE_CHAR_D = "d'";
	public static final int PLACE_CHAR_D_LENGTH = PLACE_CHAR_D.length();

	public static final String PLACE_CHAR_IN = "/ ";

	public static final String PLACE_CHAR_IN_DE = PLACE_CHAR_IN + PLACE_CHAR_DE;

	// public static final String PLACE_CHAR_IN_DE_LA = PLACE_CHAR_IN +
	// PLACE_CHAR_DE_LA;

	public static final String PLACE_CHAR_IN_DES = PLACE_CHAR_IN + PLACE_CHAR_DES;

	public static final String PLACE_CHAR_IN_DU = PLACE_CHAR_IN + PLACE_CHAR_DU;

	public static final String PLACE_CHAR_IN_LA = PLACE_CHAR_IN + PLACE_CHAR_LA;

	public static final String PLACE_CHAR_IN_L = PLACE_CHAR_IN + PLACE_CHAR_L;

	public static final String PLACE_CHAR_IN_D = PLACE_CHAR_IN + PLACE_CHAR_D;

	public static final String PLACE_CHAR_PARENTHESE = "(";
	public static final String PLACE_CHAR_PARENTHESE_STATION = PLACE_CHAR_PARENTHESE + "station ";
	public static final int PLACE_CHAR_PARENTHESE_STATION_LENGTH = PLACE_CHAR_PARENTHESE_STATION.length();
	public static final String PLACE_CHAR_PARENTHESE_STATION_BIG = PLACE_CHAR_PARENTHESE + "Station ";
	public static final int PLACE_CHAR_PARENTHESE_STATION_BIG_LENGTH = PLACE_CHAR_PARENTHESE_STATION_BIG.length();

	@Override
	public String cleanStopName(String gStopName) {
		String result = gStopName;
		// if (result.startsWith(PLACE_CHAR_DE_LA)) {
		// result = result.substring(PLACE_CHAR_DE_LA_LENGTH);
		// } else
		if (result.startsWith(PLACE_CHAR_DE)) {
			result = result.substring(PLACE_CHAR_DE_LENGTH);
		} else if (result.startsWith(PLACE_CHAR_DES)) {
			result = result.substring(PLACE_CHAR_DES_LENGTH);
		} else if (result.startsWith(PLACE_CHAR_DU)) {
			result = result.substring(PLACE_CHAR_DU_LENGTH);
		}
		if (result.startsWith(PLACE_CHAR_LA)) {
			result = result.substring(PLACE_CHAR_LA_LENGTH);
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
		} else if (result.contains(PLACE_CHAR_IN_L)) {
			result = result.replace(PLACE_CHAR_IN_L, PLACE_CHAR_IN);
		} else if (result.contains(PLACE_CHAR_IN_D)) {
			result = result.replace(PLACE_CHAR_IN_D, PLACE_CHAR_IN);
		}

		if (result.contains(PLACE_CHAR_PARENTHESE_STATION)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_STATION, PLACE_CHAR_PARENTHESE);
		}
		if (result.contains(PLACE_CHAR_PARENTHESE_STATION_BIG)) {
			result = result.replace(PLACE_CHAR_PARENTHESE_STATION_BIG, PLACE_CHAR_PARENTHESE);
		}
		// TODO MORE ?
		// TODO transform Station Papineau (Dorion / Sainte-Catherine) => Dorion
		// / Sainte-Catherine (Station Papineau) OR Dorion / Sainte-Catherine
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

	@Override
	public int getCalendarDate(GCalendarDate gCalendarDate) {
		return Integer.valueOf(gCalendarDate.date);
	}

}
