package instatiator.dailykittybot2.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.UUID;

import instatiator.dailykittybot2.db.entities.Rule;

public class EditRuleViewModel extends AbstractBotViewModel {

    private UUID rule_id;
    private String username;
    private MutableLiveData<Rule> rule;

    public void init(UUID rule_id, String username) {
        this.rule_id = rule_id;
        this.username = username;
    }

    public UUID getRuleId() {
        return rule_id;
    }

    public String getUsername() {
        return username;
    }

    public LiveData<Rule> getRule() {
        if (rule == null) {
            rule = new MutableLiveData<Rule>();
            load_rule();
        }
        return rule;
    }

    public void setRule(Rule rule) {
        ((MutableLiveData<Rule>)getRule()).setValue(rule);
    }

    private void load_rule() {
        setRule(service.get_workspace().get_rule(rule_id).getValue());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        rule = null;
        rule_id = null;
    }
}
