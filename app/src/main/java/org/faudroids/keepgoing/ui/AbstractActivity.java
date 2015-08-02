package org.faudroids.keepgoing.ui;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.googleapi.GoogleApiClientListener;
import org.faudroids.keepgoing.googleapi.GoogleApiClientObserver;
import org.faudroids.keepgoing.googleapi.GoogleApiClientService;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

import roboguice.activity.RoboActionBarActivity;


/**
 * Manages a connection to the {@link GoogleApiClientService} and forwards connection status
 * changes of the {@link GoogleApiClient}.
 */
abstract class AbstractActivity extends RoboActionBarActivity implements ServiceConnection, GoogleApiClientListener, GoogleApiClientObserver {

	protected Toolbar toolbar;
	private final boolean showBackButton;

	private GoogleApiClientService apiClientService = null;

	private final List<GoogleApiClientListener> listeners = new ArrayList<>();
	// temp cache for when callbacks come before listeners have registered
	private GoogleApiClient cachedClient = null;
	private ConnectionResult cachedConnectionResult = null;


	protected AbstractActivity(boolean showBackButton) {
		this.showBackButton = showBackButton;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup toolbar
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			if (showBackButton) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
				getSupportActionBar().setDisplayShowHomeEnabled(true);
			}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (!showBackButton) break;
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


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
		if (apiClientService != null) apiClientService.unregisterConnectionListener(this);
		super.onStop();
	}


	@Override
	public final void onServiceConnected(ComponentName name, IBinder binder) {
		// start listening for api connection
		apiClientService = ((GoogleApiClientService.LocalBinder) binder).getService();
		apiClientService.registerConnectionListener(this);
	}


	@Override
	public final void onServiceDisconnected(ComponentName name) {
		// nothing to do for now
	}


	protected Optional<GoogleApiClientService> getGoogleApiClientService() {
		return Optional.fromNullable(apiClientService);
	}



	private boolean isGoogleApiClientConnected() {
		return apiClientService != null && apiClientService.getGoogleApiClient().isConnected();
	}


	@Override
	public boolean assertIsGoogleApiClientConnected() {
		if (!isGoogleApiClientConnected()) {
			Toast.makeText(this, "Google API client not connected", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}


	@Override
	public GoogleApiClient getGoogleApiClient() {
		if (apiClientService != null) return apiClientService.getGoogleApiClient();
		return null;
	}


	@Override
	public void registerListener(GoogleApiClientListener listener) {
		if (cachedClient != null && cachedClient.isConnected()) listener.onGoogleApiClientConnected(cachedClient);
		else if (cachedConnectionResult != null) listener.onGoogleAliClientConnectionFailed(cachedConnectionResult);
		listeners.add(listener);
	}


	@Override
	public void unregisterListener(GoogleApiClientListener listener) {
		listeners.remove(listener);
	}


	@Override
	public void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		// store for next listener registration
		this.cachedClient = googleApiClient;
		this.cachedConnectionResult = null;

		// notify existing listeners
		for (GoogleApiClientListener listener : listeners) {
			listener.onGoogleApiClientConnected(googleApiClient);
		}
	}


	@Override
	public void onGoogleAliClientConnectionFailed(ConnectionResult connectionResult) {
		// store for next listener registration
		this.cachedConnectionResult = connectionResult;
		this.cachedClient = null;

		// notify existing listeners
		for (GoogleApiClientListener listener : listeners) {
			listener.onGoogleAliClientConnectionFailed(connectionResult);
		}
	}


	protected void setStatusBarColor(int statusBarColor) {
		if (Build.VERSION.SDK_INT >= 21) {
			getWindow().setStatusBarColor(statusBarColor);
		}
	}

}
