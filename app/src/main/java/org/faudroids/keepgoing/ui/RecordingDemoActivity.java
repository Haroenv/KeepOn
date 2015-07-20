package org.faudroids.keepgoing.ui;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.recording.RecordingManager;
import org.faudroids.keepgoing.recording.RecordingResult;
import org.faudroids.keepgoing.utils.DefaultTransformer;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;

@ContentView(R.layout.activity_recording_demo)
public class RecordingDemoActivity extends AbstractMapActivity {

	@Inject private RecordingManager recordingManager;
	private final RecordingListener dataListener = new RecordingListener();

	@InjectView(R.id.map) private MapView mapView;
	@InjectView(R.id.btn_toggle_recording) private Button toggleRecordingButton;
	@InjectView(R.id.txt_distance) private TextView distanceTextView;
	private GoogleMap googleMap = null;


	public RecordingDemoActivity() {
		super(true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup view
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);
	}


	@Override
	protected void onGoogleApiClientConnected(final GoogleApiClient googleApiClient) {
		// restore button state
		toggleRecordingButtonText();

		// restore map polyline
		dataListener.onLocationChanged(recordingManager.getRecordedLocations());

		// start / stop recording
		toggleRecordingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!recordingManager.isRecording()) {
					// start recording
					recordingManager.startRecording(googleApiClient);
					Toast.makeText(RecordingDemoActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
					toggleRecordingButtonText();

				} else {
					// stop recording
					recordingManager
							.stopAndSaveRecording(googleApiClient)
							.compose(new DefaultTransformer<RecordingResult>())
							.subscribe(new Action1<RecordingResult>() {
								@Override
								public void call(RecordingResult recordingResult) {
									if (recordingResult.isRecordingDiscarded()) Toast.makeText(RecordingDemoActivity.this, "Discarded empty recording", Toast.LENGTH_SHORT).show();
									else Toast.makeText(RecordingDemoActivity.this, "Recording stopped successfully: " + recordingResult.getSaveRecordingStatus().isSuccess(), Toast.LENGTH_SHORT).show();
									toggleRecordingButtonText();
								}
							});
				}
			}
		});
	}


	private void toggleRecordingButtonText() {
		if (recordingManager.isRecording()) toggleRecordingButton.setText(R.string.stop_recording);
		else toggleRecordingButton.setText(R.string.start_recording);
	}


	@Override
	public void onResume() {
		super.onResume();
		recordingManager.registerRecordingListener(dataListener);
	}


	@Override
	public void onPause() {
		recordingManager.unregisterRecordingListener();
		super.onPause();
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
					float distance = 0;
					Iterator<Location> iterator = recordedLocations.iterator();
					Location locationIter = iterator.next();
					while (iterator.hasNext()) {
						Location nextLocation = iterator.next();
						distance += locationIter.distanceTo(nextLocation);
						locationIter = nextLocation;
					}
					distanceTextView.setText(distance + " m");
				}
			});

		}
	}

}
