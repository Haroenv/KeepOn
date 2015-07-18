package org.faudroids.keepgoing.sessions;


import com.google.android.gms.fitness.data.Session;

import org.roboguice.shaded.goole.common.base.Objects;

public class SessionOverview {

	private final Session session;
	private final float totalDistanceInMeters;

	public SessionOverview(Session session, float totalDistanceInMeters) {
		this.session = session;
		this.totalDistanceInMeters = totalDistanceInMeters;
	}

	public Session getSession() {
		return session;
	}

	public float getTotalDistanceInMeters() {
		return totalDistanceInMeters;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SessionOverview that = (SessionOverview) o;
		return Objects.equal(totalDistanceInMeters, that.totalDistanceInMeters) &&
				Objects.equal(session, that.session);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(session, totalDistanceInMeters);
	}

}
