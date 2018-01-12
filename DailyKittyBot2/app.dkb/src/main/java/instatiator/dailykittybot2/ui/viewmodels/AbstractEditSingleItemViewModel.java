package instatiator.dailykittybot2.ui.viewmodels;

import android.arch.lifecycle.LiveData;

import java.util.UUID;

import instatiator.dailykittybot2.service.BotWorkspace;

public abstract class AbstractEditSingleItemViewModel<ItemType> extends AbstractBotViewModel {

    private UUID init_id;
    private String init_username;

    private LiveData<ItemType> item;

    public void init(UUID item_id, String username) {
        this.init_id = item_id;
        this.init_username = username;
        nullifyData();
    }

    public UUID getItemId() {
        return init_id;
    }

    public String getUsername() {
        return init_username;
    }

    public LiveData<ItemType> getItem() {
        if (item == null) {
            item = getItem(service.get_workspace(), init_id);
        }
        return item;
    }

    protected abstract LiveData<ItemType> getItem(BotWorkspace workspace, UUID item_id);

    @Override
    protected void onCleared() {
        super.onCleared();
        nullifyData();
    }

    private void nullifyData() {
        item = null;
    }
}
