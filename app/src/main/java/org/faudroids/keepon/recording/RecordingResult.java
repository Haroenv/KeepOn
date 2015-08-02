package org.faudroids.keepon.recording;


import com.google.android.gms.common.api.Status;

import org.roboguice.shaded.goole.common.base.Objects;

public class RecordingResult {

	private final boolean isRecordingDiscarded;
	private final Status saveRecordingStatus;

	public RecordingResult(boolean isRecordingDiscarded, Status saveRecordingStatus) {
		this.isRecordingDiscarded = isRecordingDiscarded;
		this.saveRecordingStatus = saveRecordingStatus;
	}

	public boolean isRecordingDiscarded() {
		return isRecordingDiscarded;
	}

	public Status getSaveRecordingStatus() {
		return saveRecordingStatus;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RecordingResult that = (RecordingResult) o;
		return Objects.equal(isRecordingDiscarded, that.isRecordingDiscarded) &&
				Objects.equal(saveRecordingStatus, that.saveRecordingStatus);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(isRecordingDiscarded, saveRecordingStatus);
	}

}
