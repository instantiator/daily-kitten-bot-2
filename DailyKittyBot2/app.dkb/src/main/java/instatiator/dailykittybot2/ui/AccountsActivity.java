package instatiator.dailykittybot2.ui;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.events.BotServiceStateEvent;
import instatiator.dailykittybot2.service.IBotService;
import instatiator.dailykittybot2.ui.adapters.AuthDataAdapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AccountsActivity extends AbstractBotActivity {
    private static final String TAG = AccountsActivity.class.getName();
    private static final int RC_AddAccount = 1001;

    @BindView(R.id.recycler) public RecyclerView recycler;
    @BindView(R.id.card_no_accounts) public CardView card_no_data;
    @BindView(R.id.fab_add_account) public FloatingActionButton fab_add_account;

    private AuthDataAdapter adapter;

    public AccountsActivity() {
        super(false, true, true);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_accounts;
    }

    @Override
    protected void extractArguments(Intent intent) {
        // NOP - no arguments for this activity
    }

    @Subscribe
    public void state_change(BotServiceStateEvent event) {
        updateUI();
    }

    @Override
    protected void updateUI() {
        boolean permitted = !anyOutstandingPermissions();
        boolean enabled = bound && permitted && service.get_state() != IBotService.State.Authenticating;
        boolean nothing = adapter == null || adapter.getItemCount() == 0;

        if (adapter != null) { adapter.setEnabled(enabled); }
        card_no_data.setVisibility(nothing ? VISIBLE : GONE);
    }

    @Override
    protected void initialise() {
        adapter = new AuthDataAdapter(this, service, recycler, recycler_listener);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        recycler.setLayoutManager(layout);
        recycler.setAdapter(adapter);
    }

    @Override
    protected void denitialise() {
        recycler.setAdapter(null);
    }

    private AuthDataAdapter.Listener recycler_listener = new AuthDataAdapter.Listener() {
        @Override
        public void user_selected(String user) {
            show_user_overview(user);
        }
    };

    private void show_user_overview(String user) {
        Intent intent = UserRulesOverviewActivity.create(this, user);
        startActivity(intent);
    }

    @OnClick(R.id.fab_add_account)
    public void add_account_click() {
        Intent intent = new Intent(this, AddAccountActivity.class);
        startActivityForResult(intent, RC_AddAccount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem refresh_item = menu.add(0, R.string.menu_refresh, 0, R.string.menu_refresh);
        refresh_item.setIcon(R.drawable.ic_refresh_black_24dp);
        refresh_item.getIcon().setTint(Color.WHITE);
        refresh_item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.menu_refresh:
                adapter.update();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_AddAccount:
                if (resultCode == RESULT_OK) {
                    adapter.update();
                }
                break;
            default:
                Log.w(TAG, "Unexpected result received for request code: " + requestCode);
                break;
        }
    }
}
