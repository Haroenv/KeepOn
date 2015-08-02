package org.faudroids.keepon.ui;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import org.faudroids.keepon.R;
import org.faudroids.keepon.auth.AuthManager;

import javax.inject.Inject;

import roboguice.inject.InjectView;
import timber.log.Timber;

public class SettingsFragment extends AbstractFragment {

	@InjectView(R.id.versionTextView) TextView version;
	@InjectView(R.id.authorTextView) TextView authors;
	@InjectView(R.id.creditsTextView) TextView credits;
	@InjectView(R.id.logoutTextView) TextView logout;

	@Inject AuthManager authManager;

	public SettingsFragment() {
		super(R.layout.fragment_settings);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		version.setText(getVersion());
		setOnClickDialogForTextView(authors, R.string.about, R.string.about_msg);
		setOnClickDialogForTextView(credits, R.string.credits, R.string.credits_msg);
		logout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!googleApiClientObserver.assertIsGoogleApiClientConnected()) return;
				authManager.signOut(googleApiClientObserver.getGoogleApiClient());
				getActivity().finish();
				startActivity(new Intent(getActivity(), LoginActivity.class));
			}
		});
	}

	private String getVersion() {
		try {
			return getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException nnfe) {
			Timber.e(nnfe, "failed to get version");
			return null;
		}
	}

	private AlertDialog.Builder setOnClickDialogForTextView(TextView textView, final int titleResourceId, final int msgResourceId) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
				.setTitle(titleResourceId)
				.setMessage(Html.fromHtml(getString(msgResourceId)))
				.setPositiveButton(android.R.string.ok, null);

		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = dialogBuilder.show();
				((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			}
		});

		return dialogBuilder;
	}

}