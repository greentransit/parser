package org.greentransit.parser.gtfs.data;

/**
 * GTFS stop, as defined at
 * https://developers.google.com/transit/gtfs/reference#stops_fields
 * 
 * @author Mathieu Méa
 */
public class GStop {
	public static final String FILENAME = "stops.txt";
	
	public static final String STOP_ID = "stop_id";
	public String stop_id;
	public static final String STOP_NAME = "stop_name";
	public String stop_name;
	public static final String STOP_LAT = "stop_lat";
	public String stop_lat;
	public static final String STOP_LON = "stop_lon";
	public String stop_lon;
	
	public static final String STOP_CODE = "stop_code";
	public String stop_code;
	public static final String STOP_DESC = "stop_desc";
	public String stop_desc;
	public static final String ZONE_ID = "zone_id";
	public String zone_id;
	public static final String STOP_URL = "stop_url";
	public String stop_url;
	public static final String LOCATION_TYPE = "location_type";
	public String location_type;
	public static final String PARENT_STATION = "parent_station";
	public String parent_station;
	public static final String STOP_TIMEZONE = "stop_timezone";
	public String stop_timezone;

	/** Creates a Stop with its required fields. */
	public GStop(String stop_id, String stop_name, String stop_lat,
			String stop_lon) {
		this.stop_id = stop_id;
		this.stop_name = stop_name;
		this.stop_lat = stop_lat;
		this.stop_lon = stop_lon;
	}
	
//	public GStop setStop_code(String stop_code) {
//		this.stop_code = stop_code;
//		return this;
//	}
//	
//	public GStop setStop_desc(String stop_desc) {
//		this.stop_desc = stop_desc;
//		return this;
//	}
//	
//	public GStop setZone_id(String zone_id) {
//		this.zone_id = zone_id;
//		return this;
//	}
//	
//	public GStop setStop_url(String stop_url) {
//		this.stop_url = stop_url;
//		return this;
//	}
//	
//	public GStop setLocation_type(String location_type) {
//		this.location_type = location_type;
//		return this;
//	}
//	
//	public GStop setParent_station(String parent_station) {
//		this.parent_station = parent_station;
//		return this;
//	}
//	
//	public GStop setStop_timezone(String stop_timezone) {
//		this.stop_timezone = stop_timezone;
//		return this;
//	}
}
