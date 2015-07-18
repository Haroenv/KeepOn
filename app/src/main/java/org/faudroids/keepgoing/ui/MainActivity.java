package org.faudroids.keepgoing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import org.faudroids.keepgoing.R;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_main)
public class MainActivity extends AbstractActivity {

	@InjectView(R.id.btn_fit_demo) private Button fitDemoButton;
	@InjectView(R.id.btn_map_demo) private Button mapDemoButton;
	@InjectView(R.id.btn_recording_demo) private Button recordingDemoButton;
	@InjectView(R.id.btn_show_sessions) private Button showSessionsButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fitDemoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, GoogleFitDemoActivity.class));
			}
		});

		mapDemoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, MapsActivity.class));
			}
		});

		recordingDemoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, RecordingDemoActivity.class));
			}
		});

		showSessionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SessionsOverviewActivity.class));
			}
		});
	}

}
