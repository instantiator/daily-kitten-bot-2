package instatiator.dailykittybot2.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.flt.servicelib.AbstractServiceBoundAppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import instatiator.dailykittybot2.BotApp;
import instatiator.dailykittybot2.R;
import instatiator.dailykittybot2.service.BotService;
import instatiator.dailykittybot2.service.IBotService;

public class UserTasksOverviewActivity extends AbstractServiceBoundAppCompatActivity<BotService, IBotService> {
    private static final String TAG = UserTasksOverviewActivity.class.getName();

    private static final String EXTRA_username = "username";
    private static final String EXTRA_mode = "mode";

    @BindView(R.id.recycler) RecyclerView recycler;

    private enum Mode { Unknown, View }

    private Mode mode = Mode.Unknown;
    private String username;

    public static Intent intent_view(Context context, String user) {
        Intent intent = new Intent(context, UserTasksOverviewActivity.class);
        intent.putExtra(EXTRA_username, user);
        intent.putExtra(EXTRA_mode, Mode.View.name());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tasks_overview);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mode = Mode.valueOf(intent.getStringExtra(EXTRA_mode));
        username = intent.getStringExtra(EXTRA_username);
        reinit();
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onBoundChanged(boolean isBound) {
        updateUI();
    }

    private void reinit() {

        // TODO: reinit recycler
    }

    private void updateUI() {

        if (bound) {
            switch (mode) {
                case Unknown:
                    getSupportActionBar().setTitle("please wait...");
                    break;

                case View:
                    String title = getString(R.string.activity_title_view_tasks_overview_for, username);
                    getSupportActionBar().setTitle(title);
                    break;
            }
        } else {
            getSupportActionBar().setTitle("loading...");
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override protected String[] getRequiredPermissions() { return BotApp.required_permissions; }
    @Override protected void onPermissionsGranted() { updateUI(); }
    @Override protected void onNotAllPermissionsGranted() { updateUI(); }

    @Override protected void onUnecessaryCallToRequestOverlayPermission() { }
    @Override protected void onGrantedOverlayPermission() { }
    @Override protected void onRefusedOverlayPermission() { }
}
