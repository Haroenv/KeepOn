package org.faudroids.keepon.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.faudroids.keepon.R;
import org.faudroids.keepon.challenge.ChallengeData;

import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;


public class OpenChallengesFragment extends AbstractChallengesFragment {

	private static final int REQUEST_SHOW_CHALLENGE_DETAILS = 42;

	public OpenChallengesFragment() {
		super(R.layout.fragment_challenge_overview, R.string.open_challenges, R.string.no_open_challenges);
	}


	@Override
	protected Func1<List<ChallengeData>, Observable<List<ChallengeData>>> filterChallenges() {
		return new Func1<List<ChallengeData>, Observable<List<ChallengeData>>>() {
			@Override
			public Observable<List<ChallengeData>> call(List<ChallengeData> data) {
				Iterator<ChallengeData> iter = data.iterator();
				while (iter.hasNext()) {
					if (!iter.next().isOpen()) iter.remove();
				}
				return Observable.just(data);
			}
		};
	}


	@Override
	protected AbstractChallengeViewHolder onCreateViewHolder(ViewGroup viewGroup) {
		View view = LayoutInflater
				.from(viewGroup.getContext())
				.inflate(R.layout.card_challenge_open, viewGroup, false);
		return new ChallengeViewHolder(view);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SHOW_CHALLENGE_DETAILS:
				if (resultCode != ChallengeDetailsActivity.RESULT_FINISHED_CHALLENGE) return;
				setupChallengesList();
		}
	}


	private class ChallengeViewHolder extends AbstractChallengeViewHolder {

		private final View itemView;
		private final TextView nameTextView, distanceTextView, completedTextView;
		private final ImageView imageView;

		public ChallengeViewHolder(View itemView) {
			super(itemView);
			this.itemView = itemView;
			this.nameTextView = (TextView) itemView.findViewById(R.id.txt_name);
			this.distanceTextView = (TextView) itemView.findViewById(R.id.txt_distance);
			this.completedTextView = (TextView) itemView.findViewById(R.id.txt_completed);
			this.imageView = (ImageView) itemView.findViewById(R.id.img_challenge);
		}

		@Override
		public void setData(final ChallengeData challengeData) {
			// setup view content
			nameTextView.setText(challengeData.getChallenge().getName());
			distanceTextView.setText(getString(R.string.distance_km, String.valueOf(challengeData.getChallenge().getDistanceInMeters() / 1000)));
			Picasso
					.with(getActivity())
					.load(getResources().getIdentifier(challengeData.getChallenge().getImageName(), "drawable", getActivity().getPackageName()))
					.into(imageView);

			// set completed info
			float completedPercentage = (challengeData.getCompletedDistanceInMeters() / challengeData.getChallenge().getDistanceInMeters()) * 100;
			completedTextView.setText(getString(R.string.percentage_completed, String.format("%.2f", completedPercentage)));

			// set forward to details click listener
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), ChallengeDetailsActivity.class);
					intent.putExtra(ChallengeDetailsActivity.EXTRA_CHALLENGE_DATA, challengeData);
					startActivityForResult(intent, REQUEST_SHOW_CHALLENGE_DETAILS);
				}
			});
		}

	}

}
