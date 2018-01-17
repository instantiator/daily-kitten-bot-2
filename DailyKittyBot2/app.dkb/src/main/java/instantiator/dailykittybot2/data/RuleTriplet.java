package instantiator.dailykittybot2.data;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Rule;

public class RuleTriplet {

    @Embedded
    public Rule rule;

    @Relation(parentColumn = "uuid", entityColumn = "ruleUuid")
    public List<Condition> conditions;

    @Relation(parentColumn = "uuid", entityColumn = "ruleUuid")
    public List<Outcome> outcomes;

}
