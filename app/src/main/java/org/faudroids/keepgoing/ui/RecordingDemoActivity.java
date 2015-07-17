package org.faudroids.keepgoing.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.recording.GoogleApiClientService;
import org.faudroids.keepgoing.recording.RecordingManager;

import java.util.List;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_recording_demo)
public class RecordingDemoActivity extends AbstractActivity implements OnMapReadyCallback {

	@Inject private RecordingManager recordingManager;
	@InjectView(R.id.map) private MapView mapView;
	@InjectView(R.id.btn_toggle_recording) private Button toggleRecordingButton;
	private GoogleMap googleMap = null;


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
		toggleRecordingButtonText();

		// start / stop recording
		toggleRecordingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!recordingManager.isRecording()) {
					recordingManager.startRecording(getGoogleApiClient(), new ResultCallback<Status>() {
						@Override
						public void onResult(Status status) {
							Toast.makeText(RecordingDemoActivity.this, "Recording started successfully: " + (status.isSuccess()), Toast.LENGTH_SHORT).show();
						}
					});
					recordingManager.registerDataListener(new RecordingManager.DataListener() {
						@Override
						public void onDataChanged(final List<DataPoint> dataPoints) {
							mapView.post(new Runnable() {
								@Override
								public void run() {
									googleMap.clear();
									PolylineOptions polylineOptions = new PolylineOptions();
									for (DataPoint point : dataPoints) {
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
					});
				} else {
					recordingManager.stopRecording(getGoogleApiClient(), new ResultCallback<Status>() {
						@Override
						public void onResult(Status status) {
							Toast.makeText(RecordingDemoActivity.this, "Recording stopped successfully: " + (status.isSuccess()), Toast.LENGTH_SHORT).show();
						}
					});
					recordingManager.unregisterDataListener();
				}
				toggleRecordingButtonText();
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
		this.googleMap = googleMap;
	}

}
