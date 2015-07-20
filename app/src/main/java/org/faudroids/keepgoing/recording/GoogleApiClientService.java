package org.faudroids.keepgoing.recording;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.List;

import roboguice.service.RoboService;
import timber.log.Timber;

/**
 * Manages the {@link com.google.android.gms.common.api.GoogleApiClient} lifecycle.
 */
public class GoogleApiClientService extends RoboService {

	private final ConnectionListenerAdapter connectionListenerAdapter = new ConnectionListenerAdapter();
	private GoogleApiClient googleApiClient = null;


	@Override
	public void onCreate() {
		super.onCreate();
		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Plus.API)
				.addApi(Fitness.SENSORS_API)
				.addApi(Fitness.SESSIONS_API)
				.addApi(Fitness.HISTORY_API)
				.addApi(Fitness.RECORDING_API)
				.addApi(LocationServices.API)
				.addScope(new Scope(Scopes.PLUS_ME))
				.addScope(new Scope(Scopes.PLUS_ME))
				.addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
				.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
				.addConnectionCallbacks(connectionListenerAdapter)
				.addOnConnectionFailedListener(connectionListenerAdapter)
				.build();
	}


	public GoogleApiClient getGoogleApiClient() {
		return googleApiClient;
	}


	public void registerConnectionListener(CombinedConnectionListener listener) {
		connectionListenerAdapter.registerConnectionListener(listener);
	}


	public void unregisterConnectionListener(CombinedConnectionListener listener) {
		connectionListenerAdapter.unregisterConnectionListener(listener);
	}


	@Override
	public IBinder onBind(Intent intent) {
		googleApiClient.connect();
		return new LocalBinder();
	}


	@Override
	public void onDestroy() {
		Timber.i("Disconnecting Google API client");
		if (googleApiClient.isConnected()) googleApiClient.disconnect();
		super.onDestroy();
	}


	public class LocalBinder extends Binder {

		public GoogleApiClientService getService() {
			return GoogleApiClientService.this;
		}

	}


	/**
	 * Forwards connection callbacks to the supplied listener if present, or buffers those callbacks
	 * until a listener has been set.
	 */
	private class ConnectionListenerAdapter implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

		private static final int NO_CAUSE = -1;

		private final List<CombinedConnectionListener> listenerList = new ArrayList<>();

		private ConnectionResult errorConnectionResult = null;
		private boolean isConnected = false;

		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			Timber.w("Google API connection failed");

			// mark connection error
			errorConnectionResult = connectionResult;
			isConnected = false;

			// notify listener
			for (CombinedConnectionListener listener : listenerList) listener.onConnectionFailed(connectionResult);
		}

		@Override
		public void onConnected(Bundle bundle) {
			Timber.i("Google API connection success");

			// mark connection successful
			isConnected = true;
			errorConnectionResult = null;

			// notify listeners
			for (CombinedConnectionListener listener : listenerList) listener.onConnected();
		}

		@Override
		public void onConnectionSuspended(int i) {
			// TODO
			/**
			 if (cause == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
			 Timber.i("Google API connection suspended (network lost)");
			 } else if (cause == CAUSE_SERVICE_DISCONNECTED) {
			 Timber.i("Google API connection suspended (service disconnected)");
			 } else {
			 Timber.i("Google API connection suspended (unknown cause)");
			 }
			 */
		}

		public void registerConnectionListener(CombinedConnectionListener listener) {
			listenerList.add(listener);
			if (isConnected) listener.onConnected();
			else if (errorConnectionResult != null) listener.onConnectionFailed(errorConnectionResult);
		}

		public void unregisterConnectionListener(CombinedConnectionListener listener) {
			listenerList.remove(listener);
		}

	}


	public interface CombinedConnectionListener {

		void onConnected();
		void onConnectionFailed(ConnectionResult connectionResult);

	}

}
