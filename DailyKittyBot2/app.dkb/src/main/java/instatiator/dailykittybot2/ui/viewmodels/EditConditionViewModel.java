package instatiator.dailykittybot2.ui.viewmodels;

import android.arch.lifecycle.LiveData;

import java.util.UUID;

import instatiator.dailykittybot2.db.entities.Condition;
import instatiator.dailykittybot2.service.BotWorkspace;

public class EditConditionViewModel extends AbstractEditSingleItemViewModel<Condition> {

    @Override
    protected LiveData<Condition> getItem(BotWorkspace workspace, UUID item_id) {
        return workspace.get_condition(item_id);
    }

}
