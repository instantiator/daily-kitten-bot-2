package instantiator.dailykittybot2.data;

import instantiator.dailykittybot2.BotApp;
import instantiator.dailykittybot2.R;

public enum OutcomeType {
    DoNothing(
            R.string.outcome_type_nothing,
            R.string.outcome_type_nothing_hint,
            R.drawable.ic_not_interested_black_24dp,
            false),

    AddCommentToPost(
            R.string.outcome_type_add_comment,
            R.string.outcome_type_add_comment_hint,
            R.drawable.ic_edit_black_24dp,
            true),

    UpvotePost(
            R.string.outcome_upvote_post,
            R.string.outcome_upvote_post_hint,
            R.drawable.ic_thumb_up_black_24dp,
            false),

    DownvotePost(
            R.string.outcome_downvote_post,
            R.string.outcome_downvote_post_hint,
            R.drawable.ic_thumb_down_black_24dp,
            false);

    private OutcomeType(int description_resource, int hint_resource, int icon, boolean has_specifics) {
        this.description = description_resource;
        this.hint = hint_resource;
        this.icon = icon;
        this.requires_specifics = has_specifics;
    }

    private int description;
    private int hint;
    private int icon;
    private boolean requires_specifics;

    public int getDescription() { return description; }

    public int getIcon() { return icon; }

    public int getHint() { return hint; }

    public boolean requiresSpecifics() { return requires_specifics; }

    @Override
    public String toString() { return BotApp.appContext.getString(description); }
}
