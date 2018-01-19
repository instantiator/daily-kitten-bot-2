package instantiator.dailykittybot2.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.flt.servicelib.AbstractBackgroundBindingService;
import com.flt.servicelib.BackgroundServiceConfig;

import net.dean.jraw.RedditClient;

import org.apache.commons.lang3.NotImplementedException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import instantiator.dailykittybot2.BotApp;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.db.entities.RunReport;
import instantiator.dailykittybot2.events.BotServiceStateEvent;
import instantiator.dailykittybot2.service.enaction.Enactor;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.service.execution.SubredditExecutionResult;
import instantiator.dailykittybot2.service.helpers.DataFactory;
import instantiator.dailykittybot2.service.helpers.SampleDataInjector;
import instantiator.dailykittybot2.service.helpers.TestDataInjector;
import instantiator.dailykittybot2.tasks.RunRulesParams;
import instantiator.dailykittybot2.tasks.RunRulesNotificationTask;
import instantiator.dailykittybot2.ui.AccountsListActivity;

public class BotService extends AbstractBackgroundBindingService<IBotService> implements IBotService {
    private static final String TAG = BotService.class.getName();

    private UUID device_uuid;
    private BotWorkspace workspace;

    private static final String KEY_Device_UUID = "device.uuid";

    public static final String CHANNEL_ID_EXECUTIONS = "channel_DKB_executions";

    @Override
    public void onCreate() {
        super.onCreate();
        init_defaults();
        init_workspace();
        init_channel();
        EventBus.getDefault().register(this);
    }

    private void init_channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channel_id = CHANNEL_ID_EXECUTIONS;
            String channel_name = getString(R.string.channel_executions_name);
            String channel_description = getString(R.string.channel_executions_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, importance);
            channel.setDescription(channel_description);
            channel.enableLights(true);
            channel.setLightColor(Color.YELLOW);
            channel.setSound(null, null);
            channel.enableVibration(false);
            // mChannel.enableVibration(true);
            // mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mgr.createNotificationChannel(channel);
        }
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
        Condition condition = DataFactory.create_condition(rule_id, Integer.MAX_VALUE);
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                int ordering = workspace.max_condition_ordering_for_rule(rule_id) + 1;
                condition.ordering = ordering;
                workspace.insert_condition(condition);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered updating rule", e);
            }
        });
        return condition;
    }

    @Override
    public void delete_condition(Condition condition) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.delete_condition(condition);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered deleting condition", e);
            }
        });
    }

    @Override
    public void delete_outcome(Outcome outcome) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.delete_outcome(outcome);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered deleting condition", e);
            }
        });
    }

    @Override
    public void delete_rule(Rule rule) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.delete_rule(rule);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered deleting condition", e);
            }
        });
    }

    @Override
    public void delete_all_recommendations(String username) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.delete_all_recommendations(username);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered deleting condition", e);
            }
        });
    }

    @Override
    public void delete_run_reports_for(UUID rule) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.delete_run_reports_for(rule);
                workspace.rule_forgets_last_run(rule);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered deleting condition", e);
            }
        });
    }

    @Override
    public void delete_run_reports_for(String username) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.delete_run_reports_for(username);
                workspace.rules_forget_last_run_for(username);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered deleting condition", e);
            }
        });
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
    public void insert_recommendations(List<Recommendation> recommendations) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.insert_recommendations(recommendations);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered inserting recommendations", e);
            }
        });
    }

    @Override
    public void insert_runReports(List<RunReport> reports) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                workspace.insert_runReports(reports);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered inserting recommendations", e);
            }
        });
    }

    @Override
    public void run(final String user, final RuleTriplet rule) {
        RedditSession.Listener session_listener = new RedditSession.Listener() {
            @Override
            public void state_switched(RedditSession session, RedditSession.State state, String username) {
                if (state == RedditSession.State.Authenticated) {
                    RunRulesNotificationTask task = new RunRulesNotificationTask(
                            BotService.this,
                            session,
                            BotService.this);

                    RunRulesParams params = new RunRulesParams();
                    params.account = username;
                    params.mode = RuleExecutor.ExecutionMode.ActOnLastHourSubmissions;
                    params.rules = Arrays.asList(rule);
                    task.execute(params);
                }
            }
        };

        RedditSession session = new RedditSession(this, device_uuid, session_listener);
        session.authenticate_as(user);
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

    @Override
    public void injectSampleData(final String user, final String name_str) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                SampleDataInjector injector = new SampleDataInjector(this, user, workspace.db);
                injector.inject(name_str);
            } catch (Exception e) {
                Log.e(TAG, "Exception encountered injecting sample data", e);
            }
        });
    }

    @Override
    public SubredditExecutionResult execute_rules_for_subreddit(
            RedditSession session, RuleExecutor.Listener progress_listener,
            String subreddit, List<RuleTriplet> rules, RuleExecutor.ExecutionMode mode) {

        RuleExecutor exec = new RuleExecutor(this, this, session, progress_listener);
        return exec.execute_rules_for_subreddit(subreddit, rules, mode);
    }

    @Override
    public Enaction enact(RedditClient client, Recommendation recommendation) {
        Enactor enactor = new Enactor(this, this, client);
        Enaction enaction = enactor.enact(recommendation);
        workspace.insert_enaction(enaction);
        return enaction;
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
    public UUID get_device_uuid() {
        return device_uuid;
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
        defaults.show_notification = true;
        return defaults;
    }

    @Override
    protected String[] getRequiredPermissions() {
        return BotApp.required_permissions;
    }

}
