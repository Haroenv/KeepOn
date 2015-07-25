package org.faudroids.keepgoing.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.auth.Account;
import org.faudroids.keepgoing.auth.AuthManager;

import javax.inject.Inject;

import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import timber.log.Timber;


public class MainDrawerActivity extends AbstractRoboDrawerActivity implements Target {

	@Inject private AuthManager authManager;

	@Override
	public void init(Bundle savedInstanceState) {
		// setup sections
		addSection(newSection(getString(R.string.section_demo), new MainFragment()));
		addSection(newSection(getString(R.string.open_challenges), new OpenChallengesFragment()));
		addSection(newSection(getString(R.string.finished_challenges), new FinishedChallengesFragment()));

		// setup account
		Account account = authManager.getAccount();
		addAccount(new MaterialAccount(
				getResources(),
				account.getName(),
				account.getEmail(),
				null,
				null));
		Picasso.with(this)
				.load(account.getImageUrl())
				.resize((int) getResources().getDimension(R.dimen.user_photo_drawer_size), (int) getResources().getDimension(R.dimen.user_photo_drawer_size))
				.into(this);

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


	@Override
	public void onDestroy() {
		Picasso.with(this).cancelRequest(this);
		super.onDestroy();
	}


	@Override
	public void onBitmapLoaded(Bitmap profileBitmap, Picasso.LoadedFrom from) {
		MaterialAccount account = getCurrentAccount();
		account.setPhoto(profileBitmap);
		notifyAccountDataChanged();
	}


	@Override
	public void onBitmapFailed(Drawable errorDrawable) {
		Timber.e("failed to load profile img");
	}


	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {
		// nothing to do
	}

}
