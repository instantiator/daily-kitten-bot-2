package instantiator.dailykittybot2;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import instantiator.dailykittybot2.service.BotService;

public class BotApp extends Application {
    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        init_service();
    }

    private void init_service() {
        Intent i = new Intent(this, BotService.class);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    public static String[] required_permissions = new String[] {
        Manifest.permission.INTERNET
    };
}
