package org.faudroids.keepgoing.sessions;


import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.fitness.data.Session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SessionData implements Parcelable, Comparable<SessionData> {

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

	protected SessionData(Parcel in) {
		session = (Session) in.readValue(Session.class.getClassLoader());
		if (in.readByte() == 0x01) {
			locations = new ArrayList<Location>();
			in.readList(locations, Location.class.getClassLoader());
		} else {
			locations = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(session);
		if (locations == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(locations);
		}
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<SessionData> CREATOR = new Parcelable.Creator<SessionData>() {
		@Override
		public SessionData createFromParcel(Parcel in) {
			return new SessionData(in);
		}

		@Override
		public SessionData[] newArray(int size) {
			return new SessionData[size];
		}
	};

}
