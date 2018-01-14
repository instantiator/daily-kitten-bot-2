package instatiator.dailykittybot2.util;

import android.content.Context;
import android.graphics.Color;

import instatiator.dailykittybot2.R;

public class ColourConventions {

    private Context context;

    public ColourConventions(Context context) {
        this.context = context;
    }

    public int icon_alert(boolean errors, boolean warnings) {

        if (errors) {
            return context.getColor(R.color.colorError);
        }

        if (warnings) {
            return context.getColor(R.color.colorWarning);
        }

        return context.getColor(R.color.colorAOK);
    }
}
