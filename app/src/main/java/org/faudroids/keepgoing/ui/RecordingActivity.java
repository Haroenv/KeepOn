package org.faudroids.keepgoing.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.MapView;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.Challenge;
import org.faudroids.keepgoing.recording.RecordingManager;
import org.faudroids.keepgoing.recording.RecordingResult;
import org.faudroids.keepgoing.utils.DefaultTransformer;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;

@ContentView(R.layout.activity_recording)
public class RecordingActivity extends AbstractMapActivity {

	public static final String EXTRA_CHALLENGE = "EXTRA_CHALLENGE_DATA";

	private static final String STATE_RECORDING_FINISHED = "STATE_RECORDING_FINISHED";

	@InjectView(R.id.map) private MapView mapView;
	@InjectView(R.id.txt_duration) private TextView durationTextView;
	@InjectView(R.id.txt_distance) private TextView distanceTextView;
	@InjectView(R.id.txt_avg_speed) private TextView avgSpeedTextView;
	@InjectView(R.id.btn_start_recording) private Button startRecordingButton;

	@Inject private RecordingManager recordingManager;
	@Inject private LocationManager locationManager;
	private final RecordingListener dataListener = new RecordingListener();

	private boolean recordingFinished = false; // true if one or more recordings have been finished since starting this activity

	private final TimeUpdateRunnable timeUpdateRunnable = new TimeUpdateRunnable();

	public RecordingActivity() {
		super(true, true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check for GPS
		if (!isGpsEnabled()) showEnableGpsDialog();

		// restore state
		if (savedInstanceState != null) {
			recordingFinished = savedInstanceState.getBoolean(STATE_RECORDING_FINISHED);
		}
	}


	@Override
	public void onGoogleApiClientConnected(final GoogleApiClient googleApiClient) {
		super.onGoogleApiClientConnected(googleApiClient);

		// get arguments
		final Challenge challenge = getIntent().getParcelableExtra(EXTRA_CHALLENGE);

		// restore button state
		toggleRunningText();

		// restore map polyline
		dataListener.onLocationChanged(recordingManager.getRecordedLocations());

		// start / stop recording
		startRecordingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!recordingManager.isRecording()) {
					// start recording
					recordingManager.startRecording(googleApiClient, challenge);
					Toast.makeText(RecordingActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
					toggleRunningText();
					timeUpdateRunnable.start();

				} else {
					// stop recording
					timeUpdateRunnable.stop();
					recordingFinished = true;
					recordingManager
							.stopAndSaveRecording(googleApiClient)
							.compose(new DefaultTransformer<RecordingResult>())
							.subscribe(new Action1<RecordingResult>() {
								@Override
								public void call(RecordingResult recordingResult) {
									if (recordingResult.isRecordingDiscarded()) {
										Toast.makeText(RecordingActivity.this, "Discarded empty recording", Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(RecordingActivity.this, "Recording stopped successfully: " + recordingResult.getSaveRecordingStatus().isSuccess(), Toast.LENGTH_SHORT).show();
									}
									toggleRunningText();
								}
							});
				}
			}
		});
	}


	private void toggleRunningText() {
		if (recordingManager.isRecording()) {
			startRecordingButton.setText(R.string.stop_recording);

		} else {
			startRecordingButton.setText(R.string.start_recording);

			// if something has been recorded to not clear screen
			if (recordingFinished) return;
			durationTextView.setText("00:00:00");
			distanceTextView.setText("0.00");
			avgSpeedTextView.setText("0.00");
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		recordingManager.registerRecordingListener(dataListener);
		if (recordingManager.isRecording()) timeUpdateRunnable.start();
	}


	@Override
	public void onPause() {
		recordingManager.unregisterRecordingListener();
		super.onPause();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_RECORDING_FINISHED, recordingFinished);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onBackPressed() {
		if (recordingFinished) setResult(RESULT_OK);
		else setResult(RESULT_CANCELED);
		super.onBackPressed();
	}


	private boolean isGpsEnabled() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}


	private void showEnableGpsDialog() {
		new AlertDialog
				.Builder(this)
				.setMessage(R.string.gps_disabled_prompt)
				.setCancelable(false)
				.setPositiveButton(R.string.enable_gps, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent callGPSSettingIntent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(callGPSSettingIntent);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	}


	private class RecordingListener implements RecordingManager.RecordingListener {

		@Override
		public void onLocationChanged(final List<Location> recordedLocations) {
			if (recordedLocations.isEmpty()) return;

			// update view on UI thread
			mapView.post(new Runnable() {
				@Override
				public void run() {
					// add polyline to map
					drawPolyline(recordedLocations);

					// update distance
					float distanceInMeters = 0;
					Iterator<Location> iterator = recordedLocations.iterator();
					Location locationIter = iterator.next();
					while (iterator.hasNext()) {
						Location nextLocation = iterator.next();
						distanceInMeters += locationIter.distanceTo(nextLocation);
						locationIter = nextLocation;
					}
					float distanceInKm = distanceInMeters / 1000;
					distanceTextView.setText(String.format("%.2f", distanceInKm));

					// update avg speed
					double timeInHours = (System.currentTimeMillis() / 1000 - recordingManager.getRecordingStartTimestamp()) / (60.0 * 60.0);
					double avgSpeed = distanceInKm / timeInHours;
					avgSpeedTextView.setText(String.format("%.2f", (float) avgSpeed));
				}
			});

		}
	}


	private class TimeUpdateRunnable implements Runnable {

		private final DecimalFormat timeFormat = new DecimalFormat("00");
		private boolean isRunning = false;

		@Override
		public void run() {
			if (!isRunning) return;

			// update passed time
			long passedTimeInSeconds = System.currentTimeMillis() / 1000 - recordingManager.getRecordingStartTimestamp();
			long hours = TimeUnit.SECONDS.toHours(passedTimeInSeconds);
			long minutes = TimeUnit.SECONDS.toMinutes(passedTimeInSeconds) - (hours * 60);
			long seconds = passedTimeInSeconds - ((hours * 60) + minutes) * 60;
			durationTextView.setText(timeFormat.format(hours) + ":" + timeFormat.format(minutes) + ":" + timeFormat.format(seconds));

			// reschedule this runnable
			durationTextView.postDelayed(this, 1000);
		}

		public void stop() {
			isRunning = false;
		}

		public void start() {
			isRunning = true;
			durationTextView.post(this);
		}

	}

}
