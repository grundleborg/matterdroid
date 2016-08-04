package me.gberg.matterdroid.events;

import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channels;

public class ChannelsEvent extends APIEvent {
    private final Channels channels;

    public ChannelsEvent(Throwable throwable) {
        super(throwable);
        channels = null;
    }

    public ChannelsEvent(APIError apiError) {
        super(apiError);
        channels = null;
    }

    public ChannelsEvent(final Channels channels) {
        super();
        this.channels = channels;
    }

    public Channels getChannels() {
        return this.channels;
    }
}
