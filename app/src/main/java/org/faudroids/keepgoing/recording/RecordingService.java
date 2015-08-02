package org.faudroids.keepgoing.recording;


import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import org.faudroids.keepgoing.R;
import org.faudroids.keepgoing.utils.AbstractGoogleClientApiService;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Foreground service for keeping the {@link RecordingManager} in memory and
 * ensuring that the google API client is not disconnected.
 */
public class RecordingService extends AbstractGoogleClientApiService {

	private static final int NOTIFICATION_ID = 42;

	@Inject private RecordingManager recordingManager;
	@Inject private PowerManager powerManager;
	private PowerManager.WakeLock wakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	protected int doOnStartCommand(Intent intent, int flags, int startId) {
		Timber.d("starting recording service");

		// show notification while recording
		Notification.Builder notificationBuilder = new Notification.Builder(this)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.recording_new_activity))
				.setSmallIcon(R.drawable.ic_notification);
		if (Build.VERSION.SDK_INT >= 21) {
			notificationBuilder.setColor(getResources().getColor(R.color.colorPrimary));
		}
		startForeground(NOTIFICATION_ID, notificationBuilder.build());

		// keep CPU running during recording
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RecordingWakeLock");
		wakeLock.acquire();

		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		wakeLock.release();
		super.onDestroy();
	}

}
