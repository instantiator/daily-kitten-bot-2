package instatiator.dailykittybot2.ui.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import instatiator.dailykittybot2.db.entities.Recommendation;
import instatiator.dailykittybot2.db.entities.Rule;
import instatiator.dailykittybot2.service.IBotService;

public class UserOverviewViewModel extends AbstractBotViewModel {
    private String username;
    private LiveData<List<Rule>> rules;
    private LiveData<List<Recommendation>> recommendations;

    public void init(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public LiveData<List<Rule>> getRules() {
        if (rules == null) {
            rules = service.get_workspace().rules_for(username);
        }
        return rules;
    }

    public LiveData<List<Recommendation>> getRecommendations() {
        if (recommendations == null) {
            recommendations = service.get_workspace().recommendations_for(username);
        }
        return recommendations;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        username = null;
        rules = null;
        recommendations = null;
    }
}
