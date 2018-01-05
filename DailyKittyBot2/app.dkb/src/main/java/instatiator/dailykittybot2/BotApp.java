package instatiator.dailykittybot2;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import net.dean.jraw.android.AndroidHelper;
import net.dean.jraw.android.AppInfoProvider;
import net.dean.jraw.android.ManifestAppInfoProvider;
import net.dean.jraw.android.SharedPreferencesTokenStore;
import net.dean.jraw.android.SimpleAndroidLogAdapter;
import net.dean.jraw.http.LogAdapter;
import net.dean.jraw.http.SimpleHttpLogger;
import net.dean.jraw.oauth.AccountHelper;

import java.util.UUID;

import instatiator.dailykittybot2.service.BotService;

public class BotApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init_service();
    }

    private void init_service() {
        Intent i = new Intent(this, BotService.class);
        startService(i);
    }

    public static String[] required_permissions = new String[] {
        Manifest.permission.INTERNET
    };
}
