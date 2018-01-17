package instatiator.dailykittybot2.service.execution;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Submission;

import java.util.List;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Recommendation;

public class RuleResult {
    public RuleTriplet rule;

    public boolean validates;
    public List<String> errors;
    public List<String> warnings;

    public boolean ran;

    public String username;
    public String subreddit;
    public Submission submission;
    public Comment comment; // TODO: not yet in use

    public boolean matched;

    public List<Recommendation> recommendations;
}
