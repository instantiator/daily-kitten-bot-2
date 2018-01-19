package instantiator.dailykittybot2.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import instantiator.dailykittybot2.db.dao.ConditionDao;
import instantiator.dailykittybot2.db.dao.EnactionDao;
import instantiator.dailykittybot2.db.dao.OutcomeDao;
import instantiator.dailykittybot2.db.dao.RecommendationDao;
import instantiator.dailykittybot2.db.dao.ResultDao;
import instantiator.dailykittybot2.db.dao.RuleDao;
import instantiator.dailykittybot2.db.dao.RuleTripletDao;
import instantiator.dailykittybot2.db.dao.RunReportCollationDao;
import instantiator.dailykittybot2.db.dao.RunReportDao;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.Result;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.db.entities.RunReport;
import instantiator.dailykittybot2.db.util.Converters;

@Database(entities = {
        Rule.class,
        Condition.class,
        Outcome.class,
        Recommendation.class,
        Result.class,
        RunReport.class,
        Enaction.class}, version = 18)
@TypeConverters({Converters.class})
public abstract class BotDatabase extends RoomDatabase {

    public abstract RuleDao ruleDao();
    public abstract ConditionDao conditionDao();
    public abstract OutcomeDao outcomeDao();
    public abstract RecommendationDao recommendationDao();
    public abstract ResultDao resultDao();
    public abstract RuleTripletDao ruleTripletDao();
    public abstract RunReportDao runReportDao();
    public abstract RunReportCollationDao runReportCollationDao();
    public abstract EnactionDao enactionDao();

}