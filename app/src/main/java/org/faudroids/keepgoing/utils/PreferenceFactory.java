package org.faudroids.keepgoing.utils;

import android.content.Context;

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

	public StringPreference newStringPreference(String prefName, String key) {
		return new StringPreference(context.getSharedPreferences(prefName, Context.MODE_PRIVATE), key);
	}

}
