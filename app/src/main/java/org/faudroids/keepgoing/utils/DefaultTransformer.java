package org.faudroids.keepgoing.utils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DefaultTransformer<T> implements Observable.Transformer<T, T> {

	@Override
	public Observable<T> call(Observable<T> observable) {
		return observable
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());
	}

}