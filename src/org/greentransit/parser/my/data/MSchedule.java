package org.greentransit.parser.my.data;

public class MSchedule implements Comparable<MSchedule> {

	public String serviceId;
	// public int routeId;
	public int tripId;
	public int stopId;
	public int departure;

	public MSchedule(String serviceId, int routeId, int tripId, int stopId, int departure) {
		this.stopId = stopId;
		this.tripId = tripId;
		this.serviceId = serviceId;
		this.departure = departure;
	}

	public String getUID() {
		// identifies a stop + trip + service (date) => departure
		return this.serviceId + /* "-" + this.routeId + */"-" + this.tripId + "-" + this.stopId + "-" + this.departure;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(); //
		sb.append('\'').append(MSpec.escape(serviceId)).append('\''); // service ID
		sb.append(','); //
		sb/* .append('\'') */.append(tripId)/* .append('\'') */; // trip ID
		sb.append(','); //
		sb/* .append('\'') */.append(stopId)/* .append('\'') */; // stop ID
		sb.append(','); //
		sb/* .append('\'') */.append(departure)/* .append('\'') */; // departure
		return sb.toString();
	}

	@Override
	public int compareTo(MSchedule otherSchedule) {
		// sort by service_id => trip_id => stop_id => departure
		if (!serviceId.equals(otherSchedule.serviceId)) {
			return serviceId.compareTo(otherSchedule.serviceId);
		}
		// no route ID, just for file split
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
		// no route ID, just for file split
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
