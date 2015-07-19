package org.faudroids.keepgoing.ui;

import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.sessions.Location;

import java.util.List;

import roboguice.inject.InjectView;


/**
 * Handles the initialization of a {@link MapView}. The view has to have the
 * id "map".
 */
abstract class AbstractMapActivity extends AbstractActivity implements OnMapReadyCallback {

	@InjectView(R.id.map) private MapView mapView;
	private final boolean showMyLocation;
	private GoogleMap googleMap = null;

	public AbstractMapActivity(boolean showMyLocation) {
		this.showMyLocation = showMyLocation;
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
		if (showMyLocation) googleMap.setMyLocationEnabled(true);
		this.googleMap = googleMap;
	}


	protected void drawPolyline(List<Location> locationList) {
		// add polyline to map
		googleMap.clear();
		PolylineOptions polylineOptions = new PolylineOptions();
		for (Location location : locationList) {
			polylineOptions.add(new LatLng(location.getLat(), location.getLng()));
		}
		googleMap.addPolyline(polylineOptions
				.width(5)
				.color(Color.BLUE));
	}

}
