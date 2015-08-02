package org.faudroids.keepon.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

/**
 * Helper class for creating instances of *Preference, e.g. {@link StringPreference}.
 */
public class PreferenceFactory {

	private final Context context;

	@Inject
	PreferenceFactory(Context context) {
		this.context = context;
	}

	public StringPreference newStringPreference(String prefName, String key, String defaultValue) {
		return new StringPreference(getSharedPrefs(prefName), key, defaultValue);
	}

	public BooleanPreference newBooleanPreference(String prefsName, String key, boolean defaultValue) {
		return new BooleanPreference(getSharedPrefs(prefsName), key, defaultValue);
	}

	private SharedPreferences getSharedPrefs(String prefsName) {
		return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
	}

}
