package org.faudroids.keepgoing.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.ChallengeData;
import org.faudroids.keepgoing.challenge.ChallengeManager;
import org.faudroids.keepgoing.sessions.SessionData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_challenge_details)
public class ChallengeDetailsActivity extends AbstractActivity {

	public static final String EXTRA_CHALLENGE_DATA = "EXTRA_CHALLENGE_DATA";

	@InjectView(R.id.txt_name) private TextView nameTextView;
	@InjectView(R.id.img_challenge) private ImageView imageView;
	@InjectView(R.id.txt_distance) private TextView distanceTextView;
	@InjectView(R.id.txt_distance_completed) private TextView completedDistanceTextView;
	@InjectView(R.id.txt_time) private TextView timeTextView;
	@InjectView(R.id.list_recent_activities) private ListView recentActivitiesList;
	@InjectView(R.id.btn_add_session) private FloatingActionButton addSessionButton;
	private ArrayAdapter<SessionData> recentActivitiesAdapter;

	@Inject private ChallengeManager challengeManager;
	private ChallengeData challengeData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup challenge overview
		challengeData = getIntent().getParcelableExtra(EXTRA_CHALLENGE_DATA);
		nameTextView.setText(challengeData.getChallenge().getName());
		imageView.setImageResource(getResources().getIdentifier(challengeData.getChallenge().getImageName(), "drawable", getPackageName()));
		distanceTextView.setText(getString(R.string.distance_km, String.valueOf(challengeData.getChallenge().getDistanceInMeters() / 1000)));

		// set completed kms
		float completedPercentage = challengeData.getCompletedDistanceInMeters() / challengeData.getChallenge().getDistanceInMeters();
		completedDistanceTextView.setText(getString(
				R.string.km_and_percentage_completed,
				String.format("%.1f", challengeData.getCompletedDistanceInMeters() / 1000),
				String.format("%.2f", completedPercentage)));

		// load running time
		float hours = challengeData.getCompletedTimeInSeconds() / (60.0f * 60.0f);
		timeTextView.setText(getString(R.string.hours_of_running, String.format("%.1f", hours)));

		// setup recent activities
		recentActivitiesAdapter = new ActivitiesArrayAdapter(this);
		recentActivitiesList.setAdapter(recentActivitiesAdapter);
		List<SessionData> newestSessions = new ArrayList<>();
		int endIdx = challengeData.getSessionDataList().size() - 1;
		while (endIdx >= 0 && (challengeData.getSessionDataList().size() - endIdx) <= 3) {
			newestSessions.add(challengeData.getSessionDataList().get(endIdx));
			--endIdx;
		}
		recentActivitiesAdapter.clear();
		recentActivitiesAdapter.addAll(newestSessions);
		recentActivitiesAdapter.notifyDataSetChanged();

		// setup add session button
		addSessionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChallengeDetailsActivity.this, RecordingActivity.class);
				intent.putExtra(RecordingActivity.EXTRA_CHALLENGE, challengeData.getChallenge());
				startActivity(intent);
			}
		});
	}


	private class ActivitiesArrayAdapter extends ArrayAdapter<SessionData> {

		public ActivitiesArrayAdapter(Context context) {
			super(context, R.layout.item_recent_activity);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// get views
			View itemView = inflater.inflate(R.layout.item_recent_activity, parent, false);
			TextView distanceTextView = (TextView) itemView.findViewById(R.id.txt_distance);
			TextView dateTextView = (TextView) itemView.findViewById(R.id.txt_date);

			// populate views
			final SessionData data = getItem(position);
			distanceTextView.setText(getString(R.string.distance_km, String.format("%.2f", data.getDistanceInMeters() / 1000)));
			dateTextView.setText(SimpleDateFormat.getDateInstance().format(new Date(data.getSession().getStartTime(TimeUnit.MILLISECONDS))));

			// on click forward to session details
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ChallengeDetailsActivity.this, SessionDetailsActivity.class);
					intent.putExtra(SessionDetailsActivity.EXTRA_SESSION_ID, data.getSession().getIdentifier());
					startActivity(intent);
				}
			});

			return itemView;
		}
	}

}
