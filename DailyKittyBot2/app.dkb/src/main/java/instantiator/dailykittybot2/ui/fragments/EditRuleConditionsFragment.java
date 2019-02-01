package instantiator.dailykittybot2.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;
import butterknife.OnClick;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.ui.adapters.LiveConditionsAdapter;
import instantiator.dailykittybot2.ui.viewmodels.EditRuleViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EditRuleConditionsFragment extends AbstractBotFragment<EditRuleViewModel, EditRuleConditionsFragment.Listener> {

    private LiveConditionsAdapter adapter;

    @BindView(R.id.recycler) public RecyclerView recycler;
    @BindView(R.id.card_no_conditions) public CardView card_no_conditions;
    @BindView(R.id.fab_add_condition) public FloatingActionButton fab_add_condition;

    public static EditRuleConditionsFragment create() {
        EditRuleConditionsFragment fragment = new EditRuleConditionsFragment();
        // no arguments - data can be found in the Activity's EditRuleViewModel
        return fragment;
    }

    public EditRuleConditionsFragment() {
        super(true, false);
    }

    @Override
    protected Class<EditRuleViewModel> getViewModelClass() {
        return EditRuleViewModel.class;
    }

    @Override
    protected void extractArguments(Bundle arguments) {
        // no arguments - data can be found in the Activity's EditRuleViewModel
    }

    @Override
    protected int getLayout() { return R.layout.fragment_edit_rule_conditions; }

    @Override
    protected boolean initialise() {
        adapter = new LiveConditionsAdapter(bot_activity, model.getRuleConditions(), recycler, conditions_listener, card_no_conditions);
        LinearLayoutManager layout = new LinearLayoutManager(bot_activity);
        recycler.setLayoutManager(layout);
        recycler.setAdapter(adapter);
        return true;
    }

    @Override
    protected void denitialise() {
        recycler.setAdapter(null);
    }

    @Override
    protected void updateUI() {
        boolean no_adapter = adapter == null;
        boolean no_items = no_adapter || adapter.getItemCount() == 0;
        card_no_conditions.setVisibility(no_items ? VISIBLE : GONE);
    }

    @OnClick(R.id.fab_add_condition)
    public void add_condition_click() {
        listener.request_create_condition();
    }

    private LiveConditionsAdapter.Listener conditions_listener = new LiveConditionsAdapter.Listener() {
        @Override
        public void condition_selected(Condition condition) {
            listener.condition_selected(condition);
        }

        @Override
        public void request_move_up(Condition condition) {
            listener.request_move_up(condition);
        }

        @Override
        public void request_move_down(Condition condition) {
            listener.request_move_down(condition);
        }

        @Override
        public void request_delete(final Condition condition) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_title_confirm_delete_condition)
                    .setMessage(R.string.dialog_message_confirm_delete_condition)
                    .setPositiveButton(R.string.btn_delete, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        listener.request_delete(condition);
                    })
                    .setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create()
                    .show();
        }
    };

    public interface Listener {
        void condition_selected(Condition condition);
        void request_create_condition();
        void request_move_up(Condition condition);
        void request_move_down(Condition condition);
        void request_delete(Condition condition);
    }
}
