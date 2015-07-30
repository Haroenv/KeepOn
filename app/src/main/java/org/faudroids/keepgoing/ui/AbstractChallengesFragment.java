package org.faudroids.keepgoing.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.challenge.ChallengeData;
import org.faudroids.keepgoing.challenge.ChallengeManager;
import org.faudroids.keepgoing.utils.DefaultTransformer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;


abstract class AbstractChallengesFragment extends AbstractFragment {

	private final int titleResource;
	private final int emptyResource;

	@InjectView(R.id.list) private RecyclerView recyclerView;
	private ChallengeAdapter challengeAdapter;
	@Inject private ChallengeManager challengeManager;
	@InjectView(R.id.txt_empty) private TextView emptyTextView;


	public AbstractChallengesFragment(int layoutResource, int titleResource, int emptyResource) {
		super(layoutResource);
		this.titleResource = titleResource;
		this.emptyResource = emptyResource;
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
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// set title
		activity.setTitle(titleResource);
	}


	@Override
	public void onGoogleApiClientConnected(final GoogleApiClient googleApiClient) {
		super.onGoogleApiClientConnected(googleApiClient);
		challengeManager.getAllChallenges(googleApiClient)
				.flatMap(filterChallenges())
				.compose(new DefaultTransformer<List<ChallengeData>>())
				.subscribe(new Action1<List<ChallengeData>>() {
					@Override
					public void call(List<ChallengeData> challenges) {
						challengeAdapter.setData(challenges);

						// toggle empty view
						if (challenges.isEmpty()) {
							emptyTextView.setVisibility(View.VISIBLE);
							emptyTextView.setText(emptyResource);
						} else {
							emptyTextView.setVisibility(View.GONE);
						}
					}
				});
	}


	protected abstract Func1<List<ChallengeData>, Observable<List<ChallengeData>>> filterChallenges();
	protected abstract AbstractChallengeViewHolder onCreateViewHolder(ViewGroup viewGroup);


	private class ChallengeAdapter extends RecyclerView.Adapter {

		private final List<ChallengeData> challengeDataList = new ArrayList<>();

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
			return AbstractChallengesFragment.this.onCreateViewHolder(viewGroup);
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
			((AbstractChallengeViewHolder) viewHolder).setData(challengeDataList.get(position));
		}

		@Override
		public int getItemCount() {
			return challengeDataList.size();
		}

		public void setData(List<ChallengeData> data) {
			this.challengeDataList.clear();
			this.challengeDataList.addAll(data);
			notifyDataSetChanged();
		}
	}


	protected abstract class AbstractChallengeViewHolder extends RecyclerView.ViewHolder {

		public AbstractChallengeViewHolder(View itemView) {
			super(itemView);
		}

		public abstract void setData(ChallengeData challengeData);

	}

}
