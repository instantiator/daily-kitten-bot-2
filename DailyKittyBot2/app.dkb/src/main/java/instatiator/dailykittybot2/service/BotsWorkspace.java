package instatiator.dailykittybot2.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;

import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.db.BotDatabase;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Recommendation;
import instatiator.dailykittybot2.db.entities.Rule;

public class BotsWorkspace {

    BotDatabase db;
    BotService service;

    public BotsWorkspace(BotService service) {
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

    public Rule create_rule(String username, String rulename) {
        Rule rule = new Rule();
        rule.uuid = UUID.randomUUID();
        rule.username = username;
        rule.rulename = rulename;
        db.ruleDao().insertAll(rule);
        return rule;
    }

    public void update_rule(Rule rule) {
        db.ruleDao().updateAll(rule);
    }

    public LiveData<Rule> get_rule(UUID rule) {
        return db.ruleDao().get(rule);
    }

}