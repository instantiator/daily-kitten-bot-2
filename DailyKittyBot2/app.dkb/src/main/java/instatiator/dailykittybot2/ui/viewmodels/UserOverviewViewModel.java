package instatiator.dailykittybot2.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.service.IBotService;

public class UserOverviewViewModel extends AbstractBotViewModel {
    private String username;
    private LiveData<List<Rule>> rules;

    public void init(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public LiveData<List<Rule>> getRules() {
        if (rules == null) {
            rules = new MutableLiveData<List<Rule>>();
            load_rules();
        }
        return rules;
    }

    private void load_rules() {
        rules = service.get_workspace().rules_for(username);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        username = null;
        rules = null;
    }
}
