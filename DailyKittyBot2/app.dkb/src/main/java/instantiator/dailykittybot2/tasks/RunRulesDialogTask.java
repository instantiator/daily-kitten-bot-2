package instantiator.dailykittybot2.tasks;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.models.Submission;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.service.execution.RuleResult;
import instantiator.dailykittybot2.service.execution.SubredditExecutionResult;
import instantiator.dailykittybot2.ui.AbstractBotActivity;

public class RunRulesDialogTask extends AsyncTask<RunRulesParams, RunRulesProgress, RunRulesResult> implements RuleExecutor.Listener {
    private static final String TAG = RunRulesDialogTask.class.getName();

    private AbstractBotActivity<?> context;
    private RedditSession session;
    private IBotService service;

    private RunRulesProgress current_progress;

    private MaterialDialog dialog;

    public RunRulesDialogTask(AbstractBotActivity<?> context, RedditSession session, IBotService service) {
        this.context = context;
        this.session = session;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = new MaterialDialog.Builder(context)
                .title(context.getString(R.string.dialog_title_running_rules))
                .content(R.string.dialog_message_running_rules_preparing)
                .progress(false, 0)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();
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

        //RuleExecutor exec = new RuleExecutor(context, service, session, this);
        Map<String, List<RuleTriplet>> subreddits_to_rules = RuleExecutor.reorg_rules_per_subreddit(params.rules);
        result.total_subreddits = subreddits_to_rules.keySet().size();

        current_progress = new RunRulesProgress(params.account, result.total_subreddits);

        for (String subreddit : subreddits_to_rules.keySet()) {
            List<RuleTriplet> rules = subreddits_to_rules.get(subreddit);
            //SubredditExecutionResult execution = exec.execute_rules_for_subreddit(subreddit, rules, params.mode);
            SubredditExecutionResult execution = service.execute_rules_for_subreddit(session, this, subreddit, rules, params.mode);

            result.all_run_reports.addAll(execution.subreddit_rule_RunReports);
            result.subreddits_to_results.put(subreddit, execution.subreddit_rule_RuleResults);
            result.subreddits_completed++;
        }

        for (RuleTriplet rule : params.rules) {
            rule.rule.last_run_hint = started;
            service.update_rule(rule.rule);
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(RunRulesProgress... values) {
        super.onProgressUpdate(values);
        RunRulesProgress p = values[0];

        dialog.setTitle(context.getString(R.string.dialog_title_running_rules_for, p.current_username));

        String content;
        if (p.fetching_posts) {
            content = context.getString(
                        R.string.dialog_message_running_rules_fetching_posts_in,
                        p.current_subreddit);
            dialog.setContent(content);
        } else {
            content = context.getString(
                    R.string.dialog_message_running_rules_details,
                    p.current_post_count,
                    p.total_posts,
                    p.current_subreddit);
            dialog.setContent(content);
            dialog.setMaxProgress(p.total_posts);
            dialog.setProgress(p.current_post_count);
        }

    }

    @Override
    protected void onPostExecute(RunRulesResult runResult) {
        super.onPostExecute(runResult);
        dialog.setContent(context.getString(
                R.string.execution_notification_content_completion,
                current_progress.generated_recommendation_count));

        service.insert_runReports(runResult.all_run_reports);

        for (String subreddit : runResult.subreddits_to_results.keySet()) {
            for (RuleResult result : runResult.subreddits_to_results.get(subreddit)) {
                if (result.recommendations != null) {
                    service.insert_recommendations(result.recommendations);
                }
            }
        }

        dialog.dismiss();
    }

    private RunRulesProgress copy_current_progress() {
        RunRulesProgress next = new RunRulesProgress(current_progress.current_username, current_progress.of_subreddits_count);
        next.current_subreddit = current_progress.current_subreddit;
        next.total_posts = current_progress.total_posts;
        next.fetching_posts = current_progress.fetching_posts;
        next.current_post = current_progress.current_post;
        next.current_rule = current_progress.current_rule;
        next.current_post_count = current_progress.current_post_count;
        next.current_subreddits_count = current_progress.current_subreddits_count;
        next.generated_recommendation_count = current_progress.generated_recommendation_count;
        return next;
    }

    @Override
    public void fetching_subreddit_posts(String subreddit) {
        Log.v(TAG, "Fetching... " + subreddit);
        current_progress.current_subreddit = subreddit;
        current_progress.current_subreddits_count++;
        current_progress.current_post = null;
        current_progress.current_rule = null;
        //current_progress.total_posts = 0;
        //current_progress.current_post_count = 0;
        current_progress.fetching_posts = true;
        RunRulesProgress copy = copy_current_progress();
        publishProgress(copy);
    }

    @Override
    public void testing_subreddit(String subreddit, int total_posts) {
        Log.v(TAG, "Subreddit: " + subreddit);
        current_progress.total_posts += total_posts;
        current_progress.fetching_posts = false;
        RunRulesProgress copy = copy_current_progress();
        publishProgress(copy);
    }

    @Override
    public void testing_submission(Submission submission) {
        Log.v(TAG, "Submission: " + submission.getTitle());
        current_progress.current_post = submission.getTitle();
        current_progress.current_post_count++;
        current_progress.current_rule = null;
        RunRulesProgress copy = copy_current_progress();
        publishProgress(copy);
    }

    @Override
    public void applying_rule(RuleTriplet rule) {
        Log.v(TAG, "Rule: " + rule.rule.rulename);
        current_progress.current_rule = rule.rule.rulename;
        RunRulesProgress copy = copy_current_progress();
        publishProgress(copy);
    }

    @Override
    public void generated_recommendations(int recommendations) {
        Log.v(TAG, "Recommendations generated: " + recommendations);
        current_progress.generated_recommendation_count += recommendations;
        RunRulesProgress copy = copy_current_progress();
        publishProgress(copy);
    }
}
