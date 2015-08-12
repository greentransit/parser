package org.greentransit.parser.gtfs.data;

// https://developers.google.com/transit/gtfs/reference#frequencies_fields
public class GFrequency {

	public static final String FILENAME = "frequencies.txt";

	public static final String TRIP_ID = "trip_id";
	public String trip_id;
	public static final String START_TIME = "start_time";
	public String start_time;
	public static final String END_TIME = "end_time";
	public String end_time;
	public static final String HEADWAY_SECS = "headway_secs";
	public int headway_secs;

	public GFrequency(String trip_id, String start_time, String end_time, int headway_secs) {
		this.trip_id = trip_id;
		this.start_time = start_time;
		this.end_time = end_time;
		this.headway_secs = headway_secs;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(this.trip_id).append('\'').append(',') //
				.append('\'').append(this.start_time).append('\'').append(',') //
				.append('\'').append(this.end_time).append('\'').append(',') //
				.append(this.headway_secs).append(',') //
				.toString();
	}
}
