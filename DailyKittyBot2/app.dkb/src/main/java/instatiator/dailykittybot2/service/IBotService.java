package instatiator.dailykittybot2.service;

import android.arch.lifecycle.LiveData;

import net.dean.jraw.oauth.AccountHelper;
import net.dean.jraw.oauth.DeferredPersistentTokenStore;

import java.util.List;
import java.util.UUID;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;

public interface IBotService {

    BotWorkspace get_workspace();

    AccountHelper get_account_helper();
    DeferredPersistentTokenStore get_token_store();

    State get_state();
    void authenticate_as(String user);

    LiveData<List<RuleTriplet>> get_rule_triplets_for(String username);

    Rule create_rule(String username, String rule_name);
    Condition create_condition(UUID rule_id);
    Outcome create_outcome(UUID rule_id);

    void update_rule(Rule rule);
    void update_condition(Condition condition);
    void update_outcome(Outcome outcome);

    void delete_condition(Condition condition);

    void injectTestData(String user);

    enum State {
        Initialised, Authenticating, Authenticated
    }
}
