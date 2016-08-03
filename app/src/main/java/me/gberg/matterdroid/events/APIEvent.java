package me.gberg.matterdroid.events;

import me.gberg.matterdroid.model.APIError;

public class APIEvent {
    private final Throwable throwable;
    private final APIError apiError;

    protected APIEvent(Throwable throwable) {
        this.throwable = throwable;
        this.apiError = null;
    }

    protected APIEvent(APIError apiError) {
        this.apiError = apiError;
        this.throwable = null;
    }

    protected APIEvent() {
        this.throwable = null;
        this.apiError = null;
    }

    public boolean isError() {
        return throwable != null;
    }

    public boolean isApiError() {
        return apiError != null;
    }

    public boolean isSuccess() {
        return throwable == null && apiError == null;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public APIError getApiError() {
        return apiError;
    }

}
