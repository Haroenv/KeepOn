package org.faudroids.keepgoing.ui;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;

import org.faudroids.keepgoing.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_fit_demo)
public class GoogleFitDemoActivity extends AbstractActivity {

	@InjectView(R.id.txt_status) private TextView statusText;
	@InjectView(R.id.btn_start_recording) private Button startRecordingButton;
	@InjectView(R.id.btn_stop_recording) private Button stopRecordingButton;
	@InjectView(R.id.btn_read_data) private Button readDataButton;
	@InjectView(R.id.btn_start_sensor) private Button startSensorButton;
	@InjectView(R.id.btn_stop_sensor) private Button stopSensorButton;

	protected GoogleFitDemoActivity() {
		super(true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup recording buttons
		startRecordingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!assertIsGoogleApiClientConnected()) return;
				Fitness.RecordingApi
						.subscribe(getGoogleApiClient(), DataType.TYPE_STEP_COUNT_DELTA)
						.setResultCallback(new ResultCallback<Status>() {
							@Override
							public void onResult(Status status) {
								if (status.isSuccess()) {
									if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
										Timber.i("recording already started");
									} else {
										Timber.i("recording started successfully");
									}

								} else {
									Timber.e("failed to start recording (" + status.toString() + ")");
								}
							}
						});
			}
		});

		stopRecordingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!assertIsGoogleApiClientConnected()) return;
				Fitness.RecordingApi.unsubscribe(getGoogleApiClient(), DataType.TYPE_DISTANCE_DELTA)
						.setResultCallback(new ResultCallback<Status>() {
							@Override
							public void onResult(Status status) {
								if (status.isSuccess()) {
									Timber.i("recording stopped successfully");
								} else {
									Timber.e("failed to stop recording (" + status.toString() + ")");
								}
							}
						});
			}
		});

		// setup data reading button
		readDataButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!assertIsGoogleApiClientConnected()) return;

				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				long endTime = cal.getTimeInMillis();
				cal.add(Calendar.WEEK_OF_YEAR, -1);
				long startTime = cal.getTimeInMillis();

				final DateFormat dateFormat = SimpleDateFormat.getDateInstance();
				Timber.d("Range Start: " + dateFormat.format(startTime));
				Timber.d("Range End: " + dateFormat.format(endTime));

				SessionReadRequest readRequest = new SessionReadRequest.Builder()
						.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
						.read(DataType.TYPE_LOCATION_SAMPLE)
						.build();

				Fitness.SessionsApi.readSession(getGoogleApiClient(), readRequest).setResultCallback(new ResultCallback<SessionReadResult>() {
					@Override
					public void onResult(SessionReadResult sessionReadResult) {
						for (Session session : sessionReadResult.getSessions()) {
							Timber.d(session.getName() + " from " + session.getStartTime(TimeUnit.MILLISECONDS) + " to " + session.getEndTime(TimeUnit.MILLISECONDS));

							for (DataSet dataSet : sessionReadResult.getDataSet(session)) {
								Timber.d("dataSet " + dataSet.getDataType().getName());
								for (DataPoint dataPoint : dataSet.getDataPoints()) {
									Timber.d(dataPoint.getValue(Field.FIELD_LATITUDE).asFloat() + ", " + dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat());
								}
							}
						}
					}
				});

				/*
				DataReadRequest readRequest = new DataReadRequest.Builder()
						.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.bucketByTime(1, TimeUnit.DAYS)
						.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
						.build();

				Fitness.HistoryApi.readData(getGoogleApiClient(), readRequest).setResultCallback(new ResultCallback<DataReadResult>() {
					@Override
					public void onResult(DataReadResult dataReadResult) {
						statusText.setText("Read result: " + dataReadResult.toString());
						Timber.d("read success " + dataReadResult.toString());
						Timber.d("found " + dataReadResult.getDataSets().size() + " entries");
						for (DataSet dataSet : dataReadResult.getDataSets()) {
							Timber.d("Returned data for " + dataSet.getDataType().getName());
							for (DataPoint dp : dataSet.getDataPoints()) {
								Timber.d("Data point:");
								Timber.d("\tType: " + dp.getDataType().getName());
								Timber.d("\tStart: " + SimpleDateFormat.getDateInstance().format(dp.getStartTime(TimeUnit.MILLISECONDS)));
								Timber.d("\tEnd: " + SimpleDateFormat.getDateInstance().format(dp.getEndTime(TimeUnit.MILLISECONDS)));
								for(Field field : dp.getDataType().getFields()) {
									Timber.d("\tField: " + field.getName() + " Value: " + dp.getValue(field));
								}
							}
						}
					}
				});
				*/
			}
		});

		// setup sensor buttons
		startSensorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!assertIsGoogleApiClientConnected()) return;

				Fitness.SensorsApi
						.add(getGoogleApiClient(), new SensorRequest.Builder()
										.setDataType(DataType.TYPE_STEP_COUNT_DELTA)
										.setSamplingRate(5, TimeUnit.SECONDS)
										.build(),
								new OnDataPointListener() {
									@Override
									public void onDataPoint(final DataPoint dataPoint) {
										statusText.post(new Runnable() {
											@Override
											public void run() {
												statusText.setText("");
												for (Field field : dataPoint.getDataType().getFields()) {
													Value val = dataPoint.getValue(field);
													statusText.append("Detected DataPoint field: " + field.getName());
													statusText.append("Detected DataPoint value: " + val);
												}
											}
										});
									}
								})
						.setResultCallback(new ResultCallback<Status>() {
							@Override
							public void onResult(Status status) {
								if (status.isSuccess()) {
									Timber.i("sensor start successful");
								} else {
									Timber.e("sensor start failed");
								}
							}
						});
			}
		});
	}

}
