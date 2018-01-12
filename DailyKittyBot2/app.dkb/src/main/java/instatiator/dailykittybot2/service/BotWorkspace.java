package instatiator.dailykittybot2.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;

import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.db.BotDatabase;
import instatiator.dailykittybot2.db.data.ConditionType;
import instatiator.dailykittybot2.db.data.OutcomeType;
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