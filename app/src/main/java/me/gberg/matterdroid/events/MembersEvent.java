package me.gberg.matterdroid.events;

import java.util.List;

import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Member;

public class MembersEvent extends APIEvent {
    private final String channelId;
    private final int membersCount;
    private final List<Member> members;

    public MembersEvent(Throwable throwable) {
        super(throwable);
        channelId = null;
        membersCount = 0;
        members = null;
    }

    public MembersEvent(APIError apiError) {
        super(apiError);
        channelId = null;
        membersCount = 0;
        members = null;
    }

    public MembersEvent(final String channelId, int membersCount, final List<Member> members) {
        super();
        this.channelId = channelId;
        this.membersCount = membersCount;
        this.members = members;
    }

    public final String getChannelId() {
        return channelId;
    }

    public final int getMembersCount() {
        return membersCount;
    }

    public final List<Member> getMembers() {
        return members;
    }
}
