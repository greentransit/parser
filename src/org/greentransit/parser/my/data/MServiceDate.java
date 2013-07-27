package org.greentransit.parser.my.data;

public class MServiceDate implements Comparable<MServiceDate> {

	public String serviceId;
	public String calendarDate;

	public MServiceDate(String serviceId, String calendarDate) {
		this.serviceId = serviceId;
		this.calendarDate = calendarDate;
	}

	@Override
	public int compareTo(MServiceDate otherServiceDate) {
		return calendarDate.compareTo(otherServiceDate.calendarDate);
	}

	@Override
	public boolean equals(Object obj) {
		MServiceDate ts = (MServiceDate) obj;
		if (ts.serviceId != null && !ts.serviceId.equals(serviceId)) {
			return false;
		}
		if (ts.calendarDate != null && !ts.calendarDate.equals(calendarDate)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(serviceId).append('\'').append(',') // service ID
				.append('\'').append(calendarDate).append('\'').append(',') // calendar date
				.toString();
	}

}
