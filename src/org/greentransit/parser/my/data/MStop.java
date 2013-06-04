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
		return new StringBuilder() //
				/* .append('\'') */.append(id)/* .append('\'') */.append(',') // ID
				.append('\'').append(code == null ? "" : code).append('\'').append(',') // code
				.append('\'').append(MSpec.escape(name)).append('\'').append(',') // name
				.append(lat).append(',') // lat
				.append(lng) // lng
				.toString();
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
