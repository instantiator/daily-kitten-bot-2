package instatiator.dailykittybot2.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.ui.adapters.LiveOutcomesAdapter;
import instatiator.dailykittybot2.ui.adapters.LiveRulesAdapter;
import instatiator.dailykittybot2.ui.viewmodels.EditRuleViewModel;
import instatiator.dailykittybot2.validation.OutcomeValidator;

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
    };

    public interface Listener {
        void outcome_selected(Outcome outcome);
        void request_create_outcome();
    }
}
