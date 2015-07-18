package org.faudroids.keepgoing.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.DefaultTransformer;
import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.sessions.SessionManager;
import org.faudroids.keepgoing.sessions.SessionOverview;

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
	protected void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		sessionManager.loadSessionOverviews(googleApiClient)
				.compose(new DefaultTransformer<List<SessionOverview>>())
				.subscribe(new Action1<List<SessionOverview>>() {
					@Override
					public void call(List<SessionOverview> sessionOverviews) {
						sessionsAdapter.setData(sessionOverviews);
					}
				});
	}


	private class SessionsAdapter extends RecyclerView.Adapter {

		private final List<SessionOverview> sessionsList = new ArrayList<>();

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

		public void setData(List<SessionOverview> data) {
			this.sessionsList.clear();;
			this.sessionsList.addAll(data);
			notifyDataSetChanged();
		}
	}


	private class SessionViewHolder extends RecyclerView.ViewHolder {

		private final TextView dateTextView, distanceTextView;

		public SessionViewHolder(View view) {
			super(view);
			dateTextView = (TextView) view.findViewById(R.id.txt_time);
			distanceTextView = (TextView) view.findViewById(R.id.txt_distance);
		}

		public void setData(SessionOverview overview) {
			dateTextView.setText(SimpleDateFormat.getDateInstance().format(new Date(overview.getSession().getStartTime(TimeUnit.MILLISECONDS))));
			distanceTextView.setText(overview.getTotalDistanceInMeters() + " m");
		}

	}


}
