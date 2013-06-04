package org.greentransit.parser.gtfs.data;

/**
 * GTFS route, as defined at
 * https://developers.google.com/transit/gtfs/reference#stop_times_fields
 * 
 * @author Mathieu Méa
 */
public class GStopTime {
	public static final String FILENAME = "stop_times.txt";

	public static final String TRIP_ID = "trip_id";
	public String trip_id;
	public static final String STOP_ID = "stop_id";
	public String stop_id;
	public static final String STOP_SEQUENCE = "stop_sequence";
	public int stop_sequence;
	public static final String ARRIVAL_TIME = "arrival_time";
	public String arrival_time;
	public static final String DEPARTURE_TIME = "departure_time";
	public String departure_time;

	public static final String STOP_HEADSIGN = "stop_headsign";
	public String stop_headsign;
	public static final String PICKUP_TYPE = "pickup_type";
	public GPickupType pickup_type;
	public static final String DROP_OFF_TYPE = "drop_off_type";
	public GDropOffType drop_off_type;
	public static final String SHAPE_DIST_TRAVELED = "shape_dist_traveled";
	public String shape_dist_traveled;

	/** Creates a Trip with its required fields. */
	public GStopTime(String trip_id, String arrival_time, String departure_time,
			String stop_id, int stop_sequence) {
		this.trip_id = trip_id;
		this.arrival_time = arrival_time;
		this.departure_time = departure_time;
		this.stop_id = stop_id;
		this.stop_sequence = stop_sequence;
	}
}
