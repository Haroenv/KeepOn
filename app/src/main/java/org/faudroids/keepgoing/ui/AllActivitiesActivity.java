package org.faudroids.keepgoing.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.ChallengeData;
import org.faudroids.keepgoing.sessions.SessionData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_all_activities)
public class AllActivitiesActivity extends AbstractActivity {

	public static Intent createIntent(Context context, ChallengeData challengeData, int toolbarColor, int statusbarColor) {
		Intent intent = new Intent(context, AllActivitiesActivity.class);
		intent.putExtra(EXTRA_CHALLENGE_DATA, challengeData);
		intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);
		intent.putExtra(EXTRA_STATUSBAR_COLOR, statusbarColor);
		return intent;
	}

	static final String
			EXTRA_CHALLENGE_DATA = "EXTRA_CHALLENGE_DATA",
			EXTRA_TOOLBAR_COLOR = "EXTRA_TOOLBAR_COLOR",
			EXTRA_STATUSBAR_COLOR = "EXTRA_STATUSBAR_COLOR";

	@InjectView(R.id.list) private RecyclerView recyclerView;
	@InjectView(R.id.txt_empty) private TextView emptyTextView;

	public AllActivitiesActivity() {
		super(true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ChallengeData challengeData = getIntent().getParcelableExtra(EXTRA_CHALLENGE_DATA);

		// setup toolbar
		setTitle(R.string.all_activities);
		toolbar.setBackgroundColor(getIntent().getIntExtra(EXTRA_TOOLBAR_COLOR, 0));
		setStatusBarColor(getIntent().getIntExtra(EXTRA_STATUSBAR_COLOR, 0));

		// setup activities list
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		ActivitiesAdapter activitiesAdapter = new ActivitiesAdapter();
		recyclerView.setAdapter(activitiesAdapter);
		recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

		// sort by date (newest first)
		List<SessionData> data = new ArrayList<>(challengeData.getSessionDataList());
		Collections.reverse(data);
		activitiesAdapter.setData(data);

		// toggle empty view
		if (data.isEmpty()) emptyTextView.setVisibility(View.VISIBLE);
		else emptyTextView.setVisibility(View.GONE);
	}


	private class ActivitiesAdapter extends RecyclerView.Adapter {

		private final List<SessionData> sessionDataList = new ArrayList<>();

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recent_activity, viewGroup, false);
			return new ActivityViewHolder(view);
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
			((ActivityViewHolder) viewHolder).setData(sessionDataList.get(position));
		}

		@Override
		public int getItemCount() {
			return sessionDataList.size();
		}

		public void setData(List<SessionData> data) {
			this.sessionDataList.clear();
			this.sessionDataList.addAll(data);
			notifyDataSetChanged();
		}
	}


	protected class ActivityViewHolder extends RecyclerView.ViewHolder {

		private final TextView distanceTextView, dateTextView;

		public ActivityViewHolder(View itemView) {
			super(itemView);
			this.distanceTextView = (TextView) itemView.findViewById(R.id.txt_distance);
			this.dateTextView = (TextView) itemView.findViewById(R.id.txt_date);
		}

		public void setData(final SessionData data) {
			// populate views
			distanceTextView.setText(getString(R.string.distance_km, String.format("%.2f", data.getDistanceInMeters() / 1000)));
			dateTextView.setText(SimpleDateFormat.getDateInstance().format(new Date(data.getSession().getStartTime(TimeUnit.MILLISECONDS))));

			// on click forward to session details
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(SessionDetailsActivity.createIntent(
							AllActivitiesActivity.this,
							data,
							getIntent().getIntExtra(EXTRA_TOOLBAR_COLOR, 0),
							getIntent().getIntExtra(EXTRA_STATUSBAR_COLOR, 0)));
				}
			});
		}
	}

}
