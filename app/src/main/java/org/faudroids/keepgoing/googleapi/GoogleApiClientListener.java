package org.faudroids.keepgoing.googleapi;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Listener interfaces for being informed about {@link GoogleApiClient} status changes.
 */
public interface GoogleApiClientListener {

	/**
	 * Called when the {@link GoogleApiClient} has been successfully connected.
	 */
	void onGoogleApiClientConnected(GoogleApiClient googleApiClient);

	/**
	 * Called when the was an error connecting to the {@link GoogleApiClient}.
	 */
	void onGoogleAliClientConnectionFailed(ConnectionResult connectionResult);

}
