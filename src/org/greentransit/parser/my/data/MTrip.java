package org.greentransit.parser.my.data;

public class MTrip implements Comparable<MTrip> {

	public static final int HEADSIGN_TYPE_STRING = 0;
	public static final int HEADSIGN_TYPE_DIRECTION = 1;
	public static final int HEADSIGN_TYPE_INBOUND = 2;
	public static final int HEADSIGN_TYPE_STOP_ID = 3;

	// private int id; // 10-S
	// public String headsign = ""; // blabla
	// public MyDirectionType direction = MyDirectionType.NONE; // E/W/N/S
	// public MyInboundType inbound = MyInboundType.NONE; // 0/1
	// public int targetStopId; // not useful == trip.id => trip_stops.trip_id, max(stop_sequence) > strop_id
	private int headsignType = HEADSIGN_TYPE_STRING; // 0 = String, 1 = direction, 2= inbound, 3=stopId
	private String headsignValue = "";
	private int headsignId = 0;
	public int routeId; // 10

	// private String url;

	public MTrip(/* int id, *//* String headsign, MyDirectionType direction, MyInboundType inbound, */int routeId) {
		// this.id = id;
		// this.headsign = headsign;
		// this.direction = direction;
		// this.inbound = inbound;
		this.routeId = routeId;
	}

	public int getId() {
		return Integer.valueOf(this.routeId + "0" + this.headsignId);
	}

	public String getIdString() {
		return this.routeId + "-" + this.headsignValue;
	}

	public MTrip setHeadsignString(String headsignString, int headsignId) {
		this.headsignType = HEADSIGN_TYPE_STRING;
		this.headsignValue = headsignString;
		this.headsignId = headsignId;
		return this;
	}

	public MTrip setHeadsignDirection(MDirectionType direction) {
		this.headsignType = HEADSIGN_TYPE_DIRECTION;
		this.headsignValue = direction.id;
		this.headsignId = direction.intValue();
		return this;
	}

	public MTrip setHeadsignInbound(MInboundType inbound) {
		this.headsignType = HEADSIGN_TYPE_INBOUND;
		this.headsignValue = inbound.id;
		this.headsignId = Integer.valueOf(inbound.id);
		return this;
	}

	public MTrip setHeadsignStop(MStop stop) {
		this.headsignType = HEADSIGN_TYPE_STOP_ID;
		this.headsignValue = String.valueOf(stop.id);
		this.headsignId = stop.id;
		return this;
	}

	// public String getId() {
	// return id;
	// }

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		MTrip t = (MTrip) obj;
		// if (t.id != null && !t.id.equals(id)) {
		// if (t.id != id) {
		// return false;
		// }
		// if (t.headsign != null && !t.headsign.equals(headsign)) {
		// return false;
		// }
		// if (t.direction != null && !t.direction.equals(direction)) {
		// return false;
		// }
		// if (t.inbound != null && !t.inbound.equals(inbound)) {
		// return false;
		// }

		if (t.headsignType != headsignType) {
			return false;
		}
		if (t.headsignValue != null && !t.headsignValue.equals(headsignValue)) {
			return false;
		}
		if (t.routeId != 0 && t.routeId != routeId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder() //
				/* .append('\'') */.append(getId())/* .append('\'') */.append(',') // ID
				.append(headsignType).append(',') // HEADSIGN TYPE
				.append('\'').append(headsignValue).append('\'').append(',') // HEADSIGN STRING
				.append(routeId) // ROUTE ID
				.toString();
	}

	@Override
	public int compareTo(MTrip otherTrip) {
		// sort by trip route id => trip id
		if (routeId != otherTrip.routeId) {
			return routeId - otherTrip.routeId;
		}
		return this.getIdString().compareTo(otherTrip.getIdString()); // id - otherTrip.id; //id.compareTo(otherTrip.id);
	}

}
