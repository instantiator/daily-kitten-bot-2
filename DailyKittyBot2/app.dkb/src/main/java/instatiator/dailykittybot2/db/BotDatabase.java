package instatiator.dailykittybot2.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import instatiator.dailykittybot2.db.dao.ConditionDao;
import instatiator.dailykittybot2.db.dao.OutcomeDao;
import instatiator.dailykittybot2.db.dao.RecommendationDao;
import instatiator.dailykittybot2.db.dao.ResultDao;
import instatiator.dailykittybot2.db.dao.RuleDao;
import instatiator.dailykittybot2.db.dao.RuleTripletDao;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Recommendation;
import instatiator.dailykittybot2.db.entities.Result;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.db.util.Converters;

@Database(entities = {Rule.class, Condition.class, Outcome.class, Recommendation.class, Result.class}, version = 7)
@TypeConverters({Converters.class})
public abstract class BotDatabase extends RoomDatabase {

    public abstract RuleDao ruleDao();
    public abstract ConditionDao conditionDao();
    public abstract OutcomeDao outcomeDao();
    public abstract RecommendationDao recommendationDao();
    public abstract ResultDao resultDao();
    public abstract RuleTripletDao ruleTripletDao();

}