package org.greentransit.parser.gtfs;

import java.util.List;

import org.greentransit.parser.gtfs.data.GCalendarDate;
import org.greentransit.parser.gtfs.data.GRoute;
import org.greentransit.parser.gtfs.data.GStop;
import org.greentransit.parser.gtfs.data.GStopTime;
import org.greentransit.parser.gtfs.data.GTrip;
import org.greentransit.parser.my.data.MTrip;
import org.greentransit.parser.my.data.MTripStop;

public interface GAgencyTools {
	
	int getThreadPoolSize();

	// ROUTE
	int getRouteId(GRoute gRoute);
	String getRouteShortName(GRoute gRoute);
	String getRouteLongName(GRoute gRoute);
	String getRouteColor(GRoute gRoute);
	String getRouteTextColor(GRoute gRoute);
	boolean excludeRoute(GRoute gRoute);
	
	// TRIP
	// int getTripId(GTrip gTrip);
	void setTripHeadsign(MTrip mTrip, GTrip gTrip/*, Collection<MTripStop> mTripStops*/);
	int mergeTrip(MTripStop ts1, MTripStop ts2, List<MTripStop> l1, List<MTripStop> l2, int i1, int i2);
	boolean excludeTrip(GTrip gTrip);

	// STOP
	int getStopId(GStop gStop);
	String cleanStopName(String gStopName);
	String getStopCode(GStop gStop);
	boolean excludeStop(GStop gStop);
	
	// CALENDAR DATES
	boolean excludeCalendarDates(GCalendarDate gCalendarDates);

	// SCHEDULE
	int getDepartureTime(GStopTime gStopTime);

	int getCalendarDate(GCalendarDate gCalendarDate);

}
