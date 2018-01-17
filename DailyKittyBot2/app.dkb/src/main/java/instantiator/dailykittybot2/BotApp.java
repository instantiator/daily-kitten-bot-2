package instantiator.dailykittybot2;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

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
        startService(i);
    }

    public static String[] required_permissions = new String[] {
        Manifest.permission.INTERNET
    };
}
