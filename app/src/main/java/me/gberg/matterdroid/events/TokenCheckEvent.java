package me.gberg.matterdroid.events;

import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.User;

public class TokenCheckEvent  extends APIEvent {
    private final User user;

    public TokenCheckEvent(Throwable throwable) {
        super(throwable);
        this.user = null;
    }

    public TokenCheckEvent(APIError apiError) {
        super(apiError);
        this.user = null;
    }

    public TokenCheckEvent(final User user) {
        super();
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
