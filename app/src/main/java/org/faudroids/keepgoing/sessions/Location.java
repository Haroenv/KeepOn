package org.faudroids.keepgoing.sessions;


import org.roboguice.shaded.goole.common.base.Objects;

public class Location {

	private final float lat, lng;

	public Location(float lat, float lng) {
		this.lat = lat;
		this.lng = lng;
	}

	public float getLat() {
		return lat;
	}

	public float getLng() {
		return lng;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Location location = (Location) o;
		return Objects.equal(lat, location.lat) &&
				Objects.equal(lng, location.lng);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(lat, lng);
	}

}
