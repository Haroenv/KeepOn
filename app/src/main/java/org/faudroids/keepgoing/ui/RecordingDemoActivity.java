package org.faudroids.keepgoing.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.google.GoogleApiClientService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_recording_demo)
public class RecordingDemoActivity extends AbstractActivity implements OnMapReadyCallback {

	private static final DataType FITNESS_TYPE = DataType.TYPE_LOCATION_TRACK;

	private static final String
			PREFS_NAME = "org.faudroids.keepgoing.RecordingDemoActivity",
			KEY_SESSION_ID = "KEY_SESSION_ID";


	@InjectView(R.id.map) private MapView mapView;
	@InjectView(R.id.btn_toggle_recording) private Button toggleRecordingButton;
	private boolean isRecordingRunning = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup view
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);
	}


	@Override
	protected void onServiceConnected(GoogleApiClientService service) {
		if (!assertIsGoogleApiClientConnected()) return;

		// get active subscriptions and rename button accordingly
		Fitness.RecordingApi.listSubscriptions(getGoogleApiClient(), FITNESS_TYPE)
				.setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
					@Override
					public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
						for (Subscription subscription : listSubscriptionsResult.getSubscriptions()) {
							if (subscription.getDataType().equals(FITNESS_TYPE)) {
								isRecordingRunning = true;
							} else {
								isRecordingRunning = false;
							}
							toggleRecordingButtonText();
						}
					}
				});

		// start / stop recording
		toggleRecordingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isRecordingRunning) startRecording();
				else stopRecording();
				isRecordingRunning = !isRecordingRunning;
				toggleRecordingButtonText();
			}
		});
	}


	private void startRecording() {
		// start the actual recording of data (not tied to any session yet)
		Fitness.RecordingApi
				.subscribe(getGoogleApiClient(), FITNESS_TYPE)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) {
							if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
								Timber.i("recording already started");
							} else {
								Toast.makeText(RecordingDemoActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
							}
							isRecordingRunning = true;
							toggleRecordingButtonText();

						} else {
							Timber.e("failed to start recording (" + status.toString() + ")");
							Toast.makeText(RecordingDemoActivity.this, "Failed to start recording", Toast.LENGTH_SHORT).show();
						}
					}
				});

		// create and store session id
		String sessionId = UUID.randomUUID().toString();
		SharedPreferences.Editor prefsEditor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		prefsEditor.putString(KEY_SESSION_ID, sessionId);
		prefsEditor.apply();

		// start session such that recorded that can be associated with this session
		Session session = new Session.Builder()
				.setName("TheAwesomeKeepGoingSession")
				.setIdentifier(sessionId)
				.setDescription("A session for testing")
				.setStartTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
				.build();

		PendingResult<Status> pendingResult = Fitness.SessionsApi.startSession(getGoogleApiClient(), session);
		pendingResult.setResultCallback(new ResultCallback<Status>() {
			@Override
			public void onResult(Status status) {
				Timber.i("session started: " + status.isSuccess() + " (" + status.toString() + ")");
			}
		});
	}


	private void stopRecording() {
		// stop session
		String sessionId = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_SESSION_ID, null);
		PendingResult<SessionStopResult> pendingResult = Fitness.SessionsApi.stopSession(getGoogleApiClient(), sessionId);
		pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
			@Override
			public void onResult(SessionStopResult sessionStopResult) {
				Timber.i("session stopped");
			}
		});

		// stop recording data
		Fitness.RecordingApi.unsubscribe(getGoogleApiClient(), FITNESS_TYPE)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) {
							Timber.i("recording stopped successfully");
							Toast.makeText(RecordingDemoActivity.this, "Recording stopped", Toast.LENGTH_SHORT).show();
						} else {
							Timber.e("failed to stop recording (" + status.toString() + ")");
							Toast.makeText(RecordingDemoActivity.this, "Failed to stop recording", Toast.LENGTH_SHORT).show();
						}
					}
				});
	}


	private void toggleRecordingButtonText() {
		if (isRecordingRunning) toggleRecordingButton.setText(R.string.stop_recording);
		else toggleRecordingButton.setText(R.string.start_recording);
	}


	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}


	@Override
	public void onPause() {
		mapView.onPause();
		super.onPause();
	}


	@Override
	public void onDestroy() {
		mapView.onDestroy();
		super.onDestroy();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		mapView.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onLowMemory() {
		mapView.onLowMemory();
		super.onLowMemory();
	}


	@Override
	public void onMapReady(GoogleMap googleMap) {
		googleMap.setMyLocationEnabled(true);
	}

}
