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
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.ui.adapters.LiveOutcomesAdapter;
import instantiator.dailykittybot2.ui.viewmodels.EditRuleViewModel;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EditRuleOutcomesFragment extends AbstractBotFragment<EditRuleViewModel, EditRuleOutcomesFragment.Listener> {

    private LiveOutcomesAdapter adapter;

    @BindView(R.id.recycler) public RecyclerView recycler;
    @BindView(R.id.card_no_outcomes) public CardView card_no_outcomes;
    @BindView(R.id.fab_add_outcome) public FloatingActionButton fab_add_outcome;

    public static EditRuleOutcomesFragment create() {
        EditRuleOutcomesFragment fragment = new EditRuleOutcomesFragment();
        // no arguments - data can be found in the Activity's EditRuleViewModel
        return fragment;
    }

    public EditRuleOutcomesFragment() {
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
    protected int getLayout() { return R.layout.fragment_edit_rule_outcomes; }

    @Override
    protected boolean initialise() {
        adapter = new LiveOutcomesAdapter(bot_activity, model.getRuleOutcomes(), recycler, outcomes_listener, card_no_outcomes);
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
        card_no_outcomes.setVisibility(no_items ? VISIBLE : GONE);
    }

    @OnClick(R.id.fab_add_outcome)
    public void add_condition_click() {
        listener.request_create_outcome();
    }

    private LiveOutcomesAdapter.Listener outcomes_listener = new LiveOutcomesAdapter.Listener() {
        @Override
        public void outcome_selected(Outcome outcome) {
            listener.outcome_selected(outcome);
        }

        @Override
        public void request_delete(final Outcome outcome) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_title_confirm_delete_outcome)
                    .setMessage(R.string.dialog_message_confirm_delete_outcome)
                    .setPositiveButton(R.string.btn_delete, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        listener.request_delete(outcome);
                    })
                    .setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create()
                    .show();
        }
    };

    public interface Listener {
        void outcome_selected(Outcome outcome);
        void request_create_outcome();
        void request_delete(Outcome outcome);
    }
}
