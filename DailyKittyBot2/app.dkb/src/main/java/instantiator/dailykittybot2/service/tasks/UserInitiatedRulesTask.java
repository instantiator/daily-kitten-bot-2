package instantiator.dailykittybot2.service.tasks;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.RunReport;
import instantiator.dailykittybot2.service.BotService;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.service.execution.RuleResult;
import instantiator.dailykittybot2.ui.UserOverviewActivity;

import static android.support.v4.app.NotificationCompat.PRIORITY_LOW;

public class UserInitiatedRulesTask extends AsyncTask<RunParams, RunProgress, RunResult> implements RuleExecutor.Listener {
    private static final String TAG = UserInitiatedRulesTask.class.getName();

    private Context context;
    private RedditSession session;
    private IBotService service;

    private RunProgress current_progress;

    private static int NEXT_NOTIFICATION_ID = 6000;

    private int notification_id;
    private NotificationCompat.Builder notification_builder;

    private NotificationManager mgr;

    public UserInitiatedRulesTask(Context context, RedditSession session, IBotService service) {
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
    protected RunResult doInBackground(RunParams... runParams) {
        RunParams params = runParams[0];

        RunResult result = new RunResult();
        result.username = params.account;
        result.subreddits_completed = 0;
        result.subreddits_to_results = new HashMap<>();
        result.all_run_reports = new LinkedList<>();

        Date now = new Date();
        for (RuleTriplet rule : params.rules) {
            rule.rule.last_run = now;
            service.update_rule(rule.rule);
        }

        RuleExecutor exec = new RuleExecutor(context, service, session, this);
        Map<String, List<RuleTriplet>> subreddits_to_rules = RuleExecutor.reorg_rules_per_subreddit(params.rules);
        result.total_subreddits = subreddits_to_rules.keySet().size();

        current_progress = new RunProgress(params.account, result.total_subreddits);

        for (String subreddit : subreddits_to_rules.keySet()) {
            Date started = new Date();
            List<RuleTriplet> rules = subreddits_to_rules.get(subreddit);
            List<RuleResult> results = exec.execute_rules_for_subreddit(subreddit, rules, params.mode);
            Date finished = new Date();

            Collection<RunReport> reports = exec.collate_reports_for_subreddit(results, started, finished);
            result.all_run_reports.addAll(reports);

            result.subreddits_to_results.put(subreddit, results);
            result.subreddits_completed++;
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(RunProgress... values) {
        super.onProgressUpdate(values);
        RunProgress p = values[0];

        notification_builder.setContentTitle(
                context.getString(
                        R.string.execution_notification_title_for_user,
                        p.current_username));

        notification_builder.setContentText(
                context.getString(
                        R.string.execution_notification_content_for_details,
                        p.current_subreddit,
                        p.current_post,
                        p.current_rule));

        notification_builder.setNumber(current_progress.generated_recommendation_count);

        mgr.notify(notification_id, notification_builder.build());

        String post =
                p.current_post == null ?
                        null :
                        p.current_post.substring(0, Math.min(15, p.current_post.length()));
    }

    @Override
    protected void onPostExecute(RunResult runResult) {
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

    @Override
    public void testing_subreddit(String subreddit) {
        Log.v(TAG, "Subreddit: " + subreddit);
        current_progress.current_subreddit = subreddit;
        current_progress.current_subreddits_count++;
        publishProgress(current_progress);
    }

    @Override
    public void testing_submission(Submission submission) {
        Log.v(TAG, "Submission: " + submission);
        current_progress.current_post = submission.getTitle();
        current_progress.current_post_count++;
        publishProgress(current_progress);
    }

    @Override
    public void applying_rule(RuleTriplet rule) {
        Log.v(TAG, "Rule: " + rule.rule.rulename);
        current_progress.current_rule = rule.rule.rulename;
        publishProgress(current_progress);
    }

    @Override
    public void generated_recommendations(int recommendations) {
        Log.v(TAG, "Recommendations generated: " + recommendations);
        current_progress.generated_recommendation_count += recommendations;
        publishProgress(current_progress);
    }
}
