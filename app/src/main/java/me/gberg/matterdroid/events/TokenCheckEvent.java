package me.gberg.matterdroid.events;

import me.gberg.matterdroid.model.APIError;

public class TokenCheckEvent  extends APIEvent {

    public TokenCheckEvent(Throwable throwable) {
        super(throwable);
    }

    public TokenCheckEvent(APIError apiError) {
        super(apiError);
    }

    public TokenCheckEvent() {
        super();
    }
}
