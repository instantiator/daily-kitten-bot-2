package instatiator.dailykittybot2.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.Arrays;

import butterknife.BindView;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.ui.viewmodels.EditRuleViewModel;
import me.gujun.android.taggroup.TagGroup;

public class EditRuleDetailFragment extends AbstractBotFragment<EditRuleViewModel, EditRuleDetailFragment.Listener> {

    @BindView(R.id.edit_rule_name) TextInputEditText edit_name;
    @BindView(R.id.edit_rule_subreddits) TagGroup edit_subreddits;
    @BindView(R.id.edit_rule_autorun) CheckBox edit_autorun;

    private boolean watchers_enabled;

    public static EditRuleDetailFragment create() {
        EditRuleDetailFragment fragment = new EditRuleDetailFragment();
        // no args - the rule comes from the model - shared with the activity
        return fragment;
    }

    public EditRuleDetailFragment() {
        super(true, false); // TODO
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
        watchers_enabled = true;

        edit_name.addTextChangedListener(text_edits_listener);
        edit_subreddits.setOnTagChangeListener(subreddits_change_listener);
        edit_autorun.setOnCheckedChangeListener(autorun_change_listener);

        model.getRule().observe(this, rule -> {
            watchers_enabled = false;
            updateFromRule(rule);
            watchers_enabled = true;
        });

        return true;
    }

    private void updateFromRule(Rule source) {
        edit_name.setText(source.rulename);
        edit_subreddits.setTags(source.subreddits);
        edit_autorun.setChecked(source.run_periodically);
    }

    private void save() {
        Rule rule = model.getRule().getValue();
        rule.rulename = edit_name.getText().toString();
        rule.subreddits = Arrays.asList(edit_subreddits.getTags());
        rule.run_periodically = edit_autorun.isChecked();
        listener.save_rule_detail_now(rule);
    }

    @Override
    protected void denitialise() { }

    @Override
    protected void updateUI() { }

    public interface Listener {
        void save_rule_detail_now(Rule rule);
    }

    private CheckBox.OnCheckedChangeListener autorun_change_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) { if (watchers_enabled) { save(); } }
    };

    private TextWatcher text_edits_listener = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override public void afterTextChanged(Editable editable) { if (watchers_enabled) { save(); } }
    };

    private TagGroup.OnTagChangeListener subreddits_change_listener = new TagGroup.OnTagChangeListener() {
        @Override public void onAppend(TagGroup tagGroup, String tag) { if (watchers_enabled) { save(); } }
        @Override public void onDelete(TagGroup tagGroup, String tag) { if (watchers_enabled) { save(); } }
    };
}
