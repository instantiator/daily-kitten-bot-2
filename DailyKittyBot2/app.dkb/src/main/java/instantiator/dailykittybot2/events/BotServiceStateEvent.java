package instantiator.dailykittybot2.events;

import instantiator.dailykittybot2.service.IBotService;

public class BotServiceStateEvent {

    public IBotService.State state;
    public String account;

    public BotServiceStateEvent(IBotService service, IBotService.State state, String account) {
        this.state = state;
        this.account = account;
    }

}
