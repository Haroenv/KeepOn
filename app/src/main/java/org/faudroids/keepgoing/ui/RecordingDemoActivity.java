package org.faudroids.keepgoing.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.faudroids.keepgoing.DefaultTransformer;
import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.recording.RecordingManager;

import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;

@ContentView(R.layout.activity_recording_demo)
public class RecordingDemoActivity extends AbstractActivity implements OnMapReadyCallback {

	@Inject private RecordingManager recordingManager;
	private final DataListener dataListener = new DataListener();

	@InjectView(R.id.map) private MapView mapView;
	@InjectView(R.id.btn_toggle_recording) private Button toggleRecordingButton;
	@InjectView(R.id.txt_distance) private TextView distanceTextView;
	private GoogleMap googleMap = null;


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

		// restore map + distance state
		dataListener.onLocationChanged(recordingManager.getLocationData());
		dataListener.onDistanceChanged(recordingManager.getDistanceData());

		// start / stop recording
		toggleRecordingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!recordingManager.isRecording()) {
					// start recording
					recordingManager
							.startRecording(googleApiClient)
							.compose(new DefaultTransformer<Status>())
							.subscribe(new Action1<Status>() {
								@Override
								public void call(Status status) {
									Toast.makeText(RecordingDemoActivity.this, "Recording started successfully: " + (status.isSuccess()), Toast.LENGTH_SHORT).show();
									toggleRecordingButtonText();
								}
							});

				} else {
					// stop recording
					recordingManager
							.stopAndSaveRecording(googleApiClient)
							.compose(new DefaultTransformer<Status>())
							.subscribe(new Action1<Status>() {
								@Override
								public void call(Status status) {
									Toast.makeText(RecordingDemoActivity.this, "Recording stopped successfully: " + (status.isSuccess()), Toast.LENGTH_SHORT).show();
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
		mapView.onResume();
		recordingManager.registerDataListener(dataListener);
	}


	@Override
	public void onPause() {
		recordingManager.unregisterDataListener();
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
		this.googleMap = googleMap;
	}


	private class DataListener implements RecordingManager.DataListener {

		@Override
		public void onLocationChanged(final List<DataPoint> locationData) {
			// update view on UI thread
			mapView.post(new Runnable() {
				@Override
				public void run() {
					// add polyline to map
					googleMap.clear();
					PolylineOptions polylineOptions = new PolylineOptions();
					for (DataPoint point : locationData) {
						polylineOptions.add(new LatLng(
								point.getValue(Field.FIELD_LATITUDE).asFloat(),
								point.getValue(Field.FIELD_LONGITUDE).asFloat()));
					}
					googleMap.addPolyline(polylineOptions
							.width(5)
							.color(Color.BLUE));
				}
			});
		}

		@Override
		public void onDistanceChanged(List<DataPoint> distanceData) {
			float distance = 0;
			for (DataPoint point : distanceData) {
				distance += point.getValue(Field.FIELD_DISTANCE).asFloat();
			}
			distanceTextView.setText(distance + " m");
		}

	}

}
