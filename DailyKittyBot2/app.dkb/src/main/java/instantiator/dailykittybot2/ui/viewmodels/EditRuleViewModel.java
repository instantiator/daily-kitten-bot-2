package instantiator.dailykittybot2.ui.viewmodels;

import android.arch.lifecycle.LiveData;

import java.util.List;
import java.util.UUID;

import instantiator.dailykittybot2.db.entities.Condition;
import instantiator.dailykittybot2.db.entities.Outcome;
import instantiator.dailykittybot2.db.entities.Rule;

public class EditRuleViewModel extends AbstractBotViewModel {

    private UUID init_rule_id;
    private String init_username;

    private LiveData<Rule> rule;
    private LiveData<List<Condition>> conditions;
    private LiveData<List<Outcome>> outcomes;

    public void init(UUID rule_id, String username) {
        this.init_rule_id = rule_id;
        this.init_username = username;
        // nullifyData(); // it will be refetched - this is correct!
    }

    public UUID getRuleId() {
        return init_rule_id;
    }

    public String getUsername() {
        return init_username;
    }

    public LiveData<Rule> getRule() {
        if (rule == null) {
            rule = service.get_workspace().get_rule(init_rule_id);
        }
        return rule;
    }

    /*
    public void setRule(Rule rule) {
        ((MutableLiveData<Rule>)getRule()).setValue(rule);
    }
    */

    public LiveData<List<Condition>> getRuleConditions() {
        if (conditions == null) {
            conditions = service.get_workspace().conditions_for(init_rule_id);
        }
        return conditions;
    }

    public LiveData<List<Outcome>> getRuleOutcomes() {
        if (outcomes == null) {
            outcomes = service.get_workspace().outcomes_for(init_rule_id);
        }
        return outcomes;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        nullifyData();
    }

    private void nullifyData() {
        rule = null;
        conditions = null;
        outcomes = null;
    }
}
