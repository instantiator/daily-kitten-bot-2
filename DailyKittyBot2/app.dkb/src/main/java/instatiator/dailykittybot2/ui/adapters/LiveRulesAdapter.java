package instatiator.dailykittybot2.ui.adapters;

import android.app.Activity;
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
import android.widget.TextView;

import net.dean.jraw.models.PersistedAuthData;
import net.dean.jraw.oauth.DeferredPersistentTokenStore;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.service.IBotService;
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

    private RuleValidator validator;

    public LiveRulesAdapter(AppCompatActivity activity, LiveData<List<RuleTriplet>> live_rules, RecyclerView recyclerView, Listener listener, CardView empty_card) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.empty_card = empty_card;
        this.validator = new RuleValidator(activity);

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

        holder.icon_error.setVisibility(result.errors.size() > 0 ? VISIBLE : GONE);
        holder.icon_warning.setVisibility(result.warnings.size() > 0 ? VISIBLE : GONE);
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

        @BindView(R.id.text_rule_name) public TextView text_rule_name;
        @BindView(R.id.text_rule_summary) public TextView text_rule_summary;
        @BindView(R.id.icon_warning) public ImageView icon_warning;
        @BindView(R.id.icon_error) public ImageView icon_error;

        public RuleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.rule_selected(triplet.rule);
                }
            });
        }
    }

    public interface Listener {
        void rule_selected(Rule rule);
    }
}