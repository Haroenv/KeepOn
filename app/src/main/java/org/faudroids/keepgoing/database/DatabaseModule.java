package org.faudroids.keepgoing.database;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.raizlabs.android.dbflow.runtime.TransactionManager;

public class DatabaseModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(TransactionManager.class).toInstance(TransactionManager.getInstance());
	}

}
