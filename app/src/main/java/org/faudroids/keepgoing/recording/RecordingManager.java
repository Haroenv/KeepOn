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
import timber.log.Timber;

@Singleton
public class RecordingManager {

	private static final DataType FITNESS_TYPE = DataType.TYPE_LOCATION_SAMPLE;

	private final Context context;
	private final DataSource dataSource;
	private final SensorListener sensorListener = new SensorListener();

	private boolean isRecording = false;
	private long recordingStartTimestamp;
	private List<DataPoint> dataPoints = new ArrayList<>();

	@Inject
	RecordingManager(Context context) {
		this.context = context;
		this.dataSource = new DataSource.Builder()
				.setAppPackageName(context)
				.setDataType(FITNESS_TYPE)
				.setType(DataSource.TYPE_RAW)
				.build();
	}


	public boolean isRecording() {
		return isRecording;
	}


	public Observable<Status> startRecording(final GoogleApiClient googleApiClient) {
		// mark recording start
		isRecording = true;
		recordingStartTimestamp = System.currentTimeMillis();

		// start monitoring sensors
		final SensorRequest sensorRequest = new SensorRequest.Builder()
				.setDataType(FITNESS_TYPE)
				.setSamplingRate(15, TimeUnit.SECONDS)
				.build();

		return Observable
				.defer(new Func0<Observable<Status>>() {
					@Override
					public Observable<Status> call() {
						// start service
						context.startService(new Intent(context, RecordingService.class));

						// start listener
						Status status = Fitness.SensorsApi.add(googleApiClient, sensorRequest, sensorListener).await();
						if (!status.isSuccess()) isRecording = false;
						return Observable.just(status);
					}
				})
				.flatMap(new LoggingFunction("sensor listener started"));
	}


	public void registerDataListener(DataListener dataListener) {
		sensorListener.dataListener = dataListener;
	}


	public void unregisterDataListener() {
		sensorListener.dataListener = null;
	}


	public Observable<Status> stopAndSaveRecording(final GoogleApiClient googleApiClient) {
		// create session
		final Session session = new Session.Builder()
				.setName("TheAwesomeKeepGoingSession")
				.setIdentifier(UUID.randomUUID().toString())
				.setDescription("A session for testing")
				.setStartTime(recordingStartTimestamp, TimeUnit.MILLISECONDS)
				.setEndTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.setActivity(FitnessActivities.RUNNING_JOGGING)
				.build();

		// create session data
		final DataSet dataSet = DataSet.create(dataSource);
		for (DataPoint recordedPoint : dataPoints) {
			// copy recorded points to new "source"
			DataPoint newPoint = dataSet.createDataPoint();
			newPoint.setTimeInterval(
					recordedPoint.getStartTime(TimeUnit.MILLISECONDS),
					recordedPoint.getEndTime(TimeUnit.MILLISECONDS),
					TimeUnit.MILLISECONDS);
			newPoint.getValue(Field.FIELD_LATITUDE).setFloat(recordedPoint.getValue(Field.FIELD_LATITUDE).asFloat());
			newPoint.getValue(Field.FIELD_LONGITUDE).setFloat(recordedPoint.getValue(Field.FIELD_LONGITUDE).asFloat());
			newPoint.getValue(Field.FIELD_ACCURACY).setFloat(recordedPoint.getValue(Field.FIELD_ACCURACY).asFloat());
			newPoint.getValue(Field.FIELD_ALTITUDE).setFloat(recordedPoint.getValue(Field.FIELD_ALTITUDE).asFloat());
			dataSet.add(newPoint);
		}

		return Observable
				// save session
				.defer(new Func0<Observable<Status>>() {
					@Override
					public Observable<Status> call() {
						// discard sessions that do not contain any data points
						Status saveSessionStatus = null;
						if (!dataPoints.isEmpty()) {
							SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
									.setSession(session)
									.addDataSet(dataSet)
									.build();

							saveSessionStatus = Fitness.SessionsApi.insertSession(googleApiClient, insertRequest).await();
						}

						return Observable.just(saveSessionStatus);
					}
				})
				// stop recording
				.flatMap(new Func1<Status, Observable<Status>>() {
					@Override
					public Observable<Status> call(final Status saveStatus) {
						return stopAndDiscardRecording(googleApiClient)
								.flatMap(new Func1<Status, Observable<Status>>() {
									@Override
									public Observable<Status> call(Status stopStatus) {
										// return error status is present
										if (!stopStatus.isSuccess())
											return Observable.just(stopStatus);
										return Observable.just(saveStatus);
									}
								});

					}
				});
	}


	public Observable<Status> stopAndDiscardRecording(final GoogleApiClient googleApiClient) {
		return Observable.defer(new Func0<Observable<Status>>() {
			@Override
			public Observable<Status> call() {
				// clear recording state
				isRecording = false;
				dataPoints.clear();
				recordingStartTimestamp = 0;

				// stop service
				context.stopService(new Intent(context, RecordingService.class));

				// remove sensor listener
				Status status = Fitness.SensorsApi.remove(googleApiClient, sensorListener).await();
				return Observable.just(status);
			}
		});
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
			dataPoints.add(dataPoint);
			if (dataListener != null) dataListener.onDataChanged(dataPoints);
		}

	}


	public interface DataListener {

		void onDataChanged(List<DataPoint> dataPoints);

	}

}
