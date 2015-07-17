package org.faudroids.keepgoing.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.recording.GoogleApiClientService;

import timber.log.Timber;


/**
 * Handles the Google API initial connection
 */
public class LoginActivity extends AbstractActivity {

	private static final int REQUEST_OAUTH = 42;

	private static final String STATE_AUTH_PENDING = "STATE_AUTH_PENDING";
	private boolean authInProgress = false;

	@Override
	protected void onServiceConnected(GoogleApiClientService service) {
		// start connection listener
		service.registerConnectionListener(new GoogleApiClientService.CombinedConnectionListener() {
			@Override
			public void onConnected() {
				// if connected start actual application
				startActivity(new Intent(LoginActivity.this, MainActivity.class));
			}

			@Override
			public void onConnectionFailed(ConnectionResult connectionResult) {
				// on connection error try resolving problem
				if (!connectionResult.hasResolution()) {
					Timber.w("no resolution found");
					GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), LoginActivity.this, 0).show();
					return;
				}

				if (!authInProgress) {
					try {
						Timber.w("attempting to resolve failed connection");
						authInProgress = true;
						connectionResult.startResolutionForResult(LoginActivity.this, REQUEST_OAUTH);
					} catch (IntentSender.SendIntentException e) {
						Timber.e(e, "failed to send resolution intent");
					}
				}
			}
		});
	}


	@Override
	public void onStop() {
		if (getGoogleApiClientService().isPresent()) getGoogleApiClientService().get().unregisterConnectionListener();
		super.onStop();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if resolution was successful try connecting again
		switch (requestCode) {
			case REQUEST_OAUTH:
				authInProgress = false;
				if (resultCode != RESULT_OK) return;
				if (!getGoogleApiClientService().isPresent()) return;
				GoogleApiClient apiClient = getGoogleApiClientService().get().getGoogleApiClient();
				if (!apiClient.isConnected() && !apiClient.isConnecting()) {
					apiClient.connect();
				}
				break;
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_AUTH_PENDING, authInProgress);
	}

}
