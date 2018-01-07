package instatiator.dailykittybot2.ui.fragments;

import android.os.Bundle;
import android.util.Log;

import java.util.UUID;

import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.service.BotsWorkspace;

public class EditRuleDetailFragment extends AbstractBotFragment<EditRuleDetailFragment.Listener> {

    private static final String KEY_rule_id = "rule.id";

    UUID rule_id;
    Rule edit_rule;

    public static EditRuleDetailFragment create(UUID id) {
        EditRuleDetailFragment fragment = new EditRuleDetailFragment();
        Bundle args = new Bundle();
        args.putString(KEY_rule_id, id.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public EditRuleDetailFragment() {
        super(false, false); // TODO
    }

    @Override
    protected void extractArguments(Bundle args) {
        String rule_id_str = args.getString(KEY_rule_id);
        rule_id = UUID.fromString(rule_id_str);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_edit_rule_detail;
    }

    @Override
    protected boolean initialise() {
        BotsWorkspace workspace = service.get_workspace();
        edit_rule = workspace.get_rule(rule_id);
        return true;
    }

    @Override
    protected void denitialise() {

    }

    @Override
    protected void updateUI() {

    }

    public interface Listener {
        void save_selected(Rule rule);
    }
}
