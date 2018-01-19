package instantiator.dailykittybot2.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.ConditionType;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.ui.viewmodels.EditConditionViewModel;
import instantiator.dailykittybot2.validation.ConditionValidator;
import instantiator.dailykittybot2.validation.ValidationResult;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EditConditionDetailFragment extends AbstractBotFragment<EditConditionViewModel, EditConditionDetailFragment.Listener> {

    @BindView(R.id.spin_condition_type) Spinner spin_type;
    @BindView(R.id.edit_condition_modifier) TextInputEditText edit_modifier;
    @BindView(R.id.text_option_hint) TextView text_hint;

    @BindView(R.id.card_errors) CardView card_errors;
    @BindView(R.id.text_errors) TextView text_errors;
    @BindView(R.id.card_warnings) CardView card_warnings;
    @BindView(R.id.text_warnings) TextView text_warnings;

    private ConditionValidator validator;

    private boolean watchers_enabled;

    private SpinnerAdapter type_adapter;
    private List<ConditionType> conditionTypes;

    public static EditConditionDetailFragment create() {
        EditConditionDetailFragment fragment = new EditConditionDetailFragment();
        // no args - the rule comes from the model - shared with the activity
        return fragment;
    }

    public EditConditionDetailFragment() {
        super(true, false);
    }

    @Override
    protected Class<EditConditionViewModel> getViewModelClass() {
        return EditConditionViewModel.class;
    }

    @Override
    protected void extractArguments(Bundle args) {
        // no args - the rule comes from the model - shared with the activity
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_edit_condition_detail;
    }

    @Override
    protected boolean initialise() {
        watchers_enabled = true;

        validator = new ConditionValidator(bot_activity);

        conditionTypes = Arrays.asList(ConditionType.values());

        type_adapter = new ArrayAdapter<ConditionType>(
                bot_activity,
                android.R.layout.simple_list_item_1,
                conditionTypes);

        spin_type.setAdapter(type_adapter);

        edit_modifier.addTextChangedListener(text_edits_listener);
        spin_type.setOnItemSelectedListener(spinner_listener);

        model.getItem().observe(this, condition -> {
            watchers_enabled = false;
            updateFromCondition(condition);
            watchers_enabled = true;
        });

        return true;
    }

    private void updateFromCondition(Condition source) {
        boolean exists = source != null;
        spin_type.setEnabled(exists);
        edit_modifier.setEnabled(exists);
        if (!exists) { return; }

        if (spin_type.getSelectedItemPosition() != conditionTypes.indexOf(source.type)) {
            spin_type.setSelection(conditionTypes.indexOf(source.type));
        }

        if (!StringUtils.equals(edit_modifier.getText().toString(), source.modifier)) {
            int selection_start = edit_modifier.getSelectionStart();
            int selection_end = edit_modifier.getSelectionEnd();
            edit_modifier.setText(source.modifier);
            edit_modifier.setSelection(selection_start, selection_end);
        }

        // update validation content
        ValidationResult result = validator.validate(source);
        result.updateUI(
                card_errors, text_errors,
                card_warnings, text_warnings);

        //updateHintAndModifier();
    }

    private void updateHintAndModifier() {
        ConditionType selection = (ConditionType)spin_type.getSelectedItem();
        text_hint.setText(selection.getHint());
        edit_modifier.setVisibility(selection.requiresSpecifics() ? VISIBLE : GONE);
    }

    private void save() {
        Condition condition = model.getItem().getValue();
        if (condition != null) {
            condition.modifier = edit_modifier.getText().toString();
            condition.type = (ConditionType) spin_type.getSelectedItem();
            listener.save_condition_detail_now(condition);
        }
    }

    @Override
    protected void denitialise() { }

    @Override
    protected void updateUI() { }

    public interface Listener {
        void save_condition_detail_now(Condition condition);
    }

    private Spinner.OnItemSelectedListener spinner_listener = new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (watchers_enabled) { save(); }
            updateHintAndModifier();
        }
        @Override public void onNothingSelected(AdapterView<?> adapterView) {
            if (watchers_enabled) { save(); }
            updateHintAndModifier();
        }
    };

    private TextWatcher text_edits_listener = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override public void afterTextChanged(Editable editable) { if (watchers_enabled) { save(); } }
    };

}
