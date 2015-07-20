package org.faudroids.keepgoing.ui;


import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.inject.Key;

import org.faudroids.keepgoing.googleapi.GoogleApiClientListener;
import org.faudroids.keepgoing.googleapi.GoogleApiClientObserver;
import org.faudroids.keepgoing.googleapi.GoogleApiClientService;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import roboguice.RoboGuice;
import roboguice.activity.event.OnActivityResultEvent;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.activity.event.OnNewIntentEvent;
import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnRestartEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.activity.event.OnSaveInstanceStateEvent;
import roboguice.activity.event.OnStopEvent;
import roboguice.context.event.OnConfigurationChangedEvent;
import roboguice.context.event.OnCreateEvent;
import roboguice.context.event.OnDestroyEvent;
import roboguice.context.event.OnStartEvent;
import roboguice.event.EventManager;
import roboguice.inject.ContentViewListener;
import roboguice.inject.RoboInjector;
import roboguice.util.RoboContext;

/**
 * The drawer activity has to inherit from two classes: {@link AbstractFragment} and {@link MaterialNavigationDrawer}.
 *
 * This class does just that by copying the code of the {@link AbstractFragment} (including the
 * {@link roboguice.activity.RoboActionBarActivity}) to this class.
 *
 * The {@link AbstractRoboDrawerActivity} code has been taken from
 * https://github.com/roboguice/roboguice/blob/master/roboguice/src/main/java/roboguice/activity/RoboActionBarActivity.java.
 * and is licensed under the Apache Version 2.0 license.
 */
public abstract class AbstractRoboDrawerActivity extends MaterialNavigationDrawer<Fragment> implements
		RoboContext,
		ServiceConnection,
		GoogleApiClientListener,
		GoogleApiClientObserver {

	protected EventManager eventManager;
	protected HashMap<Key<?>,Object> scopedObjects = new HashMap<Key<?>, Object>();

	@Inject
	ContentViewListener ignored; // BUG find a better place to put this

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final RoboInjector injector = RoboGuice.getInjector(this);
		eventManager = injector.getInstance(EventManager.class);
		injector.injectMembersWithoutViews(this);
		super.onCreate(savedInstanceState);
		eventManager.fire(new OnCreateEvent<Activity>(this,savedInstanceState));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		eventManager.fire(new OnSaveInstanceStateEvent(this, outState));
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		eventManager.fire(new OnRestartEvent(this));
	}

	@Override
	protected void onStart() {
		super.onStart();
		// ROBO ACTIVITY
		eventManager.fire(new OnStartEvent<Activity>(this));

		// ABSTRACT ACTIVITY
		bindService(new Intent(this, GoogleApiClientService.class), this, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		eventManager.fire(new OnResumeEvent(this));
	}

	@Override
	protected void onPause() {
		super.onPause();
		eventManager.fire(new OnPauseEvent(this));
	}

	@Override
	protected void onNewIntent( Intent intent ) {
		super.onNewIntent(intent);
		eventManager.fire(new OnNewIntentEvent(this));
	}

	@Override
	protected void onStop() {
		// ABSTRACT ACTIVITY
		unbindService(this);
		if (apiClientService != null) apiClientService.unregisterConnectionListener(this);

		// ROBO ACTIVITY
		try {
			eventManager.fire(new OnStopEvent(this));
		} finally {
			super.onStop();
		}
	}

	@Override
	protected void onDestroy() {
		try {
			eventManager.fire(new OnDestroyEvent<Activity>(this));
		} finally {
			try {
				RoboGuice.destroyInjector(this);
			} finally {
				super.onDestroy();
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		final Configuration currentConfig = getResources().getConfiguration();
		super.onConfigurationChanged(newConfig);
		eventManager.fire(new OnConfigurationChangedEvent<Activity>(this,currentConfig, newConfig));
	}

	@Override
	public void onSupportContentChanged() {
		super.onSupportContentChanged();
		RoboGuice.getInjector(this).injectViewMembers(this);
		eventManager.fire(new OnContentChangedEvent(this));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		eventManager.fire(new OnActivityResultEvent(this, requestCode, resultCode, data));
	}

	@Override
	public Map<Key<?>, Object> getScopedObjectMap() {
		return scopedObjects;
	}


	// ================= ABSTRACT ACTIVITY ================

	private GoogleApiClientService apiClientService = null;

	private final List<GoogleApiClientListener> listeners = new ArrayList<>();
	// temp cache for when callbacks come before listeners have registered
	private GoogleApiClient cachedClient = null;
	private ConnectionResult cachedConnectionResult = null;

	@Override
	public final void onServiceConnected(ComponentName name, IBinder binder) {
		// store service ref and check if api client has been connected
		apiClientService = ((GoogleApiClientService.LocalBinder) binder).getService();
		if (isGoogleApiClientConnected()) onGoogleApiClientConnected(apiClientService.getGoogleApiClient());

		// start listening for api connection
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

}