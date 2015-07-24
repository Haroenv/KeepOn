package org.faudroids.keepgoing.challenge;

import android.os.Parcel;
import android.os.Parcelable;

import org.faudroids.keepgoing.sessions.SessionData;
import org.roboguice.shaded.goole.common.base.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChallengeData implements Parcelable, Comparable<ChallengeData> {

	private final Challenge challenge;
	private final List<SessionData> sessionDataList;

	public ChallengeData(Challenge challenge, List<SessionData> sessionDataList) {
		this.challenge = challenge;
		this.sessionDataList = sessionDataList;
	}

	public Challenge getChallenge() {
		return challenge;
	}

	public List<SessionData> getSessionDataList() {
		return sessionDataList;
	}

	public float getCompletedDistanceInMeters() {
		float distance = 0;
		for (SessionData sessionData : sessionDataList) {
			distance += sessionData.getDistanceInMeters();
		}
		return distance;
	}

	public float getCompletedTimeInSeconds() {
		long totalTime = 0;
		for (SessionData sessionData : sessionDataList) {
			totalTime += (sessionData.getSession().getEndTime(TimeUnit.SECONDS) - sessionData.getSession().getStartTime(TimeUnit.SECONDS));
		}
		return totalTime;
	}

	public boolean isOpen() {
		return getCompletedDistanceInMeters() < challenge.getDistanceInMeters();
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ChallengeData that = (ChallengeData) o;
		return Objects.equal(challenge, that.challenge) &&
				Objects.equal(sessionDataList, that.sessionDataList);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(challenge, sessionDataList);
	}

	@Override
	public int compareTo(ChallengeData another) {
		return challenge.compareTo(another.challenge);
	}

	protected ChallengeData(Parcel in) {
		challenge = (Challenge) in.readValue(Challenge.class.getClassLoader());
		if (in.readByte() == 0x01) {
			sessionDataList = new ArrayList<SessionData>();
			in.readList(sessionDataList, SessionData.class.getClassLoader());
		} else {
			sessionDataList = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(challenge);
		if (sessionDataList == null) {
			dest.writeByte((byte) (0x00));
		} else {
			dest.writeByte((byte) (0x01));
			dest.writeList(sessionDataList);
		}
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<ChallengeData> CREATOR = new Parcelable.Creator<ChallengeData>() {
		@Override
		public ChallengeData createFromParcel(Parcel in) {
			return new ChallengeData(in);
		}

		@Override
		public ChallengeData[] newArray(int size) {
			return new ChallengeData[size];
		}
	};
}
