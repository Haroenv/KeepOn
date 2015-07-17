package org.faudroids.keepgoing.recording;


import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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

import roboguice.inject.ContextSingleton;
import timber.log.Timber;

@ContextSingleton
public class RecordingManager {

	private static final DataType FITNESS_TYPE = DataType.TYPE_LOCATION_SAMPLE;

	private final DataSource dataSource;
	private final SensorListener sensorListener = new SensorListener();

	private boolean isRecording = false;
	private long recordingStartTimestamp;
	private List<DataPoint> dataPoints = new ArrayList<>();

	@Inject
	RecordingManager(Context context) {
		this.dataSource = new DataSource.Builder()
				.setAppPackageName(context)
				.setDataType(FITNESS_TYPE)
				.setType(DataSource.TYPE_RAW)
				.build();
	}


	public boolean isRecording() {
		return isRecording;
	}


	public void startRecording(GoogleApiClient googleApiClient, final ResultCallback<Status> statusCallback) {
		// mark recording start
		isRecording = true;
		recordingStartTimestamp = System.currentTimeMillis();

		// start monitoring sensors
		SensorRequest sensorRequest = new SensorRequest.Builder()
				.setDataType(FITNESS_TYPE)
				.setSamplingRate(15, TimeUnit.SECONDS)
				.build();

		Fitness.SensorsApi
				.add(googleApiClient, sensorRequest, sensorListener)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) Timber.i("sensor start successful");
						else Timber.e("sensor start failed (" + status.toString() + ")");
						statusCallback.onResult(status);
					}
				});
	}


	public void registerDataListener(DataListener dataListener) {
		sensorListener.dataListener = dataListener;
	}


	public void unregisterDataListener() {
		sensorListener.dataListener = null;
	}


	public void stopRecording(GoogleApiClient googleApiClient, final ResultCallback<Status> statusCallback) {
		// remove sensor listener
		Fitness.SensorsApi.remove(googleApiClient, sensorListener)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) Timber.i("stopping sensor listener success");
						else Timber.e("stopping sensor listener failed (" + status.toString() + ")");
					}
				});

		// discard sessions that do not contain any data points
		if (!dataPoints.isEmpty()) {
			// create session
			Session session = new Session.Builder()
					.setName("TheAwesomeKeepGoingSession")
					.setIdentifier(UUID.randomUUID().toString())
					.setDescription("A session for testing")
					.setStartTime(recordingStartTimestamp, TimeUnit.MILLISECONDS)
					.setEndTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
					.setActivity(FitnessActivities.RUNNING_JOGGING)
					.build();

			// store session
			DataSet dataSet = DataSet.create(dataSource);
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
			SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
					.setSession(session)
					.addDataSet(dataSet)
					.build();

			Fitness.SessionsApi.insertSession(googleApiClient, insertRequest)
					.setResultCallback(new ResultCallback<Status>() {
						@Override
						public void onResult(Status status) {
							if (status.isSuccess()) Timber.i("session insert successful");
							else Timber.e("session insert failed (" + status.toString() + ")");
							statusCallback.onResult(status);
						}
					});
		}

		// clear recording state
		isRecording = false;
		dataPoints.clear();
		recordingStartTimestamp = 0;
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
