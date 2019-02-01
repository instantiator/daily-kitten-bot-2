package instantiator.dailykittybot2.util;

import android.content.Context;

import instantiator.dailykittybot2.R;

public class ColourConventions {

    private Context context;

    public ColourConventions(Context context) {
        this.context = context;
    }

    public int rule_icon(boolean autorun) {
        return autorun ?
                context.getColor(R.color.colorAccent) :
                context.getColor(R.color.colorPrimary);
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
