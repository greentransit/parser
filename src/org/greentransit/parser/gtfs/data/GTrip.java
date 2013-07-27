package org.greentransit.parser.gtfs.data;

/**
 * GTFS route trip, as defined at https://developers.google.com/transit/gtfs/reference#trips_fields
 * 
 * @author Mathieu Méa
 */
public class GTrip {
	public static final String FILENAME = "trips.txt";

	public static final String ROUTE_ID = "route_id";
	public String route_id;
	public static final String SERVICE_ID = "service_id";
	public String service_id;
	public static final String TRIP_ID = "trip_id";
	public String trip_id;

	public static final String TRIP_HEADSIGN = "trip_headsign";
	public String trip_headsign;
	public static final String TRIP_SHORT_NAME = "trip_short_name";
	public String trip_short_name;
	public static final String DIRECTION_ID = "direction_id";
	public String direction_id;
	public static final String BLOCK_ID = "block_id";
	public String block_id;
	public static final String SHAPE_ID = "shape_id";
	public String shape_id;

	/** Creates a Trip with its required fields. */
	public GTrip(String route_id, String service_id, String trip_id) {
		this.route_id = route_id;
		this.service_id = service_id;
		this.trip_id = trip_id;
	}

	public String getUID() {
		return this.route_id + this.trip_id;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(route_id).append('\'').append(',') //
				.append('\'').append(service_id).append('\'').append(',') //
				.append('\'').append(trip_id).append('\'').append(',') //
				.append('\'').append(trip_headsign).append('\'').append(',') //
				.append('\'').append(trip_short_name).append('\'').append(',') //
				.append('\'').append(direction_id).append('\'').append(',') //
				.append('\'').append(block_id).append('\'').append(',') //
				.append('\'').append(shape_id).append('\'') //
				.toString();
	}

	// public GTrip setTrip_headsign(String trip_headsign) {
	// this.trip_headsign = trip_headsign;
	// return this;
	// }
	//
	// public GTrip setTrip_short_name(String trip_short_name) {
	// this.trip_short_name = trip_short_name;
	// return this;
	// }
	//
	// public GTrip setDirection_id(String direction_id) {
	// this.direction_id = direction_id;
	// return this;
	// }
	//
	// public GTrip setBlock_id(String block_id) {
	// this.block_id = block_id;
	// return this;
	// }
	//
	// public GTrip setShape_id(String shape_id) {
	// this.shape_id = shape_id;
	// return this;
	// }
}
