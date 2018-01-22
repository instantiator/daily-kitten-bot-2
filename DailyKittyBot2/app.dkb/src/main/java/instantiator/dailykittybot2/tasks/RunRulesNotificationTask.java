package instantiator.dailykittybot2.tasks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.service.BotService;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.service.execution.RuleResult;
import instantiator.dailykittybot2.service.execution.SubredditExecutionResult;
import instantiator.dailykittybot2.ui.UserOverviewActivity;

import static android.support.v4.app.NotificationCompat.PRIORITY_LOW;

@Deprecated
public class RunRulesNotificationTask extends AsyncTask<RunRulesParams, RunRulesProgress, RunRulesResult> implements RuleExecutor.Listener {
    private static final String TAG = RunRulesNotificationTask.class.getName();

    private Context context;
    private RedditSession session;
    private IBotService service;

    private RunRulesProgress current_progress;

    private static int NEXT_NOTIFICATION_ID = 6000;

    private int notification_id;
    private NotificationCompat.Builder notification_builder;

    private NotificationManager mgr;

    public RunRulesNotificationTask(Context context, RedditSession session, IBotService service) {
        this.context = context;
        this.session = session;
        this.service = service;

        mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Intent resultIntent = UserOverviewActivity.create(context, session.get_username());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // stackBuilder.addParentStack(ResultActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notification_builder = new NotificationCompat.Builder(context, BotService.CHANNEL_ID_EXECUTIONS)
                .setSmallIcon(R.drawable.ic_play_circle_outline_black_24dp)
                .setContentTitle(context.getString(R.string.execution_notification_title_starting))
                .setContentText(context.getString(R.string.execution_notification_content_starting))
                .setTicker(context.getString(R.string.execution_notification_ticker_starting))
                .setPriority(PRIORITY_LOW)
                .setContentIntent(resultPendingIntent)
                .setOngoing(true)
                //.setProgress(0, 100, true)
                .setUsesChronometer(true)
                .setSound(null)
                .setWhen(new Date().getTime());

        notification_id = NEXT_NOTIFICATION_ID++;
        mgr.notify(notification_id, notification_builder.build());
    }

    @Override
    protected RunRulesResult doInBackground(RunRulesParams... runParams) {
        RunRulesParams params = runParams[0];

        RunRulesResult result = new RunRulesResult();
        result.username = params.account;
        result.subreddits_completed = 0;
        result.subreddits_to_results = new HashMap<>();
        result.all_run_reports = new LinkedList<>();

        Date now = new Date();
        for (RuleTriplet rule : params.rules) {
            rule.rule.last_run_hint = now;
            service.update_rule(rule.rule);
        }

        RuleExecutor exec = new RuleExecutor(context, service, session, this);
        Map<String, List<RuleTriplet>> subreddits_to_rules = RuleExecutor.reorg_rules_per_subreddit(params.rules);
        result.total_subreddits = subreddits_to_rules.keySet().size();

        current_progress = new RunRulesProgress(params.account, result.total_subreddits);

        for (String subreddit : subreddits_to_rules.keySet()) {
            List<RuleTriplet> rules = subreddits_to_rules.get(subreddit);
            SubredditExecutionResult execution = exec.execute_rules_for_subreddit(subreddit, rules, params.mode);

            result.all_run_reports.addAll(execution.subreddit_rule_RunReports);
            result.subreddits_to_results.put(subreddit, execution.subreddit_rule_RuleResults);
            result.subreddits_completed++;
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(RunRulesProgress... values) {
        super.onProgressUpdate(values);
        RunRulesProgress p = values[0];

        notification_builder.setContentTitle(
                context.getString(
                        R.string.execution_notification_title_for_user,
                        p.current_username));

        String content = context.getString(
                R.string.execution_notification_content_for_details,
                p.current_subreddit,
                p.current_post_count_this_subreddit);

        notification_builder.setContentText(content);

        notification_builder.setStyle(
                new NotificationCompat.BigTextStyle()
                .bigText(content));

        notification_builder.setNumber(current_progress.generated_recommendation_count);

        mgr.notify(notification_id, notification_builder.build());

    }

    @Override
    protected void onPostExecute(RunRulesResult runResult) {
        super.onPostExecute(runResult);
        notification_builder.setContentText(
                context.getString(R.string.execution_notification_content_completion,
                    current_progress.generated_recommendation_count));

        service.insert_runReports(runResult.all_run_reports);

        for (String subreddit : runResult.subreddits_to_results.keySet()) {
            for (RuleResult result : runResult.subreddits_to_results.get(subreddit)) {
                if (result.recommendations != null) {
                    service.insert_recommendations(result.recommendations);
                }
            }
        }

        mgr.cancel(notification_id);
    }

    private RunRulesProgress copy_current_progress() {
        RunRulesProgress next = new RunRulesProgress(current_progress.current_username, current_progress.of_subreddits_count);
        next.current_subreddit = current_progress.current_subreddit;
        next.total_posts_this_subreddit = current_progress.total_posts_this_subreddit;
        next.fetching_posts = current_progress.fetching_posts;
        next.current_post = current_progress.current_post;
        next.current_rule = current_progress.current_rule;
        next.current_post_count_this_subreddit = current_progress.current_post_count_this_subreddit;
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
        current_progress.total_posts_this_subreddit = 0;
        current_progress.fetching_posts = true;
        RunRulesProgress copy = copy_current_progress();
        publishProgress(copy);
    }

    @Override
    public void testing_subreddit(String subreddit, int total_posts) {
        Log.v(TAG, "Subreddit: " + subreddit);
        current_progress.total_posts_this_subreddit = total_posts;
        current_progress.fetching_posts = false;
        RunRulesProgress copy = copy_current_progress();
        publishProgress(copy);
    }

    @Override
    public void testing_submission(Submission submission) {
        Log.v(TAG, "Submission: " + submission.getTitle());
        current_progress.current_post = submission.getTitle();
        current_progress.current_post_count_this_subreddit++;
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
    public void increment_recommendations(int recommendations) {
        Log.v(TAG, "Recommendations generated: " + recommendations);
        current_progress.generated_recommendation_count += recommendations;
        RunRulesProgress copy = copy_current_progress();
        publishProgress(copy);
    }
}
