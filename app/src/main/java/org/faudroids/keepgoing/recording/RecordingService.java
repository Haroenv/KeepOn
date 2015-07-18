package org.faudroids.keepgoing.recording;


import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;

import javax.inject.Inject;

import roboguice.service.RoboService;
import timber.log.Timber;

/**
 * Foreground service for keeping the {@link RecordingManager} in memory
 * while the recording is running.
 */
public class RecordingService extends RoboService {

	private static final int NOTIFICATION_ID = 42;

	@Inject private RecordingManager recordingManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Timber.d("starting recording service");

		// show notification while recording
		startForeground(NOTIFICATION_ID, new Notification.Builder(this)
				.setContentTitle("Recording")
				.setContentText("KeepGoing is currently recording your location")
				.build());

		return START_STICKY;
	}

}
