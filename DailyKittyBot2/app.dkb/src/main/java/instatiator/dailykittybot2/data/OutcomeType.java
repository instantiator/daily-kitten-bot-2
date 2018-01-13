package instatiator.dailykittybot2.data;

import instatiator.dailykittybot2.BotApp;
import instatiator.dailykittybot2.R;

public enum OutcomeType {
    DoNothing(
            R.string.outcome_type_nothing,
            R.string.outcome_type_nothing_hint,
            false),

    AddCommentToPost(
            R.string.outcome_type_add_comment,
            R.string.outcome_type_add_comment_hint,
            true),

    UpvotePost(
            R.string.outcome_upvote_post,
            R.string.outcome_upvote_post_hint,
            false),

    DownvotePost(
            R.string.outcome_downvote_post,
            R.string.outcome_downvote_post_hint,
            false);

    private OutcomeType(int description_resource, int hint_resource, boolean specifics) {
        this.description = description_resource;
        this.hint = hint_resource;
        this.requires_specifics = specifics;
    }

    private int description;
    private int hint;
    private boolean requires_specifics;

    public int getDescription() { return description; }

    public int getHint() { return hint; }

    public boolean getRequiresSpecifics() { return requires_specifics; }

    @Override
    public String toString() { return BotApp.appContext.getString(description); }
}
