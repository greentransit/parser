package org.greentransit.parser.gtfs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.greentransit.parser.gtfs.data.GDropOffType;
import org.greentransit.parser.gtfs.data.GPickupType;
import org.greentransit.parser.gtfs.data.GRoute;
import org.greentransit.parser.gtfs.data.GSpec;
import org.greentransit.parser.gtfs.data.GStop;
import org.greentransit.parser.gtfs.data.GStopTime;
import org.greentransit.parser.gtfs.data.GTrip;
import org.greentransit.parser.gtfs.data.GTripStop;

/**
 * Class to read a GTFS specification file and return the data in a structured format.
 * 
 * @author Mathieu Méa
 */
public class GReader {

	// public static final class MyTripStopSorter implements Comparator<MyTripStop> {
	// @Override
	// public int compare(MyTripStop ts1, MyTripStop ts2) {
	// // sort by trip_id => stop_sequence
	// if (!ts1.tripId.equals(ts2.tripId)) {
	// return ts1.tripId.compareTo(ts2.tripId);
	// }
	// return ts1.stopSequence - ts2.stopSequence;
	// }
	// }

//	public static final String MAIN = "MARS12";
//
//	public static final String ROUTE_ID_START_WITH = null;// MAIN + "12";
//
//	public static final String TRIP_ID_START_WITH = null; // ROUTE_ID_START_WITH;
//
//	public static final String STOP_ID_START_WITH = null;// MAIN;

	// filter all files by service_id (trips.txt)
//	public static final String SERVICE_ID = null; // "13J_S"; // "13J_*"
//	public static final String SERVICE_ID_START_WITH = "13J";
//	public static final String ROUTE_ID = null;// "10"; // debug with only 1 route

//	public static final String ROUTE_TYPE = "3"; // bus only

//	public static final String ROUTES_LINE_START_WITH = ROUTE_ID == null ? null : ROUTE_ID + ",";
//	public static final String STOP_TIMES_LINE_START_WITH = SERVICE_ID_START_WITH;
//	public static final String STOP_TIMES_LINE_CONTAINS = ROUTE_ID == null ? null : "_" + ROUTE_ID + ","; // _10,
	// public static final String STOP_TIMES_LINE_END_WITH = "_" + ROUTE_ID;
	// public static final String TRIPS_LINE_CONTAINS = "," + SERVICE_ID_START_WITH;
//	public static final String TRIPS_LINE_START_WITH = ROUTE_ID == null ? null : ROUTE_ID /* + "," + SERVICE_ID_START_WITH */;

	// TODO use calendar_dates.txt to get current day?

	public static GSpec readGtfsZipFile(String gtfsFile, GAgencyTools agencyTools/*, String routeIdFilter, String routeTypeFilter, String serviceIdFilter, String stopIdFilter*/) {
		System.out.printf("Reading GTFS file '%s'...\n", gtfsFile);
		long start = System.currentTimeMillis();
		GSpec gspec = null;
		ZipInputStream zip = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			zip = new ZipInputStream(new FileInputStream(gtfsFile));
			isr = new InputStreamReader(zip, Charset.forName("UTF-8"));
			reader = new BufferedReader(isr);
			// List<GtfsAgency> agencies = null;
			Map<String, GRoute> routes = null;
			Map<String, GStop> stops = null;
			Map<String, GTrip> trips = null;
			List<GStopTime> stopTimes = null;
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				if (entry.isDirectory()) {
					continue;
				}
				String filename = entry.getName();
				// if (filename.equals(GtfsAgency.FILENAME)) { // AGENCY
				// List<HashMap<String, String>> fileLines = readCsv(filename, reader, null, null);
				// agencies = processAgency(fileLines);
				// } else
				if (filename.equals(GRoute.FILENAME)) { // ROUTE
					List<HashMap<String, String>> fileLines = readCsv(filename, reader, null /*ROUTES_LINE_START_WITH*/, null);
					routes = processRoutes(fileLines, agencyTools);
				} else if (filename.equals(GStop.FILENAME)) { // STOP
					List<HashMap<String, String>> fileLines = readCsv(filename, reader, null, null);
					stops = processStops(fileLines, agencyTools);
				} else if (filename.equals(GTrip.FILENAME)) { // TRIP
					List<HashMap<String, String>> fileLines = readCsv(filename, reader, null /*TRIPS_LINE_START_WITH*/, null);
					trips = processTrips(fileLines, agencyTools);
				} else if (filename.equals(GStopTime.FILENAME)) { // STOP TIME
					List<HashMap<String, String>> fileLines = readCsv(filename, reader, null /*STOP_TIMES_LINE_START_WITH*/, null /*STOP_TIMES_LINE_CONTAINS*/);
					stopTimes = processStopTimes(fileLines);
				} else {
					System.out.println("File not used: " + filename);
				}
			}
			gspec = new GSpec(/* agencies, */stops, routes, trips, stopTimes);
		} catch (IOException ioe) {
			System.out.println("I/O Error while reading GTFS file!");
			ioe.printStackTrace();
			System.exit(-1);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
				}
			}
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
				}
			}
		}
		System.out.printf("Reading GTFS file '%1$s'... DONE in %2$d seconds\n", gtfsFile, ((System.currentTimeMillis() - start) / 1000));
		// System.out.printf("Agencies: %d\n", gspec.agencies.size());
		System.out.printf("- Routes: %d\n", gspec.routes.size());
		System.out.printf("- Trips: %d\n", gspec.trips.size());
		System.out.printf("- Stops: %d\n", gspec.stops.size());
		System.out.printf("- StopTimes: %d\n", gspec.stopTimes.size());
		return gspec;
	}

	/**
	 * Helper method to read a CSV file and return the content as a list of maps.
	 * 
	 * @param csv the input CSV file content.
	 * @return a list of maps, where each list element corresponds to a row in the CSV file. The map keys are the column names from the first line of the CSV
	 *         file.
	 * @throws IOException in case of failing to read the input stream.
	 */
	private static List<HashMap<String, String>> readCsv(String filename, BufferedReader reader,
			String filterStartWith, String filterContains) throws IOException {
		System.out.println("Reading file '" + filename + "'...");
		ArrayList<HashMap<String, String>> lines = new ArrayList<HashMap<String, String>>();

		// InputStreamReader isr = new InputStreamReader(csv/* , Charset.forName("UTF-8") */);
		// BufferedReader reader = new BufferedReader(isr);

		String line;
		String[] lineColumns;
		// read column names (1st line)
		// reader.skip(1); // skip 1st empty char
		line = reader.readLine();
		if (line.charAt(0) == '\uFEFF') { // remove 1st empty char
			line = String.copyValueOf(line.toCharArray(), 1, line.length()-1);
		}
		lineColumns = line.split(",");
		
		// List<String> columnNames = Arrays.asList(lineColumns);
		// for (String lineColumn : lineColumns) {
		// columnNames.add(lineColumn);
		// System.out.println(lineColumn + " > " + lineColumn.length() + "("+lineColumn.trim().length()+")");
		// }
		String[] columnNames = lineColumns;// new String[lineColumns.length];
		// for (int ci = 0; ci < line.length; ++ci) {
		// columnNames[ci] = new String(line[ci].getBytes("UTF-8"), "UTF-8");
		// }
		if (columnNames == null || columnNames.length == 0) {
			return lines;
		}
		// System.out.println(columnNames[0]);
		// System.out.println("trip_id".equals(columnNames.get(0)));
		// read column values
		while ((line = reader.readLine()/* .readNext() */) != null) {
			if (filterStartWith != null && !line.startsWith(filterStartWith)) {
				continue;
			}
			if (filterContains != null && !line.contains(filterContains)) {
				continue;
			}
			HashMap<String, String> map = new HashMap<String, String>();
			lineColumns = line.split(",");
			for (int ci = 0; ci < lineColumns.length; ++ci) {
				// if (ci == 0) {
				// System.out.println(columnNames.get(ci) + "> " + lineColumns[ci]);
				// }
				map.put(columnNames[ci], /* String.valueOf( */lineColumns[ci]/* ) */);
				// if (ci == 0) {
				// System.out.println("trip_id".equals(columnNames.get(ci)));
				// System.out.println(map.get(columnNames.get(ci)));
				// System.out.println(map.get("trip_id"));
				// }
			}
			// System.out.println(map.get("trip_id")); //1st columns name always fail
			// System.out.println(map.get("stop_id"));
			lines.add(map);
		}
		System.out.println("File '" + filename + "' read (lines: " + lines.size() + ").");
		// reader.close();
		// isr.close();
		return lines;
	}

	public static Map<String, GTripStop> extractTripStops(GSpec gtfs) {
		System.out.println("Generating GTFS trip stops...");
		HashMap<String, GTripStop> gTripStops = new HashMap<String, GTripStop>();
		for (GStopTime gStopTime : gtfs.stopTimes) {
			if (gStopTime.trip_id == null) {
				continue;
			}
			GTrip gTrip = gtfs.trips.get(gStopTime.trip_id);
			if (gTrip == null) {
				continue;
			}
			String uid = GTripStop.getUID(gTrip.getUID(), gStopTime.stop_id);
			if (gTripStops.containsKey(uid)) {
				// check & log differences
				GTripStop gTripStop = gTripStops.get(uid);
				if (gTripStop.stop_sequence != gStopTime.stop_sequence) {
					//System.out.println("Trip stop '" + uid + "' sequence different: " + tripStop.stop_sequence + " != " + stopTime.stop_sequence);
					// TODO what should we do?
				}
				// TODO check drop off + pickup types
			} else { // add new one
				GTripStop gTripStop = new GTripStop(gTrip.trip_id, gStopTime.stop_id, gStopTime.stop_sequence/*, gStopTime.pickup_type, gStopTime.drop_off_type*/);
				gTripStops.put(uid, gTripStop);
			}
		}
		System.out.println("Generating GTFS trip stops... DONE");
		System.out.printf("- Trip stops: %d\n", gTripStops.size());
		return gTripStops;
	}

//	@SuppressWarnings("unused")
//	@Deprecated
//	private static Map<String, GtfsRouteStop> extractRouteStops(GtfsSpec gtfs) {
//		HashMap<String, GtfsRouteStop> routeStops = new HashMap<String, GtfsRouteStop>();
//		for (GtfsStopTime stopTime : gtfs.stopTimes) {
//			if (!gtfs.trips.containsKey(stopTime.trip_id)) {
//				continue; // unknown trip // should not happen
//			}
//			String route_id = gtfs.trips.get(stopTime.trip_id).route_id;
//			String uid = GtfsRouteStop.getUID(route_id, stopTime.stop_id);
//			if (routeStops.containsKey(uid)) {
//				continue; // already registered
//				// TODO compare ?
//			}
//			routeStops.put(uid, new GtfsRouteStop(route_id, stopTime.stop_id, stopTime.stop_sequence, stopTime.pickup_type, stopTime.drop_off_type));
//		}
//		return routeStops;
//	}

	private static List<GStopTime> processStopTimes(List<HashMap<String, String>> lines/* , Map<String, Trip> trips */) throws IOException {
		System.out.println("Processing stop times...");
		List<GStopTime> stopTimes = new ArrayList<GStopTime>();
		for (HashMap<String, String> line : lines) {
			// if (trips.containsKey(line.get(StopTime.TRIP_ID))) {
			try {
				GStopTime gStopTime = new GStopTime(line.get(GStopTime.TRIP_ID), line.get(GStopTime.ARRIVAL_TIME), line.get(GStopTime.DEPARTURE_TIME), line
						.get(GStopTime.STOP_ID), Integer.valueOf(line.get(GStopTime.STOP_SEQUENCE)));
				gStopTime.pickup_type = GPickupType.parse(line.get(GStopTime.PICKUP_TYPE));
				gStopTime.drop_off_type = GDropOffType.parse(line.get(GStopTime.DROP_OFF_TYPE));
				stopTimes.add(gStopTime);
			} catch (Exception e) {
				System.out.println("Error while parsing: " + line);
				e.printStackTrace();
			}
			// }
		}
		System.out.println("Processing stop times... DONE (" + stopTimes.size() + " extracted)");
		return stopTimes;
	}

	private static Map<String, GTrip> processTrips(List<HashMap<String, String>> lines, GAgencyTools agencyTools /*String serviceIdFilter*/) throws IOException {
		System.out.println("Processing trips...");
		Map<String, GTrip> trips = new HashMap<String, GTrip>();
//		Pattern serviceIdFilterpattern = serviceIdFilter == null ? null : Pattern.compile(serviceIdFilter);
		for (HashMap<String, String> line : lines) {
			try {
				// for (String lineKey : line.keySet()) {
				// System.out.println("'" + lineKey + "' > " + line.get(lineKey));// FIXME remove
				// }
				// System.out.println(" RouteId > " + line.get("route_id"));// FIXME remove
				// boolean containsRouteId = line.containsKey(Trip.ROUTE_ID);
				// System.out.println(" containsRouteId > " + containsRouteId);// FIXME remove
				String routeId = line.get(GTrip.ROUTE_ID);
				// System.out.println(" RouteId > " + routeId);// FIXME remove
				String tripId = line.get(GTrip.TRIP_ID);
				String serviceId = line.get(GTrip.SERVICE_ID);
				GTrip gTrip = new GTrip(routeId, serviceId, tripId);
				gTrip.trip_headsign = line.get(GTrip.TRIP_HEADSIGN);
				gTrip.direction_id = line.get(GTrip.DIRECTION_ID);
				gTrip.shape_id = line.get(GTrip.SHAPE_ID);
//				if (ROUTE_ID != null && !routeId.equals(ROUTE_ID)) {
//					continue; // ignore this service
//				}
//				if (ROUTE_ID_START_WITH != null && !routeId.startsWith(ROUTE_ID_START_WITH)) {
//					continue; // ignore this route
//				}
				if (agencyTools.excludeTrip(gTrip)) {
				// if (serviceIdFilter != null && !serviceId.contains(serviceIdFilter)) {
//				if (serviceIdFilterpattern != null && !serviceIdFilterpattern.matcher(serviceId).matches()) {
					continue; // ignore this service
				}
//				if (SERVICE_ID_START_WITH != null && !serviceId.startsWith(SERVICE_ID_START_WITH)) {
//					continue; // ignore this service
//				}
				trips.put(tripId, gTrip);
			} catch (Exception e) {
				System.out.println("Error while processing: " + line);
				e.printStackTrace();
				System.exit(-1);
			}
		}
		System.out.println("Processing trips... DONE (" + trips.size() + " extracted)");
		return trips;
	}

	private static HashMap<String, GStop> processStops(List<HashMap<String, String>> lines, GAgencyTools agencyTools /*String stopIdFilter*/) throws IOException {
		System.out.println("Processing stops...");
		HashMap<String, GStop> stops = new HashMap<String, GStop>();
		for (Map<String, String> line : lines) {
			String stopId = line.get(GStop.STOP_ID);
			GStop gStop = new GStop(stopId, line.get(GStop.STOP_NAME), line.get(GStop.STOP_LAT), line.get(GStop.STOP_LON));
			gStop.stop_code = line.get(GStop.STOP_CODE);
			gStop.stop_desc = line.get(GStop.STOP_DESC);
			gStop.zone_id = line.get(GStop.ZONE_ID);
			//if (stopIdFilter != null && !stopId.contains(stopIdFilter)) {
			if (agencyTools.excludeStop(gStop)) {
				continue;
			}
			stops.put(stopId, gStop);
		}
		System.out.println("Processing stops... DONE (" + stops.size() + " extracted)");
		return stops;
	}

	private static Map<String, GRoute> processRoutes(List<HashMap<String, String>> lines, GAgencyTools agencyTools/*String routeIdFilter, String routeTypeFilter*/) throws IOException {
		System.out.println("Processing routes...");
		Map<String, GRoute> routes = new HashMap<String, GRoute>();
		for (HashMap<String, String> line : lines) {
			String routeId = line.get(GRoute.ROUTE_ID);
			String routeType = line.get(GRoute.ROUTE_TYPE);
			GRoute gRoute = new GRoute(routeId, line.get(GRoute.ROUTE_SHORT_NAME), line.get(GRoute.ROUTE_LONG_NAME), routeType);
			gRoute.route_color = line.get(GRoute.ROUTE_COLOR);
			gRoute.route_text_color = line.get(GRoute.ROUTE_TEXT_COLOR);
//			if (ROUTE_ID != null && !routeId.equals(ROUTE_ID)) {
//				continue;
//			}
			if (agencyTools.excludeRoute(gRoute)) {
				continue;
			}
//			if (routeTypeFilter != null && !routeType.equals(routeTypeFilter)) {
//				continue;
//			}
//			if (routeIdFilter != null && !routeId.equals(routeIdFilter)) {
//				continue;
//			}
//			if (ROUTE_ID_START_WITH != null && !routeId.startsWith(ROUTE_ID_START_WITH)) {
//				continue;
//			}
			routes.put(routeId, gRoute);
		}
		System.out.println("Processing routes... DONE (" + routes.size() + " extracted)");
		return routes;
	}

	// private static List<GtfsAgency> processAgency(List<HashMap<String, String>> lines) throws IOException {
	// System.out.println("Processing agency...");
	// List<GtfsAgency> agencies = new ArrayList<GtfsAgency>();
	// System.out.printf("agency lines: %d\n", lines.size());
	// for (Map<String, String> line : lines) {
	// agencies.add(new GtfsAgency(line.get(GtfsAgency.AGENCY_ID), line.get(GtfsAgency.AGENCY_NAME), line.get(GtfsAgency.AGENCY_URL), line
	// .get(GtfsAgency.AGENCY_TIMEZONE)));
	// }
	// System.out.println("Processing agency... DONE (" + agencies.size() + " extracted)");
	// return agencies;
	// }

	// For testing the code.
//	public static void main1(String[] args) throws NumberFormatException, MalformedURLException, IOException {
//		long start = System.currentTimeMillis();
//		// String zipPath = args[0];
////		String zipPath = ZIP_PATH;
//
//		GSpec gtfs = GReader.readGtfsZipFile(STMBus.GTFS_FILE, STMBus.ROUTE_TYPE_FILTER, STMBus.SERVICE_ID_FILTER);
//		gtfs.tripStops = extractTripStops(gtfs);
//
//		// Generate MY OBJECTS!
//
//		MSpec my = GReader.generateMyOjects(gtfs);
//
//		// PRINTING
//		File file = null;
//		BufferedWriter ow = null;
//		System.out.println("----------");
//		System.out.println("ROUTES: " + my.routes.size());
//		file = new File(STMBus.DUMP_DIR, "routes");
//		file.delete(); // delete previous
//		try {
//			ow = new BufferedWriter(new FileWriter(file));
//			for (MRoute mRoute : my.routes) {
//				// System.out.println(mRoute.toString());
//				ow.write(mRoute.toString());
//				ow.write('\n');
//			}
//		} catch (IOException ioe) {
//			System.out.println("I/O Error while writing route file!");
//			ioe.printStackTrace();
//		} finally {
//			if (ow != null) {
//				ow.close();
//			}
//		}
//		// System.out.println("----------");
//		System.out.println("TRIPS: " + my.trips.size());
//		file = new File(STMBus.DUMP_DIR, "trips");
//		file.delete(); // delete previous
//		try {
//			ow = new BufferedWriter(new FileWriter(file));
//			for (MTrip mTrip : my.trips) {
//				// System.out.println(mTrip.toString());
//				ow.write(mTrip.toString());
//				ow.write('\n');
//			}
//		} catch (IOException ioe) {
//			System.out.println("I/O Error while writing trip file!");
//			ioe.printStackTrace();
//		} finally {
//			if (ow != null) {
//				ow.close();
//			}
//		}
//		// System.out.println("----------");
//		System.out.println("TRIP_STOPS: " + my.tripStops.size());
//		file = new File(STMBus.DUMP_DIR, "trip_stops");
//		file.delete(); // delete previous
//		try {
//			ow = new BufferedWriter(new FileWriter(file));
//			for (MTripStop mTripStop : my.tripStops) {
//				// System.out.println(mTripStop.toString());
//				ow.write(mTripStop.toString());
//				ow.write('\n');
//			}
//		} catch (IOException ioe) {
//			System.out.println("I/O Error while writing trip stops file!");
//			ioe.printStackTrace();
//		} finally {
//			if (ow != null) {
//				ow.close();
//			}
//		}
//		// System.out.println("----------");
//		System.out.println("STOPS: " + my.stops.size());
//		file = new File(STMBus.DUMP_DIR, "stops");
//		file.delete(); // delete previous
//		try {
//			ow = new BufferedWriter(new FileWriter(file));
//			for (MStop mStop : my.stops) {
//				// System.out.println(mStop.toString());
//				ow.write(mStop.toString());
//				ow.write('\n');
//			}
//		} catch (IOException ioe) {
//			System.out.println("I/O Error while writing stop file!");
//			ioe.printStackTrace();
//		} finally {
//			if (ow != null) {
//				ow.close();
//			}
//		}
//		System.out.println("----------");
//
//		// Map<String, MyTrip> myTrips = new HashMap<String, MyTrip>();
//		// for (GtfsTrip gTrip : gtfs.trips.values()) {
//		// MyTrip mTrip = new MyTrip(gTrip.trip_id, gTrip.trip_headsign, STMBus.getDirection(gTrip.trip_headsign), gTrip.route_id);
//		// if (myTrips.containsKey(mTrip.getId())) {
//		// System.out.println("Trip " + mTrip.getId() + " already in list!");
//		// }
//		// myTrips.put(mTrip.getId(), mTrip);
//		// }
//
//		// System.out.printf("RouteStops: %d\n", gtfs.routeStops.size());
//		// List<RouteStop> routeStops = new ArrayList<RouteStop>(gtfs.routeStops.values());
//		// Collections.sort(routeStops, new Comparator<RouteStop>() {
//		// @Override
//		// public int compare(RouteStop rs1, RouteStop rs2) {
//		// if (!rs1.route_id.equals(rs2.route_id)) {
//		// return rs1.route_id.compareTo(rs2.route_id);
//		// }
//		// if (rs1.stop_sequence > rs2.stop_sequence) {
//		// return +1;
//		// } else {
//		// return -1;
//		// }
//		// }
//		// });
//		// for (RouteStop rs : routeStops) {
//		// System.out.printf(" - route: %s | stop: %s | stop_sequence: %s\n", rs.route_id, rs.stop_id, rs.stop_sequence);
//		// }
//
//		// // for (Agency agency : gtfs.agencies) {
//		// // System.out.println("- Agency: " + agency.agency_id);
//		// for (Route route : gtfs.routes) {
//		// // if (agency.agency_id.equals(route.agency_id)) {
//		// System.out.println("  - Route " + route.route_id + " (" + route.route_short_name + ")");
//		// for (RouteStop routeStop : gtfs.routeStops.values()) {
//		// if (route.route_id.equals(routeStop.route_id)) {
//		// Stop stop = gtfs.stops.get(routeStop.stop_id);
//		// System.out.println("   - Stop " + routeStop.stop_id + "(" + routeStop.stop_sequence + ") " + stop.stop_name);
//		// }
//		// }
//		// // }
//		// }
//		// // }
//		System.out.println("DONE in " + ((System.currentTimeMillis() - start) / 1000) + " seconds");
//		System.exit(0);
//	}
	
	
//	private static MSpec generateMyOjects(GSpec gtfs) {
//		
////		MySpec myObjects = new MySpec(new ArrayList<MyStop>(), new ArrayList<MyRoute>(), new ArrayList<MyTrip>(), new ArrayList<MyTripStop>());
//		List<MRoute> myRoutes = new ArrayList<MRoute>();
//		List<MTrip> myTrips = new ArrayList<MTrip>();
//		List<MTripStop> myTripStops = new ArrayList<MTripStop>();
//		
//		Map<Integer, MStop> myStops = generateMyStops(gtfs);
//		
//		ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(4);
//		List<Future<MSpec>> list = new ArrayList<Future<MSpec>>();
////		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));
//		
////		int MAX_NUMBER = 100;
////		int i = 0;
//		Map<Integer, GSpec> routeToSpec = splitByRouteId(gtfs);
//		for (Entry<Integer, GSpec> rts : routeToSpec.entrySet()) {
////			System.out.println(rts.getKey() + ": scheduled > gRoutes: " + rts.getValue().routes.size() + ", gTrips: " + rts.getValue().trips.size() + ", gTripStops: " + rts.getValue().tripStops.size());
////			threadPoolExecutor.execute(/*.submit(*/new GenerateMyObjectsTask(rts.getKey(), rts.getValue(), gtfs.stops, myStops));
//			Future<MSpec> submit = threadPoolExecutor.submit(new GenerateMyObjectsTask(rts.getKey(), rts.getValue(), gtfs.stops, myStops));
//			list.add(submit);
////			if (++i >= MAX_NUMBER) {
////				break;
////			}
//		}
//		for (Future<MSpec> future : list) {
//		      try {
//		        MSpec myrouteSpec = future.get();
////		        System.out.println(myrouteSpec.routes.get(0).id + ": result > routes:"+myrouteSpec.routes.size()+",trips:"+myrouteSpec.trips.size()+",tripstops:"+myrouteSpec.tripStops.size());
//		        myRoutes.addAll(myrouteSpec.routes);
//		        myTrips.addAll(myrouteSpec.trips);
//		        myTripStops.addAll(myrouteSpec.tripStops);
//		      } catch (InterruptedException e) {
//		        e.printStackTrace();
//		      } catch (ExecutionException e) {
//		        e.printStackTrace();
//		      }
//		    }
////		myObjects.stops = myStops.values();
//		List<MStop> myStopsList = new ArrayList<MStop>(myStops.values());
//		Collections.sort(myStopsList);
////		List<MyRoute> myRoutesList = new ArrayList<MyRoute>(myRoutes.values());
//		Collections.sort(myRoutes);
////		List<MyTrip> myTripsList = new ArrayList<MyTrip>(myTrips.values());
//		Collections.sort(myTrips);
////		List<MyTripStop> myTripStopsList = new ArrayList<MyTripStop>(allMyTripStops.values());
//		Collections.sort(myTripStops);
//		threadPoolExecutor.shutdown();
////	    while (!threadPoolExecutor.isTerminated()) {
////	    	try {
////				Thread.sleep(1000);
////			} catch (InterruptedException e) {
////				e.printStackTrace();
////			}
////	    	System.out.print(".");
////	    }
////	    System.out.println("Finished all threads");
////		// remove not used stops TODO ?
////		int removedStopsCount = 0;
////		for (Iterator<Map.Entry<Integer, MyStop>> it = myStops.entrySet().iterator(); it.hasNext();) {
////			if (!tripStopIds.contains(it.next().getKey())) {
////				it.remove();
////				removedStopsCount++;
////			}
////		}
////		System.out.println("Removed " + removedStopsCount + " useless stops.");
////		System.out.println("EXITING");
////		System.exit(1); //FIXME remove
//	    return new MSpec(myStopsList, myRoutes, myTrips, myTripStops);
//	}
	
	public static Map<Integer, GSpec> splitByRouteId(GSpec gtfs, GAgencyTools agencyTools) {
		Map<Integer, GSpec> routeToSpec = new HashMap<Integer, GSpec>();
		Map<String,Integer> gRouteIdToMyRouteId = new HashMap<String, Integer>();
		Map<String,Integer> gTripIdToMyRouteId = new HashMap<String, Integer>();
		for (Entry<String, GRoute> gRoute : gtfs.routes.entrySet()) {
			int routeId = agencyTools.getRouteId(gRoute.getValue());
			gRouteIdToMyRouteId.put(gRoute.getValue().route_id, routeId);
			if (!routeToSpec.containsKey(routeId)) {
				routeToSpec.put(routeId, new GSpec(new HashMap<String, GStop>(), new HashMap<String, GRoute>() , new HashMap<String, GTrip>(), new ArrayList<GStopTime>()));
				routeToSpec.get(routeId).tripStops = new HashMap<String, GTripStop>();
			}
			if (routeToSpec.get(routeId).routes.containsKey(gRoute.getKey())) {
				System.out.println("Route ID "+gRoute.getValue().route_id+" already present!");
				System.exit(-1);
			}
			routeToSpec.get(routeId).routes.put(gRoute.getKey(), gRoute.getValue());
		}
//		System.out.println("gRouteToMyRouteIds: " + gRouteIdToMyRouteId.size());
		for (Entry<String, GTrip> gTrip : gtfs.trips.entrySet()) {
			if (!gRouteIdToMyRouteId.containsKey(gTrip.getValue().route_id)) {
//				System.out.println("Trip's Route ID '"+gTrip.getValue().route_id+"' not already present!");
//				System.exit(-1);
				continue; // not processed now (subway line...)
			}
			int routeId = gRouteIdToMyRouteId.get(gTrip.getValue().route_id);
			gTripIdToMyRouteId.put(gTrip.getValue().trip_id, routeId);
			if (!routeToSpec.containsKey(routeId)) {
				System.out.println("Trip's Route ID "+routeId+" not already present!");
				System.exit(-1);
			}
			if (routeToSpec.get(routeId).trips.containsKey(gTrip.getKey())) {
				System.out.println("Trip ID "+gTrip.getValue().trip_id+" already present!");
				System.exit(-1);
			}
			routeToSpec.get(routeId).trips.put(gTrip.getKey(), gTrip.getValue());
		}
//		System.out.println("gTripIdToMyRouteIds: " + gTripIdToMyRouteId.size());
		for (Entry<String, GTripStop> gTripStop : gtfs.tripStops.entrySet()) {
			if (!gTripIdToMyRouteId.containsKey(gTripStop.getValue().trip_id)) {
//				System.out.println("Trip's Route ID '"+gTripStop.getValue().route_id+"' not already present!");
//				System.exit(-1);
				continue; // not processed now (subway line...)
			}
			int routeId = gTripIdToMyRouteId.get(gTripStop.getValue().trip_id);
			if (!routeToSpec.containsKey(routeId)) {
				System.out.println("Trip Stop's Route ID "+routeId+" not already present!");
				System.exit(-1);
			}
			if (routeToSpec.get(routeId).tripStops.containsKey(gTripStop.getKey())) {
				System.out.println("Trip stop ID "+gTripStop.getValue().trip_id+" already present!");
				System.exit(-1);
			}
			routeToSpec.get(routeId).tripStops.put(gTripStop.getKey(), gTripStop.getValue());
		}
		return routeToSpec;
	}
	
//	public class GenerateMyObjectsTask implements Runnable {
//
//		private int routeId;
//		private GtfsSpec gtfs;
//		private Map<Integer, MyStop> stops;
//
//		public GenerateMyObjectsTask(int routeId, GtfsSpec gtfs, Map<Integer, MyStop> stops) {
//			this.routeId = routeId;
//			this.gtfs = gtfs;
//			this.stops = stops;
//		}
//
//		@Override
//		public void run() {
//			System.out.println("Starting route " + this.routeId + "... ");
//			
//			Map<Integer, MyRoute> myRoutes = new HashMap<Integer, MyRoute>();
//			Map<String, MyTrip> myTrips = new HashMap<String, MyTrip>();
//			Map<String, MyTripStop> allMyTripStops = new HashMap<String, MyTripStop>();
//			Set<Integer> tripStopIds = new HashSet<Integer>(); // the list of stop IDs used by trips
//			for (GtfsRoute gRoute : gtfs.routes.values()) {
//				MyRoute mRoute = new MyRoute(STMBus.getRouteId(gRoute), gRoute.route_short_name, gRoute.route_long_name/* , gRoute.route_type */);
//				if (myRoutes.containsKey(mRoute.id)) {
//					System.out.println("Route " + mRoute.id + " already in list!");
//					System.exit(-1);
//				}
//				// find route trips
//				Map<String, List<MyTripStop>> tripIdToMyTripStops = new HashMap<String, List<MyTripStop>>();
//				for (GtfsTrip gTrip : gtfs.trips.values()) {
//					if (!gTrip.route_id.equals(gRoute.route_id)) {
//						continue;
//					}
//					String mTripId = STMBus.getTripId(gTrip); // 33-S / 33-N
//
//					// find route trip stops
//					Map<String, MyTripStop> mTripStops = new HashMap<String, MyTripStop>();
//					for (GtfsTripStop gTripStop : gtfs.tripStops.values()) {
//						if (!gTripStop.trip_id.equals(gTrip.trip_id)) {
//							continue;
//						}
//						MyTripStop mTripStop = new MyTripStop(mTripId, STMBus.getStopId(gtfs.stops.get(gTripStop.stop_id)), gTripStop.stop_sequence,
//								MyDropOffType.parse(gTripStop.drop_off_type.id), MyPickupType.parse(gTripStop.pickup_type.id));
//						if (mTripStops.containsKey(mTripStop.getUID()) && !mTripStops.get(mTripStop.getUID()).equals(mTripStop)) {
//							System.out.println("Different trip stop " + mTripStop.getUID() + " already in list(" + mTripStop.toString() + " != "
//									+ mTripStops.get(mTripStop.getUID()).toString() + ")!");
//							System.exit(-1);
//						}
//						// if (myTripStops.containsKey(mTripStop.getUID()) && !myTripStops.get(mTripStop.getUID()).equals(mTripStop)) {
//						// System.out.println("Different trip stop " + mTripStop.getUID() + " already in list(" + mTripStop.toString() + " != "
//						// + myTripStops.get(mTripStop.getUID()).toString() + ")!");
//						// System.exit(-1);
//						// }
//						// check stop presence
//						if (!this.stops.containsKey(mTripStop.stopId)) {
//							System.out.println("Stop '" + mTripStop.stopId + "' required by trip " + mTripStop.tripId + " not present!");
//							System.exit(-1);
//						}
//						mTripStops.put(mTripStop.getUID(), mTripStop);
//						// myTripStops.put(mTripStop.getUID(), mTripStop);
//					}
//					List<MyTripStop> mTripStopsList = new ArrayList<MyTripStop>(mTripStops.values());
//					Collections.sort(mTripStopsList);
//					if (tripIdToMyTripStops.containsKey(mTripId)) {
//						List<MyTripStop> cTripStopsList = tripIdToMyTripStops.get(mTripId);
//						if (!equalsMyTripStopLists(mTripStopsList, cTripStopsList)) {
//							// System.out.println("Need to merge gtfs trip: " + gTrip.trip_id);
//							tripIdToMyTripStops.put(mTripId, mergeMyTripStopLists(mTripStopsList, cTripStopsList));
//						}
//					} else {
//						// just use it
//						tripIdToMyTripStops.put(mTripId, mTripStopsList);
//					}
//					MyTrip mTrip = new MyTrip(mTripId, mRoute.id);
//					// STMBus.getTripHeadsign(gTrip), STMBus.getDirection(gTrip), MyInboundType.parse(gTrip.direction_id),
//					STMBus.setTripHeadsign(mTrip, gTrip, mTripStops.values());
//					if (myTrips.containsKey(mTrip.id) && !myTrips.get(mTrip.id).equals(mTrip)) {
//						System.out
//								.println("Different trip " + mTrip.id + " already in list (" + mTrip.toString() + " != " + myTrips.get(mTrip.id).toString() + ")");
//						System.exit(-1);
//					}
//					myTrips.put(mTrip.id, mTrip);
//				}
//				for (List<MyTripStop> entry : tripIdToMyTripStops.values()) {
//					for (MyTripStop myTripStop : entry) {
//						if (allMyTripStops.containsKey(myTripStop.getUID()) && !allMyTripStops.get(myTripStop.getUID()).equals(myTripStop)) {
//							System.out.println("Different trip stop " + myTripStop.getUID() + " already in list(" + myTripStop.toString() + " != "
//									+ allMyTripStops.get(myTripStop.getUID()).toString() + ")!");
//							System.exit(-1);
//						}
//						allMyTripStops.put(myTripStop.getUID(), myTripStop);
//						tripStopIds.add(myTripStop.stopId);
//					}
//				}
//				myRoutes.put(mRoute.id, mRoute);
//			}
////			// remove not used stops
////			int removedStopsCount = 0;
////			for (Iterator<Map.Entry<Integer, MyStop>> it = myStops.entrySet().iterator(); it.hasNext();) {
////				if (!tripStopIds.contains(it.next().getKey())) {
////					it.remove();
////					removedStopsCount++;
////				}
////			}
////			System.out.println("Removed " + removedStopsCount + " useless stops.");
//			// put in sorter list
//			List<MyStop> myStopsList = null; // new ArrayList<MyStop>(myStops.values());
////			Collections.sort(myStopsList);
//			List<MyRoute> myRoutesList = new ArrayList<MyRoute>(myRoutes.values());
//			Collections.sort(myRoutesList);
//			List<MyTrip> myTripsList = new ArrayList<MyTrip>(myTrips.values());
//			Collections.sort(myTripsList);
//			List<MyTripStop> myTripStopsList = new ArrayList<MyTripStop>(allMyTripStops.values());
//			Collections.sort(myTripStopsList);
//			
//			MySpec myrouteSpec = new MySpec(myStopsList, myRoutesList, myTripsList, myTripStopsList);
////			return new MySpec(myStopsList, myRoutesList, myTripsList, myTripStopsList);
//			
//			System.out.println("Starting route " + this.routeId + "... DONE (routes:"+myrouteSpec.routes.size()+",trips:"+myrouteSpec.trips.size()+",tripstops:"+myrouteSpec.tripStops.size()+")");
//		}
//	}

//	private static MySpec generateMyOjects2(GtfsSpec gtfs) {
//		Map<Integer, MyStop> myStops = generateMyStops(gtfs);
//		Map<Integer, MyRoute> myRoutes = new HashMap<Integer, MyRoute>();
//		Map<String, MyTrip> myTrips = new HashMap<String, MyTrip>();
//		Map<String, MyTripStop> allMyTripStops = new HashMap<String, MyTripStop>();
//		Set<Integer> tripStopIds = new HashSet<Integer>(); // the list of stop IDs used by trips
//		for (GtfsRoute gRoute : gtfs.routes.values()) {
//			MyRoute mRoute = new MyRoute(STMBus.getRouteId(gRoute), gRoute.route_short_name, gRoute.route_long_name/* , gRoute.route_type */);
//			if (myRoutes.containsKey(mRoute.id)) {
//				System.out.println("Route " + mRoute.id + " already in list!");
//				System.exit(-1);
//			}
//			// find route trips
//			Map<String, List<MyTripStop>> tripIdToMyTripStops = new HashMap<String, List<MyTripStop>>();
//			for (GtfsTrip gTrip : gtfs.trips.values()) {
//				if (!gTrip.route_id.equals(gRoute.route_id)) {
//					continue;
//				}
//				String mTripId = STMBus.getTripId(gTrip); // 33-S / 33-N
//
//				// find route trip stops
//				Map<String, MyTripStop> mTripStops = new HashMap<String, MyTripStop>();
//				for (GtfsTripStop gTripStop : gtfs.tripStops.values()) {
//					if (!gTripStop.trip_id.equals(gTrip.trip_id)) {
//						continue;
//					}
//					MyTripStop mTripStop = new MyTripStop(mTripId, STMBus.getStopId(gtfs.stops.get(gTripStop.stop_id)), gTripStop.stop_sequence,
//							MyDropOffType.parse(gTripStop.drop_off_type.id), MyPickupType.parse(gTripStop.pickup_type.id));
//					if (mTripStops.containsKey(mTripStop.getUID()) && !mTripStops.get(mTripStop.getUID()).equals(mTripStop)) {
//						System.out.println("Different trip stop " + mTripStop.getUID() + " already in list(" + mTripStop.toString() + " != "
//								+ mTripStops.get(mTripStop.getUID()).toString() + ")!");
//						System.exit(-1);
//					}
//					// if (myTripStops.containsKey(mTripStop.getUID()) && !myTripStops.get(mTripStop.getUID()).equals(mTripStop)) {
//					// System.out.println("Different trip stop " + mTripStop.getUID() + " already in list(" + mTripStop.toString() + " != "
//					// + myTripStops.get(mTripStop.getUID()).toString() + ")!");
//					// System.exit(-1);
//					// }
//					// check stop presence
//					if (!myStops.containsKey(mTripStop.stopId)) {
//						System.out.println("Stop '" + mTripStop.stopId + "' required by trip " + mTripStop.tripId + " not present!");
//						System.exit(-1);
//					}
//					mTripStops.put(mTripStop.getUID(), mTripStop);
//					// myTripStops.put(mTripStop.getUID(), mTripStop);
//				}
//				List<MyTripStop> mTripStopsList = new ArrayList<MyTripStop>(mTripStops.values());
//				Collections.sort(mTripStopsList);
//				if (tripIdToMyTripStops.containsKey(mTripId)) {
//					List<MyTripStop> cTripStopsList = tripIdToMyTripStops.get(mTripId);
//					if (!equalsMyTripStopLists(mTripStopsList, cTripStopsList)) {
//						// System.out.println("Need to merge gtfs trip: " + gTrip.trip_id);
//						tripIdToMyTripStops.put(mTripId, mergeMyTripStopLists(mTripStopsList, cTripStopsList));
//					}
//				} else {
//					// just use it
//					tripIdToMyTripStops.put(mTripId, mTripStopsList);
//				}
//				MyTrip mTrip = new MyTrip(mTripId, mRoute.id);
//				// STMBus.getTripHeadsign(gTrip), STMBus.getDirection(gTrip), MyInboundType.parse(gTrip.direction_id),
//				STMBus.setTripHeadsign(mTrip, gTrip, mTripStops.values());
//				if (myTrips.containsKey(mTrip.id) && !myTrips.get(mTrip.id).equals(mTrip)) {
//					System.out
//							.println("Different trip " + mTrip.id + " already in list (" + mTrip.toString() + " != " + myTrips.get(mTrip.id).toString() + ")");
//					System.exit(-1);
//				}
//				myTrips.put(mTrip.id, mTrip);
//			}
//			for (List<MyTripStop> entry : tripIdToMyTripStops.values()) {
//				for (MyTripStop myTripStop : entry) {
//					if (allMyTripStops.containsKey(myTripStop.getUID()) && !allMyTripStops.get(myTripStop.getUID()).equals(myTripStop)) {
//						System.out.println("Different trip stop " + myTripStop.getUID() + " already in list(" + myTripStop.toString() + " != "
//								+ allMyTripStops.get(myTripStop.getUID()).toString() + ")!");
//						System.exit(-1);
//					}
//					allMyTripStops.put(myTripStop.getUID(), myTripStop);
//					tripStopIds.add(myTripStop.stopId);
//				}
//			}
//			myRoutes.put(mRoute.id, mRoute);
//		}
//		// remove not used stops
//		int removedStopsCount = 0;
//		for (Iterator<Map.Entry<Integer, MyStop>> it = myStops.entrySet().iterator(); it.hasNext();) {
//			if (!tripStopIds.contains(it.next().getKey())) {
//				it.remove();
//				removedStopsCount++;
//			}
//		}
//		System.out.println("Removed " + removedStopsCount + " useless stops.");
//		// put in sorter list
//		List<MyStop> myStopsList = new ArrayList<MyStop>(myStops.values());
//		Collections.sort(myStopsList);
//		List<MyRoute> myRoutesList = new ArrayList<MyRoute>(myRoutes.values());
//		Collections.sort(myRoutesList);
//		List<MyTrip> myTripsList = new ArrayList<MyTrip>(myTrips.values());
//		Collections.sort(myTripsList);
//		List<MyTripStop> myTripStopsList = new ArrayList<MyTripStop>(allMyTripStops.values());
//		Collections.sort(myTripStopsList);
//		return new MySpec(myStopsList, myRoutesList, myTripsList, myTripStopsList);
//	}

	

//	public static void main2(String[] args) {
//		List<MyTripStop> l1 = new ArrayList<MyTripStop>();
//		l1.add(new MyTripStop("tripid", 50001, 1));
//		l1.add(new MyTripStop("tripid", 50002, 2));
//		l1.add(new MyTripStop("tripid", 50003, 3));
//		l1.add(new MyTripStop("tripid", 50004, 4));
//		l1.add(new MyTripStop("tripid", 50005, 5));
//		List<MyTripStop> l2 = new ArrayList<MyTripStop>();
//		l2.add(new MyTripStop("tripid", 50001, 1));
//		l2.add(new MyTripStop("tripid", 50002, 2));
//		l2.add(new MyTripStop("tripid", 50007, 3));
//		l2.add(new MyTripStop("tripid", 50003, 4));
//		l2.add(new MyTripStop("tripid", 50004, 5));
//		l2.add(new MyTripStop("tripid", 50005, 6));
//		List<MyTripStop> nl = mergeMyTripStopLists(l1, l2);
//		System.out.println("nl size = " + nl.size());
//		System.out.println("nl 3 = " + nl.get(2).stopId + "-" + nl.get(2).stopSequence);
//		System.out.println("nl 4 = " + nl.get(3).stopId + "-" + nl.get(3).stopSequence);
//	}

//	private static boolean isInList(List<MTripStop> l, int stopId) {
//		for (MTripStop ts : l) {
//			if (ts.stopId == stopId) {
//				return true;
//			}
//		}
//		return false;
//	}

//	public static boolean equalsMyTripStopLists(List<MTripStop> l1, List<MTripStop> l2) {
//		if (l1 == null && l2 == null) {
//			return true;
//		}
//		if (l1.size() != l2.size()) {
//			return false;
//		}
//		for (int i = 0; i < l1.size(); i++) {
//			if (!l1.get(i).equals(l2.get(i))) {
//				return false;
//			}
//		}
//		return true;
//	}

//	private static Map<Integer, MStop> generateMyStops(GSpec gtfs) {
//		Map<Integer, MStop> myStops = new HashMap<Integer, MStop>();
//		for (GStop gStop : gtfs.stops.values()) {
//			MStop mStop = new MStop(STMBus.getStopId(gStop), gStop.stop_code, STMBus.cleanStopName(gStop.stop_name), gStop.stop_lat, gStop.stop_lon);
//			if (myStops.containsKey(mStop.id)) {
//				System.out.println("Stop " + mStop.id + " already in list!");
//				System.exit(-1);
//			}
//			myStops.put(mStop.id, mStop);
//		}
//		return myStops;
//	}

	// Not used.
	private GReader() {
	}
}
