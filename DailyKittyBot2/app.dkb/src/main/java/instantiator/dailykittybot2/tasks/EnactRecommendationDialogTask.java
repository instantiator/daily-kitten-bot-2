package instantiator.dailykittybot2.tasks;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.LinkedList;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;

public class EnactRecommendationDialogTask extends AsyncTask<EnactRecommendationParams, EnactRecommendationProgress, EnactRecommendationResult> {
    private static final String TAG = EnactRecommendationDialogTask.class.getName();

    private AppCompatActivity context;
    private RedditSession session;
    private IBotService service;

    private MaterialDialog dialog;

    public EnactRecommendationDialogTask(AppCompatActivity context, RedditSession session, IBotService service) {
        this.context = context;
        this.session = session;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = new MaterialDialog.Builder(context)
                .title(context.getString(R.string.dialog_title_enacting_recommendations))
                .content(R.string.dialog_message_enacting_recommendations_preparing)
                .progress(false, 0)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();
    }

    @Override
    protected EnactRecommendationResult doInBackground(EnactRecommendationParams... enactRecommendationParams) {
        EnactRecommendationResult result = new EnactRecommendationResult();
        result.enactions = new LinkedList<>();

        EnactRecommendationProgress progress = new EnactRecommendationProgress();
        for (EnactRecommendationParams param : enactRecommendationParams) {
            progress.current_action = describe(param.recommendation);
            publishProgress(progress);

            Enaction enaction = service.enact(session.getClient(), param.recommendation);
            result.enactions.add(enaction);

            if (enaction.success) {
                progress.successes++;
            } else {
                progress.failures++;
            }
        }

        return result;
    }

    private String describe(Recommendation recommendation) {
        return String.format("%s: %s",
                recommendation.type.getActionString(),
                recommendation.targetSummary);
    }

    @Override
    protected void onProgressUpdate(EnactRecommendationProgress... values) {
        super.onProgressUpdate(values);
        EnactRecommendationProgress p = values[0];
        dialog.setContent(p.current_action);
    }

    @Override
    protected void onPostExecute(EnactRecommendationResult enactRecommendationResult) {
        super.onPostExecute(enactRecommendationResult);

        // TODO: save all results? -- nope they're already saved


        dialog.dismiss();
    }
}
