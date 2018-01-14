package instatiator.dailykittybot2.ui.adapters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.util.ColourConventions;
import instatiator.dailykittybot2.validation.RuleValidator;
import instatiator.dailykittybot2.validation.ValidationResult;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LiveRulesAdapter extends RecyclerView.Adapter<LiveRulesAdapter.RuleHolder> {

    private final AppCompatActivity activity;
    private List<RuleTriplet> triplets;
    private RecyclerView recyclerView;
    private Listener listener;
    private CardView empty_card;
    private ColourConventions colours;
    private RuleValidator validator;

    public LiveRulesAdapter(AppCompatActivity activity, LiveData<List<RuleTriplet>> live_rules, RecyclerView recyclerView, Listener listener, CardView empty_card) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.empty_card = empty_card;
        this.validator = new RuleValidator(activity);
        this.colours = new ColourConventions(activity);

        this.triplets = live_rules.getValue();
        update_empty_card();

        live_rules.observe(activity, new Observer<List<RuleTriplet>>() {
            @Override
            public void onChanged(@Nullable List<RuleTriplet> triplets) {
                LiveRulesAdapter.this.triplets = triplets;
                notifyDataSetChanged();
                update_empty_card();
            }
        });
    }

    private void update_empty_card() {
        boolean empty = triplets == null || triplets.size() == 0;
        empty_card.setVisibility(empty ? VISIBLE : GONE);
    }

    @Override
    public RuleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, null);
        return new RuleHolder(view);
    }

    @Override
    public void onBindViewHolder(RuleHolder holder, int position) {
        RuleTriplet triplet = triplets.get(position);
        holder.triplet = triplet;
        holder.text_rule_name.setText(triplet.rule.rulename);
        holder.text_rule_summary.setText(summarise(triplet.rule));

        ValidationResult result = validator.validate(triplet);

        boolean errors = result.errors.size() > 0;
        boolean warnings = result.warnings.size() > 0;
        holder.icon_alert.setVisibility(errors || warnings ? VISIBLE : GONE);
        holder.icon_alert.getDrawable().setTint(colours.icon_alert(errors, warnings));

        holder.icon_current.getDrawable().setTint(colours.rule_icon(triplet.rule.run_periodically));
        holder.text_rule_name.setTextColor(colours.rule_icon(triplet.rule.run_periodically));
    }

    private String summarise(Rule rule) {
        String summary_subs =
            rule.subreddits.size() > 0 ?
                    activity.getString(R.string.summary_rule_subreddits, TextUtils.join(", ", rule.subreddits)) :
                    activity.getString(R.string.summary_rule_no_subreddits);
        String summary_autorun =
            rule.run_periodically ?
                    activity.getString(R.string.summary_rule_autorun) :
                    activity.getString(R.string.summary_rule_no_autorun);

        return TextUtils.join(" ", new String[] { summary_subs, summary_autorun });
    }

    @Override
    public int getItemCount() {
        return triplets != null ? triplets.size() : 0;
    }

    public class RuleHolder extends RecyclerView.ViewHolder {
        public RuleTriplet triplet;

        @BindView(R.id.icon_current) public ImageView icon_current;
        @BindView(R.id.text_rule_name) public TextView text_rule_name;
        @BindView(R.id.text_rule_summary) public TextView text_rule_summary;
        @BindView(R.id.icon_alert) public ImageView icon_alert;
        @BindView(R.id.icon_menu) public ImageView icon_menu;

        PopupMenu popup;

        public RuleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.rule_selected(triplet.rule);
                }
            });

            popup = new PopupMenu(icon_menu.getContext(), icon_menu);
            popup.getMenu().add(0, R.string.menu_rule_view, 0, R.string.menu_rule_view);
            popup.getMenu().add(0, R.string.menu_rule_delete, 0, R.string.menu_rule_delete);
            popup.getMenu().add(0, R.string.menu_rule_run, 0, R.string.menu_rule_run);

            popup.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.string.menu_rule_view:
                        listener.rule_selected(triplet.rule);
                        return true;
                    case R.string.menu_rule_delete:
                        listener.request_delete(triplet.rule);
                        return true;
                    case R.string.menu_rule_run:
                        listener.request_run(triplet.rule);
                        return true;
                    default:
                        return false;
                }
            });
        }

        @OnClick(R.id.icon_menu)
        public void overflow_click() {
            popup.show();
        }

    }

    public interface Listener {
        void rule_selected(Rule rule);
        void request_delete(Rule rule);
        void request_run(Rule rule);
    }
}