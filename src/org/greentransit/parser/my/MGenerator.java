package org.greentransit.parser.my;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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

	private static Map<Integer, MStop> generateMStops(Map<String, GStop> gStops, GAgencyTools agencyTools) {
		// System.out.println("Generating stops objects... ");
		Map<Integer, MStop> mStops = new HashMap<Integer, MStop>();
		for (GStop gStop : gStops.values()) {
			MStop mStop = new MStop(agencyTools.getStopId(gStop), agencyTools.getStopCode(gStop), agencyTools.cleanStopName(gStop.stop_name), gStop.stop_lat,
					gStop.stop_lon);
			if (mStops.containsKey(mStop.id) && !mStop.equals(mStops.get(mStop.id))) {
				System.out.println("Stop " + mStop.id + " already in list!");
				System.out.println(mStop.toString());
				System.out.println(mStops.get(mStop.id));
				// System.exit(-1); // TODO HERE NOW what? should use calendar to only process the required bus stops?
			}
			mStops.put(mStop.id, mStop);
		}
		// System.out.println("Generating stops objects... DONE");
		return mStops;
	}

	public static MSpec generateMSpec(Map<Integer, GSpec> gtfsByMRouteId, Map<String, GStop> gStops, GAgencyTools agencyTools) {
		System.out.println("Generating routes, trips, trip stops & stops objects... ");
		Map<Integer, MStop> mStops = generateMStops(gStops, agencyTools);
		List<MRoute> mRoutes = new ArrayList<MRoute>();
		List<MTrip> mTrips = new ArrayList<MTrip>();
		List<MTripStop> mTripStops = new ArrayList<MTripStop>();
		TreeMap<Integer, List<MSchedule>> mSchedules = new TreeMap<Integer, List<MSchedule>>();
		List<MServiceDate> mServiceDates = new ArrayList<MServiceDate>();
		ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(agencyTools.getThreadPoolSize());
		List<Future<MSpec>> list = new ArrayList<Future<MSpec>>();
		for (Entry<Integer, GSpec> rts : gtfsByMRouteId.entrySet()) {
			// System.out.println(rts.getKey() + ": scheduled > gRoutes: " + rts.getValue().routes.size() + ", gTrips: " + rts.getValue().trips.size() +
			// ", gTripStops: " + rts.getValue().tripStops.size());
			Future<MSpec> submit = threadPoolExecutor.submit(new GenerateMObjectsTask(agencyTools, rts.getKey(), rts.getValue(), gStops, mStops));
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
				mSchedules.putAll(mRouteSpec.schedules);
				mServiceDates.addAll(mRouteSpec.serviceDates);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		threadPoolExecutor.shutdown();
		// generate trip stops IDs
		Set<Integer> tripStopIds = new HashSet<Integer>();
		for (MTripStop mTripStop : mTripStops) {
			tripStopIds.add(mTripStop.stopId);
		}
		// remove useless stops
		int removedStopsCount = 0;
		for (Iterator<Map.Entry<Integer, MStop>> it = mStops.entrySet().iterator(); it.hasNext();) {
			if (!tripStopIds.contains(it.next().getKey())) {
				it.remove();
				removedStopsCount++;
			}
		}
		System.out.println("Removed " + removedStopsCount + " useless stops.");

		List<MStop> mStopsList = new ArrayList<MStop>(mStops.values());
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
		System.out.printf("- Stops: %d\n", mStops.size());
		System.out.printf("- Service Dates: %d\n", mServiceDates.size());
		System.out.printf("- Schedules: %d\n", mSchedules.size());
		return new MSpec(mStopsList, mRoutes, mTrips, mTripStops, mServiceDates, mSchedules);
	}

	public static void dumpFiles(MSpec mSpec, String dumpDir, final String fileBase) {
		// TODO delete all files at the beginning and write data ASAP instead of keeping all in memory before here
		File file = null;
		BufferedWriter ow = null;
		file = new File(dumpDir, fileBase + "service_dates");
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
		// delete all "...schedules_route_*"
		final File[] files = new File(dumpDir).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				// return name.matches(fileBase + "schedules_route_*");
				return name.startsWith(fileBase + "schedules_route_");
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
		for (Integer routeId : mSpec.schedules.keySet()) {
			for (String serviceId : allServiceIds) {
				// file = new File(dumpDir, fileBase + "schedules_route_" + routeId + "_service_" + MSpec.escape(serviceId).toLowerCase(Locale.ENGLISH));
				// file.delete(); // delete previous
				try {
					final List<MSchedule> mRouteSchedules = mSpec.schedules.get(routeId);
					if (mRouteSchedules != null && mRouteSchedules.size() > 0) {
						final String fileName = fileBase + "schedules_route_" + routeId + "_service_" + MSpec.escape(serviceId).toLowerCase(Locale.ENGLISH); // no upper case in Android res files!
						file = new File(dumpDir, fileName);
						// file.delete(); // delete previous
						ow = new BufferedWriter(new FileWriter(file));
						for (MSchedule mSchedule : mRouteSchedules) {
							if (mSchedule.serviceId.equals(serviceId)) {
								// System.out.println(mSchedule.toString());
								ow.write(mSchedule.toString());
								ow.write('\n');
							}
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
		}
		file = new File(dumpDir, fileBase + "routes");
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
		file = new File(dumpDir, fileBase + "trips");
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
		file = new File(dumpDir, fileBase + "trip_stops");
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
		file = new File(dumpDir, fileBase + "stops");
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
	}

	public Integer getLastStopId(List<MTripStop> tripStops) {
		Collections.sort(tripStops);
		return tripStops.get(tripStops.size() - 1).stopId;
	}
}
