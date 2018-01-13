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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.validation.ConditionValidator;
import instatiator.dailykittybot2.validation.ValidationResult;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LiveConditionsAdapter extends RecyclerView.Adapter<LiveConditionsAdapter.ConditionHolder> {

    private final AppCompatActivity activity;
    private List<Condition> conditions;
    private RecyclerView recyclerView;
    private Listener listener;
    private CardView empty_card;

    private ConditionValidator validator;

    public LiveConditionsAdapter(AppCompatActivity activity, LiveData<List<Condition>> live_conditions, RecyclerView recyclerView, Listener listener, CardView empty_card) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.empty_card = empty_card;
        this.validator = new ConditionValidator(activity);

        this.conditions = live_conditions.getValue();

        update_empty_card();

        live_conditions.observe(activity, new Observer<List<Condition>>() {
            @Override
            public void onChanged(@Nullable List<Condition> conditions) {
                LiveConditionsAdapter.this.conditions = conditions;
                notifyDataSetChanged();
                update_empty_card();
            }
        });
    }

    private void update_empty_card() {
        boolean empty = conditions == null || conditions.size() == 0;
        empty_card.setVisibility(empty ? VISIBLE : GONE);
    }

    @Override
    public ConditionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_condition, null);
        return new ConditionHolder(view);
    }

    @Override
    public void onBindViewHolder(ConditionHolder holder, int position) {
        Condition condition = conditions.get(position);
        holder.condition = condition;
        holder.text_condition_name.setText(activity.getString(condition.type.getDescription()));
        holder.text_condition_summary.setText(condition.modifier);

        ValidationResult result = validator.validate(condition);

        holder.icon_error.setVisibility(result.errors.size() > 0 ? VISIBLE : GONE);
        holder.icon_warning.setVisibility(result.warnings.size() > 0 ? VISIBLE : GONE);
    }

    @Override
    public int getItemCount() {
        return conditions != null ? conditions.size() : 0;
    }

    public class ConditionHolder extends RecyclerView.ViewHolder {
        public Condition condition;

        @BindView(R.id.text_condition_name) public TextView text_condition_name;
        @BindView(R.id.text_condition_summary)public TextView text_condition_summary;
        @BindView(R.id.icon_warning) public ImageView icon_warning;
        @BindView(R.id.icon_error) public ImageView icon_error;

        public ConditionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.condition_selected(condition);
                }
            });
        }
    }

    public interface Listener {
        void condition_selected(Condition cOndition);
    }
}