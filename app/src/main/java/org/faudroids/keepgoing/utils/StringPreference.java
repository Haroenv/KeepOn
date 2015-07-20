package org.faudroids.keepgoing.utils;

import android.content.SharedPreferences;

public class StringPreference extends AbstractPreference<String> {

	public StringPreference(SharedPreferences preferences, String key) {
		super(preferences, key);
	}


	@Override
	protected void doSet(String value, SharedPreferences.Editor editor) {
		editor.putString(key, value);
	}

	public String get() {
		return preferences.getString(key, null);
	}

}
