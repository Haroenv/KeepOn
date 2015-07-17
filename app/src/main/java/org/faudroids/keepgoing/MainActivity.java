package org.faudroids.keepgoing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.faudroids.keepgoing.ui.GoogleFitDemoActivity;
import org.faudroids.keepgoing.ui.MapsActivity;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {

	@InjectView(R.id.btn_fit_demo) private Button fitDemoButton;
	@InjectView(R.id.btn_map_demo) private Button mapDemoButton;

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
	}

}
