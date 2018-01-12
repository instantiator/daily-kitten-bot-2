package instatiator.dailykittybot2.data;

import instatiator.dailykittybot2.R;

public enum OutcomeType {
    NothingSelected(R.string.outcome_type_nothing),
    AddCommentToPost(R.string.outcome_type_add_comment),
    UpvotePost(R.string.outcome_upvote_post),
    DownvotePost(R.string.outcome_downvote_post);

    private OutcomeType(int description_resource) {
        this.description = description_resource;
    }

    private int description;

    public int getDescription() { return description; }

    @Override
    public String toString() { return name(); }
}
