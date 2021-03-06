package instantiator.dailykittybot2.ui;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;

import java.util.UUID;

import butterknife.BindView;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.ui.fragments.EditConditionDetailFragment;
import instantiator.dailykittybot2.ui.viewmodels.EditConditionViewModel;

public class EditConditionActivity extends AbstractBotActivity<EditConditionViewModel>
    implements EditConditionDetailFragment.Listener {

    private static final String KEY_mode = "mode";
    private static final String KEY_condition_id = "condition.id";
    private static final String KEY_rule_id = "rule.id";
    private static final String KEY_username = "username";

    private enum Mode { Edit }

    private Mode mode;
    private UUID condition_id;
    private UUID rule_id;
    private String username;

    @BindView(R.id.fragment_frame) FrameLayout fragment_frame;

    public EditConditionActivity() {
        super(true, true, false);
    }

    public static Intent edit(Context context, String username, Condition condition) {
        Intent intent = new Intent(context, EditConditionActivity.class);
        intent.putExtra(KEY_mode, Mode.Edit.name());
        intent.putExtra(KEY_condition_id, condition.uuid.toString());
        intent.putExtra(KEY_rule_id, condition.ruleUuid.toString());
        intent.putExtra(KEY_username, username);
        return intent;
    }

    @Override
    protected void onBoundChanged(boolean isBound) {
        super.onBoundChanged(isBound);
    }

    @Override
    protected Class<EditConditionViewModel> getViewModelClass() {
        return EditConditionViewModel.class;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_edit_condition;
    }

    @Override
    protected void extractArguments(Intent intent) {
        String mode_str = intent.getStringExtra(KEY_mode);
        String rule_id_str = intent.getStringExtra(KEY_rule_id);
        String condition_id_str = intent.getStringExtra(KEY_condition_id);
        mode = Mode.valueOf(mode_str);
        if (mode == Mode.Edit) {
            condition_id = UUID.fromString(condition_id_str);
        }
        rule_id = UUID.fromString(rule_id_str);
        username = intent.getStringExtra(KEY_username);
    }

    @Override
    protected void initialise_model() {
        model.init(condition_id, username);
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    protected boolean initialise() {

        model.getItem().observe(this, new Observer<Condition>() {
            @Override
            public void onChanged(@Nullable Condition condition) {
                updateTitle(condition);
            }
        });

        EditConditionDetailFragment edit_fragment = new EditConditionDetailFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_frame, edit_fragment)
                .commit();

        return true;
    }

    @Override
    protected void denitialise() {
        fragment_frame.removeAllViews();
    }

    private void updateTitle(Condition condition) {
        if (!bound) {
            getSupportActionBar().setTitle(R.string.please_wait);
        } else {
            getSupportActionBar().setTitle(getString(R.string.activity_title_edit_condition));
        }
    }

    @Override
    protected void updateUI() {
        if (model != null && model.getItem() != null) {
            updateTitle(model.getItem().getValue());
        } else {
            updateTitle(null);
        }
    }

    @Override
    public void save_condition_detail_now(Condition condition) {
        service.update_condition(condition);
    }

}
