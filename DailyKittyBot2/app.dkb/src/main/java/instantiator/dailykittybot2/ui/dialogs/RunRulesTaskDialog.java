package instantiator.dailykittybot2.ui.dialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import net.dean.jraw.models.Submission;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.ui.AbstractBotActivity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RunRulesTaskDialog {

    private Context context;
    private State state;
    private AlertDialog dialog;
    private Listener listener;

    private Date started;
    private Date finished;

    private String current_username;
    private String current_subreddit;
    private Submission current_post;
    private Rule current_rule;
    private int current_subreddit_posts_total;
    private int current_subreddit_posts_count;
    private int generated_recommendation_count;

    @BindView(R.id.text_dialog_title) public TextView text_dialog_title;
    @BindView(R.id.time_dialog_time) public RelativeTimeTextView time_dialog_time;
    @BindView(R.id.icon_dialog_task) public ImageView icon_dialog_task;
    @BindView(R.id.text_dialog_current_task) public TextView text_dialog_current_task;
    @BindView(R.id.busy_indeterminate) public ProgressBar busy_indeterminate;

    @BindView(R.id.text_dialog_subreddit) public TextView text_dialog_subreddit;
    @BindView(R.id.text_dialog_post_title) public TextView text_dialog_post_title;
    @BindView(R.id.time_dialog_post_date) public RelativeTimeTextView time_dialog_post_date;
    @BindView(R.id.text_dialog_rule_name) public TextView text_dialog_rule_name;
    @BindView(R.id.busy_progress) public ProgressBar busy_progress;

    @BindView(R.id.text_dialog_generated_recommendation_count) public TextView text_dialog_generated_recommendation_count;
    @BindView(R.id.btn_close) public Button btn_close;
    @BindView(R.id.btn_cancel) public Button btn_cancel;
    @BindView(R.id.btn_finish) public Button btn_finish;
    @BindView(R.id.btn_skip) public Button btn_skip;

    public RunRulesTaskDialog(AbstractBotActivity<?> context, String username, Listener listener) {
        this.context = context;
        this.current_username = username;
        this.listener = listener;
        this.dialog = new AlertDialog.Builder(context)
                .setView(R.layout.dialog_task_run_rules)
                .setCancelable(false)
                .create();
    }

    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
            ButterKnife.bind(this, dialog);
        }
        reset();
    }

    public void reset() {
        set_subreddit(null);
        set_state(State.Init);
    }

    public void set_state(State state) {
        this.state = state;
        if (state == State.Init) {
            started = new Date();
            finished = null;
            generated_recommendation_count = 0;
            updateUI();
        }
        if (state == State.Finished) {
            finished = new Date();
            set_subreddit(null);
        }
    }

    public void set_subreddit(String subreddit) {
        if (!StringUtils.equals(subreddit, current_subreddit)) {
            current_subreddit = subreddit;
            current_subreddit_posts_total = 0;
            current_subreddit_posts_count = 0;
            set_post(null);
        }
    }

    public void set_post(Submission post) {
        current_post = post;
        current_subreddit_posts_count += (post != null ? 1 : 0);
        set_rule(null);
    }

    public void set_rule(Rule rule) {
        current_rule = rule;
        updateUI();
    }

    public void increment_subreddit_post_total(int increment) {
        current_subreddit_posts_total += increment;
        updateUI();
    }

    public void increment_generated_recommendation_count(int increment) {
        generated_recommendation_count += increment;
        updateUI();
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            reset();
            dialog.dismiss();
        }
    }

    public enum State {
        Init(R.string.dialog_running_rules_activity_preparing, R.drawable.ic_hourglass_empty_black_24dp, false, false),
        Fetching(R.string.dialog_running_rules_activity_fetching_posts, R.drawable.ic_file_download_black_24dp, true, true),
        Applying(R.string.dialog_running_rules_activity_applying_rules, R.drawable.ic_playlist_add_check_black_24dp, true, false),
        Paused(R.string.dialog_running_rules_activity_paused, R.drawable.ic_pause_black_24dp, true, false),
        Saving(R.string.dialog_running_rules_activity_storing_recommendations, R.drawable.ic_lightbulb_outline_black_24dp, true, true),
        Finished(R.string.dialog_running_rules_activity_finished, R.drawable.ic_done_all_black_24dp, false, false),
        Cancelled(R.string.dialog_running_rules_activity_cancelled, R.drawable.ic_not_interested_black_24dp, false, false);

        private int description;
        private int icon;
        private boolean working;
        private boolean indeterminate;

        private State(int resource, int icon, boolean busy, boolean indeterminate) {
            this.description = resource;
            this.icon = icon;
            this.working = busy;
            this.indeterminate = indeterminate;
        }

        public int getDescription() { return description; }
        public boolean isWorking() { return working; }
        public boolean isIndeterminate() { return indeterminate; }
        public int getIcon() { return icon; }
    }

    private void updateUI() {
        if (current_username == null) {
            text_dialog_title.setText(R.string.dialog_running_rules_title);
        } else {
            text_dialog_title.setText(context.getString(R.string.dialog_running_rules_title_for, current_username));
        }

        if (finished != null) {
            time_dialog_time.setVisibility(VISIBLE);
            time_dialog_time.setPrefix(context.getString(R.string.dialog_running_rules_time_display_finished_prefix));
            time_dialog_time.setReferenceTime(finished.getTime());
        } else if (started != null) {
            time_dialog_time.setVisibility(VISIBLE);
            time_dialog_time.setPrefix(context.getString(R.string.dialog_running_rules_time_display_started_prefix));
            time_dialog_time.setReferenceTime(started.getTime());
        } else {
            time_dialog_time.setVisibility(GONE);
        }

        text_dialog_current_task.setText(state.getDescription());
        icon_dialog_task.setImageResource(state.getIcon());

        setVisible(text_dialog_subreddit, R.string.dialog_running_rules_subreddit_is, current_subreddit);
        setVisible(text_dialog_post_title, R.string.dialog_running_rules_post_title_is, current_post);
        setVisible(time_dialog_post_date, R.string.dialog_running_rules_post_created_prefix, current_post);
        setVisible(text_dialog_rule_name, R.string.dialog_running_rules_rule_name_is, current_rule);

        busy_indeterminate.setVisibility(state.isWorking() ? VISIBLE : GONE);

        busy_progress.setIndeterminate(state.isIndeterminate());
        busy_progress.setMax(current_subreddit_posts_total);
        busy_progress.setProgress(current_subreddit_posts_count);

        if (state != State.Cancelled) {
            text_dialog_generated_recommendation_count.setText(context.getString(
                    R.string.dialog_running_rules_generated_recommendations,
                    generated_recommendation_count));
        } else {
            text_dialog_generated_recommendation_count.setText(
                    R.string.dialog_running_rules_generated_recommendations_cancelled);
        }

        btn_close.setVisibility(state.isWorking() ? GONE : VISIBLE);
        btn_finish.setVisibility(state.isWorking() ? VISIBLE: GONE);
        btn_cancel.setVisibility(state.isWorking() ? VISIBLE: GONE);
        btn_skip.setVisibility(state.isWorking() ? VISIBLE: GONE);
    }

    @OnClick(R.id.btn_close)
    public void close_click() {
        dialog.dismiss();
    }

    @OnClick(R.id.btn_finish)
    public void finish_click() {
        listener.request_finish();
    }

    @OnClick(R.id.btn_cancel)
    public void cancel_click() {
        listener.request_cancel();
    }

    @OnClick(R.id.btn_skip)
    public void skip_click() {
        listener.request_skip_subreddit(current_subreddit);
    }

    private void setVisible(RelativeTimeTextView view, int resource, Submission post) {
        setVisible(view, post);
        if (post != null) {
            view.setPrefix(context.getString(resource));
            view.setReferenceTime(post.getCreated().getTime());
        }
    }

    private void setVisible(TextView view, int resource, Rule rule) {
        if (rule == null) {
            setVisible(view, null);
        } else {
            setVisible(view, resource, rule.rulename);
        }
    }

    private void setVisible(TextView view, int resource, Submission post) {
        if (post == null) {
            setVisible(view, null);
        } else {
            setVisible(view, resource, post.getTitle());
        }
    }

    private void setVisible(TextView view, int resource, String modifier) {
        setVisible(view, modifier);
        if (modifier != null) {
            view.setText(context.getString(resource, modifier));
        }
    }

    private void setVisible(View view, Object object) {
        view.setVisibility(object == null ? GONE : VISIBLE);
    }

    public interface Listener {
        void request_skip_subreddit(String subreddit);
        void request_finish();
        void request_cancel();
    }
}
