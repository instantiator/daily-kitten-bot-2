package instantiator.dailykittybot2.tasks;

import android.arch.persistence.room.util.StringUtil;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.models.Submission;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.service.execution.RuleResult;
import instantiator.dailykittybot2.service.execution.SubredditExecutionResult;
import instantiator.dailykittybot2.ui.AbstractBotActivity;
import instantiator.dailykittybot2.ui.dialogs.RunRulesTaskDialog;

public class RunRulesDialogTask extends AsyncTask<RunRulesParams, Void, RunRulesResult>
        implements RunRulesTaskDialog.Listener, RuleExecutor.Listener {

    private static final String TAG = RunRulesDialogTask.class.getName();

    private AbstractBotActivity<?> context;
    private RedditSession session;
    private IBotService service;
    private String username;
    private RuleExecutor exec;

    private RunRulesTaskDialog dialog;

    private boolean flag_cancel;
    private boolean flag_finish;
    private boolean flag_skip_subreddit;
    private String flagged_subreddit;

    public RunRulesDialogTask(AbstractBotActivity<?> context, RedditSession session, IBotService service, String username) {
        this.context = context;
        this.session = session;
        this.service = service;
        this.username = username;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        exec = new RuleExecutor(context, service, session, this);
        dialog = new RunRulesTaskDialog(context, username, this);
        dialog.show();
    }

    @Override
    protected RunRulesResult doInBackground(RunRulesParams... runParams) {
        RunRulesParams params = runParams[0];

        Date started = new Date();

        RunRulesResult result = new RunRulesResult();
        result.username = params.account;
        result.subreddits_completed = 0;
        result.subreddits_to_results = new HashMap<>();
        result.all_run_reports = new LinkedList<>();

        Map<String, List<RuleTriplet>> subreddits_to_rules = RuleExecutor.reorg_rules_per_subreddit(params.rules);
        result.total_subreddits = subreddits_to_rules.keySet().size();


        for (String subreddit : subreddits_to_rules.keySet()) {
            List<RuleTriplet> rules = subreddits_to_rules.get(subreddit);

            SubredditExecutionResult execution = exec.execute_rules_for_subreddit(subreddit, rules, params.mode);

            result.all_run_reports.addAll(execution.subreddit_rule_RunReports);
            result.subreddits_to_results.put(subreddit, execution.subreddit_rule_RuleResults);
            result.subreddits_completed++;

            if (flag_skip_subreddit && StringUtils.equals(subreddit, flagged_subreddit)) {
                flag_skip_subreddit = false;
                flagged_subreddit = null;
            }
            if (flag_cancel || flag_finish) { break; }
        }

        for (RuleTriplet rule : params.rules) {
            rule.rule.last_run_hint = started;
            service.update_rule(rule.rule);
        }

        return result;
    }

    @Override
    protected void onPostExecute(RunRulesResult runResult) {
        super.onPostExecute(runResult);

        if (!flag_cancel) {
            dialog.set_state(RunRulesTaskDialog.State.Saving);
            service.insert_runReports(runResult.all_run_reports);
            for (String subreddit : runResult.subreddits_to_results.keySet()) {
                for (RuleResult result : runResult.subreddits_to_results.get(subreddit)) {
                    if (result.recommendations != null) {
                        service.insert_recommendations(result.recommendations);
                    }
                }
            }
            dialog.set_state(RunRulesTaskDialog.State.Finished);

        } else {
            dialog.set_state(RunRulesTaskDialog.State.Cancelled);
        }

        //dialog.dismiss();
    }

    @Override
    public void fetching_subreddit_posts(String subreddit) {
        Log.v(TAG, "Fetching subreddit... " + subreddit);
        context.runOnUiThread(() -> {
            dialog.set_subreddit(subreddit);
            dialog.set_state(RunRulesTaskDialog.State.Fetching);
        });
    }

    @Override
    public void testing_subreddit(String subreddit, int additional_posts) {
        Log.v(TAG, "Testing subreddit: " + subreddit);
        context.runOnUiThread(() -> {
            dialog.set_subreddit(subreddit);
            dialog.increment_subreddit_post_total(additional_posts);
            dialog.set_state(RunRulesTaskDialog.State.Applying);
        });
    }

    @Override
    public void testing_submission(Submission submission) {
        Log.v(TAG, "Submission: " + submission.getTitle());
        context.runOnUiThread(() -> {
            dialog.set_post(submission);
        });
    }

    @Override
    public void applying_rule(RuleTriplet triplet) {
        Log.v(TAG, "Rule: " + triplet.rule.rulename);
        context.runOnUiThread(() -> {
            dialog.set_rule(triplet.rule);
        });
    }

    @Override
    public void increment_recommendations(int recommendations) {
        Log.v(TAG, "Recommendations generated: " + recommendations);
        context.runOnUiThread(() -> {
            dialog.increment_generated_recommendation_count(recommendations);
        });
    }

    @Override
    public void request_cancel() {
        flag_cancel = true;
        if (exec != null) {
            exec.flag_cancel();
        }
    }

    @Override
    public void request_finish() {
        flag_finish = true;
        if (exec != null) {
            exec.flag_finish();
        }
    }

    @Override
    public void request_skip_subreddit(String subreddit) {
        flag_skip_subreddit = true;
        flagged_subreddit = subreddit;
        if (exec != null) {
            exec.flag_skip_subreddit(subreddit);
        }
    }

}
