package instantiator.dailykittybot2.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import butterknife.BindView;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.ui.viewmodels.EditRuleViewModel;
import instantiator.dailykittybot2.util.ListUtils;
import instantiator.dailykittybot2.validation.RuleValidator;
import instantiator.dailykittybot2.validation.ValidationResult;
import me.gujun.android.taggroup.TagGroup;

public class EditRuleDetailFragment extends AbstractBotFragment<EditRuleViewModel, EditRuleDetailFragment.Listener> {

    @BindView(R.id.edit_rule_name) TextInputEditText edit_name;
    @BindView(R.id.edit_rule_subreddits) TagGroup edit_subreddits;
    @BindView(R.id.edit_rule_autorun) CheckBox edit_autorun;

    @BindView(R.id.card_rule_errors) CardView card_rule_errors;
    @BindView(R.id.text_rule_errors) TextView text_rule_errors;
    @BindView(R.id.card_rule_warnings) CardView card_rule_warnings;
    @BindView(R.id.text_rule_warnings) TextView text_rule_warnings;

    private boolean watchers_enabled;

    private RuleValidator validator;

    public static EditRuleDetailFragment create() {
        EditRuleDetailFragment fragment = new EditRuleDetailFragment();
        // no args - the rule comes from the model - shared with the activity
        return fragment;
    }

    public EditRuleDetailFragment() {
        super(true, false);
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

        validator = new RuleValidator(bot_activity);

        model.getRule().observe(this, rule -> {
            watchers_enabled = false;
            updateFromRule(rule);
            watchers_enabled = true;
        });

        model.getRuleConditions().observe(this, conditions -> {
            updateValidationContent();
        });

        model.getRuleOutcomes().observe(this, outcomes -> {
            updateValidationContent();
        });

        return true;
    }

    private void updateFromRule(Rule source) {
        boolean exists = source != null;
        edit_name.setEnabled(exists);
        edit_autorun.setEnabled(exists);
        edit_subreddits.setEnabled(exists);

        if (!exists) { return; }

        int selection_start = edit_name.getSelectionStart();
        int selection_end = edit_name.getSelectionEnd();

        if (!StringUtils.equals(edit_name.getText().toString(), source.rulename)) {
            edit_name.setText(source.rulename);
            edit_name.setSelection(selection_start, selection_end);
        }

        if (edit_autorun.isChecked() != source.run_periodically) {
            edit_autorun.setChecked(source.run_periodically);
        }

        if (!ListUtils.sameStrings(edit_subreddits.getTags(), source.subreddits)) {
            View focussed = bot_activity.getCurrentFocus();

            edit_subreddits.setTags(source.subreddits);

            focussed.requestFocus();
            if (focussed == edit_name) {
                edit_name.setSelection(selection_start, selection_end);
            }
        }

        // update validation content
        updateValidationContent();
    }

    private void updateValidationContent() {
        RuleTriplet triplet = new RuleTriplet();
        triplet.rule = model.getRule().getValue();
        triplet.conditions = model.getRuleConditions().getValue();
        triplet.outcomes = model.getRuleOutcomes().getValue();

        ValidationResult result = validator.validate(triplet);
        result.updateUI(
                card_rule_errors, text_rule_errors,
                card_rule_warnings, text_rule_warnings);
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
