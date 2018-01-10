package instatiator.dailykittybot2.ui.adapters;

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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Outcome;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LiveOutcomesAdapter extends RecyclerView.Adapter<LiveOutcomesAdapter.OutcomeHolder> {

    private final AppCompatActivity activity;
    private List<Outcome> outcomes;
    private RecyclerView recyclerView;
    private Listener listener;
    private CardView empty_card;

    public LiveOutcomesAdapter(AppCompatActivity activity, LiveData<List<Outcome>> live_outcomes, RecyclerView recyclerView, Listener listener, CardView empty_card) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.empty_card = empty_card;

        this.outcomes = live_outcomes.getValue();
        update_empty_card();

        live_outcomes.observe(activity, new Observer<List<Outcome>>() {
            @Override
            public void onChanged(@Nullable List<Outcome> rules) {
                LiveOutcomesAdapter.this.outcomes = outcomes;
                notifyDataSetChanged();
                update_empty_card();
            }
        });
    }

    private void update_empty_card() {
        boolean empty = outcomes == null || outcomes.size() == 0;
        empty_card.setVisibility(empty ? VISIBLE : GONE);
    }

    @Override
    public OutcomeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outcome, null);
        return new OutcomeHolder(view);
    }

    @Override
    public void onBindViewHolder(OutcomeHolder holder, int position) {
        Outcome outcome = outcomes.get(position);
        holder.outcome = outcome;
        holder.text_outcome_name.setText(outcome.type);
        holder.text_outcome_summary.setText(outcome.modifier);
    }

    @Override
    public int getItemCount() {
        return outcomes != null ? outcomes.size() : 0;
    }

    public class OutcomeHolder extends RecyclerView.ViewHolder {
        public Outcome outcome;

        @BindView(R.id.text_outcome_name) public TextView text_outcome_name;
        @BindView(R.id.text_outcome_summary)public TextView text_outcome_summary;

        public OutcomeHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.outcome_selected(outcome);
                }
            });
        }
    }

    public interface Listener {
        void outcome_selected(Outcome outcome);
    }
}