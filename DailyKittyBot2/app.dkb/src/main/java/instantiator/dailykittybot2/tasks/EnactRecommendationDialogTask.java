package instantiator.dailykittybot2.tasks;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Date;
import java.util.LinkedList;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.RedditSession;
import instantiator.dailykittybot2.service.enaction.Enactor;

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
        result.successes = new LinkedList<>();
        result.failures = new LinkedList<>();

        EnactRecommendationProgress progress = new EnactRecommendationProgress();
        for (EnactRecommendationParams param : enactRecommendationParams) {
            progress.current_action = describe(param.recommendation);
            publishProgress(progress);

            Enactor enactor = new Enactor(context, service, session.getClient());
            Enaction enaction = enactor.enact(param.recommendation);
            result.enactions.add(enaction);

            param.recommendation.lastAttempted = enaction.started;
            param.recommendation.succeeded = enaction.success;
            param.recommendation.failed = !enaction.success;
            param.recommendation.failMessages = enaction.errors;

            if (enaction.success) {
                progress.successes++;
                result.successes.add(param.recommendation);
            } else {
                progress.failures++;
                result.failures.add(param.recommendation);
            }

            service.insert_enaction(enaction);
            service.update_recommendation(param.recommendation);
        } // each param = each recommendation

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
        dialog.dismiss();
    }
}
