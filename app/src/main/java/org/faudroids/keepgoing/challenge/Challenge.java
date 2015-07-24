package org.faudroids.keepgoing.challenge;

import android.os.Parcel;
import android.os.Parcelable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.faudroids.keepgoing.database.KeepGoingDatabase;
import org.roboguice.shaded.goole.common.base.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

@Table(databaseName = KeepGoingDatabase.NAME)
public class Challenge extends BaseModel implements Parcelable, Comparable<Challenge> {

    @Column
    @PrimaryKey(autoincrement = true)
    private long id;

	@Column
	@Unique(unique = true)
	private String name;

	@Column
	private float distanceInMeters;

	@Column
	private String description;

	@Column
	private String imageName;

	@Column
	private String sessionIds;

	public Challenge() {
		// empty DB constructor
	}

	public Challenge(long id, String name, float distanceInMeters, String description, String imageName) {
		this.id = id;
		this.name = name;
		this.distanceInMeters = distanceInMeters;
		this.description = description;
		this.imageName = imageName;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public float getDistanceInMeters() {
		return distanceInMeters;
	}

	public String getDescription() {
		return description;
	}

	public String getImageName() {
		return imageName;
	}

	public String getSessionIds() {
		return this.sessionIds;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDistanceInMeters(float distanceInMeters) {
		this.distanceInMeters = distanceInMeters;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public void setSessionIds(String sessionIds) {
		this.sessionIds = sessionIds;
	}

	public void setSessionIdList(ArrayList<String> sessionIds) {
		this.setSessionIds(toSessionIdString(sessionIds));
	}

	public void addSessionId(String sessionId) {
		ArrayList<String> sessionIds = getSessionIdList();
		sessionIds.add(sessionId);
		setSessionIdList(sessionIds);
	}

	public void removeSessionId(String sessionId) {
		ArrayList<String> sessionIds = getSessionIdList();
		sessionIds.remove(sessionId);
		setSessionIdList(sessionIds);
	}

	public ArrayList<String> getSessionIdList() {
		return fromSessionIdString(this.getSessionIds());
	}

	private String toSessionIdString(ArrayList<String> sessionId) {
		// converts the list of strings to a single string
		Iterator<String> sessionIdIterator = sessionId.iterator();
		StringBuilder sessionIdStringBuilder = new StringBuilder();
		while(sessionIdIterator.hasNext()) {
			sessionIdStringBuilder.append(sessionIdIterator.next()).append("|");
		}
		return sessionIdStringBuilder.toString();
	}

	private ArrayList<String> fromSessionIdString(String sessionIdString) {
		if (sessionIdString == null) return new ArrayList<>();
		// parses the lists of ids from a combined id string
		String[] sessionIds = sessionIdString.split("\\|");
		return new ArrayList<>(Arrays.asList(sessionIds));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Challenge challenge = (Challenge) o;
		return Objects.equal(id, challenge.id) &&
				Objects.equal(distanceInMeters, challenge.distanceInMeters) &&
				Objects.equal(name, challenge.name) &&
				Objects.equal(description, challenge.description) &&
				Objects.equal(imageName, challenge.imageName) &&
				Objects.equal(sessionIds, challenge.sessionIds);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, distanceInMeters, description, imageName, sessionIds);
	}

	protected Challenge(Parcel in) {
        id = in.readLong();
        name = in.readString();
        distanceInMeters = in.readFloat();
        description = in.readString();
		imageName = in.readString();
        sessionIds = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeFloat(distanceInMeters);
        dest.writeString(description);
		dest.writeString(imageName);
        dest.writeString(sessionIds);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Challenge> CREATOR = new Parcelable.Creator<Challenge>() {
        @Override
        public Challenge createFromParcel(Parcel in) {
            return new Challenge(in);
        }

        @Override
        public Challenge[] newArray(int size) {
            return new Challenge[size];
        }
    };

	@Override
	public int compareTo(Challenge another) {
		return name.compareTo(another.name);
	}

}
