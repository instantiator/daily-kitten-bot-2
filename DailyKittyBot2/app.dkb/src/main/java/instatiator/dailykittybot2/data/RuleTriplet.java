package instatiator.dailykittybot2.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;

public class RuleTriplet {

    @Embedded
    public Rule rule;

    @Relation(parentColumn = "uuid", entityColumn = "ruleUuid")
    public List<Condition> conditions;

    @Relation(parentColumn = "uuid", entityColumn = "ruleUuid")
    public List<Outcome> outcomes;

}
