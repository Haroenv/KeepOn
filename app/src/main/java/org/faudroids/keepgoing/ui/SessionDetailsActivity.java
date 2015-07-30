package org.faudroids.keepgoing.ui;

import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.sessions.SessionData;
import org.faudroids.keepgoing.sessions.SessionManager;
import org.faudroids.keepgoing.utils.DefaultTransformer;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;

@ContentView(R.layout.activity_recording)
public class SessionDetailsActivity extends AbstractMapActivity {

	static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";

	private static final DecimalFormat timeFormat = new DecimalFormat("00");

	@InjectView(R.id.txt_duration) private TextView durationTextView;
	@InjectView(R.id.txt_distance) private TextView distanceTextView;
	@InjectView(R.id.txt_avg_speed) private TextView avgSpeedTextView;
	@InjectView(R.id.btn_start_recording) private Button startRecordingButton;

	@Inject private SessionManager sessionManager;

	public SessionDetailsActivity() {
		super(true, false);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startRecordingButton.setVisibility(TextView.GONE);
	}


	@Override
	public void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		super.onGoogleApiClientConnected(googleApiClient);
		String sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
		Timber.i("loading session with id " + sessionId);

		sessionManager.loadSessionData(googleApiClient, sessionId)
				.compose(new DefaultTransformer<SessionData>())
				.subscribe(new Action1<SessionData>() {
					@Override
					public void call(SessionData sessionData) {
						drawPolyline(sessionData.getLocations());

						// update distance
						float distanceInMeters = 0;
						Iterator<Location> iterator = sessionData.getLocations().iterator();
						Location locationIter = iterator.next();
						while (iterator.hasNext()) {
							Location nextLocation = iterator.next();
							distanceInMeters += locationIter.distanceTo(nextLocation);
							locationIter = nextLocation;
						}
						float distanceInKm = distanceInMeters / 1000;
						distanceTextView.setText(String.format("%.2f", distanceInKm));

						// update avg speed
						double timeInHours = (sessionData.getSession().getEndTime(TimeUnit.SECONDS) - sessionData.getSession().getStartTime(TimeUnit.SECONDS)) / (60.0 * 60.0);
						double avgSpeed = distanceInKm / timeInHours;
						avgSpeedTextView.setText(String.format("%.2f", (float) avgSpeed));

						// update passed time
						long passedTimeInSeconds = sessionData.getSession().getEndTime(TimeUnit.SECONDS) - sessionData.getSession().getStartTime(TimeUnit.SECONDS);
						long hours = TimeUnit.SECONDS.toHours(passedTimeInSeconds);
						long minutes = TimeUnit.SECONDS.toMinutes(passedTimeInSeconds) - (hours * 60);
						long seconds = passedTimeInSeconds - ((hours * 60) + minutes) * 60;
						durationTextView.setText(timeFormat.format(hours) + ":" + timeFormat.format(minutes) + ":" + timeFormat.format(seconds));
					}
				});
	}

}
