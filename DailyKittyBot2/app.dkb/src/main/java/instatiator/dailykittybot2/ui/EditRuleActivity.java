package instatiator.dailykittybot2.ui;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.service.BotsWorkspace;
import instatiator.dailykittybot2.ui.pagers.EditRulePagerAdapter;
import instatiator.dailykittybot2.ui.pagers.UserOverviewPagerAdapter;

public class EditRuleActivity extends AbstractBotActivity {

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

    private Rule edit_rule;

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
    protected boolean initialise() {
        BotsWorkspace workspace = service.get_workspace();

        if (rule_uuid == null) {
            edit_rule = workspace.create_rule(username, null);
            rule_uuid = edit_rule.uuid;
        } else {
            edit_rule = workspace.get_rule(rule_uuid);
        }


        boolean rule_ok = check_rule_ok(edit_rule);

        if (!rule_ok) {
            Log.w(TAG, "Attempt to read rule for wrong user.");
            informUser(R.string.toast_warning_rule_not_for_you);
            finish();
            return false;
        }

        pager_adapter = new EditRulePagerAdapter(this, getSupportFragmentManager(), rule_uuid);
        pager.setAdapter(pager_adapter);
        tabs.setupWithViewPager(pager);
        return true;
    }

    private boolean check_rule_ok(Rule rule) {
        boolean rule_not_null = rule != null;
        boolean rule_username_ok = rule_not_null && edit_rule.username.equals(username);
        return
            rule_not_null &&
            rule_username_ok;
    }

    @Override
    protected void denitialise() {
        // NOP
    }

    @Override
    protected void updateUI() {
        if (bound) {
            String title = getString(R.string.activity_title_edit_rule);
            getSupportActionBar().setTitle(title);
        } else {
            getSupportActionBar().setTitle(R.string.please_wait);
        }

    }
}
