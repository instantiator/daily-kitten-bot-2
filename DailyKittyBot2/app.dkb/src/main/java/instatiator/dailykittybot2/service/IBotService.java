package instatiator.dailykittybot2.service;

import net.dean.jraw.oauth.AccountHelper;
import net.dean.jraw.oauth.DeferredPersistentTokenStore;

import instatiator.dailykittybot2.db.entities.Rule;

public interface IBotService {

    BotsWorkspace get_workspace();

    AccountHelper get_account_helper();
    DeferredPersistentTokenStore get_token_store();

    State get_state();
    void authenticate_as(String user);

    void update_rule(Rule rule);
    void injectTestData(String user);

    enum State {
        Initialised, Authenticating, Authenticated
    }
}
