package org.faudroids.keepgoing.ui;


import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

import org.faudroids.keepgoing.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;

@ContentView(R.layout.activity_fit_demo)
public class GoogleFitDemoActivity extends RoboActivity {

	private static final int REQUEST_OAUTH = 42;

	private static final String STATE_AUTH_PENDING = "STATE_AUTH_PENDING";
	private boolean authInProgress = false;

	private GoogleApiClient apiClient = null;

	@InjectView(R.id.txt_status) private TextView statusText;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			authInProgress = savedInstanceState.getBoolean(STATE_AUTH_PENDING);
		}

		buildFitnessClient();
	}


	private void buildFitnessClient() {
		apiClient = new GoogleApiClient.Builder(this)
				.addApi(Fitness.SENSORS_API)
				.addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(Bundle bundle) {
						Timber.i("Google API connection success");
						statusText.setText("Connection success");
					}

					@Override
					public void onConnectionSuspended(int cause) {
						if (cause == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
							Timber.i("Google API connection suspended (network lost)");
						} else if (cause == CAUSE_SERVICE_DISCONNECTED) {
							Timber.i("Google API connection suspended (service disconnected)");
						} else {
							Timber.i("Google API connection suspended (unknown cause)");
						}
					}
				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult connectionResult) {
						Timber.w("Google API connection failed");
						statusText.setText("Connection failure");

						if (!connectionResult.hasResolution()) {
							Timber.w("no resolution found");
							GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), GoogleFitDemoActivity.this, 0).show();
							return;
						}

						if (!authInProgress) {
							try {
								Timber.w("attempting to resolve failed connection");
								authInProgress = true;
								connectionResult.startResolutionForResult(GoogleFitDemoActivity.this, REQUEST_OAUTH);
							} catch (IntentSender.SendIntentException e) {
								Timber.e(e, "failed to send resolution intent");
							}
						}
					}
				})
				.build();
	}


	@Override
	public void onStart() {
		super.onStart();
		Timber.i("connecting API client");
		apiClient.connect();
		statusText.setText("Connecting ...");
	}


	@Override
	public void onStop() {
		super.onStop();
		Timber.i("connecting API client");
		if (apiClient.isConnected()) {
			apiClient.disconnect();
			statusText.setText("Disconnecting ...");
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_OAUTH:
				authInProgress = false;
				if (resultCode != RESULT_OK) return;
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
