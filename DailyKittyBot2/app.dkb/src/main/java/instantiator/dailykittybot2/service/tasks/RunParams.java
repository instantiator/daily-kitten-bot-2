package instantiator.dailykittybot2.service.tasks;

import java.util.List;

import instantiator.dailykittybot2.data.RuleTriplet;
import instantiator.dailykittybot2.service.execution.RuleExecutor;

public class RunParams {

    public String account;
    public List<RuleTriplet> rules;
    public RuleExecutor.ExecutionMode mode;

}
