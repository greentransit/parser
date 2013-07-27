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
import org.greentransit.parser.my.data.MInboundType;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MTrip;
import org.greentransit.parser.my.data.MTripStop;

// http://www.rtl-longueuil.qc.ca/en-CA/open-data/gtfs-files/
// http://www.rtl-longueuil.qc.ca/CMS/MediaFree/file/GTFS/20130107.zip
// NOT COMPLETED YET:
// - the DATA is a mess: trips are looping (passing by same stop twice) and the same trips are passing to almost completely different stops... 
// - no information online (will require off line DB on the device)
@Deprecated
public class RTLBus implements GAgencyTools {

	public static final String ROUTE_ID_FILTER = null; // "30"; // "JANV13";
	public static final String ROUTE_TYPE_FILTER = "3"; // bus only
	public static final String SERVICE_ID_FILTER = null; // "JANV13"; // TODO use calendar
	public static final String STOP_ID_FILTER = SERVICE_ID_FILTER;
	public static final int THREAD_POOL_SIZE = 1;
	// TODO extract "Taxi"
	
	public static void main(String[] args) {
		new RTLBus().start(args);
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
		if (gRoute.route_short_name.startsWith("T")){ // exclude Taxi
			return true;
		}
		return false;
	}

	// @Override
	public int getTripId(GTrip gTrip) {
//		String[] shapeIdPart = gTrip.shape_id.split("_");
//		int direction = Integer.valueOf(shapeIdPart[1]) % 2;
//		return shapeIdPart[0] + "_" + direction;
		return Integer.valueOf(gTrip.route_id + "0" + gTrip.direction_id);
	}

	@Override
	public void setTripHeadsign(MTrip mTrip, GTrip gTrip/*, Collection<MTripStop> mTripStops*/) {
		mTrip.setHeadsignInbound(MInboundType.parse(gTrip.direction_id));
	}
	
	private static final List<String> MERGE_BEFORE = Arrays.asList(new String[] { "5_0,3382,5447", "9_0,1439,1466", "10_1,1521,3549", "14_0,3342,4984", "14_1,1607,2182", "19_1,1438,5423", "20_0,3423,3549", "25_0,4404,4910", "132_0,4744,5447" });
	private static final List<String> MERGE_AFTER_ = Arrays.asList(new String[] { "5_1,2579,4454", "9_1,1439,1466", "20_1,1518,3558", "30_1,2005,4429", "34_1,2149,2181"});
	
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
		System.out.println("l1:" + MTripStop.printStops(l1));
		System.out.println("l2:" + MTripStop.printStops(l2));
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
	
	public static final String PLACE_CHAR_ET = " et ";
	public static final String PLACE_CHAR_AV = "av. ";
	public static final String PLACE_CHAR_BOUL = "boul. ";

	@Override
	public String cleanStopName(String gStopName) {
		String result = gStopName;
		if (result.contains(PLACE_CHAR_ET)) {
			result = result.replace(PLACE_CHAR_ET, " / ");
		}
		if (result.contains(PLACE_CHAR_AV)) {
    		result = result.replace(PLACE_CHAR_AV, " ");
    	}
		if (result.contains(PLACE_CHAR_BOUL)) {
    		result = result.replace(PLACE_CHAR_BOUL, " ");
    	}
		return MSpec.cleanLabel(result);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return null;
	}

	@Override
	public int getStopId(GStop gStop) {
		return Integer.valueOf(gStop.stop_id);
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

}
