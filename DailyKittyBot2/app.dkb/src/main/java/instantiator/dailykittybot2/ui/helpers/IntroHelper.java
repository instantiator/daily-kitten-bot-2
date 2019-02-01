package instantiator.dailykittybot2.ui.helpers;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.rubengees.introduction.IntroductionBuilder;
import com.rubengees.introduction.Option;
import com.rubengees.introduction.Slide;

import java.util.ArrayList;
import java.util.List;

import instantiator.dailykittybot2.R;

import static android.content.Context.MODE_PRIVATE;

public class IntroHelper {
    private static final String PREFS_NAME = "IntroHelper.settings";

    private static final String KEY_previously_shown = "previously_shown";

    private AppCompatActivity context;

    public IntroHelper(AppCompatActivity activity) {
        this.context = activity;
    }

    public boolean has_run() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getBoolean(KEY_previously_shown, false);
    }

    public void set_run(boolean run) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(KEY_previously_shown, run);
        editor.commit();
    }

    public void initiate() {
        new IntroductionBuilder(context)
                .withSlides(generateSlides())
                .introduceMyself();
        set_run(true);
    }

    private List<Slide> generateSlides() {
        List<Slide> result = new ArrayList<>();

        result.add(
            new Slide()
                .withTitle(context.getString(R.string.slide_1_title_welcome))
                .withDescription(R.string.slide_1_description_welcome)
                .withImage(R.drawable.screenshot_accounts)
                .withColorResource(R.color.slide_1_background));

        result.add(
                new Slide()
                        .withTitle(context.getString(R.string.slide_2_title_welcome))
                        .withDescription(R.string.slide_2_description_welcome)
                        .withImage(R.drawable.screenshot_reddit)
                        .withColorResource(R.color.slide_2_background));

        result.add(
                new Slide()
                        .withTitle(context.getString(R.string.slide_3_title_welcome))
                        .withDescription(R.string.slide_3_description_welcome)
                        .withImage(R.drawable.screenshot_rules)
                        .withColorResource(R.color.slide_3_background));

        result.add(
                new Slide()
                        .withTitle(context.getString(R.string.slide_4_title_welcome))
                        .withDescription(R.string.slide_4_description_welcome)
                        .withImage(R.drawable.screenshot_recommendations)
                        .withColorResource(R.color.slide_4_background));

        result.add(
                new Slide()
                        .withTitle(context.getString(R.string.slide_5_title_welcome))
                        .withDescription(R.string.slide_5_description_welcome)
                        .withImage(R.drawable.screenshot_accounts)
                        .withColorResource(R.color.slide_5_background));

        result.add(
                new Slide()
                        .withTitle(context.getString(R.string.slide_6_title_welcome))
                        .withDescription(R.string.slide_6_description_welcome)
                        .withImage(R.drawable.screenshot_sample_data)
                        .withColorResource(R.color.slide_6_background));

        return result;
    }

}
