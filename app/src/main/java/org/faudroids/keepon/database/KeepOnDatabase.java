package org.faudroids.keepon.database;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(
		name = KeepOnDatabase.NAME,
		version = KeepOnDatabase.VERSION,
        foreignKeysSupported = true
)
public class KeepOnDatabase {

    public static final String NAME = "KeepOnDatabase";
    public static final int VERSION = 1;
}
