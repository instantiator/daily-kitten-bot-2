package instatiator.dailykittybot2.ui;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.ui.fragments.UserRulesFragment;
import instatiator.dailykittybot2.ui.pagers.UserOverviewPagerAdapter;

public class UserOverviewActivity extends AbstractBotActivity implements UserRulesFragment.Listener {
    private static final String EXTRA_username = "username";

    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.tabs) TabLayout tabs;

    private String username;
    private UserOverviewPagerAdapter pager_adapter;

    public UserOverviewActivity() {
        super(true, true, false);
    }

    public static Intent create(Context context, String user) {
        Intent intent = new Intent(context, UserOverviewActivity.class);
        intent.putExtra(EXTRA_username, user);
        return intent;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_user_overview;
    }

    @Override
    protected void extractArguments(Intent intent) {
        username = intent.getStringExtra(EXTRA_username);
    }

    @Override
    protected boolean initialise() {
        if (username != null) {
            pager_adapter = new UserOverviewPagerAdapter(this, getSupportFragmentManager(), username);
            pager.setAdapter(pager_adapter);
            tabs.setupWithViewPager(pager);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void denitialise() {
        // NOP
    }

    @Override
    protected void updateUI() {
        if (bound) {
            String title = getString(R.string.activity_title_overview_for, username);
            getSupportActionBar().setTitle(title);
        } else {
            getSupportActionBar().setTitle(R.string.please_wait);
        }
    }

    @Override
    public void rule_selected(Rule rule) {
        informUser("User rule selected.");
    }

    @Override
    public void request_create_rule() {
        Intent intent = EditRuleActivity.create(this, username);
        startActivity(intent);
    }
}
