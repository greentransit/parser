package org.greentransit.parser.my.data;

public class MServiceDate implements Comparable<MServiceDate> {

	public String serviceId;
	public int calendarDate;

	public MServiceDate(String serviceId, int calendarDate) {
		this.serviceId = serviceId;
		this.calendarDate = calendarDate;
	}

	@Override
	public int compareTo(MServiceDate otherServiceDate) {
		// return calendarDate.compareTo(otherServiceDate.calendarDate);
		return calendarDate - otherServiceDate.calendarDate;
	}

	@Override
	public boolean equals(Object obj) {
		MServiceDate ts = (MServiceDate) obj;
		if (ts.serviceId != null && !ts.serviceId.equals(serviceId)) {
			return false;
		}
		//if (ts.calendarDate != null && !ts.calendarDate.equals(calendarDate)) {
		if (ts.calendarDate != 0 && ts.calendarDate != calendarDate) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(serviceId).append('\'').append(',') // service ID
				/*.append('\'')*/.append(calendarDate)/*.append('\'')*/ // calendar date
				.toString();
	}

}
