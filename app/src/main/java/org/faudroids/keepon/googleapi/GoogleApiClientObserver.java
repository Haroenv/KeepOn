package org.faudroids.keepon.googleapi;


import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Observer interface which lets {@link GoogleApiClientListener} register
 * for status updates.
 */
public interface GoogleApiClientObserver {

	/**
	 * Registers a listener for status updates.
	 */
	void registerListener(GoogleApiClientListener listener);

	/**
	 * Unregisters a listener from connection updates.
	 */
	void unregisterListener(GoogleApiClientListener listener);

	/**
	 * Checks whether the {@link GoogleApiClient} is present and displays an error message otherwise.
	 */
	boolean assertIsGoogleApiClientConnected();

	/**
	 * Returns the {@link GoogleApiClient} without (!) checking its state.
	 */
	GoogleApiClient getGoogleApiClient();

}
