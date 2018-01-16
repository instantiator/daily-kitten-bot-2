package instatiator.dailykittybot2.data;

import instatiator.dailykittybot2.BotApp;
import instatiator.dailykittybot2.R;

public enum TargetType {
    Post(R.string.target_type_post),
    Comment(R.string.target_type_comment);

    private int description;

    private TargetType(int description_resource) {
        this.description = description_resource;
    }

    public int getDescription() {
        return description;
    }

    @Override
    public String toString() { return BotApp.appContext.getString(description); }

}
