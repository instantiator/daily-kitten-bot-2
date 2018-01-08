package instatiator.dailykittybot2.ui.adapters;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dean.jraw.models.PersistedAuthData;
import net.dean.jraw.oauth.DeferredPersistentTokenStore;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.service.IBotService;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LiveRulesAdapter extends RecyclerView.Adapter<LiveRulesAdapter.RuleHolder> {

    private final AppCompatActivity activity;
    private List<Rule> rules;
    private RecyclerView recyclerView;
    private Listener listener;
    private CardView empty_card;

    public LiveRulesAdapter(AppCompatActivity activity, LiveData<List<Rule>> live_rules, RecyclerView recyclerView, Listener listener, CardView empty_card) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.empty_card = empty_card;

        this.rules = live_rules.getValue();
        update_empty_card();

        live_rules.observe(activity, new Observer<List<Rule>>() {
            @Override
            public void onChanged(@Nullable List<Rule> rules) {
                LiveRulesAdapter.this.rules = rules;
                notifyDataSetChanged();
                update_empty_card();
            }
        });
    }

    private void update_empty_card() {
        boolean empty = rules == null || rules.size() == 0;
        empty_card.setVisibility(empty ? VISIBLE : GONE);
    }

    @Override
    public RuleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, null);
        return new RuleHolder(view);
    }

    @Override
    public void onBindViewHolder(RuleHolder holder, int position) {
        Rule rule = rules.get(position);
        holder.rule = rule;
        holder.text_rule_name.setText(rule.rulename);
        holder.text_rule_summary.setText("");
    }

    @Override
    public int getItemCount() {
        return rules != null ? rules.size() : 0;
    }

    public class RuleHolder extends RecyclerView.ViewHolder {
        public Rule rule;

        @BindView(R.id.text_rule_name) public TextView text_rule_name;
        @BindView(R.id.text_rule_summary)public TextView text_rule_summary;

        public RuleHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.rule_selected(rule);
                }
            });
        }
    }

    public interface Listener {
        void rule_selected(Rule rule);
    }
}