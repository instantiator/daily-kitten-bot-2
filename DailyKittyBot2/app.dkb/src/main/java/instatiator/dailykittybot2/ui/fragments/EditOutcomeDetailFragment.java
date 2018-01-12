package instatiator.dailykittybot2.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.data.ConditionType;
import instatiator.dailykittybot2.db.data.OutcomeType;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.ui.viewmodels.EditConditionViewModel;
import instatiator.dailykittybot2.ui.viewmodels.EditOutcomeViewModel;

public class EditOutcomeDetailFragment extends AbstractBotFragment<EditOutcomeViewModel, EditOutcomeDetailFragment.Listener> {

    @BindView(R.id.spin_outcome_type) Spinner spin_type;
    @BindView(R.id.edit_outcome_modifier) TextInputEditText edit_modifier;

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
        View had_focus = getView().findFocus();
        int selection_start = 0;
        int selection_end = 0;
        if (had_focus == edit_modifier) {
            selection_start = edit_modifier.getSelectionStart();
            selection_end = edit_modifier.getSelectionEnd();
        }

        if (source != null) {
            spin_type.setSelection(outcomeTypes.indexOf(source.type));
            edit_modifier.setText(source.modifier);
        }

        if (had_focus != null) {
            had_focus.requestFocus();
            if (had_focus == edit_modifier) {
                if (selection_start > edit_modifier.getText().length() ||
                        selection_end > edit_modifier.getText().length()) {
                    edit_modifier.setSelection(edit_modifier.getText().length());
                } else {
                    edit_modifier.setSelection(selection_start, selection_end);
                }
            }
        }
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
        @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) { if (watchers_enabled) { save(); } }
        @Override public void onNothingSelected(AdapterView<?> adapterView) { if (watchers_enabled) { save(); } }
    };

    private TextWatcher text_edits_listener = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override public void afterTextChanged(Editable editable) { if (watchers_enabled) { save(); } }
    };

}
