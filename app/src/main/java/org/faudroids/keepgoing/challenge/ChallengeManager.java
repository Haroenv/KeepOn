package org.faudroids.keepgoing.challenge;

import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.faudroids.keepgoing.database.TransactionListenerAdapter;
import org.faudroids.keepgoing.utils.BooleanPreference;
import org.faudroids.keepgoing.utils.PreferenceFactory;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Handles {@link Challenge} objects.
 */
public class ChallengeManager {

	private static final String PREFS_NAME = "org.faudroids.keepgoing.challenges.ChallengeManager";
	private static final String KEY_FIRST_START = "KEY_FIRST_START";

	private final TransactionManager transactionManager;
	private final BooleanPreference firstStartPref;

	@Inject
	ChallengeManager(TransactionManager transactionManager, PreferenceFactory preferenceFactory) {
		this.transactionManager = transactionManager;
		this.firstStartPref = preferenceFactory.newBooleanPreference(PREFS_NAME, KEY_FIRST_START, true);
	}


	public Observable<List<Challenge>> getAllChallenges() {
		if (firstStartPref.get()) {
			firstStartPref.set(false);
			Challenge challenge = new Challenge(0, "Great Wall of China", 8851.8f, "None", "challenge_great_wall_of_china.jpg");
			challenge.insert();

		}

		TransactionListenerAdapter<List<Challenge>> adapter = new TransactionListenerAdapter<>();
		transactionManager.addTransaction(new SelectListTransaction<>(
				new Select().all().from(Challenge.class),
				adapter));

		return adapter.toObservable();
	}

}
