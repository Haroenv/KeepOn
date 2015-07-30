package org.faudroids.keepgoing.ui;

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

	static final String EXTRA_CHALLENGE_DATA = "EXTRA_CHALLENGE_DATA";

	@InjectView(R.id.list) private RecyclerView recyclerView;

	public AllActivitiesActivity() {
		super(true);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.all_activities);
		ChallengeData challengeData = getIntent().getParcelableExtra(EXTRA_CHALLENGE_DATA);

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
					Intent intent = new Intent(AllActivitiesActivity.this, SessionDetailsActivity.class);
					intent.putExtra(SessionDetailsActivity.EXTRA_SESSION_ID, data.getSession().getIdentifier());
					startActivity(intent);
				}
			});
		}
	}

}
