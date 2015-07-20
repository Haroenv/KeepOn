package org.faudroids.keepgoing.database;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = KeepGoingDatabase.NAME, version = KeepGoingDatabase.VERSION,
        foreignKeysSupported = true)
public class KeepGoingDatabase {

    public static final String NAME = "KeepGoingDatabase";
    public static final int VERSION = 1;
}
