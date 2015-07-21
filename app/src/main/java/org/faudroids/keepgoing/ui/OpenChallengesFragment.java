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

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.Challenge;
import org.faudroids.keepgoing.challenge.ChallengeManager;
import org.faudroids.keepgoing.utils.DefaultTransformer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import rx.functions.Action1;


public class OpenChallengesFragment extends AbstractFragment {

	@InjectView(R.id.list) private RecyclerView recyclerView;
	private ChallengeAdapter challengeAdapter;
	@Inject private ChallengeManager challengeManager;


	public OpenChallengesFragment() {
		super(R.layout.fragment_challenge_overview);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// setup sessions list
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);
		challengeAdapter = new ChallengeAdapter();
		recyclerView.setAdapter(challengeAdapter);
	}


	@Override
	public void onGoogleApiClientConnected(GoogleApiClient googleApiClient) {
		super.onGoogleApiClientConnected(googleApiClient);
		challengeManager.getAllChallenges()
				.compose(new DefaultTransformer<List<Challenge>>())
				.subscribe(new Action1<List<Challenge>>() {
					@Override
					public void call(List<Challenge> challenges) {
						challengeAdapter.setData(challenges);
					}
				});
	}


	private class ChallengeAdapter extends RecyclerView.Adapter {

		private final List<Challenge> challengeList = new ArrayList<>();

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
			View view = LayoutInflater
					.from(viewGroup.getContext())
					.inflate(R.layout.card_challenge_overview, viewGroup, false);
			return new ChallengeViewHolder(view);
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
			((ChallengeViewHolder) viewHolder).setData(challengeList.get(position));
		}

		@Override
		public int getItemCount() {
			return challengeList.size();
		}

		public void setData(List<Challenge> data) {
			this.challengeList.clear();
			this.challengeList.addAll(data);
			notifyDataSetChanged();
		}
	}


	private class ChallengeViewHolder extends RecyclerView.ViewHolder {

		private final View itemView;
		private final TextView nameTextView, distanceTextView, completedTextView;

		public ChallengeViewHolder(View itemView) {
			super(itemView);
			this.itemView = itemView;
			this.nameTextView = (TextView) itemView.findViewById(R.id.txt_name);
			this.distanceTextView = (TextView) itemView.findViewById(R.id.txt_distance);
			this.completedTextView = (TextView) itemView.findViewById(R.id.txt_completed);
		}

		public void setData(final Challenge challenge) {
			// setup view content
			nameTextView.setText(challenge.getName());
			distanceTextView.setText(getString(R.string.distance_km, String.valueOf(challenge.getDistance())));
			completedTextView.setText(getString(R.string.percentage_completed, String.valueOf(41)));

			// set forward to details click listener
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), ChallengeDetailsActivity.class);
					intent.putExtra(ChallengeDetailsActivity.EXTRA_CHALLENGE, challenge);
					startActivity(intent);
				}
			});
		}

	}

}
