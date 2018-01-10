package instatiator.dailykittybot2.ui.fragments;

import android.os.Bundle;

import java.util.UUID;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.ui.viewmodels.EditRuleViewModel;

public class EditRuleDetailFragment extends AbstractBotFragment<EditRuleViewModel, EditRuleDetailFragment.Listener> {

    public static EditRuleDetailFragment create() {
        EditRuleDetailFragment fragment = new EditRuleDetailFragment();
        // no args - the rule comes from the model - shared with the activity
        return fragment;
    }

    public EditRuleDetailFragment() {
        super(false, false); // TODO
    }

    @Override
    protected Class<EditRuleViewModel> getViewModelClass() {
        return EditRuleViewModel.class;
    }

    @Override
    protected void extractArguments(Bundle args) {
        // no args - the rule comes from the model - shared with the activity
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_edit_rule_detail;
    }

    @Override
    protected boolean initialise() {
        // rule comes from the model
        // observe the rule in case it changes or something
        return true;
    }

    @Override
    protected void denitialise() { }

    @Override
    protected void updateUI() {

    }

    public interface Listener {
        void save_selected(Rule rule);
    }
}
