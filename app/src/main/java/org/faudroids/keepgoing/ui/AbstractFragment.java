package org.faudroids.keepgoing.ui;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.googleapi.GoogleApiClientListener;
import org.faudroids.keepgoing.googleapi.GoogleApiClientObserver;

import roboguice.fragment.provided.RoboFragment;

abstract class AbstractFragment extends RoboFragment implements GoogleApiClientListener {

	protected GoogleApiClientObserver googleApiClientObserver;
	private final int layoutResource;


	public AbstractFragment(int layoutResource) {
		this.layoutResource = layoutResource;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layoutResource, container, false);
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof GoogleApiClientObserver)) {
			throw new IllegalStateException("activity must implement " + GoogleApiClientObserver.class);
		}
		googleApiClientObserver = (GoogleApiClientObserver) activity;
		googleApiClientObserver.registerListener(this);
	}


	@Override
	public void onStop() {
		super.onStop();
		googleApiClientObserver.unregisterListener(this);
	}


	@Override
	public void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		// empty default implementation
	}


	@Override
	public void onGoogleAliClientConnectionFailed(ConnectionResult connectionResult) {
		// empty default implementation
	}

}
