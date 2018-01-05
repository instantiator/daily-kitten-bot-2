package instatiator.dailykittybot2.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.BotApp;
import instatiator.dailykittybot2.R;

public class UserRulesOverviewActivity extends AbstractBotActivity {
    private static final String EXTRA_username = "username";

    @BindView(R.id.recycler) RecyclerView recycler;

    private String username;

    public UserRulesOverviewActivity() {
        super(true, true, false);
    }

    public static Intent create(Context context, String user) {
        Intent intent = new Intent(context, UserRulesOverviewActivity.class);
        intent.putExtra(EXTRA_username, user);
        return intent;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_user_rules_overview;
    }

    @Override
    protected void extractArguments(Intent intent) {
        username = intent.getStringExtra(EXTRA_username);
    }

    @Override
    protected void initialise() {

    }

    @Override
    protected void denitialise() {

    }

    @Override
    protected void updateUI() {
        if (bound) {
            String title = getString(R.string.activity_title_rules_overview_for, username);
            getSupportActionBar().setTitle(title);
        } else {
            getSupportActionBar().setTitle(R.string.please_wait);
        }
    }

}
