package instatiator.dailykittybot2.service;

import android.content.Context;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.android.AndroidHelper;
import net.dean.jraw.android.AppInfoProvider;
import net.dean.jraw.android.ManifestAppInfoProvider;
import net.dean.jraw.android.SharedPreferencesTokenStore;
import net.dean.jraw.android.SimpleAndroidLogAdapter;
import net.dean.jraw.http.LogAdapter;
import net.dean.jraw.http.SimpleHttpLogger;
import net.dean.jraw.oauth.AccountHelper;
import net.dean.jraw.oauth.DeferredPersistentTokenStore;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class RedditSession {
    private static final String TAG = RedditSession.class.getName();

    private Context context;
    private Context appContext;
    private UUID device_uuid;
    private AppInfoProvider appInfo;
    private SharedPreferencesTokenStore tokenStore;
    private AccountHelper accountHelper;
    private LogAdapter logAdapter;

    private String current_username;
    private State current_state;

    private Listener listener;

    public enum State {
        Initialised, Authenticating, Authenticated
    }

    public RedditSession(Context context, UUID device_uuid, Listener listener) {
        this.context = context;
        this.device_uuid = device_uuid;
        this.listener = listener;

        this.appContext = context.getApplicationContext();
        appInfo = new ManifestAppInfoProvider(appContext);
        tokenStore = new SharedPreferencesTokenStore(appContext);
        tokenStore.load();
        tokenStore.setAutoPersist(true);
        accountHelper = AndroidHelper.accountHelper(appInfo, device_uuid, tokenStore);
        accountHelper.onSwitch(redditClient -> { switched(redditClient); return null; });
        logAdapter = new SimpleAndroidLogAdapter(Log.VERBOSE);
    }

    public DeferredPersistentTokenStore get_tokenStore() { return tokenStore; }

    public AccountHelper get_accountHelper() { return accountHelper; }

    public RedditClient getClient() { return accountHelper.getReddit(); }

    private void switched(RedditClient client) {
        client.setLogger(new SimpleHttpLogger(SimpleHttpLogger.DEFAULT_LINE_LENGTH, logAdapter));
        if (accountHelper.isAuthenticated()) {
            switch_state(State.Authenticated, client.getAuthManager().currentUsername());
        } else {
            switch_state(State.Initialised, null);
        }
    }

    public State get_state() {
        return current_state;
    }

    public String get_username() {
        return current_username;
    }

    private void switch_state(State state, String username) {
        if (state != current_state || !StringUtils.equalsIgnoreCase(username, current_username)) {
            this.current_username = username;
            this.current_state = state;
            if (listener != null) {
                listener.state_switched(this, current_state, current_username);
            }
        }
    }

    public void authenticate_as(String username) {
        try {
            switch_state(State.Authenticating, null);
            accountHelper.switchToUser(username);

            if (accountHelper.isAuthenticated()) {
                switch_state(State.Authenticated, username);
            } else {
                switch_state(State.Initialised, null);
            }

        } catch (Exception e) {
            Log.e(TAG, "Unable to switch to user " + username);
            switch_state(State.Initialised, null);
        }

    }

    public interface Listener {
        void state_switched(RedditSession session, State state, String username);
    }
}
