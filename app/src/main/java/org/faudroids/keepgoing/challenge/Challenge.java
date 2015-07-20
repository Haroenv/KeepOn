package org.faudroids.keepgoing.challenge;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.faudroids.keepgoing.database.KeepGoingDatabase;
import org.roboguice.shaded.goole.common.base.Objects;

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
