package org.faudroids.keepgoing.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


@ContentView(R.layout.activity_challenge_details)
public class ChallengeDetailsActivity extends AbstractActivity {

	public static final String
			EXTRA_CHALLENGE_DATA = "EXTRA_CHALLENGE_DATA",
			EXTRA_HIDE_RECORDING_BUTTON = "EXTRA_HIDE_RECORDING_BUTTON";

	private static final int REQUEST_START_RECORDING = 42;

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


	public ChallengeDetailsActivity() {
		super(true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		challengeData = getIntent().getParcelableExtra(EXTRA_CHALLENGE_DATA);

		// setup header
		collapsingToolbarLayout.setTitle(challengeData.getChallenge().getName());
		collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));
		collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.FontToolbarHeader);

		// setup challenge overview
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(challengeData.getChallenge().getImageName(), "drawable", getPackageName()));
		imageView.setImageBitmap(bitmap);
		distanceTextView.setText(getString(R.string.distance_km, String.valueOf(challengeData.getChallenge().getDistanceInMeters() / 1000)));

		// setup toolbar color
		toolbarColor = getResources().getColor(R.color.colorPrimary);
		statusbarColor = getResources().getColor(R.color.colorPrimaryDark);
		Palette.from(bitmap).maximumColorCount(32).generate(new Palette.PaletteAsyncListener() {
			@Override
			public void onGenerated(Palette palette) {
				toolbarColor = palette.getMutedColor(toolbarColor);
				statusbarColor = palette.getDarkMutedColor(statusbarColor);
				collapsingToolbarLayout.setContentScrimColor(toolbarColor);
				setStatusBarColor(statusbarColor);
			}
		});

		// setup add session button
		boolean hideRecordingButton = getIntent().getBooleanExtra(EXTRA_HIDE_RECORDING_BUTTON, false);
		if (hideRecordingButton) addSessionButton.setVisibility(View.GONE);
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
								ChallengeDetailsActivity.this.challengeData = challengeData;
								setupChallengeProgress();
							}
						});
				break;
		}
	}


	private void setupChallengeProgress() {
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

}
