package org.faudroids.keepgoing.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.ChallengeData;
import org.faudroids.keepgoing.challenge.ChallengeManager;
import org.faudroids.keepgoing.sessions.SessionData;
import org.faudroids.keepgoing.utils.DefaultTransformer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;


@ContentView(R.layout.activity_challenge_details)
public class ChallengeDetailsActivity extends AbstractActivity {

	public static final int RESULT_FINISHED_CHALLENGE = 42;

	public static final String
			EXTRA_CHALLENGE_DATA = "EXTRA_CHALLENGE_DATA",
			EXTRA_HIDE_RECORDING_BUTTON = "EXTRA_HIDE_RECORDING_BUTTON";

	private static final int REQUEST_START_RECORDING = 42;

	private static final String STATE_HIDING_RECORDING_BUTTON = "HIDING_RECORDING_BUTTON";

	@InjectView(R.id.img_challenge) private ImageView imageView;
	@InjectView(R.id.collapsing_toolbar) private CollapsingToolbarLayout collapsingToolbarLayout;

	@InjectView(R.id.txt_distance) private TextView distanceTextView;
	@InjectView(R.id.txt_distance_completed) private TextView completedDistanceTextView;
	@InjectView(R.id.txt_time) private TextView timeTextView;
	@InjectView(R.id.btn_add_session) private FloatingActionButton addSessionButton;

	@InjectView(R.id.card_recent_activities) private CardView recentActivitiesCard;
	@InjectView(R.id.txt_recent_activities) private TextView recentActivitiesTextView;
	@InjectView(R.id.layout_recent_activities) private LinearLayout recentActivitiesLayout;

	@Inject private ChallengeManager challengeManager;
	private ChallengeData challengeData;

	private int toolbarColor, statusbarColor;
	private boolean hidingRecordingButton;


	public ChallengeDetailsActivity() {
		super(true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		challengeData = getIntent().getParcelableExtra(EXTRA_CHALLENGE_DATA);
		hidingRecordingButton = getIntent().getBooleanExtra(EXTRA_HIDE_RECORDING_BUTTON, false);
		if (savedInstanceState != null) hidingRecordingButton = savedInstanceState.getBoolean(STATE_HIDING_RECORDING_BUTTON);

		// setup header
		collapsingToolbarLayout.setTitle(challengeData.getChallenge().getName());
		collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));
		collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.FontToolbarHeader);

		// setup challenge overview
		Picasso.with(this)
				.load(getResources().getIdentifier(challengeData.getChallenge().getImageName(), "drawable", getPackageName()))
				.transform(new ToolbarColorTransformation())
				.into(imageView);
		distanceTextView.setText(getString(R.string.distance_km, String.valueOf(challengeData.getChallenge().getDistanceInMeters() / 1000)));

		// setup default toolbar colors
		toolbarColor = getResources().getColor(R.color.colorPrimary);
		statusbarColor = getResources().getColor(R.color.colorPrimaryDark);

		// setup add session button
		addSessionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(RecordingActivity.createIntent(
						ChallengeDetailsActivity.this,
						challengeData.getChallenge(),
						toolbarColor,
						statusbarColor),
						REQUEST_START_RECORDING);
			}
		});

		// setup progress
		setupChallengeProgress();

		// setup all recent activities click listener
		recentActivitiesTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(AllActivitiesActivity.createIntent(
						ChallengeDetailsActivity.this,
						challengeData,
						toolbarColor,
						statusbarColor));
			}
		});
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_START_RECORDING:
				if (resultCode != RESULT_OK) break;
				// reload challenge
				challengeManager.getChallenge(getGoogleApiClient(), challengeData.getChallenge().getId())
						.compose(new DefaultTransformer<ChallengeData>())
						.subscribe(new Action1<ChallengeData>() {
							@Override
							public void call(ChallengeData challengeData) {
								// check if challenge has been finished
								if (!challengeData.isOpen()) {
									hidingRecordingButton = true;
									new AlertDialog.Builder(ChallengeDetailsActivity.this)
											.setTitle(R.string.challenge_finished)
											.setMessage(R.string.challenge_finished_message)
											.setPositiveButton(android.R.string.ok, null)
											.setIcon(R.drawable.ic_trophy)
											.show();
								}

								// update stats
								ChallengeDetailsActivity.this.challengeData = challengeData;
								setupChallengeProgress();

							}
						});
				break;
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_HIDING_RECORDING_BUTTON, hidingRecordingButton);
		super.onSaveInstanceState(outState);
	}


	@Override
	public void onBackPressed() {
		if (!challengeData.isOpen()) {
			setResult(RESULT_FINISHED_CHALLENGE);
		}
		super.onBackPressed();
	}


	private void setupChallengeProgress() {
		// toggle recording button
		if (hidingRecordingButton) addSessionButton.setVisibility(View.GONE);

		// set completed kms
		float completedPercentage = (challengeData.getCompletedDistanceInMeters() / challengeData.getChallenge().getDistanceInMeters()) * 100;
		completedDistanceTextView.setText(getString(
				R.string.km_and_percentage_completed,
				String.format("%.1f", challengeData.getCompletedDistanceInMeters() / 1000),
				String.format("%.2f", completedPercentage)));

		// load running time
		float hours = challengeData.getCompletedTimeInSeconds() / (60.0f * 60.0f);
		timeTextView.setText(getString(R.string.hours_of_running, String.format("%.1f", hours)));

		// get recent activities
		List<SessionData> newestSessions = new ArrayList<>();
		int endIdx = challengeData.getSessionDataList().size() - 1;
		while (endIdx >= 0 && (challengeData.getSessionDataList().size() - endIdx) <= 3) {
			newestSessions.add(challengeData.getSessionDataList().get(endIdx));
			--endIdx;
		}

		// create views
		recentActivitiesLayout.removeAllViews();
		boolean firstIter = true;
		for (SessionData session : newestSessions) {
			// add separator
			if (!firstIter) {
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						getResources().getDimensionPixelSize(R.dimen.one_dp));
				int margin = getResources().getDimensionPixelSize(R.dimen.recent_activities_margin);
				layoutParams.setMargins(margin, 0, 0, 0);
				recentActivitiesLayout.addView(createSeparatorView(recentActivitiesLayout), layoutParams);
			}
			if (firstIter) firstIter = false;

			// add activity view
			recentActivitiesLayout.addView(
					createRecentActivityView(recentActivitiesLayout, session),
					new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		// hide activities card if empty
		if (newestSessions.isEmpty()) recentActivitiesCard.setVisibility(View.GONE);
		else recentActivitiesCard.setVisibility(View.VISIBLE);
	}


	private View createRecentActivityView(ViewGroup parent, final SessionData data) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// get views
		View itemView = inflater.inflate(R.layout.item_recent_activity, parent, false);
		TextView distanceTextView = (TextView) itemView.findViewById(R.id.txt_distance);
		TextView dateTextView = (TextView) itemView.findViewById(R.id.txt_date);

		// populate views
		distanceTextView.setText(getString(R.string.distance_km, String.format("%.2f", data.getDistanceInMeters() / 1000)));
		dateTextView.setText(SimpleDateFormat.getDateInstance().format(new Date(data.getSession().getStartTime(TimeUnit.MILLISECONDS))));

		// on click forward to session details
		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(SessionDetailsActivity.createIntent(
						ChallengeDetailsActivity.this,
						data,
						toolbarColor,
						statusbarColor));
			}
		});

		return itemView;
	}


	private View createSeparatorView(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.separator, parent, false);
	}


	private class ToolbarColorTransformation implements Transformation {
		@Override
		public Bitmap transform(Bitmap source) {
			Palette.from(source).maximumColorCount(32).generate(new Palette.PaletteAsyncListener() {
				@Override
				public void onGenerated(Palette palette) {
					toolbarColor = palette.getMutedColor(toolbarColor);
					statusbarColor = palette.getDarkMutedColor(statusbarColor);
					collapsingToolbarLayout.setContentScrimColor(toolbarColor);
					setStatusBarColor(statusbarColor);
				}
			});
			return source;
		}

		@Override
		public String key() {
			return "toolbar";
		}
	}

}
