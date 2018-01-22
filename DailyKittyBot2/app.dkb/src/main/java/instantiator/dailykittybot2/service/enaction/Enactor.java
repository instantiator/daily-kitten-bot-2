package instantiator.dailykittybot2.service.enaction;

import android.content.Context;
import android.util.Log;

import net.dean.jraw.RedditClient;

import java.util.Date;
import java.util.UUID;

import instantiator.dailykittybot2.R;
import instantiator.dailykittybot2.db.entities.Enaction;
import instantiator.dailykittybot2.db.entities.Recommendation;
import instantiator.dailykittybot2.service.IBotService;
import instantiator.dailykittybot2.service.helpers.DataFactory;

public class Enactor {
    private static final String TAG = Enactor.class.getName();

    Context context;
    IBotService service;
    RedditClient reddit;

    public Enactor(Context context, IBotService service, RedditClient client) {
        this.reddit = client;
        this.context = context;
        this.service = service;
    }

    public Enaction enact(Recommendation recommendation) {
        String username = reddit.me().getUsername();
        Enaction enaction = DataFactory.create_enaction(username, recommendation.uuid);
        enaction.started = new Date();

         try {

             switch (recommendation.type) {
                 case DoNothing:
                     doNothing(recommendation, enaction);
                     break;

                 case AddCommentToPost:
                     addComment(recommendation, enaction);
                     break;

                 case UpvotePost:
                     upvote(recommendation, enaction);
                     break;

                 case DownvotePost:
                     downvote(recommendation, enaction);
                     break;

                 case SavePost:
                     save(recommendation, enaction);
                     break;

                 default:
                     throw new IllegalArgumentException(
                             "Unrecognised outcome type in recommendation: " + recommendation.type.name());
             }

             enaction.success = true;

         } catch (Exception e) {
             Log.w(TAG, "Unable to perform outcome action: " + recommendation.type.name());
             enaction.success = false;
             enaction.errors.add(e.getMessage());
         } finally {
             enaction.completed = new Date();
         }

        return enaction;
    }

    private void doNothing(Recommendation rec, Enaction enaction) {
        Log.v(TAG, "redditClient.getAuthenticationMethod().name() = " + reddit.getAuthMethod().name());
        Log.v(TAG, "redditClient.getAuthenticationMethod().toString() = " + reddit.getAuthMethod().toString());
        Log.v(TAG, "redditClient.me().getUsername() = " + reddit.me().getUsername());
        enaction.description_short = context.getString(R.string.enaction_nothing_happened_short);
        enaction.description_long = context.getString(
                R.string.enaction_nothing_happened_long,
                rec.targetSummary,
                rec.targetSubreddit);
    }

    private void upvote(Recommendation rec, Enaction enaction) {
        Log.v(TAG, "redditClient.getAuthenticationMethod().name() = " + reddit.getAuthMethod().name());
        Log.v(TAG, "redditClient.getAuthenticationMethod().toString() = " + reddit.getAuthMethod().toString());
        Log.v(TAG, "redditClient.me().getUsername() = " + reddit.me().getUsername());
        reddit.submission(rec.targetSubmissionId).upvote();

        enaction.description_short = context.getString(R.string.enaction_upvoted_short);
        enaction.description_long = context.getString(
                R.string.enaction_upvoted_long,
                rec.targetSummary,
                rec.targetSubreddit);
    }

    private void downvote(Recommendation rec, Enaction enaction) {
        Log.v(TAG, "redditClient.getAuthenticationMethod().name() = " + reddit.getAuthMethod().name());
        Log.v(TAG, "redditClient.getAuthenticationMethod().toString() = " + reddit.getAuthMethod().toString());
        Log.v(TAG, "redditClient.me().getUsername() = " + reddit.me().getUsername());
        reddit.submission(rec.targetSubmissionId).downvote();

        enaction.description_short = context.getString(R.string.enaction_downvoted_short);
        enaction.description_long = context.getString(
                R.string.enaction_downvoted_long,
                rec.targetSummary,
                rec.targetSubreddit);
    }

    private void save(Recommendation rec, Enaction enaction) {
        Log.v(TAG, "redditClient.getAuthenticationMethod().name() = " + reddit.getAuthMethod().name());
        Log.v(TAG, "redditClient.getAuthenticationMethod().toString() = " + reddit.getAuthMethod().toString());
        Log.v(TAG, "redditClient.me().getUsername() = " + reddit.me().getUsername());
        reddit.submission(rec.targetSubmissionId).save();

        enaction.description_short = context.getString(R.string.enaction_saved_short);
        enaction.description_long = context.getString(
                R.string.enaction_saved_long,
                rec.targetSummary,
                rec.targetSubreddit);
    }

    private void addComment(Recommendation rec, Enaction enaction) {
        Log.v(TAG, "redditClient.getAuthenticationMethod().name() = " + reddit.getAuthMethod().name());
        Log.v(TAG, "redditClient.getAuthenticationMethod().toString() = " + reddit.getAuthMethod().toString());
        Log.v(TAG, "redditClient.me().getUsername() = " + reddit.me().getUsername());
        reddit.submission(rec.targetSubmissionId).reply(rec.modifier);

        enaction.description_short = context.getString(R.string.enaction_commented_short);
        enaction.description_long = context.getString(
                R.string.enaction_commented_long,
                rec.targetSummary,
                rec.targetSubreddit,
                rec.modifier);
    }

}
