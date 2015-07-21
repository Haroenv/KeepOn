package org.faudroids.keepgoing.challenge;

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

import timber.log.Timber;

@Table(databaseName = KeepGoingDatabase.NAME)
public class Challenge extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    private long id;

	@Column
	@Unique(unique = true)
	private String name;

	@Column
	private float distance;

	@Column
	private String description;

	@Column
	private String sessionIds;

	private String toSessionIdString(ArrayList<String> sessionId) {
		Iterator<String> sessionIdIterator = sessionId.iterator();
		StringBuilder sessionIdStringBuilder = new StringBuilder();

		while(sessionIdIterator.hasNext()) {
			sessionIdStringBuilder.append(sessionIdIterator.next()).append("|");
		}

		return sessionIdStringBuilder.toString();
	}

	private ArrayList<String> fromSessionIdString(String sessionIdString) {
		String[] sessionIds = sessionIdString.split("\\|");

		return new ArrayList<>(Arrays.asList(sessionIds));
	}

	public Challenge() {
		// empty DB constructor
	}

	public Challenge(long id, String name, float distance, String description) {
		this.id = id;
		this.name = name;
		this.distance = distance;
		this.description = description;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public float getDistance() {
		return distance;
	}

	public String getDescription() {
		return description;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSessionIds(String sessionIds) {
		this.sessionIds = sessionIds;
	}

	public void setSessionIdList(ArrayList<String> sessionIds) {
		this.setSessionIds(toSessionIdString(sessionIds));
	}

	public void addSessionIdToList(String newElement) {
		ArrayList<String> sessionIds = this.getSessionIdList();
		sessionIds.add(newElement);
		this.setSessionIdList(sessionIds);
	}

	public void removeSessionIdFromList(String sessionId) {
		ArrayList<String> sessionIds = this.getSessionIdList();
		sessionIds.remove(sessionId);
		this.setSessionIdList(sessionIds);
	}

	public String getSessionIds() {
		return this.sessionIds;
	}

	public ArrayList<String> getSessionIdList() {
		return fromSessionIdString(this.getSessionIds());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Challenge challenge = (Challenge) o;
		return Objects.equal(id, challenge.id) &&
				Objects.equal(distance, challenge.distance) &&
				Objects.equal(name, challenge.name) &&
				Objects.equal(description, challenge.description);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, distance, description);
	}

}
