package org.greentransit.parser.my.data;

public class MSchedule implements Comparable<MSchedule> {

	public String serviceId;
	public int tripId;
	public int stopId;
	public int departure;

	public MSchedule(String serviceId, int tripId, int stopId, int departure) {
		this.stopId = stopId;
		this.tripId = tripId;
		this.serviceId = serviceId;
		this.departure = departure;
	}

	public String getUID() {
		// identifies a stop + trip + service (date) => departure
		return this.serviceId + "-" + this.tripId + "-" + this.stopId + "-" + this.departure;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				.append('\'').append(serviceId).append('\'').append(',') // service ID
				/* .append('\'') */.append(tripId)/* .append('\'') */.append(',') // trip ID
				/* .append('\'') */.append(stopId)/* .append('\'') */.append(',') // stop ID
				/* .append('\'') */.append(departure)/* .append('\'') */// departure
				.toString();
	}

	@Override
	public int compareTo(MSchedule otherSchedule) {
		// sort by service_id => trip_id => stop_id => departure
		if (!serviceId.equals(otherSchedule.serviceId)) {
			return serviceId.compareTo(otherSchedule.serviceId);
		}
		if (tripId != otherSchedule.tripId) {
			return tripId - otherSchedule.tripId;
		}
		if (stopId != otherSchedule.stopId) {
			return stopId - otherSchedule.stopId;
		}
		return departure - otherSchedule.departure;
		// return departure.compareTo(otherSchedule.departure);
	}

	@Override
	public boolean equals(Object obj) {
		MSchedule ts = (MSchedule) obj;
		if (ts.serviceId != null && !ts.serviceId.equals(serviceId)) {
			return false;
		}
		// if (ts.tripId != null && !ts.tripId.equals(tripId)) {
		if (ts.tripId != 0 && ts.tripId != tripId) {
			return false;
		}
		// if (ts.stopId != null && !ts.stopId.equals(stopId)) {
		if (ts.stopId != 0 && ts.stopId != stopId) {
			return false;
		}
		// if (ts.departure != null && !ts.departure.equals(departure)) {
		if (ts.departure != 0 && ts.departure != departure) {
			return false;
		}
		// if (ts.dropOff != null && !ts.dropOff.equals(dropOff)) {
		// return false;
		// }
		// if (ts.pickup != null && !ts.pickup.equals(pickup)) {
		// return false;
		// }
		return true;
	}

}
