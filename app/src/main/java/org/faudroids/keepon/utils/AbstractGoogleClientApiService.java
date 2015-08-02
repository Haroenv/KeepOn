package org.faudroids.keepon.utils;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepon.googleapi.GoogleApiClientListener;
import org.faudroids.keepon.googleapi.GoogleApiClientService;

import roboguice.service.RoboService;
import timber.log.Timber;

public abstract class AbstractGoogleClientApiService extends RoboService implements ServiceConnection, GoogleApiClientListener {

	private GoogleApiClientService apiClientService = null;

	@Override
	public final int onStartCommand(Intent intent, int flags, int startId) {
		// bind service which handles the google api client. Service will be kept alive
		// until the last activity finishes
		bindService(new Intent(this, GoogleApiClientService.class), this, Context.BIND_AUTO_CREATE);
		return doOnStartCommand(intent, flags, startId);
	}

	protected abstract int doOnStartCommand(Intent intent, int flags, int startId);


	@Override
	public void onDestroy() {
		unbindService(this);
		if (apiClientService != null) apiClientService.unregisterConnectionListener(this);
		super.onDestroy();
	}


	@Override
	public final void onServiceConnected(ComponentName name, IBinder binder) {
		Timber.d("Google API client service connected");

		// store service ref and check if api client has been connected
		apiClientService = ((GoogleApiClientService.LocalBinder) binder).getService();
		if (isGoogleApiClientConnected()) onGoogleApiClientConnected(apiClientService.getGoogleApiClient());

		// start listening for api connection
		apiClientService.registerConnectionListener(this);
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		apiClientService = null;
	}


	private boolean isGoogleApiClientConnected() {
		return apiClientService != null && apiClientService.getGoogleApiClient().isConnected();
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
