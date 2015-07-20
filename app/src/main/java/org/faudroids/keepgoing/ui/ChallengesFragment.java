package org.faudroids.keepgoing.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.Challenge;
import org.faudroids.keepgoing.challenge.ChallengeManager;
import org.faudroids.keepgoing.utils.DefaultTransformer;

import java.util.List;

import javax.inject.Inject;

import rx.functions.Action1;
import timber.log.Timber;


public class ChallengesFragment extends AbstractFragment {

	@Inject private ChallengeManager challengeManager;

	public ChallengesFragment() {
		super(R.layout.fragment_main);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		challengeManager
				.getAllChallenges()
				.compose(new DefaultTransformer<List<Challenge>>())
				.subscribe(new Action1<List<Challenge>>() {
					@Override
					public void call(List<Challenge> challenges) {
						Toast.makeText(getActivity(), "Found " + challenges.size() + " challenges", Toast.LENGTH_SHORT).show();
						for (Challenge challenge : challenges) {
							Timber.d(challenge.getName());
						}
					}
				});
	}

}
