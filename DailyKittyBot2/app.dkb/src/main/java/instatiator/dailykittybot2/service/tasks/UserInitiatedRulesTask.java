package instatiator.dailykittybot2.service.tasks;

import android.app.Notification;
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
import java.util.List;
import java.util.Map;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.service.BotService;
import instatiator.dailykittybot2.service.IBotService;
import instatiator.dailykittybot2.service.RedditSession;
import instatiator.dailykittybot2.service.execution.RuleExecutor;
import instatiator.dailykittybot2.service.execution.RuleResult;
import instatiator.dailykittybot2.ui.UserOverviewActivity;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
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
        this.context= context;
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

        RuleExecutor exec = new RuleExecutor(context, session, this);

        Map<String, List<RuleTriplet>> subreddits_to_rules = RuleExecutor.reorg_rules_per_subreddit(params.rules);
        result.total_subreddits = subreddits_to_rules.keySet().size();

        current_progress = new RunProgress(params.account, result.total_subreddits);

        for (String subreddit : subreddits_to_rules.keySet()) {
            List<RuleTriplet> rules = subreddits_to_rules.get(subreddit);

            List<RuleResult> results = exec.execute_rules_for_subreddit(subreddit, rules, params.mode);

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

        Log.v(TAG, String.format("Username: %s, Subreddit: %s, Post: %s, Rule: %s",
                p.current_username,
                p.current_subreddit,
                post,
                p.current_rule));
    }

    @Override
    protected void onPostExecute(RunResult runResult) {
        super.onPostExecute(runResult);
        // clear UI
        // pass new recommendations over to service

        notification_builder.setContentText(
                context.getString(R.string.execution_notification_content_completion,
                    current_progress.generated_recommendation_count));

        for (String subreddit : runResult.subreddits_to_results.keySet()) {
            for (RuleResult result : runResult.subreddits_to_results.get(subreddit)) {
                service.insert_recommendations(result.recommendations);
            }
        }

        mgr.cancel(notification_id);
    }

    @Override
    public void testing_subreddit(String subreddit) {
        current_progress.current_subreddit = subreddit;
        current_progress.current_subreddits_count++;
        publishProgress(current_progress);
    }

    @Override
    public void testing_submission(Submission submission) {
        current_progress.current_post = submission.getTitle();
        current_progress.current_post_count++;
        publishProgress(current_progress);
    }

    @Override
    public void applying_rule(RuleTriplet rule) {
        current_progress.current_rule = rule.rule.rulename;
        publishProgress(current_progress);
    }

    @Override
    public void generated_recommendations(int recommendations) {
        current_progress.generated_recommendation_count += recommendations;
        publishProgress(current_progress);
    }
}
