package org.faudroids.keepgoing.ui;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.recording.GoogleApiClientService;
import org.roboguice.shaded.goole.common.base.Optional;

import roboguice.activity.RoboActivity;
import timber.log.Timber;

abstract class AbstractActivity extends RoboActivity implements ServiceConnection {

	private Optional<GoogleApiClientService> apiClientService = Optional.absent();

	@Override
	protected void onStart() {
		super.onStart();

		// bind service which handles the google api client. Service will be kept alive
		// until the last activity finishes
		bindService(new Intent(this, GoogleApiClientService.class), this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public final void onServiceConnected(ComponentName name, IBinder binder) {
		Timber.d("Google API client service conntected");
		apiClientService = Optional.of(((GoogleApiClientService.LocalBinder) binder).getService());
		onServiceConnected(apiClientService.get());
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		apiClientService = Optional.absent();
	}

	protected Optional<GoogleApiClientService> getGoogleApiClientService() {
		return apiClientService;
	}

	/**
	 * @return true if the api client is connected, false otherwise. If connection is not present
	 * an error message will be shown.
	 */
	protected boolean assertIsGoogleApiClientConnected() {
		if (!apiClientService.isPresent() || !apiClientService.get().getGoogleApiClient().isConnected()) {
			Toast.makeText(this, "Google API client not connected", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	/**
	 * @return the {@link GoogleApiClient} without (!) checking whether service or the api client
	 * have been connected
	 */
	protected GoogleApiClient getGoogleApiClient() {
		return apiClientService.get().getGoogleApiClient();
	}

	/**
	 * Called when the {@link GoogleApiClientService} is bound to this activity.
	 */
	protected void onServiceConnected(GoogleApiClientService service) {
		// override when required
	}

}
