package org.faudroids.keepgoing.recording;


import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.inject.Inject;

import org.faudroids.keepgoing.challenge.Challenge;
import org.faudroids.keepgoing.sessions.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

@Singleton
public class RecordingManager {

	private final Context context;
	private final DataSource locationDataSource;
	private final LocationListenerAdapter locationListenerAdapter = new LocationListenerAdapter();
	private final SessionManager sessionManager;

	private boolean isRecording = false;
	private long recordingStartTimestamp; // unix timestamp in seconds
	private Challenge challenge; // which challenge this recording should be added to
	private final List<Location> recordedLocations = new ArrayList<>();

	@Inject
	RecordingManager(Context context, SessionManager sessionManager) {
		this.context = context;
		this.sessionManager = sessionManager;

		this.locationDataSource = new DataSource.Builder()
				.setAppPackageName(context)
				.setType(DataSource.TYPE_RAW)
				.setDataType(DataType.TYPE_LOCATION_SAMPLE)
				.build();
	}


	public boolean isRecording() {
		return isRecording;
	}


	public List<Location> getRecordedLocations() {
		return recordedLocations;
	}


	public long getRecordingStartTimestamp() {
		return recordingStartTimestamp;
	}


	public void startRecording(final GoogleApiClient googleApiClient, Challenge challenge) {
		// mark recording start
		this.isRecording = true;
		this.recordingStartTimestamp = System.currentTimeMillis() / 1000;
		this.challenge = challenge;

		// start recording service
		context.startService(new Intent(context, RecordingService.class));

		// start location updates
		LocationRequest locationRequest = new LocationRequest();
		locationRequest.setInterval(10000);
		locationRequest.setFastestInterval(5000);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListenerAdapter);
	}


	public void registerRecordingListener(RecordingListener recordingListener) {
		locationListenerAdapter.recordingListener = recordingListener;
	}


	public void unregisterRecordingListener() {
		locationListenerAdapter.recordingListener = null;
	}


	public Observable<RecordingResult> stopAndSaveRecording(final GoogleApiClient googleApiClient) {
		Timber.d("storing " + recordedLocations.size() + " locations");
		return Observable.defer(new Func0<Observable<RecordingResult>>() {
			@Override
			public Observable<RecordingResult> call() {
				// create location data
				DataSet locationDataSet = recordedLocationsToDataSet();

				Observable<Status> resultObservable;

				if (!locationDataSet.isEmpty()) {
					// create session
					long startTimestamp = locationDataSet.getDataPoints().get(0).getTimestamp(TimeUnit.MILLISECONDS);
					final Session session = new Session.Builder()
							.setName("TheAwesomeKeepGoingSession")
							.setIdentifier(UUID.randomUUID().toString())
							.setDescription("A session for testing")
							.setStartTime(startTimestamp, TimeUnit.MILLISECONDS)
							.setEndTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
							.setActivity(FitnessActivities.RUNNING_JOGGING)
							.build();

					resultObservable = sessionManager
							// save session
							.createSession(googleApiClient, session, locationDataSet)
							// add session to challenge
							.flatMap(new Func1<Status, Observable<Status>>() {
								@Override
								public Observable<Status> call(Status status) {
									if (status.isSuccess()) {
										challenge.addSessionId(session.getIdentifier());
										challenge.save();
									}
									return Observable.just(status);
								}
							});


				} else {
					// discard empty sessions
					Timber.w("discarding empty session");
					resultObservable = Observable.just(null);
				}

				stopAndDiscardRecording(googleApiClient);
				return resultObservable.flatMap(new Func1<Status, Observable<RecordingResult>>() {
					@Override
					public Observable<RecordingResult> call(Status saveStatus) {
						RecordingResult recordingResult;
						if (saveStatus == null) recordingResult = new RecordingResult(true, null);
						else recordingResult = new RecordingResult(false, saveStatus);
						return Observable.just(recordingResult);
					}
				});
			}
		});
	}


	public void stopAndDiscardRecording(GoogleApiClient googleApiClient) {
		stopRecording(googleApiClient);
		resetRecording();
	}


	private void stopRecording(GoogleApiClient googleApiClient) {
		LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListenerAdapter);
		context.stopService(new Intent(context, RecordingService.class));
	}


	private void resetRecording() {
		isRecording = false;
		recordedLocations.clear();
	}


	private DataSet recordedLocationsToDataSet() {
		final DataSet dataSet = DataSet.create(locationDataSource);
		for (Location location : recordedLocations) {
			DataPoint newPoint = dataSet.createDataPoint();
			newPoint.setTimestamp(location.getTime(), TimeUnit.MILLISECONDS);
			newPoint.getValue(Field.FIELD_LATITUDE).setFloat((float) location.getLatitude());
			newPoint.getValue(Field.FIELD_LONGITUDE).setFloat((float) location.getLongitude());
			newPoint.getValue(Field.FIELD_ALTITUDE).setFloat((float) location.getAltitude());
			newPoint.getValue(Field.FIELD_ACCURACY).setFloat(location.getAccuracy());
			dataSet.add(newPoint);
		}
		return dataSet;
	}


	/**
	 * "Merges" two {@link Status} objects by taking the error one if present.
	 */
	private Status combineStatus(Status status1, Status status2) {
		// only one status available?
		if (status1 == null) return status2;
		if (status2 == null) return status1;

		// is one was successful and the other not, return the error
		if (status1.isSuccess()) return status2;
		if (status2.isSuccess()) return status1;

		// if one is resolvable return that status
		if (status1.hasResolution()) return status1;
		if (status2.hasResolution()) return status2;

		// return any status, both are successful
		return status1;
	}

	private class LoggingFunction implements Func1<Status, Observable<Status>> {

		private final String message;

		public LoggingFunction(String message) {
			this.message = message;
		}

		@Override
		public Observable<Status> call(Status status) {
			if (status.isSuccess()) Timber.i(message + ": " + status.isSuccess());
			else Timber.e(message + ": " + status.isSuccess() + " (" + status.toString() + ")");
			return Observable.just(status);
		}
	}


	private class LocationListenerAdapter implements LocationListener {

		private RecordingListener recordingListener = null;

		@Override
		public void onLocationChanged(Location location) {
			recordedLocations.add(location);
			Timber.d("location changed (" + recordedLocations.size() + " items)");
			if (recordingListener != null) recordingListener.onLocationChanged(recordedLocations);
		}

	}


	public interface RecordingListener {

		void onLocationChanged(List<Location> recordedLocations);

	}

}
