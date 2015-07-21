package org.faudroids.keepgoing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.auth.AuthManager;

import javax.inject.Inject;

import roboguice.inject.InjectView;


public class MainFragment extends AbstractFragment {

	@InjectView(R.id.btn_fit_demo) private Button fitDemoButton;
	@InjectView(R.id.btn_map_demo) private Button mapDemoButton;
	@InjectView(R.id.btn_recording_demo) private Button recordingDemoButton;
	@InjectView(R.id.btn_show_sessions) private Button showSessionsButton;
	@InjectView(R.id.btn_logout) private Button logoutButton;

	@Inject private AuthManager authManager;


	public MainFragment() {
		super(R.layout.fragment_main);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		fitDemoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), GoogleFitDemoActivity.class));
			}
		});

		mapDemoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), MapsActivity.class));
			}
		});

		recordingDemoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), RecordingActivity.class));
			}
		});

		showSessionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), SessionsOverviewActivity.class));
			}
		});

		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!googleApiClientObserver.assertIsGoogleApiClientConnected()) return;

				// logout
				authManager.signOut(googleApiClientObserver.getGoogleApiClient());
				getActivity().finish();
				startActivity(new Intent(getActivity(), LoginActivity.class));
			}
		});

	}

}
