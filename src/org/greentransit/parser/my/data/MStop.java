package org.greentransit.parser.my.data;

public class MStop implements Comparable<MStop> {

	public int id;

	public String code;
	public String name;

	public String lat;// double?
	public String lng;// double?

	// private String url;

	public MStop(int id, String code, String name, String lat, String lng) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.lat = lat;
		this.lng = lng;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(); //
		sb/* .append('\'') */.append(id)/* .append('\'') */; // ID
		sb.append(','); //
		sb.append('\'').append(code == null ? "" : code).append('\'');// code
		sb.append(','); //
		sb.append('\'').append(MSpec.escape(name)).append('\''); // name
		sb.append(','); //
		sb.append(lat); // lat
		sb.append(','); //
		sb.append(lng); // lng
		return sb.toString();
	}

	@Override
	public int compareTo(MStop otherStop) {
		// sort by stop id
		// return id.compareTo(otherStop.id);
		return id - otherStop.id;
	}

	@Override
	public boolean equals(Object obj) {
		MStop o = (MStop) obj;
		// if (!this.id.equals(o.id)) {
		if (this.id != o.id) {
			return false;
		}
		if (this.code != null && !this.code.equals(o.code)) {
			return false;
		}
		if (!this.name.equals(o.name)) {
			return false;
		}
		if (!this.lat.equals(o.lat)) {
			return false;
		}
		if (!this.lng.equals(o.lng)) {
			return false;
		}
		return true;
	}
}
