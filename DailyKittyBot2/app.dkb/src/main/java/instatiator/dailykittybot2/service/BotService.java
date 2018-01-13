package instatiator.dailykittybot2.service;

import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;
import android.util.Log;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;

import net.dean.jraw.android.AndroidHelper;
import net.dean.jraw.android.AppInfoProvider;
import net.dean.jraw.android.ManifestAppInfoProvider;
import net.dean.jraw.android.SharedPreferencesTokenStore;
import net.dean.jraw.android.SimpleAndroidLogAdapter;
import net.dean.jraw.http.LogAdapter;
import net.dean.jraw.http.SimpleHttpLogger;
import net.dean.jraw.oauth.AccountHelper;
import net.dean.jraw.oauth.DeferredPersistentTokenStore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import instatiator.dailykittybot2.BotApp;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.events.BotServiceStateEvent;
import instatiator.dailykittybot2.service.helpers.DataFactory;
import instatiator.dailykittybot2.service.helpers.TestDataInjector;
import instatiator.dailykittybot2.ui.AccountsListActivity;

public class BotService extends AbstractBackgroundBindingService<IBotService> implements IBotService {
    private static final String TAG = BotService.class.getName();

    private State state;

    private AccountHelper accountHelper;
    private SharedPreferencesTokenStore tokenStore;

    private UUID device_uuid;
    private BotWorkspace workspace;

    private static final String KEY_Device_UUID = "device.uuid";

    @Override
    public void onCreate() {
        super.onCreate();
        init_defaults();
        init_reddit();
        init_workspace();
        switch_state(state.Initialised);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void init_defaults() {
        if (device_uuid == null) {
            device_uuid = UUID.randomUUID();
        }
    }

    private void init_workspace() {
        workspace = new BotWorkspace(this);
    }

    private void init_reddit() {
        AppInfoProvider provider = new ManifestAppInfoProvider(getApplicationContext());

        tokenStore = new SharedPreferencesTokenStore(getApplicationContext());
        tokenStore.load();
        tokenStore.setAutoPersist(true);

        accountHelper = AndroidHelper.accountHelper(provider, device_uuid, tokenStore);

        accountHelper.onSwitch(redditClient -> {
            LogAdapter logAdapter = new SimpleAndroidLogAdapter(Log.VERBOSE);
            redditClient.setLogger(new SimpleHttpLogger(SimpleHttpLogger.DEFAULT_LINE_LENGTH, logAdapter));

            if (accountHelper.isAuthenticated()) {
                switch_state(State.Authenticated, redditClient.getAuthManager().currentUsername());
            } else {
                switch_state(State.Initialised);
            }

            return null;
        });
    }

    private void switch_state(State next) {
        switch_state(next, null);
    }

    private void switch_state(State next, String account) {
        if (state != next) {
            state = next;
            EventBus.getDefault().postSticky(new BotServiceStateEvent(this, state, account));
        }
    }

    @Override
    public State get_state() { return state; }

    @Override
    public AccountHelper get_account_helper() {
        return accountHelper;
    }

    @Override
    public DeferredPersistentTokenStore get_token_store() { return tokenStore; }

    @Override
    public void authenticate_as(String user) {
        try {
            switch_state(State.Authenticating);
            accountHelper.switchToUser(user);

            if (accountHelper.isAuthenticated()) {
                switch_state(State.Authenticated);
            } else {
                switch_state(State.Initialised);
            }

        } catch (Exception e) {
            Log.e(TAG, "Unable to switch to user " + user);
            switch_state(State.Initialised);
        }
    }

    @Override
    public LiveData<List<RuleTriplet>> get_rule_triplets_for(String username) {
        return workspace.rule_triplets_for(username);
    }

    @Override
    public Rule create_rule(String username, String rule_name) {
        final Rule rule = DataFactory.create_rule(username, rule_name);
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.insert_rule(rule);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered updating rule", e);
            }
        });
        return rule;
    }

    @Override
    public Condition create_condition(UUID rule_id) {
        final Condition condition = DataFactory.create_condition(rule_id);
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.insert_condition(condition);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered updating rule", e);
            }
        });
        return condition;
    }

    @Override
    public Outcome create_outcome(UUID rule_id) {
        final Outcome outcome = DataFactory.create_outcome(rule_id);
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.insert_outcome(outcome);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered updating rule", e);
            }
        });
        return outcome;
    }

    @Override
    public void update_rule(final Rule rule) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.update_rule(rule);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered updating rule", e);
            }
        });
    }

    @Override
    public void update_condition(final Condition condition) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.update_condition(condition);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered updating condition", e);
            }
        });
    }

    @Override
    public void update_outcome(Outcome outcome) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.update_outcome(outcome);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered updating outcome", e);
            }
        });
    }

    @Override
    public void injectTestData(final String user) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                TestDataInjector injector = new TestDataInjector(workspace.db, user);
                injector.inject_full_test_rule(3, 3);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered injecting test data", e);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void became_authenticated(BotServiceStateEvent event) {
        if (event.state == State.Authenticated) {
            // TODO: confirm mode, do stuff - eg. task sets
        }
    }

    @Override
    public BotWorkspace get_workspace() {
        return workspace;
    }

    @Override
    protected void restoreFrom(SharedPreferences prefs) {
        String uuid_str = prefs.getString(KEY_Device_UUID, null);
        if (uuid_str != null) { device_uuid = UUID.fromString(uuid_str); }
    }

    @Override
    protected void storeTo(SharedPreferences.Editor editor) {
        if (device_uuid != null) { editor.putString(KEY_Device_UUID, device_uuid.toString()); }
    }

    @Override
    protected BackgroundServiceConfig configure(BackgroundServiceConfig defaults) {
        String title = getString(R.string.notification_running_title);
        String content = getString(R.string.notification_running_content);
        String ticker = getString(R.string.notification_running_ticker);
        int icon = R.drawable.ic_adb_black_24dp;
        Class activity = AccountsListActivity.class;
        defaults.setNotification(title, content, ticker, icon, activity);
        return defaults;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return BotApp.required_permissions;
    }

}
