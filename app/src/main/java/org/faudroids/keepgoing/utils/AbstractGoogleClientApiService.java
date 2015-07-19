package org.faudroids.keepgoing.utils;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.recording.GoogleApiClientService;
import org.roboguice.shaded.goole.common.base.Optional;

import roboguice.service.RoboService;
import timber.log.Timber;

public abstract class AbstractGoogleClientApiService extends RoboService implements ServiceConnection {

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
		if (apiClientService != null) apiClientService.unregisterConnectionListener();
		super.onDestroy();
	}

	@Override
	public final void onServiceConnected(ComponentName name, IBinder binder) {
		Timber.d("Google API client service connected");

		// store service ref and check if api client has been connected
		apiClientService = ((GoogleApiClientService.LocalBinder) binder).getService();
		if (isGoogleApiClientConnected()) onGoogleApiClientConnected(apiClientService.getGoogleApiClient());

		// start listening for api connection
		apiClientService.registerConnectionListener(new GoogleApiClientService.CombinedConnectionListener() {
			@Override
			public void onConnected() {
				onGoogleApiClientConnected(apiClientService.getGoogleApiClient());
			}

			@Override
			public void onConnectionFailed(ConnectionResult connectionResult) {
				onGoogleAliClientConnectionFailed(connectionResult);
			}
		});
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		apiClientService = null;
	}

	protected Optional<GoogleApiClientService> getGoogleApiClientService() {
		return Optional.fromNullable(apiClientService);
	}

	private boolean isGoogleApiClientConnected() {
		return apiClientService != null && apiClientService.getGoogleApiClient().isConnected();
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

}
