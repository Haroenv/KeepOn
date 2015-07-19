package org.faudroids.keepgoing.sessions;


import com.google.android.gms.fitness.data.Session;

import org.roboguice.shaded.goole.common.base.Objects;

import java.util.List;

public class SessionDetails extends SessionOverview {

	private final List<Location> locations;

	public SessionDetails(Session session, float totalDistanceInMeters, List<Location> locations) {
		super(session, totalDistanceInMeters);
		this.locations = locations;
	}

	public List<Location> getLocations() {
		return locations;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		SessionDetails that = (SessionDetails) o;
		return Objects.equal(locations, that.locations);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), locations);
	}
}
