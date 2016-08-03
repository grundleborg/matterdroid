package me.gberg.matterdroid.events;

import me.gberg.matterdroid.model.APIError;

public class LoginEvent extends APIEvent{
    public LoginEvent(Throwable throwable) {
        super(throwable);
    }

    public LoginEvent(APIError apiError) {
        super(apiError);
    }

    public LoginEvent() {
        super();
    }
}
