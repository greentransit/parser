package org.greentransit.parser.gtfs.data;

/**
 * GTFS trip stops, as NOT defined at https://developers.google.com/transit/gtfs/reference.
 * 
 * Uses stops, stop times & trips to generate trip stops.
 * 
 * Trip stops is specially useful to store the route trip stops without having to store the big stop_time file.
 * 
 * @author Mathieu Méa
 */
public class GTripStop {
	// public static final String FILENAME = "stop_times.txt";

	// mandatory
	public static final String TRIP_ID = "trip_id";
	public String trip_id;
	public static final String STOP_ID = "stop_id";
	public String stop_id;
	public static final String STOP_SEQUENCE = "stop_sequence";
	public int stop_sequence;
	// optional
//	public static final String PICKUP_TYPE = "pickup_type";
//	public GPickupType pickup_type;
//	public static final String DROP_OFF_TYPE = "drop_off_type";
//	public GDropOffType drop_off_type;

	/** Creates a Trip Stop with its required fields. */
	public GTripStop(String trip_id, String stop_id, int stop_sequence/*, GPickupType pickup_type, GDropOffType drop_off_type*/) {
		this.trip_id = trip_id;
		this.stop_id = stop_id;
		this.stop_sequence = stop_sequence;
//		this.pickup_type = pickup_type;
//		this.drop_off_type = drop_off_type;
	}
	
	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(trip_id).append('\'').append(',') //
				.append('\'').append(stop_id).append('\'').append(',') //
				.append('\'').append(stop_sequence).append('\'').append(',') //
				.toString();
	}

//	/** Creates a Trip Stop with its required fields. */
//	public GTripStop(String trip_id, String stop_id, int stop_sequence) {
//		this.trip_id = trip_id;
//		this.stop_id = stop_id;
//		this.stop_sequence = stop_sequence;
//		this.pickup_type = GPickupType.REGULAR;
//		this.drop_off_type = GDropOffType.REGULAR;
//	}

//	public GTripStop setPickup_type(GPickupType pickup_type) {
//		this.pickup_type = pickup_type;
//		return this;
//	}
//
//	public GTripStop setDrop_off_type(GDropOffType drop_off_type) {
//		this.drop_off_type = drop_off_type;
//		return this;
//	}

	// public String getUID() {
	// return this.trip_id + this.stop_id;
	// }


	public static String getUID(String trip_uid, String stop_id) {
		return trip_uid + stop_id;
	}

}
