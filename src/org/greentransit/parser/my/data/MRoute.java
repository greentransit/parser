package org.greentransit.parser.my.data;

import org.apache.commons.lang3.StringUtils;

public class MRoute implements Comparable<MRoute> {

	public int id;
	public String shortName;
	public String longName;

	public String color;
	public String textColor;

	// private String url; useless for now

	// public String type; defined in agency
	// private String agencyId; only 1 agency per DB

	public MRoute(int id, String shortName, String longName) {
		this.id = id;
		this.shortName = shortName;
		this.longName = longName;
		// this.type = type;
	}

	// public String getId() {
	// return id; // STM: OK, STL: not OK
	// // if short route = integer > use short route
	// }

	@Override
	public String toString() {
		return new StringBuilder().append(id).append(',') // ID
				.append('\'').append(shortName).append('\'').append(',') // short name
				.append('\'').append(MSpec.escape(longName)).append('\'').append(',') // long name
				.append('\'').append(color == null ? "" : color).append('\'').append(',') // color
				.append('\'').append(textColor == null ? "" : textColor).append('\'') // text color
				/* .append(',').append(type) */.toString();
	}

	@Override
	public int compareTo(MRoute otherRoute) {
		// sort by route id
		return id - otherRoute.id;
	}

	@Override
	public boolean equals(Object obj) {
		MRoute o = (MRoute) obj;
		if (this.id != o.id) {
			return false;
		}
		if (!StringUtils.equals(this.shortName, o.shortName)) {
			return false;
		}
		if (!StringUtils.equals(this.longName, o.longName)) {
			return false;
		}
		return true;
	}
}
