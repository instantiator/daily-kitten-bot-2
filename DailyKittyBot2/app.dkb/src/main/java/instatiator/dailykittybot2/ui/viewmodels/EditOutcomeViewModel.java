package instatiator.dailykittybot2.ui.viewmodels;

import android.arch.lifecycle.LiveData;

import java.util.UUID;

import instatiator.dailykittybot2.db.entities.Outcome;
import instatiator.dailykittybot2.service.BotWorkspace;

public class EditOutcomeViewModel extends AbstractEditSingleItemViewModel<Outcome> {

    @Override
    protected LiveData<Outcome> getItem(BotWorkspace workspace, UUID item_id) {
        return workspace.get_outcome(item_id);
    }

}
