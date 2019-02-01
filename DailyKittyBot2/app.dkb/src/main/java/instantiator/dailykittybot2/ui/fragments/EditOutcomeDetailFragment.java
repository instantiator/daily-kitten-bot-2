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
import instantiator.dailykittybot2.data.OutcomeType;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.ui.viewmodels.EditOutcomeViewModel;
import instantiator.dailykittybot2.validation.OutcomeValidator;
import instantiator.dailykittybot2.validation.ValidationResult;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class EditOutcomeDetailFragment extends AbstractBotFragment<EditOutcomeViewModel, EditOutcomeDetailFragment.Listener> {

    @BindView(R.id.spin_outcome_type) Spinner spin_type;
    @BindView(R.id.text_option_hint) TextView text_hint;
    @BindView(R.id.edit_outcome_modifier) TextInputEditText edit_modifier;

    @BindView(R.id.card_errors) CardView card_errors;
    @BindView(R.id.text_errors) TextView text_errors;
    @BindView(R.id.card_warnings) CardView card_warnings;
    @BindView(R.id.text_warnings) TextView text_warnings;

    private OutcomeValidator validator;

    private boolean watchers_enabled;

    private SpinnerAdapter type_adapter;
    private List<OutcomeType> outcomeTypes;

    public static EditOutcomeDetailFragment create() {
        EditOutcomeDetailFragment fragment = new EditOutcomeDetailFragment();
        // no args - the rule comes from the model - shared with the activity
        return fragment;
    }

    public EditOutcomeDetailFragment() {
        super(true, false);
    }

    @Override
    protected Class<EditOutcomeViewModel> getViewModelClass() {
        return EditOutcomeViewModel.class;
    }

    @Override
    protected void extractArguments(Bundle args) {
        // no args - the rule comes from the model - shared with the activity
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_edit_outcome_detail;
    }

    @Override
    protected boolean initialise() {
        watchers_enabled = true;

        outcomeTypes = Arrays.asList(OutcomeType.values());

        validator = new OutcomeValidator(bot_activity);

        type_adapter = new ArrayAdapter<OutcomeType>(
                bot_activity,
                android.R.layout.simple_list_item_1,
                outcomeTypes);

        spin_type.setAdapter(type_adapter);

        edit_modifier.addTextChangedListener(text_edits_listener);
        spin_type.setOnItemSelectedListener(spinner_listener);

        model.getItem().observe(this, outcome -> {
            watchers_enabled = false;
            updateFromOutcome(outcome);
            watchers_enabled = true;
        });

        return true;
    }

    private void updateFromOutcome(Outcome source) {

        boolean exists = source != null;
        spin_type.setEnabled(exists);
        edit_modifier.setEnabled(exists);
        if (!exists) { return; }

        if (spin_type.getSelectedItemPosition() != outcomeTypes.indexOf(source.type)) {
            spin_type.setSelection(outcomeTypes.indexOf(source.type));
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
        OutcomeType selection = (OutcomeType)spin_type.getSelectedItem();
        text_hint.setText(selection.getHint());
        edit_modifier.setVisibility(selection.requiresSpecifics() ? VISIBLE : GONE);
    }

    private void save() {
        Outcome outcome = model.getItem().getValue();
        outcome.modifier = edit_modifier.getText().toString();
        outcome.type = (OutcomeType) spin_type.getSelectedItem();
        listener.save_outcome_detail_now(outcome);
    }

    @Override
    protected void denitialise() { }

    @Override
    protected void updateUI() { }

    public interface Listener {
        void save_outcome_detail_now(Outcome outcome);
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
