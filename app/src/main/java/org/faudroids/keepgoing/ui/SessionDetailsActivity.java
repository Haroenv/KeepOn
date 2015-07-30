package org.faudroids.keepgoing.ui;

import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.sessions.SessionData;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_recording)
public class SessionDetailsActivity extends AbstractMapActivity {

	static final String EXTRA_SESSION = "EXTRA_SESSION";

	private static final DecimalFormat timeFormat = new DecimalFormat("00");

	@InjectView(R.id.txt_duration) private TextView durationTextView;
	@InjectView(R.id.txt_distance) private TextView distanceTextView;
	@InjectView(R.id.txt_avg_speed) private TextView avgSpeedTextView;
	@InjectView(R.id.btn_start_recording) private Button startRecordingButton;

	private  SessionData sessionData;

	public SessionDetailsActivity() {
		super(true, false);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startRecordingButton.setVisibility(TextView.GONE);
		sessionData = getIntent().getParcelableExtra(EXTRA_SESSION);

		// set title
		setTitle(SimpleDateFormat.getDateInstance().format(new Date(sessionData.getSession().getStartTime(TimeUnit.MILLISECONDS))));

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


	@Override
	public void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		super.onGoogleApiClientConnected(googleApiClient);

		// update map
		drawPolyline(sessionData.getLocations());
	}

}
