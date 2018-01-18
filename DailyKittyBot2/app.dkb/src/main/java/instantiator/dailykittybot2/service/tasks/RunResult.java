package instantiator.dailykittybot2.service.tasks;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import instantiator.dailykittybot2.db.entities.RunReport;
import instantiator.dailykittybot2.service.execution.RuleResult;

public class RunResult {

    public String username;
    public int subreddits_completed = 0;
    public int total_subreddits = 0;

    public Map<String, Collection<RuleResult>> subreddits_to_results;

    public List<RunReport> all_run_reports;
}
