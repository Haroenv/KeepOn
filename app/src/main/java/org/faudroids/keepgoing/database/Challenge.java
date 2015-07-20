package org.faudroids.keepgoing.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

@ModelContainer
@Table(databaseName = KeepGoingDatabase.NAME)
public class Challenge extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long challengeId;

    long getChallengeId() {
        return this.challengeId;
    }

    @Column
    @Unique(unique = true)
    String challengeName;

    String getChallengeName() {
        return challengeName;
    }

    void setChallengeName(String challengeName) {
        this.challengeName = challengeName;
    }

    @Column
    double challengeDistance;

    double getChallengeDistance() {
        return this.challengeDistance;
    }

    void setChallengeDistance(double distance) {
        this.challengeDistance = distance;
    }

    @Column
    String challengeDescription;

    String getChallengeDescription() {
        return this.challengeDescription;
    }

    void setChallengeDescription(String description) {
        this.challengeDescription = description;
    }

    private List<User> currentChallengers;
    private List<Record> challengeRecords;

    //Returns a list of all users currently working on this challenge
    @OneToMany(methods = OneToMany.Method.ALL, variableName = "currentChallengers")
    public List<User> getCurrentChallengers() {
        if(currentChallengers == null) {
            currentChallengers = new Select().from(User.class).where(Condition.column(User$Table
                    .CHALLENGEFOREIGNKEYCONTAINER_CURRENTCHALLENGE).is(challengeId)).queryList();
        }
        return currentChallengers;
    }

    //Returns a list of all records for this challenge
    @OneToMany(methods = OneToMany.Method.ALL, variableName = "challengeRecords")
    public List<Record> getChallengeRecords() {
        if(challengeRecords == null) {
            challengeRecords = new Select().from(Record.class).where(Condition.column(Record$Table
                    .CHALLENGEFOREIGNKEYCONTAINER_FROMCHALLENGE).is(challengeId)).queryList();
        }
        return challengeRecords;
    }
}
