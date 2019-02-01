package instantiator.dailykittybot2.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.flt.servicelib.AbstractServiceBoundAppCompatActivity;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import instantiator.dailykittybot2.BotApp;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.service.BotService;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.ui.viewmodels.AbstractBotViewModel;

public abstract class AbstractBotActivity<VM extends AbstractBotViewModel> extends AbstractServiceBoundAppCompatActivity<BotService, IBotService> {
    protected final String TAG;

    protected final boolean uses_butterknife;
    protected final boolean uses_events;
    protected final boolean uses_back_button;

    protected VM model;

    protected boolean initialised;

    protected AbstractBotActivity(boolean uses_back_button, boolean uses_butterknife, boolean uses_events) {
        super();
        this.TAG = getClass().getName();
        this.inferredServiceClass = BotService.class;
        this.uses_butterknife = uses_butterknife;
        this.uses_events = uses_events;
        this.uses_back_button = uses_back_button;
    }

    protected abstract Class<VM> getViewModelClass();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        model = ViewModelProviders.of(this).get(getViewModelClass());

        if (uses_butterknife) {
            ButterKnife.bind(this);
        }
        if (uses_back_button) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        parseIntent(getIntent());
    }

    protected abstract int getLayout();

    public IBotService getService() { return service; }

    @Override
    protected void onPause() {
        super.onPause();
        if (uses_events) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (uses_events) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);
    }

    protected void parseIntent(Intent intent) {
        if (intent != null) {
            extractArguments(intent);
            if (bound) {
                reinit();
                updateUI();
            }
        }
    }

    protected abstract void extractArguments(Intent intent);

    @Override
    protected void onBoundChanged(boolean isBound) {
        if (isBound) {
            model.setService(service);
            initialise_model();
            reinit();
            updateUI();
        } else {
            deinit();
        }
    }

    protected abstract void initialise_model();

    protected void reinit() {
        initialised = false;
        init();
    }

    private void init() {
        if (!initialised) {
            initialised = initialise();
        }
    }

    protected abstract boolean initialise();
    protected abstract void denitialise();

    private void deinit() {
        if (initialised) {
            denitialise();
            initialised = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, R.string.menu_display_oss_licenses, 0, R.string.menu_display_oss_licenses);
        menu.add(0, R.string.menu_control_reddit_apps, 0, R.string.menu_control_reddit_apps);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.menu_display_oss_licenses:
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
                return true;

            case R.string.menu_control_reddit_apps:
                visit_url(getString(R.string.url_reddit_apps_preferences));
                return true;

            case android.R.id.home:
                if (uses_back_button) { finish(); }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected abstract void updateUI();

    protected void visit_url(String url) {
        try {
            Uri uri = Uri.parse(url);
            visit_url(uri);
        } catch (Exception e) {
            Log.e(TAG, "Could not parse url: " + url);
            informUser(R.string.toast_warning_cannot_open_url);
        }
    }

    protected void visit_url(Uri uri) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(browserIntent);
        } catch (Exception e) {
            Log.e(TAG, "Could not open url: " + uri.toString());
            informUser(R.string.toast_warning_cannot_open_uri);
        }
    }

    @Override protected void onGrantedOverlayPermission() { }
    @Override protected void onRefusedOverlayPermission() { }
    @Override protected String[] getRequiredPermissions() { return BotApp.required_permissions; }
    @Override protected void onPermissionsGranted() { updateUI(); }
    @Override protected void onNotAllPermissionsGranted() { updateUI(); }
    @Override protected void onUnecessaryCallToRequestOverlayPermission() { }
}
