package instantiator.dailykittybot2.ui.adapters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.util.ColourConventions;
import instantiator.dailykittybot2.validation.OutcomeValidator;
import instantiator.dailykittybot2.validation.ValidationResult;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LiveOutcomesAdapter extends RecyclerView.Adapter<LiveOutcomesAdapter.OutcomeHolder> {

    private final AppCompatActivity activity;
    private List<Outcome> outcomes;
    private RecyclerView recyclerView;
    private Listener listener;
    private CardView empty_card;
    private ColourConventions colours;
    private OutcomeValidator validator;

    public LiveOutcomesAdapter(AppCompatActivity activity, LiveData<List<Outcome>> live_outcomes, RecyclerView recyclerView, Listener listener, CardView empty_card) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.empty_card = empty_card;
        this.validator = new OutcomeValidator(activity);
        this.colours = new ColourConventions(activity);
        this.outcomes = live_outcomes.getValue();

        update_empty_card();

        live_outcomes.observe(activity, new Observer<List<Outcome>>() {
            @Override
            public void onChanged(@Nullable List<Outcome> outcomes) {
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
        holder.text_outcome_name.setText(outcome.type.getDescription());
        holder.text_outcome_summary.setText(outcome.modifier);
        holder.icon_current.setImageResource(outcome.type.getIcon());

        ValidationResult result = validator.validate(outcome);

        boolean errors = result.errors.size() > 0;
        boolean warnings = result.warnings.size() > 0;
        holder.icon_alert.setVisibility(errors || warnings ? VISIBLE : GONE);
        holder.icon_alert.getDrawable().mutate().setTint(colours.icon_alert(errors, warnings));
    }

    @Override
    public int getItemCount() {
        return outcomes != null ? outcomes.size() : 0;
    }

    public class OutcomeHolder extends RecyclerView.ViewHolder {
        public Outcome outcome;

        @BindView(R.id.text_outcome_name) public TextView text_outcome_name;
        @BindView(R.id.text_outcome_summary)public TextView text_outcome_summary;
        @BindView(R.id.icon_current) public ImageView icon_current;
        @BindView(R.id.icon_alert) public ImageView icon_alert;
        @BindView(R.id.icon_menu) public ImageView icon_menu;

        PopupMenu popup;

        public OutcomeHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.outcome_selected(outcome);
                }
            });

            popup = new PopupMenu(icon_menu.getContext(), icon_menu);
            popup.getMenu().add(0, R.string.menu_outcome_view, 0, R.string.menu_outcome_view);
            popup.getMenu().add(0, R.string.menu_outcome_delete, 0, R.string.menu_outcome_delete);

            popup.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.string.menu_outcome_view:
                        listener.outcome_selected(outcome);
                        return true;
                    case R.string.menu_outcome_delete:
                        listener.request_delete(outcome);
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
        void outcome_selected(Outcome outcome);
        void request_delete(Outcome outcome);
    }
}