package instantiator.dailykittybot2.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.service.execution.RuleExecutor;
import instantiator.dailykittybot2.ui.adapters.LiveRulesAdapter;
import instantiator.dailykittybot2.ui.viewmodels.UserOverviewViewModel;
import instantiator.dailykittybot2.validation.RuleValidator;
import instantiator.dailykittybot2.validation.ValidationResult;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class UserRulesFragment extends AbstractBotFragment<UserOverviewViewModel, UserRulesFragment.Listener> {

    private static final String KEY_username = "username";

    private LiveRulesAdapter adapter;

    @BindView(R.id.recycler) public RecyclerView recycler;
    @BindView(R.id.card_no_rules) public CardView card_no_rules;
    @BindView(R.id.fab_add_rule) public FloatingActionButton fab_add_rule;

    public static UserRulesFragment create() {
        return new UserRulesFragment();
    }

    public UserRulesFragment() {
        super(true, false);
    }

    @Override
    protected Class<UserOverviewViewModel> getViewModelClass() {
        return UserOverviewViewModel.class;
    }

    @Override
    protected void extractArguments(Bundle arguments) {
        // NOP
    }

    @Override
    protected int getLayout() { return R.layout.fragment_user_rules; }

    @Override
    protected boolean initialise() {
        adapter = new LiveRulesAdapter(bot_activity, model.getTriplets(), recycler, rules_listener, card_no_rules);
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
        card_no_rules.setVisibility(no_items ? VISIBLE : GONE);
    }

    @OnClick(R.id.fab_add_rule)
    public void add_rule_click() {
        listener.request_create_rule();
    }

    private LiveRulesAdapter.Listener rules_listener = new LiveRulesAdapter.Listener() {
        @Override
        public void rule_selected(Rule rule) {
            listener.rule_selected(rule);
        }

        @Override
        public void request_delete(Rule rule) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_title_confirm_delete_rule)
                    .setMessage(R.string.dialog_message_confirm_delete_rule)
                    .setPositiveButton(R.string.btn_delete, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        listener.request_delete(rule);
                    })
                    .setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create()
                    .show();
        }

        @Override
        public void request_forget_rule_reports(Rule rule) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_title_confirm_forget_rule_run_reports)
                    .setMessage(R.string.dialog_message_confirm_forget_rule_run_reports)
                    .setPositiveButton(R.string.btn_forget, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        listener.request_forget_run_reports(rule);
                    })
                    .setNegativeButton(R.string.btn_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create()
                    .show();
        }

        @Override
        public void request_run(RuleTriplet triplet) {
            RuleValidator validator = new RuleValidator(bot_activity);
            ValidationResult result = validator.validate(triplet);
            if (result.validates) {
                confirm_run(triplet);
            } else {
                warn_about_validations(triplet, result);
            }
        }

        private void warn_about_validations(RuleTriplet triplet, ValidationResult result) {
            String message = bot_activity.getString(
                    R.string.dialog_message_warn_cannot_run_failed_validation,
                    result.errors.size(),
                    result.warnings.size(),
                    triplet.rule.rulename);

            new MaterialDialog.Builder(bot_activity)
                    .title(R.string.dialog_title_warn_cannot_run_failed_validation)
                    .content(message)
                    .icon(bot_activity.getDrawable(R.drawable.ic_warning_black_24dp))
                    .positiveText(R.string.btn_close)
                    .show();
        }

        private void confirm_run(RuleTriplet triplet) {
            final RuleExecutor.ExecutionMode[] run_modes;
            int message;
            int preselect_index;
            if (triplet.rule.last_run_hint != null) {
                run_modes = RuleExecutor.ExecutionMode.all();
                message = R.string.dialog_message_confirm_manual_rule_run_has_previous;
                preselect_index = 0;
            } else {
                run_modes = RuleExecutor.ExecutionMode.allWithoutRespect();
                message = R.string.dialog_message_confirm_manual_rule_run_no_previous;
                preselect_index = -1;
            }

            new MaterialDialog.Builder(bot_activity)
                    .title(R.string.dialog_title_confirm_manual_rule_run)
                    .content(message)
                    .items(Arrays.asList(run_modes))
                    .itemsCallbackSingleChoice(preselect_index, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            RuleExecutor.ExecutionMode mode = run_modes[which];
                            listener.request_run(triplet, mode);
                            return true;
                        }
                    })
                    .positiveText(R.string.btn_run)
                    .negativeText(R.string.btn_cancel)
                    .show();
        }
    };

    public interface Listener {
        void rule_selected(Rule rule);
        void request_create_rule();
        void request_delete(Rule rule);
        void request_run(RuleTriplet rule, RuleExecutor.ExecutionMode mode);
        void request_forget_run_reports(Rule rule);
    }
}
