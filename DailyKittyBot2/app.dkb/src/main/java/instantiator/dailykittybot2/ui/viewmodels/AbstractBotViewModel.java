package instantiator.dailykittybot2.ui.viewmodels;

import android.arch.lifecycle.ViewModel;

import instantiator.dailykittybot2.service.IBotService;

public abstract class AbstractBotViewModel extends ViewModel {

    protected IBotService service;

    public void setService(IBotService service) {
        this.service = service;
    }

    public IBotService getService() {
        return service;
    }

}
