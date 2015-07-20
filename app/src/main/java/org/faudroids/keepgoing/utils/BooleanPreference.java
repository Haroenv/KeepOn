package org.faudroids.keepgoing.utils;

import android.content.SharedPreferences;

public class BooleanPreference extends AbstractPreference<Boolean> {

	public BooleanPreference(SharedPreferences preferences, String key, Boolean defaultValue) {
		super(preferences, key, defaultValue);
	}


	@Override
	protected void doSet(Boolean value, SharedPreferences.Editor editor) {
		editor.putBoolean(key, value);
	}

	@Override
	protected Boolean doGet() {
		return preferences.getBoolean(key, false);
	}

}
