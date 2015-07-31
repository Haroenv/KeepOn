package org.faudroids.keepgoing.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.auth.Account;
import org.faudroids.keepgoing.auth.AuthManager;

import javax.inject.Inject;

import roboguice.inject.ContentView;


@ContentView(R.layout.activity_main)
public class MainDrawerActivity extends AbstractActivity implements Drawer.OnDrawerItemClickListener {

	private static final int
			ID_OPEN_CHALLENGES = 0,
			ID_FINISHED_CHALLENGES = 1,
			ID_SETTINGS = 2,
			ID_FEEDBACK = 3;

	@Inject private AuthManager authManager;
	private Drawer drawer;
	private int visibleFragmentId;


	public MainDrawerActivity() {
		super(false);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// show first fragment
		showFragment(new OpenChallengesFragment(), false);
		visibleFragmentId = ID_OPEN_CHALLENGES;

		// setup image loading for nav drawer
		DrawerImageLoader.init(new DrawerImageLoader.IDrawerImageLoader() {
			@Override
			public void set(ImageView imageView, Uri uri, Drawable drawable) {
				Picasso.with(imageView.getContext()).load(uri).into(imageView);
			}

			@Override
			public void cancel(ImageView imageView) {
				Picasso.with(imageView.getContext()).cancelRequest(imageView);
			}

			@Override
			public Drawable placeholder(Context context) {
				return null;
			}
		});

		// setup account in nav drawer
		Account account = authManager.getAccount();
		AccountHeader accountHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.drawable.background)
				.addProfiles(
						new ProfileDrawerItem().withName(account.getName()).withEmail(account.getEmail()).withIcon(account.getImageUrl())
				)
				.withProfileImagesClickable(false)
				.withSelectionListEnabledForSingleProfile(false)
				.build();

		// setup actual nav drawer
		drawer = new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.withAccountHeader(accountHeader)
				.addDrawerItems(
						new PrimaryDrawerItem().withName(R.string.open_challenges).withIdentifier(ID_OPEN_CHALLENGES),
						new PrimaryDrawerItem().withName(R.string.finished_challenges).withIdentifier(ID_FINISHED_CHALLENGES)
				)
				.addStickyDrawerItems(
						new PrimaryDrawerItem().withName(R.string.settings).withIconTintingEnabled(true).withIcon(R.drawable.ic_settings).withIdentifier(ID_SETTINGS),
						new PrimaryDrawerItem().withName(R.string.feedback).withIconTintingEnabled(true).withIcon(R.drawable.ic_email).withIdentifier(ID_FEEDBACK)
				)
				.withOnDrawerItemClickListener(this)
				.build();
	}


	private void showFragment(Fragment fragment, boolean replace) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		if (!replace) transaction.add(R.id.container, fragment);
		else transaction.replace(R.id.container, fragment);
		transaction.addToBackStack("").commit();
	}


	@Override
	public boolean onItemClick(AdapterView<?> adapterView, View view, int position, long id, IDrawerItem item) {
		drawer.closeDrawer();
		if (item.getIdentifier() == visibleFragmentId) return false;

		switch (item.getIdentifier()) {
			case ID_FEEDBACK:
				String address = getString(R.string.feedback_mail_address);
				String subject = getString(
						R.string.feedback_mail_subject,
						getString(R.string.app_name));
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
				intent.putExtra(Intent.EXTRA_SUBJECT, subject);
				Intent mailer = Intent.createChooser(intent, getString(R.string.feedback_mail_chooser));
				startActivity(mailer);
				return false;

			case ID_OPEN_CHALLENGES:
				showFragment(new OpenChallengesFragment(), true);
				break;

			case ID_FINISHED_CHALLENGES:
				showFragment(new FinishedChallengesFragment(), true);
				break;

			case ID_SETTINGS:
				showFragment(new SettingsFragment(), true);
				break;

			default:
				return false;
		}

		visibleFragmentId = item.getIdentifier();
		return true;
	}

}
