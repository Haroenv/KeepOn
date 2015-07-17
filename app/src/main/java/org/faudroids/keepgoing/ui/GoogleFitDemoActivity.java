package org.faudroids.keepgoing.ui;


import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.faudroids.keepgoing.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_fit_demo)
public class GoogleFitDemoActivity extends RoboActivity {

	private static final int REQUEST_OAUTH = 42;

	private static final String STATE_AUTH_PENDING = "STATE_AUTH_PENDING";
	private boolean authInProgress = false;

	private GoogleApiClient apiClient = null;

	@InjectView(R.id.txt_status) private TextView statusText;
	@InjectView(R.id.btn_start_recording) private Button startRecordingButton;
	@InjectView(R.id.btn_stop_recording) private Button stopRecordingButton;
	@InjectView(R.id.btn_read_data) private Button readDataButton;
	@InjectView(R.id.btn_start_sensor) private Button startSensorButton;
	@InjectView(R.id.btn_stop_sensor) private Button stopSensorButton;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup google api client
		if (savedInstanceState != null) {
			authInProgress = savedInstanceState.getBoolean(STATE_AUTH_PENDING);
		}
		buildFitnessClient();

		// setup recording buttons
		startRecordingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Fitness.RecordingApi
						.subscribe(apiClient, DataType.TYPE_STEP_COUNT_DELTA)
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
				Fitness.RecordingApi.unsubscribe(apiClient, DataType.TYPE_DISTANCE_DELTA)
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
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				long endTime = cal.getTimeInMillis();
				cal.add(Calendar.WEEK_OF_YEAR, -1);
				long startTime = cal.getTimeInMillis();

				DateFormat dateFormat = SimpleDateFormat.getDateInstance();
				Timber.d("Range Start: " + dateFormat.format(startTime));
				Timber.d("Range End: " + dateFormat.format(endTime));

				DataReadRequest readRequest = new DataReadRequest.Builder()
						.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.bucketByTime(1, TimeUnit.DAYS)
						.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
						.build();

				Fitness.HistoryApi.readData(apiClient, readRequest).setResultCallback(new ResultCallback<DataReadResult>() {
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
			}
		});

		// setup sensor buttons
		startSensorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Fitness.SensorsApi
						.add(apiClient, new SensorRequest.Builder()
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


	private void buildFitnessClient() {
		apiClient = new GoogleApiClient.Builder(this)
				.addApi(Fitness.SENSORS_API)
				.addApi(Fitness.HISTORY_API)
				.addApi(Fitness.RECORDING_API)
				.addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
				.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(Bundle bundle) {
						Timber.i("Google API connection success");
						statusText.setText("Connection success");
					}

					@Override
					public void onConnectionSuspended(int cause) {
						if (cause == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
							Timber.i("Google API connection suspended (network lost)");
						} else if (cause == CAUSE_SERVICE_DISCONNECTED) {
							Timber.i("Google API connection suspended (service disconnected)");
						} else {
							Timber.i("Google API connection suspended (unknown cause)");
						}
					}
				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult connectionResult) {
						Timber.w("Google API connection failed");
						statusText.setText("Connection failure");

						if (!connectionResult.hasResolution()) {
							Timber.w("no resolution found");
							GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), GoogleFitDemoActivity.this, 0).show();
							return;
						}

						if (!authInProgress) {
							try {
								Timber.w("attempting to resolve failed connection");
								authInProgress = true;
								connectionResult.startResolutionForResult(GoogleFitDemoActivity.this, REQUEST_OAUTH);
							} catch (IntentSender.SendIntentException e) {
								Timber.e(e, "failed to send resolution intent");
							}
						}
					}
				})
				.build();
	}


	@Override
	public void onStart() {
		super.onStart();
		Timber.i("connecting API client");
		apiClient.connect();
		statusText.setText("Connecting ...");
	}


	@Override
	public void onStop() {
		super.onStop();
		Timber.i("connecting API client");
		if (apiClient.isConnected()) {
			apiClient.disconnect();
			statusText.setText("Disconnecting ...");
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_OAUTH:
				authInProgress = false;
				if (resultCode != RESULT_OK) return;
				if (!apiClient.isConnected() && !apiClient.isConnecting()) {
					apiClient.connect();
				}
				break;
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_AUTH_PENDING, authInProgress);
	}

}
