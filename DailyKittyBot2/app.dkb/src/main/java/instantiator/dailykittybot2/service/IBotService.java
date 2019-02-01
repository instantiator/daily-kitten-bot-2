package instantiator.dailykittybot2.service;

import android.arch.lifecycle.LiveData;

import net.dean.jraw.RedditClient;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.db.entities.RunReport;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.service.execution.SubredditExecutionResult;
import instantiator.dailykittybot2.ui.AbstractBotActivity;

public interface IBotService {

    BotWorkspace get_workspace();

    //AccountHelper get_account_helper();
    //DeferredPersistentTokenStore get_token_store();

    //State get_state();
    //void authenticate_as(String user);

    UUID get_device_uuid();

    LiveData<List<RuleTriplet>> get_rule_triplets_for(String username);

    Rule create_rule(String username, String rule_name);
    Condition create_condition(UUID rule_id);
    Outcome create_outcome(UUID rule_id);

    void update_rule(Rule rule);
    void update_condition(Condition condition);
    void update_outcome(Outcome outcome);
    void update_recommendation(Recommendation recommendation);

    void insert_recommendations(List<Recommendation> recommendations);
    void insert_runReports(List<RunReport> reports);
    void insert_enaction(Enaction enaction);

    void delete_condition(Condition condition);
    void delete_outcome(Outcome outcome);
    void delete_rule(Rule rule);

    void delete_run_reports_for(UUID rule);
    void delete_run_reports_for(String username);
    void delete_all_recommendations(String username);

    void injectSampleData(String user, String name);

    void run(String username, RuleTriplet rules);

    enum State {
        Initialised, Authenticating, Authenticated
    }
}
