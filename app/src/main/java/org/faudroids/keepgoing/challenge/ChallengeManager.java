package org.faudroids.keepgoing.challenge;

import com.google.android.gms.common.api.GoogleApiClient;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.SelectSingleModelTransaction;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.faudroids.keepgoing.database.TransactionListenerAdapter;
import org.faudroids.keepgoing.sessions.SessionData;
import org.faudroids.keepgoing.sessions.SessionManager;
import org.faudroids.keepgoing.utils.BooleanPreference;
import org.faudroids.keepgoing.utils.PreferenceFactory;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Handles {@link Challenge} objects.
 */
public class ChallengeManager {

	private static final String PREFS_NAME = "org.faudroids.keepgoing.challenges.ChallengeManager";
	private static final String KEY_FIRST_START = "KEY_FIRST_START";

	private final TransactionManager transactionManager;
	private final SessionManager sessionManager;
	private final BooleanPreference firstStartPref;


	@Inject
	ChallengeManager(TransactionManager transactionManager, SessionManager sessionManager, PreferenceFactory preferenceFactory) {
		this.transactionManager = transactionManager;
		this.sessionManager = sessionManager;
		this.firstStartPref = preferenceFactory.newBooleanPreference(PREFS_NAME, KEY_FIRST_START, true);
	}


	public Observable<List<ChallengeData>> getAllChallenges(final GoogleApiClient googleApiClient) {
		return setupChallenges()
				.flatMap(new Func1<Void, Observable<List<Challenge>>>() {
					@Override
					public Observable<List<Challenge>> call(Void nothing) {
						TransactionListenerAdapter<List<Challenge>> adapter = new TransactionListenerAdapter<>();
						transactionManager.addTransaction(new SelectListTransaction<>(
								new Select().all().from(Challenge.class),
								adapter));

						return adapter.toObservable();
					}
				})
				.flatMap(new Func1<List<Challenge>, Observable<Challenge>>() {
					@Override
					public Observable<Challenge> call(List<Challenge> challenges) {
						return Observable.from(challenges);
					}
				})
				.flatMap(new Func1<Challenge, Observable<ChallengeData>>() {
					@Override
					public Observable<ChallengeData> call(final Challenge challenge) {
						return challengeToChallengeData(googleApiClient, challenge);
					}
				})
				.toSortedList();
	}


	public Observable<ChallengeData> getChallenge(final GoogleApiClient googleApiClient, final long id) {
		return Observable
				.defer(new Func0<Observable<Challenge>>() {
					@Override
					public Observable<Challenge> call() {
						TransactionListenerAdapter<Challenge> adapter = new TransactionListenerAdapter<>();
						transactionManager.addTransaction(new SelectSingleModelTransaction<>(
								Challenge.class,
								adapter,
								Condition.column(Challenge$Table.ID).is(id)));
						return adapter.toObservable();
					}
				})
				.flatMap(new Func1<Challenge, Observable<ChallengeData>>() {
					@Override
					public Observable<ChallengeData> call(final Challenge challenge) {
						return challengeToChallengeData(googleApiClient, challenge);
					}
				});
	}


	private Observable<ChallengeData> challengeToChallengeData(final GoogleApiClient googleApiClient, final Challenge challenge) {
		return Observable.from(challenge.getSessionIdList())
				.flatMap(new Func1<String, Observable<SessionData>>() {
					@Override
					public Observable<SessionData> call(String sessionId) {
						return sessionManager.loadSessionData(googleApiClient, sessionId);
					}
				})
				.toSortedList()
				.map(new Func1<List<SessionData>, ChallengeData>() {
					@Override
					public ChallengeData call(List<SessionData> sessionDataList) {
						return new ChallengeData(challenge, sessionDataList);
					}
				});
	}


	private Observable<Void> setupChallenges() {
		return Observable.defer(new Func0<Observable<Void>>() {
			@Override
			public Observable<Void> call() {
				if (firstStartPref.get()) {
					firstStartPref.set(false);
					Challenge challenge = new Challenge(0, "Great Wall of China", 8851800, "None", "challenge_great_wall_of_china");
					challenge.insert();
				}
				return Observable.just(null);
			}
		});
	}

}
