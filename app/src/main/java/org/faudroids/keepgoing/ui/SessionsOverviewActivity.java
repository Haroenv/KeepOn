package org.faudroids.keepgoing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.sessions.SessionData;
import org.faudroids.keepgoing.utils.DefaultTransformer;
import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.sessions.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;

@ContentView(R.layout.activity_sessions_overview)
public class SessionsOverviewActivity extends AbstractActivity {

	@InjectView(R.id.list) private RecyclerView recyclerView;
	private SessionsAdapter sessionsAdapter;
	@Inject private SessionManager sessionManager;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup sessions list
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		sessionsAdapter = new SessionsAdapter();
		recyclerView.setAdapter(sessionsAdapter);
	}


	@Override
	public void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		super.onGoogleApiClientConnected(googleApiClient);
		sessionManager.loadSessionData(googleApiClient)
				.compose(new DefaultTransformer<List<SessionData>>())
				.subscribe(new Action1<List<SessionData>>() {
					@Override
					public void call(List<SessionData> sessionOverviews) {
						sessionsAdapter.setData(sessionOverviews);
					}
				});
	}


	private class SessionsAdapter extends RecyclerView.Adapter {

		private final List<SessionData> sessionsList = new ArrayList<>();

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
			View view = LayoutInflater
					.from(viewGroup.getContext())
					.inflate(R.layout.card_session, viewGroup, false);
			SessionViewHolder viewHolder = new SessionViewHolder(view);
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
			((SessionViewHolder) viewHolder).setData(sessionsList.get(position));
		}

		@Override
		public int getItemCount() {
			return sessionsList.size();
		}

		public void setData(List<SessionData> data) {
			this.sessionsList.clear();;
			this.sessionsList.addAll(data);
			notifyDataSetChanged();
		}
	}


	private class SessionViewHolder extends RecyclerView.ViewHolder {

		private final View itemView;
		private final TextView dateTextView, distanceTextView;

		public SessionViewHolder(View itemView) {
			super(itemView);
			this.itemView = itemView;
			this.dateTextView = (TextView) itemView.findViewById(R.id.txt_time);
			this.distanceTextView = (TextView) itemView.findViewById(R.id.txt_distance);
		}

		public void setData(final SessionData data) {
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent detailsIntent = new Intent(SessionsOverviewActivity.this, SessionDetailsActivity.class);
					detailsIntent.putExtra(SessionDetailsActivity.EXTRA_SESSION_ID, data.getSession().getIdentifier());
					startActivity(detailsIntent);
				}
			});
			dateTextView.setText(SimpleDateFormat.getDateInstance().format(new Date(data.getSession().getStartTime(TimeUnit.MILLISECONDS))));
			distanceTextView.setText(data.getDistanceInMeters() + " m");
		}

	}


}
