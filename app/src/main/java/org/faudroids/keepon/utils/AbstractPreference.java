package org.faudroids.keepon.utils;

import android.content.SharedPreferences;

abstract class AbstractPreference<T> {

	protected final SharedPreferences preferences;
	protected final String key;
	protected final T defaultValue;

	public AbstractPreference(SharedPreferences preferences, String key, T defaultValue) {
		this.preferences = preferences;
		this.key = key;
		this.defaultValue = defaultValue;
	}

	public boolean isSet() {
		return preferences.contains(key);
	}

	public final void set(T value) {
		SharedPreferences.Editor editor = preferences.edit();
		doSet(value, editor);
		editor.apply();
	}

	public final void clear() {
		preferences.edit().remove(key).apply();
	}

	protected abstract void doSet(T value, SharedPreferences.Editor editor);

	public final T get() {
		if (!isSet()) return defaultValue;
		else return doGet();
	}

	protected abstract T doGet();

}
