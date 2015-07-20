package org.faudroids.keepgoing.utils;

import android.content.SharedPreferences;

public class StringPreference extends AbstractPreference<String> {

	public StringPreference(SharedPreferences preferences, String key, String defaultValue) {
		super(preferences, key, defaultValue);
	}


	@Override
	protected void doSet(String value, SharedPreferences.Editor editor) {
		editor.putString(key, value);
	}

	@Override
	protected String doGet() {
		return preferences.getString(key, null);
	}

}
