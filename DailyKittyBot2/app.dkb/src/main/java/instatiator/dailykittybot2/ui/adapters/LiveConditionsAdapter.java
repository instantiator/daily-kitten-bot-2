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
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.util.ColourConventions;
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
    private ColourConventions colours;
    private ConditionValidator validator;

    public LiveConditionsAdapter(AppCompatActivity activity, LiveData<List<Condition>> live_conditions, RecyclerView recyclerView, Listener listener, CardView empty_card) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.empty_card = empty_card;
        this.validator = new ConditionValidator(activity);
        this.colours = new ColourConventions(activity);
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

        boolean errors = result.errors.size() > 0;
        boolean warnings = result.warnings.size() > 0;
        holder.icon_alert.setVisibility(errors || warnings ? VISIBLE : GONE);
        holder.icon_alert.getDrawable().setTint(colours.icon_alert(errors, warnings));
    }

    @Override
    public int getItemCount() {
        return conditions != null ? conditions.size() : 0;
    }

    public class ConditionHolder extends RecyclerView.ViewHolder {
        public Condition condition;

        public PopupMenu popup;

        @BindView(R.id.text_condition_name) public TextView text_condition_name;
        @BindView(R.id.text_condition_summary)public TextView text_condition_summary;
        @BindView(R.id.icon_alert) public ImageView icon_alert;

        @BindView(R.id.icon_move_up) public ImageView icon_move_up;
        @BindView(R.id.icon_move_down) public ImageView icon_move_down;
        @BindView(R.id.icon_menu) public ImageView icon_menu;

        public ConditionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(view -> listener.condition_selected(condition));

            popup = new PopupMenu(icon_menu.getContext(), icon_menu);
            popup.getMenu().add(0, R.string.menu_condition_view, 0, R.string.menu_condition_view);
            popup.getMenu().add(0, R.string.menu_condition_delete, 0, R.string.menu_condition_delete);
            popup.getMenu().add(0, R.string.menu_condition_move_up, 0, R.string.menu_condition_move_up);
            popup.getMenu().add(0, R.string.menu_condition_move_down, 0, R.string.menu_condition_move_down);

            popup.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.string.menu_condition_view:
                        listener.condition_selected(condition);
                        return true;
                    case R.string.menu_condition_move_up:
                        listener.request_move_up(condition);
                        return true;
                    case R.string.menu_condition_move_down:
                        listener.request_move_down(condition);
                        return true;
                    case R.string.menu_condition_delete:
                        listener.request_delete(condition);
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

        @OnClick(R.id.icon_move_up)
        public void up_click() {
            listener.request_move_up(condition);
        }

        @OnClick(R.id.icon_move_down)
        public void down_click() {
            listener.request_move_down(condition);
        }
    }

    public interface Listener {
        void condition_selected(Condition condition);
        void request_move_up(Condition condition);
        void request_move_down(Condition condition);
        void request_delete(Condition condition);
    }
}