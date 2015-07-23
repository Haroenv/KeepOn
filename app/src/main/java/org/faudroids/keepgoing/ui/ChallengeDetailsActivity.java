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
import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.Challenge;
import org.faudroids.keepgoing.challenge.ChallengeManager;
import org.faudroids.keepgoing.utils.DefaultTransformer;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;


@ContentView(R.layout.activity_challenge_details)
public class ChallengeDetailsActivity extends AbstractActivity {

	public static final String EXTRA_CHALLENGE = "EXTRA_CHALLENGE";

	@InjectView(R.id.txt_name) private TextView nameTextView;
	@InjectView(R.id.img_challenge) private ImageView imageView;
	@InjectView(R.id.txt_distance) private TextView distanceTextView;
	@InjectView(R.id.txt_distance_completed) private TextView completedDistanceTextView;
	@InjectView(R.id.txt_time) private TextView timeTextView;
	@InjectView(R.id.list_recent_activities) private ListView recentActivitiesList;
	@InjectView(R.id.btn_add_session) private FloatingActionButton addSessionButton;
	private ArrayAdapter<String> recentActivitiesAdapter;

	@Inject private ChallengeManager challengeManager;
	private Challenge challenge;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup challenge overview
		challenge = getIntent().getParcelableExtra(EXTRA_CHALLENGE);
		nameTextView.setText(challenge.getName());
		imageView.setImageResource(getResources().getIdentifier(challenge.getImageName(), "drawable", getPackageName()));
		distanceTextView.setText(getString(R.string.distance_km, String.valueOf(challenge.getDistance() / 1000)));

		// setup recent activities
		recentActivitiesAdapter = new ActivitiesArrayAdapter(this);
		recentActivitiesAdapter.add("12 km in 2 hours");
		recentActivitiesAdapter.add("5.1 km in 1 hours");
		recentActivitiesAdapter.add("21 km in 3 hours");
		recentActivitiesList.setAdapter(recentActivitiesAdapter);

		// setup add session button
		addSessionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChallengeDetailsActivity.this, RecordingActivity.class);
				intent.putExtra(RecordingActivity.EXTRA_CHALLENGE, challenge);
				startActivity(intent);
			}
		});
	}


	@Override
	public void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		super.onGoogleApiClientConnected(googleApiClient);

		// setup completed kms
		challengeManager.getDistanceForChallenge(googleApiClient, challenge)
				.compose(new DefaultTransformer<Float>())
				.subscribe(new Action1<Float>() {
					@Override
					public void call(Float completedDistanceInMeters) {
						float completedPercentage = completedDistanceInMeters / challenge.getDistance();
						completedDistanceTextView.setText(getString(
								R.string.km_and_percentage_completed,
								String.format("%.1f", completedDistanceInMeters / 1000),
								String.format("%.2f", completedPercentage)));
					}
				});
	}


	private class ActivitiesArrayAdapter extends ArrayAdapter<String> {

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
			distanceTextView.setText(getItem(position));
			dateTextView.setText("12.11.2015");

			return itemView;
		}
	}

}
