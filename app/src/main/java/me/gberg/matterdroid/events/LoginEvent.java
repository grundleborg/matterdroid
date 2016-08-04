package me.gberg.matterdroid.events;

import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.User;

public class LoginEvent extends APIEvent {
    private User user;

    public LoginEvent(Throwable throwable) {
        super(throwable);
        user = null;
    }

    public LoginEvent(APIError apiError) {
        super(apiError);
        user = null;
    }

    public LoginEvent(final User user) {
        super();
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }
}
