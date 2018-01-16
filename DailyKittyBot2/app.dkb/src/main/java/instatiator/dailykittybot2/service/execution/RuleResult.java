package instatiator.dailykittybot2.service.execution;

import java.util.List;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Recommendation;

public class RuleResult {
    public RuleTriplet rule;

    public boolean validates;
    public List<String> errors;
    public List<String> warnings;

    public boolean ran;
    public boolean matched;

    public List<Recommendation> recommendations;
}
