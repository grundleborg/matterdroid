package me.gberg.matterdroid.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class ImmutableChannels {

    public abstract List<Channel> channels();

    public final Channel getChannelForId(final String channelId) {
        for (final Channel channel: channels()) {
            if (channel.id().equals(channelId)) {
                return channel;
            }
        }
        return null;
    }
}
