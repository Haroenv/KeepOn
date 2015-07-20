package org.faudroids.keepgoing.database;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;

public class TransactionListenerAdapter<T> extends com.raizlabs.android.dbflow.runtime.transaction.TransactionListenerAdapter<T> {

	private final FutureAdapter futureAdapter = new FutureAdapter();

	@Override
	public void onResultReceived(final T t) {
		futureAdapter.setValue(t);
	}


	public Observable<T> toObservable() {
		return Observable.from(futureAdapter);
	}


	public class FutureAdapter implements Future<T> {

		private boolean isDone;
		private T value;

		public void setValue(T value) {
			synchronized (this) {
				this.value = value;
				this.isDone = true;
				this.notify();
			}
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return isDone;
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			synchronized (this) {
				while (value == null) {
					this.wait();
				}
			}
			return value;
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return get();
		}
	}

}
