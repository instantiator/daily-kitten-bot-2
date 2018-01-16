package instatiator.dailykittybot2.service;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;

import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.BotDatabase;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Recommendation;
import instatiator.dailykittybot2.db.entities.Rule;

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

    public LiveData<List<Recommendation>> recommendations_for(String username) {
        return db.recommendationDao().loadAllByUsername(username);
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
            db.recommendationDao()
                    .insertAll(
                            recommendations.toArray(new Recommendation[recommendations.size()]));
        }
    }

    public void delete_condition(Condition condition) { db.conditionDao().delete(condition); }

    public void delete_outcome(Outcome outcome) { db.outcomeDao().delete(outcome); }

    public void delete_rule(Rule rule) { db.ruleDao().delete(rule); }

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