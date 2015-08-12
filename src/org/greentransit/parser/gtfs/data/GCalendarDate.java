package org.greentransit.parser.gtfs.data;

public class GCalendarDate {

	public static final String FILENAME = "calendar_dates.txt";

	public static final String SERVICE_ID = "service_id";
	public String service_id;

	public static final String DATE = "date";
	public int date; // YYYYMMDD

	public static final String EXCEPTION_DATE = "exception_type";
	public GCalendarDatesExceptionType exception_type;

	/** Creates a Calendar Date with its required fields. */
	public GCalendarDate(String service_id, int date, GCalendarDatesExceptionType exception_type) {
		this.service_id = service_id;
		this.date = date;
		this.exception_type = exception_type;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(service_id).append('\'').append(',') //
				.append('\'').append(date).append('\'').append(',') //
				.append('\'').append(exception_type).append('\'').append(',') //
				.toString();
	}

	public String getUID() {
		return this.date + this.service_id;
	}
}
