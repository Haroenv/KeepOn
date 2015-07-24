package org.faudroids.keepgoing.sessions;


import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func0;
import timber.log.Timber;

public class SessionManager {

	@Inject
	SessionManager() { }


	public Observable<Status> createSession(final GoogleApiClient googleApiClient, final Session session, final DataSet ... dataSets) {
		return Observable.defer(new Func0<Observable<Status>>() {
			@Override
			public Observable<Status> call() {
				SessionInsertRequest.Builder requestBuilder = new SessionInsertRequest.Builder().setSession(session);
				for (DataSet dataSet : dataSets) requestBuilder.addDataSet(dataSet);
				Status status = Fitness.SessionsApi.insertSession(googleApiClient, requestBuilder.build()).await();
				return Observable.just(status);
			}
		});
	}


	public Observable<SessionData> loadSessionData(final GoogleApiClient googleApiClient, final String sessionId) {
		// create read request (only read distance, not location for overview)
		final SessionReadRequest readRequest = createReadRequest()
				.setSessionId(sessionId)
				.build();
		return Observable.defer(new Func0<Observable<SessionData>>() {
			@Override
			public Observable<SessionData> call() {
				SessionReadResult readResult = Fitness.SessionsApi.readSession(googleApiClient, readRequest).await();

				if (readResult.getSessions().isEmpty()) throw new IllegalStateException("error finding session " + sessionId);

				// parse result
				Session session = readResult.getSessions().get(0);
				List<Location> locations = locationsFromDataSet(readResult.getDataSet(session, DataType.TYPE_LOCATION_SAMPLE).get(0));
				Timber.d("found " + locations.size() + " locations");
				return Observable.just(new SessionData(session, locations));
			}
		});
	}

	public Observable<List<SessionData>> loadSessionData(final GoogleApiClient googleApiClient) {
		// create read request (only read distance, not location for overview)
		final SessionReadRequest readRequest = createReadRequest().build();
		return Observable
				.defer(new Func0<Observable<List<SessionData>>>() {
					@Override
					public Observable<List<SessionData>> call() {
						SessionReadResult readResult = Fitness.SessionsApi.readSession(googleApiClient, readRequest).await();

						// parse result
						List<SessionData> sessionsList = new ArrayList<>();
						for (Session session : readResult.getSessions()) {
							List<Location> locations = locationsFromDataSet(readResult.getDataSet(session, DataType.TYPE_LOCATION_SAMPLE).get(0));
							Timber.d("found " +locations.size() + " locations");
							sessionsList.add(new SessionData(session, locations));
						}

						Collections.sort(sessionsList);
						return Observable.just(sessionsList);
					}
				});
	}


	private SessionReadRequest.Builder createReadRequest() {
		// load entries from the last 5 years
		// TODO dynamically load more?
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		long endTime = cal.getTimeInMillis();
		cal.add(Calendar.YEAR, -5);
		long startTime = cal.getTimeInMillis();

		// create read request (only read distance, not location for overview)
		return new SessionReadRequest
				.Builder()
				.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
				.read(DataType.TYPE_LOCATION_SAMPLE);
	}


	private List<Location> locationsFromDataSet(DataSet distanceDataSet) {
		List<Location> locations = new ArrayList<>();
		for (DataPoint dataPoint : distanceDataSet.getDataPoints()) {
			float lat = dataPoint.getValue(Field.FIELD_LATITUDE).asFloat();
			float lng = dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat();
			long timestamp = dataPoint.getTimestamp(TimeUnit.MILLISECONDS);
			float accuracy = dataPoint.getValue(Field.FIELD_ACCURACY).asFloat();
			float altitude = dataPoint.getValue(Field.FIELD_ALTITUDE).asFloat();

			Location location = new Location("");
			location.setLatitude(lat);
			location.setLongitude(lng);
			location.setTime(timestamp);
			location.setAccuracy(accuracy);
			location.setAltitude(altitude);
			locations.add(location);
		}
		return locations;
	}

}
