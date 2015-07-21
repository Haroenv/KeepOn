package org.faudroids.keepgoing.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.auth.AuthManager;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import timber.log.Timber;


/**
 * Handles the Google API initial connection
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends AbstractActivity {

	private static final int REQUEST_OAUTH = 42;

	private static final String
			STATE_AUTH_PENDING = "STATE_AUTH_PENDING",
			STATE_SIGN_IN_SUCCESSFUL = "STATE_SIGN_IN_SUCCESSFUL",
			STATE_SIGN_IN_CLICKED = "STATE_SIGN_IN_CLICKED",
			STATE_SIGN_IN_CONNECTION_RESULT= "STATE_SIGN_IN_CONNECTION_RESULT";

	@InjectView(R.id.btn_sign_in) private SignInButton signInButton;
	private boolean authInProgress = false;
	private boolean signInSuccessful = false;
	private boolean signInClicked = false;
	private ConnectionResult signInConnectionResult = null;

	@Inject private AuthManager authManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if already logged in
		if (authManager.isSignedIn()) {
			startMainActivity();
			return;
		}

		// setup sign in button
		signInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				signInClicked = true;
				if (signInSuccessful) startMainActivity();
				else if (signInConnectionResult != null) resolveSignInError();
				// else do nothing and wait for callbacks
			}
		});

		// restore state
		if (savedInstanceState != null) {
			authInProgress = savedInstanceState.getBoolean(STATE_AUTH_PENDING);
			signInSuccessful = savedInstanceState.getBoolean(STATE_SIGN_IN_SUCCESSFUL);
			signInClicked = savedInstanceState.getBoolean(STATE_SIGN_IN_CLICKED);
			signInConnectionResult = savedInstanceState.getParcelable(STATE_SIGN_IN_CONNECTION_RESULT);
		}
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
		outState.putBoolean(STATE_SIGN_IN_CLICKED, signInClicked);
		outState.putBoolean(STATE_SIGN_IN_SUCCESSFUL, signInSuccessful);
		outState.putParcelable(STATE_SIGN_IN_CONNECTION_RESULT, signInConnectionResult);
	}


	@Override
	public void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		super.onGoogleApiClientConnected(googleApiClient);
		signInSuccessful = true;
		if (signInClicked) startMainActivity();
	}


	@Override
	public void onGoogleAliClientConnectionFailed(ConnectionResult connectionResult) {
		super.onGoogleAliClientConnectionFailed(connectionResult);
		signInConnectionResult = connectionResult;
		if (signInClicked) resolveSignInError();
	}


	private void startMainActivity() {
		if (!authManager.isSignedIn()) authManager.signIn(getGoogleApiClient());
		startActivity(new Intent(LoginActivity.this, MainDrawerActivity.class));
		finish();
	}


	private void resolveSignInError() {
		if (!signInConnectionResult.hasResolution()) {
			Timber.w("no resolution found");
			GooglePlayServicesUtil.getErrorDialog(signInConnectionResult.getErrorCode(), LoginActivity.this, 0).show();
			return;
		}

		if (!authInProgress) {
			try {
				Timber.w("attempting to resolve failed connection");
				authInProgress = true;
				signInConnectionResult.startResolutionForResult(LoginActivity.this, REQUEST_OAUTH);
			} catch (IntentSender.SendIntentException e) {
				Timber.e(e, "failed to send resolution intent");
			}
		}
	}

}
