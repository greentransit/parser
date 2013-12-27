package org.greentransit.parser.my;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.greentransit.parser.gtfs.GAgencyTools;
import org.greentransit.parser.gtfs.data.GCalendarDate;
import org.greentransit.parser.gtfs.data.GRoute;
import org.greentransit.parser.gtfs.data.GSpec;
import org.greentransit.parser.gtfs.data.GStop;
import org.greentransit.parser.gtfs.data.GStopTime;
import org.greentransit.parser.gtfs.data.GTrip;
import org.greentransit.parser.gtfs.data.GTripStop;
import org.greentransit.parser.my.data.MRoute;
import org.greentransit.parser.my.data.MSchedule;
import org.greentransit.parser.my.data.MServiceDate;
import org.greentransit.parser.my.data.MSpec;
import org.greentransit.parser.my.data.MStop;
import org.greentransit.parser.my.data.MTrip;
import org.greentransit.parser.my.data.MTripStop;

public class GenerateMObjectsTask implements Callable<MSpec> {

	private GAgencyTools agencyTools;
	private int routeId;
	private GSpec gtfs;
	private Map<Integer, MStop> stops;
	private Map<String, GStop> gstops;

	public GenerateMObjectsTask(GAgencyTools agencyTools, int routeId, GSpec gtfs, Map<String, GStop> gstops, Map<Integer, MStop> stops) {
		this.agencyTools = agencyTools;
		this.routeId = routeId;
		this.gtfs = gtfs;
		this.gstops = gstops;
		this.stops = stops;
	}

	@Override
	// public void run() {
	public MSpec call() {
		System.out.println(this.routeId + ": processing... ");
		HashMap<Integer, MServiceDate> mServiceDates = new HashMap<Integer, MServiceDate>();
		HashMap<String, MSchedule> mSchedules = new HashMap<String, MSchedule>();
		Map<Integer, MRoute> mRoutes = new HashMap<Integer, MRoute>();
		Map<Integer, MTrip> mTrips = new HashMap<Integer, MTrip>();
		Map<String, MTripStop> allMTripStops = new HashMap<String, MTripStop>();
		Set<Integer> tripStopIds = new HashSet<Integer>(); // the list of stop IDs used by trips
		Set<String> serviceIds = new HashSet<String>(); 
		for (GRoute gRoute : gtfs.routes.values()) {
			MRoute mRoute = new MRoute(agencyTools.getRouteId(gRoute), agencyTools.getRouteShortName(gRoute), agencyTools.getRouteLongName(gRoute)/* , gRoute.route_type */);
			mRoute.color = agencyTools.getRouteColor(gRoute);
			mRoute.textColor = agencyTools.getRouteTextColor(gRoute);
			if (mRoutes.containsKey(mRoute.id) && !mRoute.equals(mRoutes.get(mRoute.id))) {
				System.out.println("Route " + mRoute.id + " already in list!");
				System.out.println(mRoute.toString());
				System.out.println(mRoutes.get(mRoute.id).toString());
				System.exit(-1);
			}
			// find route trips
			Map<Integer, List<MTripStop>> tripIdToMTripStops = new HashMap<Integer, List<MTripStop>>();
			for (GTrip gTrip : gtfs.trips.values()) {
				if (!gTrip.route_id.equals(gRoute.route_id)) {
					continue;
				}
				MTrip mTrip = new MTrip(/*mTripId,*/ mRoute.id);
				agencyTools.setTripHeadsign(mTrip, gTrip/*, mTripStops.values()*/);
				if (mTrips.containsKey(mTrip.getId()) && !mTrips.get(mTrip.getId()).equals(mTrip)) {
					System.out.println("Different trip " + mTrip.getId() + " already in list (" + mTrip.toString() + " != " + mTrips.get(mTrip.getId()).toString() + ")");
					System.exit(-1);
				}
				Integer mTripId = mTrip.getId();// agencyTools.getTripId(gTrip);
				// find route trip stops
				Map<String, MTripStop> mTripStops = new HashMap<String, MTripStop>();
				for (GTripStop gTripStop : gtfs.tripStops.values()) {
					if (!gTripStop.trip_id.equals(gTrip.trip_id)) {
						continue;
					}
					int mStopId = gstops.containsKey(gTripStop.stop_id.trim()) ? agencyTools.getStopId(gstops.get(gTripStop.stop_id.trim())) : 0;
					if (mStopId == 0) {
						System.out.println("Can't found gtfs stop id '" + gTripStop.stop_id + "' from trip ID '" + gTripStop.trip_id + "' (" + gTrip.trip_id
								+ ")");
						continue; // System.exit(-1);
					}
					MTripStop mTripStop = new MTripStop(mTripId, mTrip.getIdString(), mStopId, gTripStop.stop_sequence/*, MDropOffType.parse(gTripStop.drop_off_type.id),
							MPickupType.parse(gTripStop.pickup_type.id)*/);
					if (mTripStops.containsKey(mTripStop.getUID()) && !mTripStops.get(mTripStop.getUID()).equals(mTripStop)) {
						System.out.println("Different trip stop " + mTripStop.getUID() + " already in list(" + mTripStop.toString() + " != "
								+ mTripStops.get(mTripStop.getUID()).toString() + ")!");
						System.exit(-1);
					}
					// if (myTripStops.containsKey(mTripStop.getUID()) && !myTripStops.get(mTripStop.getUID()).equals(mTripStop)) {
					// System.out.println("Different trip stop " + mTripStop.getUID() + " already in list(" + mTripStop.toString() + " != "
					// + myTripStops.get(mTripStop.getUID()).toString() + ")!");
					// System.exit(-1);
					// }
					// check stop presence
					if (!this.stops.containsKey(mTripStop.stopId)) {
						System.out.println("Stop '" + mTripStop.stopId + "' required by trip " + mTripStop.tripIdString + " not present!");
						System.exit(-1);
					}
					mTripStops.put(mTripStop.getUID(), mTripStop);
					// myTripStops.put(mTripStop.getUID(), mTripStop);
					
					for (GStopTime gStopTime : gtfs.stopTimes) {
						if (!gStopTime.trip_id.equals(gTripStop.trip_id) || !gStopTime.stop_id.equals(gTripStop.stop_id)) {
							continue;
						}
						MSchedule mSchedule = new MSchedule(gTrip.service_id, mRoute.id, mTripId, mStopId, agencyTools.getDepartureTime(gStopTime));
						if (mSchedules.containsKey(mSchedule.getUID()) && !mSchedules.get(mSchedule.getUID()).equals(mSchedule)) {
							System.out.println("Different schedule " + mSchedule.getUID() + " already in list(" + mSchedule.toString() + " != " + mSchedules.get(mSchedule.getUID()).toString() + ")!");
							System.exit(-1);
						}
						mSchedules.put(mSchedule.getUID(), mSchedule);
					}
					serviceIds.add(gTrip.service_id);
				}
				List<MTripStop> mTripStopsList = new ArrayList<MTripStop>(mTripStops.values());
				Collections.sort(mTripStopsList);
				if (tripIdToMTripStops.containsKey(mTripId)) {
					List<MTripStop> cTripStopsList = tripIdToMTripStops.get(mTripId);
					if (!equalsMyTripStopLists(mTripStopsList, cTripStopsList)) {
						// System.out.println("Need to merge gtfs trip: " + gTrip.trip_id);
						tripIdToMTripStops.put(mTripId, mergeMyTripStopLists(mTripStopsList, cTripStopsList));
					}
				} else {
					// just use it
					tripIdToMTripStops.put(mTripId, mTripStopsList);
				}
//				MTrip mTrip = new MTrip(/*mTripId,*/ mRoute.id);
//				agencyTools.setTripHeadsign(mTrip, gTrip/*, mTripStops.values()*/);
//				if (mTrips.containsKey(mTrip.getTripId()) && !mTrips.get(mTrip.getTripId()).equals(mTrip)) {
//					System.out.println("Different trip " + mTrip.getTripId() + " already in list (" + mTrip.toString() + " != " + mTrips.get(mTrip.getTripId()).toString() + ")");
//					System.exit(-1);
//				}
				mTrips.put(mTrip.getId(), mTrip);
			}
			for (List<MTripStop> entry : tripIdToMTripStops.values()) {
				for (MTripStop mTripStop : entry) {
					if (allMTripStops.containsKey(mTripStop.getUID()) && !allMTripStops.get(mTripStop.getUID()).equals(mTripStop)) {
						System.out.println("Different trip stop " + mTripStop.getUID() + " already in list(" + mTripStop.toString() + " != "
								+ allMTripStops.get(mTripStop.getUID()).toString() + ")!");
						System.exit(-1);
					}
					allMTripStops.put(mTripStop.getUID(), mTripStop);
					tripStopIds.add(mTripStop.stopId);
				}
			}
			mRoutes.put(mRoute.id, mRoute);
		}
		// SERVICE DATES
		for (GCalendarDate gCalendarDate : gtfs.calendarDates.values()) {
			if (!serviceIds.contains(gCalendarDate.service_id)) {
				continue;
			}
			int calendarDate = agencyTools.getCalendarDate(gCalendarDate);
			mServiceDates.put(calendarDate, new MServiceDate(gCalendarDate.service_id, calendarDate));
		}
		// // remove not used stops
		// int removedStopsCount = 0;
		// for (Iterator<Map.Entry<Integer, MyStop>> it = myStops.entrySet().iterator(); it.hasNext();) {
		// if (!tripStopIds.contains(it.next().getKey())) {
		// it.remove();
		// removedStopsCount++;
		// }
		// }
		// System.out.println("Removed " + removedStopsCount + " useless stops.");
		// put in sorter list
		List<MStop> mStopsList = null; // new ArrayList<MyStop>(myStops.values());
		// Collections.sort(myStopsList);
		List<MRoute> mRoutesList = new ArrayList<MRoute>(mRoutes.values());
		Collections.sort(mRoutesList);
		List<MTrip> mTripsList = new ArrayList<MTrip>(mTrips.values());
		Collections.sort(mTripsList);
		List<MTripStop> mTripStopsList = new ArrayList<MTripStop>(allMTripStops.values());
		Collections.sort(mTripStopsList);
		List<MServiceDate> mServiceDatesList = new ArrayList<MServiceDate>(mServiceDates.values());
		Collections.sort(mServiceDatesList);
		List<MSchedule> mSchedulesList = new ArrayList<MSchedule>(mSchedules.values());
		Collections.sort(mSchedulesList);
		Map<Integer, List<MSchedule>> mScheduleMap = new HashMap<Integer, List<MSchedule>>();
		mScheduleMap.put(routeId, mSchedulesList);
		MSpec myrouteSpec = new MSpec(mStopsList, mRoutesList, mTripsList, mTripStopsList, mServiceDatesList, mScheduleMap);
		// return new MySpec(myStopsList, myRoutesList, myTripsList, myTripStopsList);
		System.out.println(this.routeId + ": processing... DONE");
		return myrouteSpec;
	}

	public static boolean equalsMyTripStopLists(List<MTripStop> l1, List<MTripStop> l2) {
		if (l1 == null && l2 == null) {
			return true;
		}
		if (l1.size() != l2.size()) {
			return false;
		}
		for (int i = 0; i < l1.size(); i++) {
			if (!l1.get(i).equals(l2.get(i))) {
				return false;
			}
		}
		return true;
	}

	public List<MTripStop> mergeMyTripStopLists(List<MTripStop> l1, List<MTripStop> l2) {
		List<MTripStop> nl = new ArrayList<MTripStop>();
		int i1 = 0, i2 = 0;
		for (; i1 < l1.size() && i2 < l2.size();) {
			MTripStop ts1 = l1.get(i1);
			MTripStop ts2 = l2.get(i2);
			if (isInList(nl, ts1.stopId)) {
				System.out.println("Skipped " + ts1.toString() + " because already in the merged list (1).");
				i1++; // skip this stop because already in the merged list
				continue;
			}
			if (isInList(nl, ts2.stopId)) {
				System.out.println("Skipped " + ts1.toString() + " because already in the merged list (2).");
				i2++; // skip this stop because already in the merged list
				continue;
			}
			if (ts1.stopId == ts2.stopId) {
				// TODO merge other parameters such as drop off / pick up ...
				nl.add(ts1);
				i1++;
				i2++;
				continue;
			}
			// find next match
			// look for stop in other list
			boolean inL1 = isInList(l1, ts2.stopId);
			boolean inL2 = isInList(l2, ts1.stopId);
			if (inL1 && !inL2) {
				nl.add(ts1);
				i1++;
				continue;
			}
			if (!inL1 && inL2) {
				nl.add(ts2);
				i2++;
				continue;
			}
			// MANUAL MERGE
			// } else if (inL1 && inL2) {
			// nl.add(agencyTools.chooseTripStop(ts1, ts2, l1, l2, i1, i2));
			// } else if (!inL1 && !inL2) {
			// Can't randomly choose one of them because stops might be in different order than real life,
			// "Let's not let that happen." -- Aaron S. (1986 - 2013)
			int merge = agencyTools.mergeTrip(ts1, ts2, l1, l2, i1, i2);
			if (merge > 0) {
				nl.add(ts1);
				i1++;
			} else if (merge < 0) {
				nl.add(ts2);
				i2++;
			} else { // merge == 0
				// System.out.println("Have to resolve: " + ts1.toString() + " vs " + ts2.toString());
				// System.exit(-1);
				return nl;
			}
		}
		// add remaining stops
		for (; i1 < l1.size();) {
			nl.add(l1.get(i1++));
		}
		for (; i2 < l2.size();) {
			nl.add(l2.get(i2++));
		}
		// set stop sequence
		for (int i = 0; i < nl.size(); i++) {
			nl.get(i).stopSequence = i + 1;
		}
		return nl;
	}

	private static boolean isInList(List<MTripStop> l, int stopId) {
		for (MTripStop ts : l) {
			// if (ts.stopId.equals(stopId)) {
			if (ts.stopId == stopId) {
				return true;
			}
		}
		return false;
	}
}