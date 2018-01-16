package instatiator.dailykittybot2.service.tasks;

import java.util.List;

import instatiator.dailykittybot2.data.RuleTriplet;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.service.execution.RuleExecutor;

public class RunParams {

    public String account;
    public List<RuleTriplet> rules;
    public RuleExecutor.ExecutionMode mode;

}
