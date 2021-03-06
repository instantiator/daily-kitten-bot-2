package instantiator.dailykittybot2.service;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;

import java.util.List;
import java.util.UUID;

import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.BotDatabase;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.db.entities.RunReport;

public class BotWorkspace {

    BotDatabase db;
    BotService service;

    public BotWorkspace(BotService service) {
        this.service = service;
        db = Room.databaseBuilder(
                service.getApplicationContext(),
                BotDatabase.class,
                "dkb-database")
                    .fallbackToDestructiveMigration() // TODO: provide migrations for live
                    .build();
    }

    public BotDatabase getDb() { return db; };

    public LiveData<List<Rule>> rules_for(String username) {
        return db.ruleDao().loadAllByUsername(username);
    }

    public LiveData<List<Recommendation>> recommendations_for(String username, boolean filter_for_unenacted) {
        if (!filter_for_unenacted) {
            return db.recommendationDao().loadAllByUsername(username);
        } else {
            return db.recommendationDao().loadUnenactedByUsername(username);
        }
    }

    public LiveData<List<Condition>> conditions_for(UUID rule) {
        return db.conditionDao().loadAllByRule(rule);
    }

    public LiveData<List<Outcome>> outcomes_for(UUID rule) {
        return db.outcomeDao().loadAllByRule(rule);
    }

    public int max_condition_ordering_for_rule(UUID rule) {
        return db.conditionDao().getMaxOrderingForRule(rule);
    }

    public RunReport get_last_report_for(String username, String subreddit, UUID ruleUuid) {
        return db.runReportDao().get_last_run_report_for(username, subreddit, ruleUuid);
    }

    public void insert_rule(Rule rule) {
        db.ruleDao().insertAll(rule);
    }

    public void insert_condition(Condition condition) {
        db.conditionDao().insertAll(condition);
    }

    public void insert_outcome(Outcome outcome) {
        db.outcomeDao().insertAll(outcome);
    }

    public void update_rule(Rule rule) {
        db.ruleDao().updateAll(rule);
    }

    public void update_condition(Condition condition) {
        db.conditionDao().updateAll(condition);
    }

    public void update_outcome(Outcome outcome) {
        db.outcomeDao().updateAll(outcome);
    }

    public void insert_recommendations(List<Recommendation> recommendations) {
        if (recommendations != null) {
            db.recommendationDao().insertAll(recommendations.toArray(new Recommendation[recommendations.size()]));
        }
    }

    public void insert_runReports(List<RunReport> reports) {
        if (reports != null) {
            db.runReportDao().insertAll(reports.toArray(new RunReport[reports.size()]));
        }
    }

    public void delete_condition(Condition condition) { db.conditionDao().delete(condition); }

    public void delete_outcome(Outcome outcome) { db.outcomeDao().delete(outcome); }

    public void delete_rule(Rule rule) { db.ruleDao().delete(rule); }

    public void delete_all_recommendations(String username) {
        db.recommendationDao().delete_all_for(username);
    }

    public void rules_forget_last_run_for(String username) {
        db.ruleDao().rulesForgetLastRun(username);
    }

    public void rule_forgets_last_run(UUID rule) {
        db.ruleDao().ruleForgetsLastRun(rule);
    }

    public void delete_run_reports_for(UUID rule) {
        db.runReportDao().delete_all_for(rule);
    }

    public void delete_run_reports_for(String username) {
        db.runReportDao().delete_all_for(username);
    }

    public void insert_enaction(Enaction enaction) {
        db.enactionDao().insertAll(enaction);
    }

    public void update_recommendation(Recommendation recommendation) {
        db.recommendationDao().updateAll(recommendation);
    }

    public LiveData<List<RuleTriplet>> rule_triplets_for(String username) {
        return db.ruleTripletDao().loadAllByUsername(username);
    }

    public LiveData<Rule> get_rule(UUID rule) {
        return db.ruleDao().get(rule);
    }

    public LiveData<Condition> get_condition(UUID condition) {
        return db.conditionDao().get(condition);
    }

    public LiveData<Outcome> get_outcome(UUID outcome) {
        return db.outcomeDao().get(outcome);
    }

    public LiveData<Recommendation> get_recommendation(UUID recommendation) {
        return db.recommendationDao().get(recommendation);
    }

}