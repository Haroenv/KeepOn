package org.faudroids.keepgoing.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.auth.Account;
import org.faudroids.keepgoing.auth.AuthManager;

import javax.inject.Inject;

import it.neokree.materialnavigationdrawer.elements.MaterialAccount;


public class MainDrawerActivity extends AbstractRoboDrawerActivity {

	@Inject private AuthManager authManager;

	@Override
	public void init(Bundle savedInstanceState) {
		// setup sections
		addSection(newSection(getString(R.string.section_demo), new MainFragment()));
		addSection(newSection(getString(R.string.section_demo), new ChallengesFragment()));

		// setup account
		Account account = authManager.getAccount();
		addAccount(new MaterialAccount(
				getResources(),
				account.getName(),
				account.getEmail(),
				null,
				null));

		// setup feedback
		String address = getString(R.string.feedback_mail_address);
		String subject = getString(
				R.string.feedback_mail_subject,
				getString(R.string.app_name));
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		Intent mailer = Intent.createChooser(intent, getString(R.string.feedback_mail_chooser));
		this.addBottomSection(newSection(getString(R.string.feedback), R.drawable.ic_email, mailer));
	}

}
