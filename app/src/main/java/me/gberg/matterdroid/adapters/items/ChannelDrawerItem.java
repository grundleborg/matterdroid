package me.gberg.matterdroid.adapters.items;

import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import me.gberg.matterdroid.model.Channel;

public class ChannelDrawerItem extends SecondaryDrawerItem {
    private Channel channel;

    public ChannelDrawerItem withChannel(final Channel channel) {
        this.channel = channel;
        return this;
    }

    public final Channel getChannel() {
        return this.channel;
    }
}
