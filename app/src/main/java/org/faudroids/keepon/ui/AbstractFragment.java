package org.faudroids.keepon.ui;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepon.googleapi.GoogleApiClientListener;
import org.faudroids.keepon.googleapi.GoogleApiClientObserver;

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

		// set google api client observer
		if (!(activity instanceof GoogleApiClientObserver)) {
			throw new IllegalStateException("activity must implement " + GoogleApiClientObserver.class);
		}
		googleApiClientObserver = (GoogleApiClientObserver) activity;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// delay registering for api status updates until injection has been finished
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
