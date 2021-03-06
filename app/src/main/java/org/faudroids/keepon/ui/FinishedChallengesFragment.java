package org.faudroids.keepon.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.faudroids.keepon.R;
import org.faudroids.keepon.auth.AuthManager;
import org.faudroids.keepon.challenge.ChallengeData;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;


public class FinishedChallengesFragment extends AbstractChallengesFragment {

	@Inject private AuthManager authManager;

	public FinishedChallengesFragment() {
		super(R.layout.fragment_challenge_overview, R.string.finished_challenges, R.string.no_finished_challenges);
	}


	@Override
	protected Func1<List<ChallengeData>, Observable<List<ChallengeData>>> filterChallenges() {
		return new Func1<List<ChallengeData>, Observable<List<ChallengeData>>>() {
			@Override
			public Observable<List<ChallengeData>> call(List<ChallengeData> data) {
				Iterator<ChallengeData> iter = data.iterator();
				while (iter.hasNext()) {
					if (iter.next().isOpen()) iter.remove();
				}
				return Observable.just(data);
			}
		};
	}


	@Override
	protected AbstractChallengeViewHolder onCreateViewHolder(ViewGroup viewGroup) {
		View view = LayoutInflater
				.from(viewGroup.getContext())
				.inflate(R.layout.card_challenge_finished, viewGroup, false);
		return new ChallengeViewHolder(view);
	}


	private class ChallengeViewHolder extends AbstractChallengeViewHolder {

		private final View containerView;
		private final TextView nameTextView, distanceTextView, timeTextView;
		private final ImageView challengeImageView, userImageView;

		public ChallengeViewHolder(View itemView) {
			super(itemView);
			this.containerView = itemView.findViewById(R.id.container);
			this.nameTextView = (TextView) itemView.findViewById(R.id.txt_name);
			this.distanceTextView = (TextView) itemView.findViewById(R.id.txt_distance);
			this.timeTextView = (TextView) itemView.findViewById(R.id.txt_time);
			this.challengeImageView = (ImageView) itemView.findViewById(R.id.img_challenge);
			this.userImageView = (ImageView) itemView.findViewById(R.id.img_profile);
		}

		@Override
		public void setData(final ChallengeData challengeData) {
			// setup view content
			nameTextView.setText(challengeData.getChallenge().getName());
			challengeImageView.setImageResource(getResources().getIdentifier(challengeData.getChallenge().getImageName(), "drawable", getActivity().getPackageName()));
			Picasso.with(getActivity())
					.load(authManager.getAccount().getImageUrl())
					.resizeDimen(R.dimen.user_photo_challenge_size, R.dimen.user_photo_challenge_size)
					.transform(new CircleTransformation())
					.into(userImageView);

			// set completed info
			distanceTextView.setText(getString(R.string.distance_km, String.format("%.1f", challengeData.getCompletedDistanceInMeters() / 1000f)));
			float hours = challengeData.getCompletedTimeInSeconds() / (60.0f * 60.0f);
			timeTextView.setText(getString(R.string.hours_of_running, String.format("%.1f", hours)));

			// setup on click
			containerView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), ChallengeDetailsActivity.class);
					intent.putExtra(ChallengeDetailsActivity.EXTRA_CHALLENGE_DATA, challengeData);
					intent.putExtra(ChallengeDetailsActivity.EXTRA_HIDE_RECORDING_BUTTON, true);
					startActivity(intent);
				}
			});
		}

	}

}
