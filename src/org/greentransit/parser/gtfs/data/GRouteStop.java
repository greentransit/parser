package org.greentransit.parser.gtfs.data;

/**
 * GTFS route, as NOT defined at
 * https://developers.google.com/transit/gtfs/reference
 * 
 * @author Mathieu Méa
 */
@Deprecated
public class GRouteStop {
//	public static final String FILENAME = "stop_times.txt";

	public static final String ROUTE_ID = "route_id";
	public String route_id;
	public static final String STOP_ID= "stop_id";
	public String stop_id;
	public static final String STOP_SEQUENCE = "stop_sequence";
	public int stop_sequence;
	public static final String PICKUP_TYPE = "pickup_type";
	public GPickupType pickup_type;
	public static final String DROP_OFF_TYPE = "drop_off_type";
	public GDropOffType drop_off_type;

	/** Creates a Trip with its required fields. */
	public GRouteStop(String route_id, String stop_id, int stop_sequence,
			GPickupType pickup_type, GDropOffType drop_off_type) {
		this.route_id = route_id;
		this.stop_id = stop_id;
		this.stop_sequence = stop_sequence;
		this.pickup_type = pickup_type;
		this.drop_off_type = drop_off_type;
	}

	public static String getUID(String route_id, String trip_id) {
		return route_id + trip_id;
	}

}
