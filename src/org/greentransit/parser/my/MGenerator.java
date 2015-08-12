package org.greentransit.parser.my;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.greentransit.parser.gtfs.GAgencyTools;
import org.greentransit.parser.gtfs.data.GSpec;
import org.greentransit.parser.gtfs.data.GStop;
import org.greentransit.parser.my.data.MRoute;
import org.greentransit.parser.my.data.MSchedule;
import org.greentransit.parser.my.data.MServiceDate;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MStop;
import org.greentransit.parser.my.data.MTrip;
import org.greentransit.parser.my.data.MTripStop;

public class MGenerator {

	public static MSpec generateMSpec(Map<Integer, GSpec> gtfsByMRouteId, Map<String, GStop> gStops, GAgencyTools agencyTools) {
		System.out.println("Generating routes, trips, trip stops & stops objects... ");
		List<MRoute> mRoutes = new ArrayList<MRoute>();
		List<MTrip> mTrips = new ArrayList<MTrip>();
		List<MTripStop> mTripStops = new ArrayList<MTripStop>();
		TreeMap<Integer, List<MSchedule>> mStopSchedules = new TreeMap<Integer, List<MSchedule>>();
		List<MServiceDate> mServiceDates = new ArrayList<MServiceDate>();
		ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(agencyTools.getThreadPoolSize());
		List<Future<MSpec>> list = new ArrayList<Future<MSpec>>();
		final List<Integer> routeIds = new ArrayList<Integer>(gtfsByMRouteId.keySet());
		Collections.sort(routeIds);
		for (Integer routeId : routeIds) {
			GSpec routeGTFS = gtfsByMRouteId.get(routeId);
			if (routeGTFS.trips == null || routeGTFS.trips.size() == 0) {
				System.out.println("Skip route ID " + routeId + " because no route trip.");
				continue;
			}
			// System.out.println(rts.getKey() + ": scheduled > gRoutes: " + rts.getValue().routes.size() + ", gTrips: " + rts.getValue().trips.size() +
			// ", gTripStops: " + rts.getValue().tripStops.size());
			final Future<MSpec> submit = threadPoolExecutor.submit(new GenerateMObjectsTask(agencyTools, routeId, routeGTFS, gStops/* , mStops */));
			list.add(submit);
		}
		for (Future<MSpec> future : list) {
			try {
				MSpec mRouteSpec = future.get();
				// System.out.println(myrouteSpec.routes.get(0).id +
				// ": result > routes:"+myrouteSpec.routes.size()+",trips:"+myrouteSpec.trips.size()+",tripstops:"+myrouteSpec.tripStops.size());
				mRoutes.addAll(mRouteSpec.routes);
				mTrips.addAll(mRouteSpec.trips);
				mTripStops.addAll(mRouteSpec.tripStops);
				for (Entry<Integer, List<MSchedule>> stopScheduleEntry : mRouteSpec.stopSchedules.entrySet()) {
					if (!mStopSchedules.containsKey(stopScheduleEntry.getKey())) {
						mStopSchedules.put(stopScheduleEntry.getKey(), new ArrayList<MSchedule>());
					}
					mStopSchedules.get(stopScheduleEntry.getKey()).addAll(stopScheduleEntry.getValue());
				}
				mServiceDates.addAll(mRouteSpec.serviceDates);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		threadPoolExecutor.shutdown();
		System.out.println("Generating stops objects... ");
		// generate trip stops stops IDs to check stop usefulness
		Set<Integer> tripStopStopIds = new HashSet<Integer>();
		for (MTripStop mTripStop : mTripStops) {
			tripStopStopIds.add(mTripStop.getStopId());
		}

		// generate stops
		List<MStop> mStopsList = new ArrayList<MStop>();
		Set<Integer> mStopIds = new HashSet<Integer>();
		int skippedStopsCount = 0;
		for (GStop gStop : gStops.values()) {
			MStop mStop = new MStop(agencyTools.getStopId(gStop), agencyTools.getStopCode(gStop), agencyTools.cleanStopName(gStop.stop_name), gStop.stop_lat,
					gStop.stop_lon);
			if (mStopIds.contains(mStop.id)) {
				System.out.println("Stop " + mStop.id + " already in list!");
				// System.exit(-1); // TODO what? should use calendar to only process the required bus stops?
				continue;
			}
			if (!tripStopStopIds.contains(mStop.id)) {
				skippedStopsCount++;
				continue;
			}
			mStopsList.add(mStop);
			mStopIds.add(mStop.id);
		}
		System.out.println("Skipped " + skippedStopsCount + " useless stops.");
		System.out.println("Generating stops objects... DONE");

		Collections.sort(mStopsList);
		Collections.sort(mRoutes);
		Collections.sort(mTrips);
		Collections.sort(mTripStops);
		Collections.sort(mServiceDates);
		// Collections.sort(mSchedules); TreeMap = sorted
		System.out.println("Generating routes, trips, trip stops & stops objects... DONE");
		System.out.printf("- Routes: %d\n", mRoutes.size());
		System.out.printf("- Trips: %d\n", mTrips.size());
		System.out.printf("- Trip stops: %d\n", mTripStops.size());
		System.out.printf("- Stops: %d\n", mStopsList.size());
		System.out.printf("- Service Dates: %d\n", mServiceDates.size());
		System.out.printf("- Stop Schedules: %d\n", mStopSchedules.size());
		return new MSpec(mStopsList, mRoutes, mTrips, mTripStops, mServiceDates, /* mRouteSchedules */null, mStopSchedules);
	}

	public static void dumpFiles(MSpec mSpec, String dumpDir, final String fileBase) {
		// TODO delete all files at the beginning and write data ASAP instead of keeping all in memory before here
		long start = System.currentTimeMillis();
		final File dumpDirF = new File(dumpDir);
		if (!dumpDirF.exists()) {
			dumpDirF.mkdir();
		}
		System.out.println("Writing " + "V1" + " files (" + dumpDirF.toURI() + ")...");
		File file = null;
		BufferedWriter ow = null;
		file = new File(dumpDirF, fileBase + "service_dates");
		file.delete(); // delete previous
		try {
			ow = new BufferedWriter(new FileWriter(file));
			for (MServiceDate mServiceDate : mSpec.serviceDates) {
				// System.out.println(mServiceDate.toString());
				ow.write(mServiceDate.toString());
				ow.write('\n');
			}
		} catch (IOException ioe) {
			System.out.println("I/O Error while writing service dates file!");
			ioe.printStackTrace();
			System.exit(-1);
		} finally {
			if (ow != null) {
				try {
					ow.close();
				} catch (IOException e) {
				}
			}
		}
		final File[] files = dumpDirF.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.startsWith(fileBase + "schedules_stop_");
			}
		});
		for (final File f : files) {
			if (!f.delete()) {
				System.err.println("Can't remove " + f.getAbsolutePath());
			}
		}
		List<String> allServiceIds = new ArrayList<String>();
		for (MServiceDate mServiceDate : mSpec.serviceDates) {
			allServiceIds.add(mServiceDate.serviceId);
		}
		for (Integer stopId : mSpec.stopSchedules.keySet()) {
			try {
				final List<MSchedule> mStopSchedules = mSpec.stopSchedules.get(stopId);
				if (mStopSchedules != null && mStopSchedules.size() > 0) {
					final String fileName = fileBase + "schedules_stop_" + stopId; // + ".json";
					file = new File(dumpDirF, fileName);
					boolean empty = true;
					ow = new BufferedWriter(new FileWriter(file));
					for (MSchedule mSchedule : mStopSchedules) {
						ow.write(mSchedule.toString()); // toJSON()?
						ow.write('\n');
						empty = false;
					}
					if (empty) {
						file.delete();
					}
				}
			} catch (IOException ioe) {
				System.out.println("I/O Error while writing schedule file!");
				ioe.printStackTrace();
				System.exit(-1);
			} finally {
				if (ow != null) {
					try {
						ow.close();
					} catch (IOException e) {
					}
				}
			}
		}
		file = new File(dumpDirF, fileBase + "routes");
		file.delete(); // delete previous
		try {
			ow = new BufferedWriter(new FileWriter(file));
			for (MRoute mRoute : mSpec.routes) {
				// System.out.println(mRoute.toString());
				ow.write(mRoute.toString());
				ow.write('\n');
			}
		} catch (IOException ioe) {
			System.out.println("I/O Error while writing route file!");
			ioe.printStackTrace();
			System.exit(-1);
		} finally {
			if (ow != null) {
				try {
					ow.close();
				} catch (IOException e) {
				}
			}
		}
		file = new File(dumpDirF, fileBase + "trips");
		file.delete(); // delete previous
		try {
			ow = new BufferedWriter(new FileWriter(file));
			for (MTrip mTrip : mSpec.trips) {
				// System.out.println(mTrip.toString());
				ow.write(mTrip.toString());
				ow.write('\n');
			}
		} catch (IOException ioe) {
			System.out.println("I/O Error while writing trip file!");
			ioe.printStackTrace();
			System.exit(-1);
		} finally {
			if (ow != null) {
				try {
					ow.close();
				} catch (IOException e) {
				}
			}
		}
		file = new File(dumpDirF, fileBase + "trip_stops");
		file.delete(); // delete previous
		try {
			ow = new BufferedWriter(new FileWriter(file));
			for (MTripStop mTripStop : mSpec.tripStops) {
				ow.write(mTripStop.toString());
				ow.write('\n');
			}
		} catch (IOException ioe) {
			System.out.println("I/O Error while writing trip stops file!");
			ioe.printStackTrace();
			System.exit(-1);
		} finally {
			if (ow != null) {
				try {
					ow.close();
				} catch (IOException e) {
				}
			}
		}
		file = new File(dumpDirF, fileBase + "stops");
		file.delete(); // delete previous
		try {
			ow = new BufferedWriter(new FileWriter(file));
			for (MStop mStop : mSpec.stops) {
				ow.write(mStop.toString());
				ow.write('\n');
			}
		} catch (IOException ioe) {
			System.out.println("I/O Error while writing stop file!");
			ioe.printStackTrace();
			System.exit(-1);
		} finally {
			if (ow != null) {
				try {
					ow.close();
				} catch (IOException e) {
				}
			}
		}
		System.out.println("Writing files (" + dumpDirF.toURI() + ")... DONE in " + MGenerator.getPrettyDuration(System.currentTimeMillis() - start) + ".");
	}

	public static Integer getLastStopId(List<MTripStop> tripStops) {
		Collections.sort(tripStops);
		return tripStops.get(tripStops.size() - 1).getStopId();
	}

	public static final long MILLIS_PER_MILLIS = 1;
	public static final long MILLIS_PER_SECOND = 1000;
	public static final long SECONDS_PER_MINUTE = 60;
	public static final long MINUTES_PER_HOUR = 60;
	public static final long MILLIS_PER_MINUTE = SECONDS_PER_MINUTE * MILLIS_PER_SECOND;
	public static final long MILLIS_PER_HOUR = MINUTES_PER_HOUR * MILLIS_PER_MINUTE;

	public static String getPrettyDuration(long durationInMs) {
		StringBuilder sb = new StringBuilder();
		long ms = durationInMs / MILLIS_PER_MILLIS % MILLIS_PER_SECOND;
		durationInMs -= ms * MILLIS_PER_MILLIS;
		long s = durationInMs / MILLIS_PER_SECOND % SECONDS_PER_MINUTE;
		durationInMs -= s * MILLIS_PER_SECOND;
		long m = durationInMs / MILLIS_PER_MINUTE % MINUTES_PER_HOUR;
		durationInMs -= m * MILLIS_PER_MINUTE;
		long h = durationInMs / MILLIS_PER_HOUR;
		boolean printing = false;
		if (printing || h > 0) {
			printing = true;
			sb.append(h).append("h ");
		}
		if (printing || m > 0) {
			printing = true;
			sb.append(m).append("m ");
		}
		if (printing || s > 0) {
			printing = true;
			sb.append(s).append("s ");
		}
		sb.append(ms).append("ms ");
		return sb.toString();
	}
}
