package org.greentransit.parser.gtfs.data;

/**
 * GTFS services, as NOT defined at https://developers.google.com/transit/gtfs/reference.
 * 
 * Uses calendar_dates to generate services.
 * 
 * Services is specially useful to store the services ID without having to store the big calendar_dates file.
 * 
 * @author Mathieu Méa
 */
public class GService {

	// mandatory
	public static final String SERVICE_ID = "service_id";
	public String service_id;

	/** Creates a Service with its required fields. */
	public GService(String service_id) {
		this.service_id = service_id;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(service_id).append('\'') //
				.toString();
	}

}
