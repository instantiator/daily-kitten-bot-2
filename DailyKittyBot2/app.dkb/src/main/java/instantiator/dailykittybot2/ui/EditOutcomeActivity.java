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
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.ui.fragments.EditOutcomeDetailFragment;
import instantiator.dailykittybot2.ui.viewmodels.EditOutcomeViewModel;

public class EditOutcomeActivity extends AbstractBotActivity<EditOutcomeViewModel>
        implements EditOutcomeDetailFragment.Listener {

    private static final String KEY_mode = "mode";
    private static final String KEY_outcome_id = "outcome.id";
    private static final String KEY_rule_id = "rule.id";
    private static final String KEY_username = "username";

    private enum Mode { Create, Edit }

    private Mode mode;
    private UUID outcome_id;
    private UUID rule_id;
    private String username;

    @BindView(R.id.fragment_frame) FrameLayout fragment_frame;

    public EditOutcomeActivity() {
        super(true, true, false);
    }

    public static Intent create(Context context, String username, UUID rule_id) {
        Intent intent = new Intent(context, EditOutcomeActivity.class);
        intent.putExtra(KEY_mode, Mode.Create.name());
        intent.putExtra(KEY_rule_id, rule_id.toString());
        intent.putExtra(KEY_username, username);
        return intent;
    }

    public static Intent edit(Context context, String username, Outcome outcome) {
        Intent intent = new Intent(context, EditOutcomeActivity.class);
        intent.putExtra(KEY_mode, Mode.Edit.name());
        intent.putExtra(KEY_outcome_id, outcome.uuid.toString());
        intent.putExtra(KEY_rule_id, outcome.ruleUuid.toString());
        intent.putExtra(KEY_username, username);
        return intent;
    }

    @Override
    protected void onBoundChanged(boolean isBound) {
        super.onBoundChanged(isBound);
    }

    @Override
    protected Class<EditOutcomeViewModel> getViewModelClass() {
        return EditOutcomeViewModel.class;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_edit_outcome;
    }

    @Override
    protected void extractArguments(Intent intent) {
        String mode_str = intent.getStringExtra(KEY_mode);
        String rule_id_str = intent.getStringExtra(KEY_rule_id);
        String outcome_id_str = intent.getStringExtra(KEY_outcome_id);
        mode = Mode.valueOf(mode_str);
        if (mode == Mode.Edit) {
            outcome_id = UUID.fromString(outcome_id_str);
        }
        rule_id = UUID.fromString(rule_id_str);
        username = intent.getStringExtra(KEY_username);
    }

    @Override
    protected void initialise_model() {
        model.init(outcome_id, username);
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    protected boolean initialise() {

        if (model.getItem().getValue() == null && mode == Mode.Create) {
            Outcome outcome = service.create_outcome(rule_id);
            model.init(outcome.uuid, username);
        }

        model.getItem().observe(this, new Observer<Outcome>() {
            @Override
            public void onChanged(@Nullable Outcome outcome) {
                updateTitle(outcome);
            }
        });

        EditOutcomeDetailFragment edit_fragment = new EditOutcomeDetailFragment();
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

    private void updateTitle(Outcome outcome) {
        if (!bound) {
            getSupportActionBar().setTitle(R.string.please_wait);
        } else {
            getSupportActionBar().setTitle(getString(R.string.activity_title_edit_outcome));
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
    public void save_outcome_detail_now(Outcome outcome) {
        service.update_outcome(outcome);
    }

}
