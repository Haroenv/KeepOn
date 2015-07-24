package org.faudroids.keepgoing.challenge;

import com.google.android.gms.common.api.GoogleApiClient;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.faudroids.keepgoing.database.TransactionListenerAdapter;
import org.faudroids.keepgoing.sessions.SessionManager;
import org.faudroids.keepgoing.sessions.SessionOverview;
import org.faudroids.keepgoing.utils.BooleanPreference;
import org.faudroids.keepgoing.utils.PreferenceFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
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


	public Observable<List<Challenge>> getAllChallenges() {
		if (firstStartPref.get()) {
			firstStartPref.set(false);
			Challenge challenge = new Challenge(0, "Great Wall of China", 8851800, "None", "challenge_great_wall_of_china");
			challenge.insert();
		}

		TransactionListenerAdapter<List<Challenge>> adapter = new TransactionListenerAdapter<>();
		transactionManager.addTransaction(new SelectListTransaction<>(
				new Select().all().from(Challenge.class),
				adapter));

		return adapter.toObservable();
	}


	public Observable<List<SessionOverview>> getSessionsForChallenge(final GoogleApiClient googleApiClient, Challenge challenge) {
		return Observable.from(challenge.getSessionIdList())
				.flatMap(new Func1<String, Observable<SessionOverview>>() {
					@Override
					public Observable<SessionOverview> call(String sessionId) {
						return sessionManager.loadSessionOverview(googleApiClient, sessionId);
					}
				})
				.toSortedList();
	}


	public Observable<Float> getDistanceInMetersForChallenge(final GoogleApiClient googleApiClient, Challenge challenge) {
		List<String> sessionIds = challenge.getSessionIdList();
		if (sessionIds.isEmpty()) return Observable.just(0.0f);
		return getSessionsForChallenge(googleApiClient,  challenge)
				.flatMap(new Func1<List<SessionOverview>, Observable<Float>>() {
					@Override
					public Observable<Float> call(List<SessionOverview> sessionOverviews) {
						float distance = 0;
						for (SessionOverview overview : sessionOverviews) {
							distance += overview.getTotalDistanceInMeters();
						}
						return Observable.just(distance);
					}
				});
	}


	public Observable<Long> getTotalTimeInSeconds(final GoogleApiClient googleApiClient, Challenge challenge) {
		List<String> sessionIds = challenge.getSessionIdList();
		if (sessionIds.isEmpty()) return Observable.just(0l);
		return getSessionsForChallenge(googleApiClient,  challenge)
				.flatMap(new Func1<List<SessionOverview>, Observable<Long>>() {
					@Override
					public Observable<Long> call(List<SessionOverview> sessionOverviews) {
						long totalTime = 0;
						for (SessionOverview overview : sessionOverviews) {
							totalTime += (overview.getSession().getEndTime(TimeUnit.SECONDS) - overview.getSession().getStartTime(TimeUnit.SECONDS));
						}
						return Observable.just(totalTime);
					}
				});
	}

}
