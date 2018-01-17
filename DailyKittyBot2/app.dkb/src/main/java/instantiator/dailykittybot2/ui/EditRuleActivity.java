package instantiator.dailykittybot2.ui;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Rule;
import instantiator.dailykittybot2.ui.fragments.EditRuleConditionsFragment;
import instantiator.dailykittybot2.ui.fragments.EditRuleDetailFragment;
import instantiator.dailykittybot2.ui.fragments.EditRuleOutcomesFragment;
import instantiator.dailykittybot2.ui.pagers.EditRulePagerAdapter;
import instantiator.dailykittybot2.ui.viewmodels.EditRuleViewModel;

public class EditRuleActivity extends AbstractBotActivity<EditRuleViewModel>
        implements EditRuleDetailFragment.Listener, EditRuleConditionsFragment.Listener, EditRuleOutcomesFragment.Listener {

    private static final String KEY_mode = "mode";
    private static final String KEY_rule_id = "rule.id";
    private static final String KEY_username = "rule.username";

    private enum Mode { Create, Edit }

    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.tabs) TabLayout tabs;

    private EditRulePagerAdapter pager_adapter;

    private Mode mode;
    private UUID rule_uuid;
    private String username;

    public EditRuleActivity() {
        super(true, true, false);
    }

    public static Intent create(Context context, String username) {
        Intent intent = new Intent(context, EditRuleActivity.class);
        intent.putExtra(KEY_mode, Mode.Create.name());
        intent.putExtra(KEY_username, username);
        return intent;
    }

    public static Intent edit(Context context, String username, Rule rule) {
        Intent intent = new Intent(context, EditRuleActivity.class);
        intent.putExtra(KEY_mode, Mode.Edit.name());
        intent.putExtra(KEY_rule_id, rule.uuid.toString());
        intent.putExtra(KEY_username, username);
        return intent;
    }

    @Override
    protected Class<EditRuleViewModel> getViewModelClass() {
        return EditRuleViewModel.class;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_edit_rule;
    }

    @Override
    protected void extractArguments(Intent intent) {
        String mode_str = intent.getStringExtra(KEY_mode);
        String rule_id_str = intent.getStringExtra(KEY_rule_id);
        mode = Mode.valueOf(mode_str);
        if (mode == Mode.Edit) {
            rule_uuid = UUID.fromString(rule_id_str);
        }
        username = intent.getStringExtra(KEY_username);
    }

    @Override
    protected void initialise_model() {
        model.init(rule_uuid, username);
    }

    @Override
    protected boolean initialise() {

        if (model.getRule().getValue() == null && mode == Mode.Create) {
            Rule rule = service.create_rule(username, null);
            model.init(rule.uuid, username);
        }

        model.getRule().observe(this, new Observer<Rule>() {
            @Override
            public void onChanged(@Nullable Rule rule) {
                updateTitle(rule);
            }
        });

        pager_adapter = new EditRulePagerAdapter(this, getSupportFragmentManager());
        pager.setAdapter(pager_adapter);
        tabs.setupWithViewPager(pager);
        return true;
    }

    @Override
    protected void denitialise() {
        // NOP
    }

    private void updateTitle(Rule rule) {
        if (!bound) {
            getSupportActionBar().setTitle(R.string.please_wait);
        } else {
            if (rule != null && !StringUtils.isEmpty(rule.rulename)) {
                getSupportActionBar().setTitle(getString(R.string.activity_title_edit_rule_named, rule.rulename));
            } else {
                getSupportActionBar().setTitle(getString(R.string.activity_title_edit_rule));
            }
        }
    }

    @Override
    protected void updateUI() {
        if (model != null && model.getRule() != null) {
            updateTitle(model.getRule().getValue());
        } else {
            updateTitle(null);
        }
    }

    @Override
    public void save_rule_detail_now(Rule rule) {
        service.update_rule(rule);
    }

    @Override
    public void outcome_selected(Outcome outcome) {
        Intent intent = EditOutcomeActivity.edit(this, username, outcome);
        startActivity(intent);
    }

    @Override
    public void request_create_outcome() {
        Intent intent = EditOutcomeActivity.create(this, username, rule_uuid);
        startActivity(intent);
    }

    @Override
    public void condition_selected(Condition condition) {
        Intent intent = EditConditionActivity.edit(this, username, condition);
        startActivity(intent);
    }

    @Override
    public void request_create_condition() {
        Intent intent = EditConditionActivity.create(this, username, model.getRuleId());
        startActivity(intent);
    }

    @Override
    public void request_delete(Condition condition) {
        service.delete_condition(condition);
    }

    @Override
    public void request_delete(Outcome outcome) {
        service.delete_outcome(outcome);
    }

    @Override
    public void request_move_up(Condition condition) {
        List<Condition> conditions = model.getRuleConditions().getValue();
        int index = conditions.indexOf(condition);
        if (index > 0) {
            Condition above = conditions.get(index-1);

            int swap = above.ordering;
            above.ordering = condition.ordering;
            condition.ordering = swap;

            service.update_condition(condition);
            service.update_condition(above);
        }
    }

    @Override
    public void request_move_down(Condition condition) {
        List<Condition> conditions = model.getRuleConditions().getValue();
        int index = conditions.indexOf(condition);
        if (index < conditions.size() - 1) {
            Condition below = conditions.get(index+1);

            int swap = below.ordering;
            below.ordering = condition.ordering;
            condition.ordering = swap;

            service.update_condition(condition);
            service.update_condition(below);
        }
    }
}
