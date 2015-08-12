package org.greentransit.parser.gtfs;

import org.greentransit.parser.gtfs.data.GCalendar;
import org.greentransit.parser.gtfs.data.GCalendarDate;
import org.greentransit.parser.gtfs.data.GRoute;
import org.greentransit.parser.gtfs.data.GStop;
import org.greentransit.parser.gtfs.data.GStopTime;
import org.greentransit.parser.gtfs.data.GTrip;
import org.greentransit.parser.my.data.MTrip;

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
	void setTripHeadsign(MTrip mTrip, GTrip gTrip/* , Collection<MTripStop> mTripStops */);

	boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge);

	boolean excludeTrip(GTrip gTrip);

	// STOP
	int getStopId(GStop gStop);

	String cleanStopName(String gStopName);

	String getStopCode(GStop gStop);

	boolean excludeStop(GStop gStop);

	// CALENDAR
	boolean excludeCalendar(GCalendar gCalendar);

	// CALENDAR DATE
	boolean excludeCalendarDate(GCalendarDate gCalendarDates);

	// SCHEDULE
	int getDepartureTime(GStopTime gStopTime);

}
