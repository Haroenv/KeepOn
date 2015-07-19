package org.faudroids.keepgoing.ui;

import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.utils.DefaultTransformer;
import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.sessions.SessionDetails;
import org.faudroids.keepgoing.sessions.SessionManager;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;

@ContentView(R.layout.activity_sessions_details)
public class SessionDetailsActivity extends AbstractMapActivity {

	static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";

	@InjectView(R.id.txt_distance) private TextView distanceView;
	@Inject private SessionManager sessionManager;

	public SessionDetailsActivity() {
		super(false);
	}


	@Override
	protected void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		String sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
		Timber.i("loading session with id " + sessionId);

		sessionManager.loadSessionDetails(googleApiClient, sessionId)
				.compose(new DefaultTransformer<SessionDetails>())
				.subscribe(new Action1<SessionDetails>() {
					@Override
					public void call(SessionDetails sessionDetails) {
						Timber.d("drawing " + sessionDetails.getLocations().size() + " waypoints and distance of " + sessionDetails.getTotalDistanceInMeters());
						drawPolyline(sessionDetails.getLocations());
						distanceView.setText(sessionDetails.getTotalDistanceInMeters() + " m");
					}
				});
	}

}
