package org.faudroids.keepgoing.sessions;


import android.location.Location;

import com.google.android.gms.fitness.data.Session;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SessionData implements Comparable<SessionData> {

	private final Session session;
	private final List<Location> locations;
	private Float cachedDistanceInMeters = null;

	public SessionData(Session session, List<Location> locations) {
		this.session = session;
		this.locations = locations;
	}

	public Session getSession() {
		return session;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public float getDistanceInMeters() {
		if (cachedDistanceInMeters != null) return cachedDistanceInMeters;
		cachedDistanceInMeters = 0f;
		if (locations.isEmpty()) return cachedDistanceInMeters;
		Iterator<Location> locationIterator = locations.iterator();
		Location location = locationIterator.next();
		while (locationIterator.hasNext()) {
			Location nextLocation = locationIterator.next();
			cachedDistanceInMeters += location.distanceTo(nextLocation);
			location = nextLocation;
		}
		return cachedDistanceInMeters;
	}

	@Override
	public int compareTo(SessionData another) {
		return Long.valueOf(session.getStartTime(TimeUnit.MILLISECONDS)).compareTo(another.session.getStartTime(TimeUnit.MILLISECONDS));
	}
}
