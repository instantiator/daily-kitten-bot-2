package instantiator.dailykittybot2.ui;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Arrays;

import butterknife.BindView;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.service.helpers.SampleDataInjector;
import instantiator.dailykittybot2.tasks.EnactRecommendationDialogTask;
import instantiator.dailykittybot2.tasks.EnactRecommendationParams;
import instantiator.dailykittybot2.tasks.RunRulesDialogTask;
import instantiator.dailykittybot2.tasks.RunRulesParams;
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
        menu.add(0, R.string.menu_inject_sample_data, 0, R.string.menu_inject_sample_data);
        menu.add(0, R.string.menu_delete_all_recommendations, 0, R.string.menu_delete_all_recommendations);
        menu.add(0, R.string.menu_forget_all_run_reports, 0, R.string.menu_forget_all_run_reports);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.menu_inject_sample_data:
                confirm_inject_sampleData();
                return true;
            case R.string.menu_forget_all_run_reports:
                confirm_delete_all_run_reports();
                return true;
            case R.string.menu_delete_all_recommendations:
                confirm_delete_all_recommendations();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirm_inject_sampleData() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_select_sample_data)
                .items(SampleDataInjector.get_rule_names(this))
                .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
                    service.injectSampleData(username, text.toString());
                    return true;
                })
                .positiveText(R.string.btn_choose)
                .negativeText(R.string.btn_cancel)
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

    private void confirm_delete_all_run_reports() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_confirm_forget_all_run_reports)
                .setMessage(R.string.dialog_message_confirm_forget_all_run_reports)
                .setPositiveButton(R.string.btn_forget_all, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    service.delete_run_reports_for(username);
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
        Rule new_rule = service.create_rule(username, null);
        Intent intent = EditRuleActivity.edit(this, username, new_rule);
        startActivity(intent);
    }

    @Override
    public void request_delete(Rule rule) {
        service.delete_rule(rule);
    }

    @Override
    public void request_forget_run_reports(Rule rule) {
        service.delete_run_reports_for(rule.uuid);
    }

    @Override
    public void request_run(RuleTriplet rule, RuleExecutor.ExecutionMode mode) {
        RedditSession.Listener session_listener = new RedditSession.Listener() {
            @Override
            public void state_switched(RedditSession session, RedditSession.State state, String username) {
                if (state == RedditSession.State.Authenticated) {

                    RunRulesDialogTask task = new RunRulesDialogTask(
                            UserOverviewActivity.this,
                            session,
                            service,
                            username);

                    RunRulesParams params = new RunRulesParams();
                    params.account = username;
                    params.mode = mode;
                    params.rules = Arrays.asList(rule);
                    task.execute(params);
                }
            }
        };

        RedditSession session = new RedditSession(this, service.get_device_uuid(), session_listener);
        session.authenticate_as(rule.rule.username);
    }

    @Override
    public void request_view_post(Recommendation recommendation) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, recommendation.targetPostUri);
        startActivity(browserIntent);
    }

    private String describe(Recommendation recommendation) {
        switch (recommendation.type) {
            case DownvotePost:
            case UpvotePost:
                return getString(
                        R.string.dialog_message_describe_recommendation_simple,
                            recommendation.type.getActionString(),
                            recommendation.targetSubreddit,
                            recommendation.targetSummary);

            case AddCommentToPost:
                return getString(
                        R.string.dialog_message_describe_recommendation_comment,
                        recommendation.targetSubreddit,
                        recommendation.targetSummary,
                        recommendation.modifier);

            case DoNothing:
                return getString(
                        R.string.dialog_message_describe_recommendation_nothing,
                        recommendation.targetSubreddit,
                        recommendation.targetSummary);

            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void recommendation_selected(Recommendation recommendation) {
        show_recommendation_details(recommendation);
    }

    @Override
    public void accept_recommendation(Recommendation recommendation) {
        RedditSession.Listener session_listener = new RedditSession.Listener() {
            @Override
            public void state_switched(RedditSession session, RedditSession.State state, String username) {
                if (state == RedditSession.State.Authenticated) {
                    EnactRecommendationDialogTask task = new EnactRecommendationDialogTask(
                            UserOverviewActivity.this,
                            session,
                            service);

                    recommendation.userAccepted = true;
                    recommendation.userRejected = false;
                    service.update_recommendation(recommendation);
                    EnactRecommendationParams params = new EnactRecommendationParams();
                    params.recommendation = recommendation;
                    task.execute(params);
                }
            }
        };

        RedditSession session = new RedditSession(this, service.get_device_uuid(), session_listener);
        session.authenticate_as(recommendation.username);
    }

    @Override
    public void reject_recommendation(Recommendation recommendation) {
        recommendation.userAccepted = false;
        recommendation.userRejected = true;
        service.update_recommendation(recommendation);
    }

    private void show_recommendation_details(Recommendation recommendation) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_describe_recommendation)
                .setMessage(describe(recommendation))
                .setPositiveButton(R.string.btn_view_post, (dialogInterface, i) -> {
                    request_view_post(recommendation);
                })
                .setNegativeButton(R.string.btn_close, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .create()
                .show();
    }
}
