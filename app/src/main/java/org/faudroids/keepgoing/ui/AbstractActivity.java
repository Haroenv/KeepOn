package org.faudroids.keepgoing.ui;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.recording.GoogleApiClientService;
import org.roboguice.shaded.goole.common.base.Optional;

import roboguice.activity.RoboActivity;

abstract class AbstractActivity extends RoboActivity implements ServiceConnection {

	private final GoogleApiClientService.CombinedConnectionListener apiConnectionListener = new GoogleApiClientConnectionListener();
	private GoogleApiClientService apiClientService = null;

	@Override
	protected void onStart() {
		super.onStart();

		// bind service which handles the google api client. Service will be kept alive
		// until the last activity finishes
		bindService(new Intent(this, GoogleApiClientService.class), this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		unbindService(this);
		if (apiClientService != null) apiClientService.unregisterConnectionListener(apiConnectionListener);
		super.onStop();
	}

	@Override
	public final void onServiceConnected(ComponentName name, IBinder binder) {
		// store service ref and check if api client has been connected
		apiClientService = ((GoogleApiClientService.LocalBinder) binder).getService();
		if (isGoogleApiClientConnected()) onGoogleApiClientConnected(apiClientService.getGoogleApiClient());

		// start listening for api connection
		apiClientService.registerConnectionListener(apiConnectionListener);
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		// nothing to do for now
	}

	protected Optional<GoogleApiClientService> getGoogleApiClientService() {
		return Optional.fromNullable(apiClientService);
	}

	/**
	 * @return true if the api client is connected, false otherwise. If connection is not present
	 * an error message will be shown.
	 */
	protected boolean assertIsGoogleApiClientConnected() {
		if (!isGoogleApiClientConnected()) {
			Toast.makeText(this, "Google API client not connected", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	private boolean isGoogleApiClientConnected() {
		return apiClientService != null && apiClientService.getGoogleApiClient().isConnected();
	}

	/**
	 * @return the {@link GoogleApiClient} without (!) checking whether service or the api client
	 * have been connected
	 */
	protected GoogleApiClient getGoogleApiClient() {
		if (apiClientService != null) return apiClientService.getGoogleApiClient();
		return null;
	}

	/**
	 * Called when the {@link GoogleApiClientService} is bound to this activity.
	 */
	protected void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		// override when required
	}

	protected void onGoogleAliClientConnectionFailed(ConnectionResult connectionResult) {
		// nothing to do for now
	}


	private class GoogleApiClientConnectionListener implements GoogleApiClientService.CombinedConnectionListener {

		@Override
		public void onConnected() {
			onGoogleApiClientConnected(apiClientService.getGoogleApiClient());
		}

		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			onGoogleAliClientConnectionFailed(connectionResult);
		}
	}

}
