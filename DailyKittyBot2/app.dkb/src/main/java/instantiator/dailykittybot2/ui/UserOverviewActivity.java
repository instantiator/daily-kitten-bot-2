package instantiator.dailykittybot2.ui;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Arrays;

import butterknife.BindView;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.service.tasks.RunParams;
import instantiator.dailykittybot2.service.tasks.UserInitiatedRulesTask;
import instantiator.dailykittybot2.ui.fragments.UserRecommendationsFragment;
import instantiator.dailykittybot2.ui.fragments.UserRulesFragment;
import instantiator.dailykittybot2.ui.pagers.UserOverviewPagerAdapter;
import instantiator.dailykittybot2.ui.viewmodels.UserOverviewViewModel;

public class UserOverviewActivity extends AbstractBotActivity<UserOverviewViewModel>
        implements UserRulesFragment.Listener, UserRecommendationsFragment.Listener {

    private static final String EXTRA_username = "username";

    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.tabs) TabLayout tabs;

    private UserOverviewPagerAdapter pager_adapter;

    private String username;

    public UserOverviewActivity() {
        super(true, true, false);
    }

    public static Intent create(Context context, String user) {
        Intent intent = new Intent(context, UserOverviewActivity.class);
        intent.putExtra(EXTRA_username, user);
        return intent;
    }

    @Override
    protected Class<UserOverviewViewModel> getViewModelClass() {
        return UserOverviewViewModel.class;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_user_overview;
    }

    @Override
    protected void extractArguments(Intent intent) {
        username = intent.getStringExtra(EXTRA_username);
    }

    @Override
    protected void initialise_model() {
        model.init(username);
    }

    @Override
    protected boolean initialise() {
        if (username != null) {
            pager_adapter = new UserOverviewPagerAdapter(this, getSupportFragmentManager(), username);
            pager.setAdapter(pager_adapter);
            tabs.setupWithViewPager(pager);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void denitialise() {
        // NOP
    }

    @Override
    protected void updateUI() {
        if (bound) {
            String title = getString(R.string.activity_title_overview_for, username);
            getSupportActionBar().setTitle(title);
        } else {
            getSupportActionBar().setTitle(R.string.please_wait);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, R.string.menu_inject_test_data, 0, R.string.menu_inject_test_data);
        menu.add(0, R.string.menu_delete_all_recommendations, 0, R.string.menu_delete_all_recommendations);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.menu_inject_test_data:
                confirm_inject_testData();
                return true;
            case R.string.menu_delete_all_recommendations:
                confirm_delete_all_recommendations();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirm_inject_testData() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_confirm_inject_test_data)
                .setMessage(R.string.dialog_message_confirm_inject_test_data)
                .setPositiveButton(R.string.btn_inject, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    service.injectTestData(username);
                })
                .setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
    }

    private void confirm_delete_all_recommendations() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_confirm_delete_all_recommendations)
                .setMessage(R.string.dialog_message_confirm_delete_all_recommendations)
                .setPositiveButton(R.string.btn_delete_all, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    service.delete_all_recommendations(username);
                })
                .setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
    }

    @Override
    public void rule_selected(Rule rule) {
        Intent intent = EditRuleActivity.edit(this, username, rule);
        startActivity(intent);
    }

    @Override
    public void request_create_rule() {
        Intent intent = EditRuleActivity.create(this, username);
        startActivity(intent);
    }

    @Override
    public void request_delete(Rule rule) {
        service.delete_rule(rule);
    }

    @Override
    public void request_run(RuleTriplet rule) {
        RedditSession.Listener session_listener = new RedditSession.Listener() {
            @Override
            public void state_switched(RedditSession session, RedditSession.State state, String username) {
                if (state == RedditSession.State.Authenticated) {
                    UserInitiatedRulesTask task = new UserInitiatedRulesTask(
                            UserOverviewActivity.this,
                            session,
                            service);

                    RunParams params = new RunParams();
                    params.account = username;
                    params.mode = RuleExecutor.ExecutionMode.RespectRuleLastRun;
                    params.rules = Arrays.asList(rule);
                    task.execute(params);
                }
            }
        };

        RedditSession session = new RedditSession(this, service.get_device_uuid(), session_listener);
        session.authenticate_as(rule.rule.username);

    }

    @Override
    public void recommendation_selected(Recommendation recommendation) {
        // TODO
    }

    @Override
    public void accept_recommendation(Recommendation recommendation) {
        // TODO
    }

    @Override
    public void reject_recommendation(Recommendation recommendation) {
        // TODO
    }
}
