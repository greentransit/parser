package org.greentransit.parser.my.data;

import java.util.List;

public class MTripStop implements Comparable<MTripStop> {

	private int tripId;
	public String tripIdString;
	private int stopId;
	public int stopSequence;
	private String uid;

	// public MDropOffType dropOff;
	// public MPickupType pickup;
	// private String url;

	public MTripStop(int tripId, String tripIdString, int stopId, int stopSequence/* , MDropOffType dropOff, MPickupType pickup */) {
		this.tripId = tripId;
		this.tripIdString = tripIdString;
		this.stopId = stopId;
		this.stopSequence = stopSequence;
		this.uid = this.tripId + "" + this.stopId;
		// this.dropOff = dropOff;
		// this.pickup = pickup;
	}

	// public MTripStop(String tripId, int stopId, int stopSequence) {
	// this.tripId = tripId;
	// this.stopId = stopId;
	// this.stopSequence = stopSequence;
	// this.dropOff = MDropOffType.REGULAR;
	// this.pickup = MPickupType.REGULAR;
	// }

	public String getUID() {
		// identifies a trip + stop
		return this.uid;
	}

	public int getStopId() {
		return stopId;
	}

	@Override
	public boolean equals(Object obj) {
		MTripStop ts = (MTripStop) obj;
		// if (ts.tripId != null && !ts.tripId.equals(tripId)) {
		if (ts.tripId != 0 && ts.tripId != tripId) {
			return false;
		}
		// if (ts.stopId != null && !ts.stopId.equals(stopId)) {
		if (ts.stopId != 0 && ts.stopId != stopId) {
			return false;
		}
		if (ts.stopSequence != 0 && ts.stopSequence != stopSequence) {
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

	@Override
	public String toString() {
		return new StringBuilder() //
				// .append(',') // ID
				/* .append('\'') */.append(tripId)/* .append('\'') */.append(',') // TRIP ID
				/* .append('\'') */.append(stopId)/* .append('\'') */.append(',') // STOP ID
				.append(stopSequence)/* .append(',') */// STOP SEQUENCe
				// .append(pickup).append(',') // PICKUP
				// .append(dropOff) // DROP OFF
				.toString();
	}

	@Override
	public int compareTo(MTripStop otherTripStop) {
		// sort by trip_id => stop_sequence
		// if (!tripId.equals(otherTripStop.tripId)) {
		if (tripId != otherTripStop.tripId) {
			// return tripId.compareTo(otherTripStop.tripId);
			return tripId - otherTripStop.tripId;
		}
		return stopSequence - otherTripStop.stopSequence;
	}

	public static String printStops(List<MTripStop> l) {
		StringBuilder sb = new StringBuilder();
		for (MTripStop mTripStop : l) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(mTripStop.stopId);
		}
		return sb.toString();
	}

}
