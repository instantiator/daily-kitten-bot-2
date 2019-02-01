package instantiator.dailykittybot2.data;

import instantiator.dailykittybot2.BotApp;
import instantiator.dailykittybot2.R;

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
