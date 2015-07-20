package org.faudroids.keepgoing.utils;

import android.content.SharedPreferences;

abstract class AbstractPreference<T> {

	protected final SharedPreferences preferences;
	protected final String key;

	public AbstractPreference(SharedPreferences preferences, String key) {
		this.preferences = preferences;
		this.key = key;
	}

	public boolean isSet() {
		return preferences.contains(key);
	}

	public void set(T value) {
		SharedPreferences.Editor editor = preferences.edit();
		doSet(value, editor);
		editor.apply();
	}

	protected abstract void doSet(T value, SharedPreferences.Editor editor);

	public abstract T get();

}
