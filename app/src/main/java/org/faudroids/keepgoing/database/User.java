package org.faudroids.keepgoing.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

@Table(databaseName = KeepGoingDatabase.NAME)
public class User extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long userId;

    long getUserId() {
        return userId;
    }

    @Column
    @Unique
    String userName;

    String getUserName() {
        return this.userName;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    //How are sessions identified?
    @Column
    String currentUserSession;

    String getCurrentUserSession() {
        return currentUserSession;
    }

    void setCurrentUserSession(String currentUserSession) {
        this.currentUserSession = currentUserSession;
    }

    @Column
    @ForeignKey(
    references = {@ForeignKeyReference(columnName = "currentChallenge",
                    columnType = Long.class,
                    foreignColumnName = "challengeId")},
    saveForeignKeyModel = false)
    ForeignKeyContainer<Challenge> challengeForeignKeyContainer;

    public void setChallenge(Challenge challenge) {
        challengeForeignKeyContainer= new ForeignKeyContainer<>(Challenge.class);
        challengeForeignKeyContainer.setModel(challenge);
    }

    Challenge getChallenge() {
        return challengeForeignKeyContainer.toModel();
    }
}
