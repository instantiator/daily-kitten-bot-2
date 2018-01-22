package instantiator.dailykittybot2.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.flt.servicelib.AbstractServiceBoundAppCompatActivity;

import net.dean.jraw.oauth.OAuthException;
import net.dean.jraw.oauth.StatefulAuthHelper;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import instantiator.dailykittybot2.BotApp;
import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.service.BotService;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class AddAccountActivity extends AbstractServiceBoundAppCompatActivity<BotService, IBotService> {

    @BindView(R.id.web_view) WebView web;

    private boolean started_process = false;

    private RedditSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.activity_title_add_account);
        clear_web_view();
    }

    @Override
    protected void onBoundChanged(boolean isBound) {
        if (isBound && !started_process) {
            started_process = true;
            session = new RedditSession(this, service.get_device_uuid(), null);
            begin_process();
        }
    }

    private void begin_process() {
        clear_web_view();
        final StatefulAuthHelper helper = session.get_accountHelper().switchToNewUser();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (helper.isFinalRedirectUrl(url)) {
                    web.stopLoading();
                    web.setVisibility(GONE); // hide it - we're done!

                    // Try to authenticate the user
                    new AuthenticateTask(AddAccountActivity.this, helper).execute(url);
                }
            }
        });

        boolean requestRefreshToken = true;
        boolean useMobileSite = true;
        String[] scopes = getResources().getStringArray(R.array.reddit_authentication_scopes);
        String authUrl = helper.getAuthorizationUrl(requestRefreshToken, useMobileSite, scopes);

        web.setVisibility(VISIBLE);
        web.loadUrl(authUrl);
    }

    private void clear_web_view() {
        web.clearCache(true);
        web.clearHistory();

        // Stolen from https://github.com/ccrama/Slide/blob/a2184269/app/src/main/java/me/ccrama/redditslide/Activities/Login.java#L92
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
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
    @Override protected void onPermissionsGranted() { }
    @Override protected void onNotAllPermissionsGranted() { }
    @Override protected void onUnecessaryCallToRequestOverlayPermission() { }
    @Override protected void onGrantedOverlayPermission() { }
    @Override protected void onRefusedOverlayPermission() { }

    private static final class AuthenticateTask extends AsyncTask<String, Void, Boolean> {
        private static final String TAG = AuthenticateTask.class.getName();

        private final WeakReference<Activity> activity;
        private final StatefulAuthHelper helper;

        AuthenticateTask(Activity context, StatefulAuthHelper helper) {
            this.activity = new WeakReference<>(context);
            this.helper = helper;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                helper.onUserChallenge(urls[0]);
                Log.i(TAG, "User challenge tasks_completed.");
                return true;
            } catch (OAuthException e) {
                Log.w(TAG, "OAuthException received during user challenge.", e);
                return false;
            } catch (Exception e) {
                Log.e(TAG, "Unexpected exception received during user challenge.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Activity host = this.activity.get();
            if (host != null) {
                host.setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED, new Intent());
                host.finish();
            }
        }
    }

}
