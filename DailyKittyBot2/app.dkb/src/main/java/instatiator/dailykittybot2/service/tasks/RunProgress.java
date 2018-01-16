package instatiator.dailykittybot2.service.tasks;

public class RunProgress {

    public int current_subreddits_count = 0;
    public int of_subreddits_count = 0;

    public String current_username;

    public int current_post_count = 0;

    public String current_subreddit;
    public String current_post;
    public String current_rule;

    public int generated_recommendation_count = 0;

    public RunProgress(String user, int subreddits_count) {
        this.of_subreddits_count = subreddits_count;
        this.current_username = user;
    }

}
