package org.faudroids.keepgoing.recording;


import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

@Singleton
public class RecordingManager {

	private final Context context;
	private final DataSource locationDataSource, distanceDataSource;
	private final SensorListener sensorListener = new SensorListener();

	private boolean isRecording = false;
	private long recordingStartTimestamp;
	private List<DataPoint>
			locationData = new ArrayList<>(),
			distanceData = new ArrayList<>(); // distance deltas

	@Inject
	RecordingManager(Context context) {
		this.context = context;

		DataSource.Builder sourceBuilder = new DataSource.Builder()
				.setAppPackageName(context)
				.setType(DataSource.TYPE_RAW);
		this.locationDataSource = sourceBuilder.setDataType(DataType.TYPE_LOCATION_SAMPLE).build();
		this.distanceDataSource = sourceBuilder.setDataType(DataType.TYPE_DISTANCE_DELTA).build();
	}


	public boolean isRecording() {
		return isRecording;
	}


	public List<DataPoint> getLocationData() {
		return locationData;
	}


	public List<DataPoint> getDistanceData() {
		return distanceData;
	}


	public Observable<Status> startRecording(final GoogleApiClient googleApiClient) {
		// mark recording start
		isRecording = true;
		recordingStartTimestamp = System.currentTimeMillis();

		// start both location and distance sensors
		return Observable.zip(
				startRecording(googleApiClient, DataType.TYPE_LOCATION_SAMPLE),
				startRecording(googleApiClient, DataType.TYPE_DISTANCE_DELTA),
				new Func2<Status, Status, Status>() {
					@Override
					public Status call(Status locationSensorStatus, Status distanceSensorStatus) {
						boolean success = locationSensorStatus.isSuccess() && distanceSensorStatus.isSuccess();

						// start service
						if (success) context.startService(new Intent(context, RecordingService.class));
						else isRecording = false;

						return combineStatus(locationSensorStatus, distanceSensorStatus);
					}
				});
	}


	private Observable<Status> startRecording(final GoogleApiClient googleApiClient, DataType dataType) {
		final SensorRequest sensorRequest = new SensorRequest.Builder()
				.setDataType(dataType)
				.setSamplingRate(15, TimeUnit.SECONDS)
				.build();

		return Observable
				.defer(new Func0<Observable<Status>>() {
					@Override
					public Observable<Status> call() {
						// start listener
						Status status = Fitness.SensorsApi.add(googleApiClient, sensorRequest, sensorListener).await();
						return Observable.just(status);
					}
				})
				.flatMap(new LoggingFunction("sensor listener " + dataType.getName() + " started"));
	}


	public void registerDataListener(DataListener dataListener) {
		sensorListener.dataListener = dataListener;
	}


	public void unregisterDataListener() {
		sensorListener.dataListener = null;
	}


	public Observable<Status> stopAndSaveRecording(final GoogleApiClient googleApiClient) {

		return stopAndDiscardRecording(googleApiClient)
				.flatMap(new Func1<Status, Observable<Status>>() {
					@Override
					public Observable<Status> call(Status stopSensorsStatus) {
						// create session
						Session session = new Session.Builder()
								.setName("TheAwesomeKeepGoingSession")
								.setIdentifier(UUID.randomUUID().toString())
								.setDescription("A session for testing")
								.setStartTime(recordingStartTimestamp, TimeUnit.MILLISECONDS)
								.setEndTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
								.setActivity(FitnessActivities.RUNNING_JOGGING)
								.build();

						// create location data
						DataSet locationDataSet = dataToDataSet(locationDataSource, locationData, Field.FIELD_LATITUDE, Field.FIELD_LONGITUDE, Field.FIELD_ACCURACY, Field.FIELD_ALTITUDE);
						DataSet distanceDataSet = dataToDataSet(distanceDataSource, distanceData, Field.FIELD_DISTANCE);

						// discard sessions that do not contain any data points
						Status saveSessionStatus = null;
						if (!locationDataSet.isEmpty() && !distanceDataSet.isEmpty()) {
							SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
									.setSession(session)
									.addDataSet(locationDataSet)
									.addDataSet(distanceDataSet)
									.build();

							saveSessionStatus = Fitness.SessionsApi.insertSession(googleApiClient, insertRequest).await();
						}

						return Observable.just(combineStatus(saveSessionStatus, stopSensorsStatus));

					}
				});
	}


	public Observable<Status> stopAndDiscardRecording(final GoogleApiClient googleApiClient) {
		return Observable.defer(new Func0<Observable<Status>>() {
			@Override
			public Observable<Status> call() {
				// clear recording state
				isRecording = false;
				distanceData.clear();
				locationData.clear();

				// stop service
				context.stopService(new Intent(context, RecordingService.class));

				// remove sensor listener
				Status status = Fitness.SensorsApi.remove(googleApiClient, sensorListener).await();
				return Observable.just(status);
			}
		});
	}


	private DataSet dataToDataSet(DataSource dataSource, List<DataPoint> dataPoints, Field ... fields) {
		final DataSet dataSet = DataSet.create(dataSource);
		for (DataPoint recordedPoint : dataPoints) {
			// copy recorded points to new "source"
			DataPoint newPoint = dataSet.createDataPoint();
			newPoint.setTimeInterval(
					recordedPoint.getStartTime(TimeUnit.MILLISECONDS),
					recordedPoint.getEndTime(TimeUnit.MILLISECONDS),
					TimeUnit.MILLISECONDS);

			// copy fields
			for (Field field : fields) {
				newPoint.getValue(field).setFloat(recordedPoint.getValue(field).asFloat());
			}
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


	private class SensorListener implements OnDataPointListener {

		private DataListener dataListener = null;

		@Override
		public void onDataPoint(final DataPoint dataPoint) {
			if (dataPoint.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE)) {
				// location data
				locationData.add(dataPoint);
				if (dataListener != null) dataListener.onLocationChanged(locationData);

			} else if (dataPoint.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
				// distance data
				distanceData.add(dataPoint);
				if (dataListener != null) dataListener.onDistanceChanged(distanceData);

			} else {
				// unknown
				Timber.w("unexpected data point with type " + dataPoint.getDataType().getName());
			}
		}

	}


	public interface DataListener {

		void onLocationChanged(List<DataPoint> locationData);
		void onDistanceChanged(List<DataPoint> distanceData);

	}

}
