package org.faudroids.keepgoing.sessions;


import com.google.android.gms.fitness.data.Session;

import org.roboguice.shaded.goole.common.base.Objects;

import java.util.concurrent.TimeUnit;

public class SessionOverview implements Comparable<SessionOverview> {

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

	@Override
	public int compareTo(SessionOverview another) {
		return Long.valueOf(session.getStartTime(TimeUnit.MILLISECONDS)).compareTo(another.session.getStartTime(TimeUnit.MILLISECONDS));
	}

}
