package instatiator.dailykittybot2.ui;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import butterknife.BindView;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.ui.fragments.UserRulesFragment;
import instatiator.dailykittybot2.ui.pagers.UserOverviewPagerAdapter;
import instatiator.dailykittybot2.ui.viewmodels.UserOverviewViewModel;

public class UserOverviewActivity extends AbstractBotActivity<UserOverviewViewModel> implements UserRulesFragment.Listener {
    private static final String EXTRA_username = "username";

    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.tabs) TabLayout tabs;

    private UserOverviewPagerAdapter pager_adapter;

    private String username;

    public UserOverviewActivity() {
        super(true, true, false);
    }

    public static Intent create(Context context, String user) {
        Intent intent = new Intent(context, UserOverviewActivity.class);
        intent.putExtra(EXTRA_username, user);
        return intent;
    }

    @Override
    protected Class<UserOverviewViewModel> getViewModelClass() {
        return UserOverviewViewModel.class;
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
    protected void initialise_model() {
        model.init(username);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, R.string.menu_inject_test_data, 0, R.string.menu_inject_test_data);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.menu_inject_test_data:
                service.injectTestData(username);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void rule_selected(Rule rule) {
        Intent intent = EditRuleActivity.edit(this, username, rule);
        startActivity(intent);
    }

    @Override
    public void request_create_rule() {
        Intent intent = EditRuleActivity.create(this, username);
        startActivity(intent);
    }

}
