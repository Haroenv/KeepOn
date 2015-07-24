package org.faudroids.keepgoing.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.ChallengeData;

import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;


public class FinishedChallengesFragment extends AbstractChallengesFragment {

	public FinishedChallengesFragment() {
		super(R.layout.fragment_challenge_overview);
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

		private final TextView nameTextView, distanceTextView, timeTextView;
		private final ImageView imageView;

		public ChallengeViewHolder(View itemView) {
			super(itemView);
			this.nameTextView = (TextView) itemView.findViewById(R.id.txt_name);
			this.distanceTextView = (TextView) itemView.findViewById(R.id.txt_distance);
			this.timeTextView = (TextView) itemView.findViewById(R.id.txt_time);
			this.imageView = (ImageView) itemView.findViewById(R.id.img_challenge);
		}

		@Override
		public void setData(final ChallengeData challengeData) {
			// setup view content
			nameTextView.setText(challengeData.getChallenge().getName());
			imageView.setImageResource(getResources().getIdentifier(challengeData.getChallenge().getImageName(), "drawable", getActivity().getPackageName()));

			// set completed info
			distanceTextView.setText(getString(R.string.distance_km, String.format("%.1f", challengeData.getCompletedDistanceInMeters() / 1000f)));
			float hours = challengeData.getCompletedTimeInSeconds() / (60.0f * 60.0f);
			timeTextView.setText(getString(R.string.hours_of_running, String.format("%.1f", hours)));
		}

	}

}
