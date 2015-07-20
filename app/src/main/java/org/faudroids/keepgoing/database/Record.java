package org.faudroids.keepgoing.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

@Table(databaseName = KeepGoingDatabase.NAME)
public class Record extends BaseModel{

    @Column
    @PrimaryKey(autoincrement = true)
    long recordId;

    @Column
    @ForeignKey(
    references = {@ForeignKeyReference(columnName = "fromChallenge",
                    columnType = Long.class,
                    foreignColumnName = "challengeId")},
    saveForeignKeyModel = false)
    ForeignKeyContainer<Challenge> challengeForeignKeyContainer;
}
