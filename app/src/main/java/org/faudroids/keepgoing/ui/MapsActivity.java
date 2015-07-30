package org.faudroids.keepgoing.ui;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.faudroids.keepgoing.R;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_map)
public class MapsActivity extends AbstractActivity implements OnMapReadyCallback {

	@InjectView(R.id.map) private MapView mapView;

	protected MapsActivity() {
		super(true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);
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
		Timber.d("map is ready");
	}

}
