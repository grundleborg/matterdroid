package me.gberg.matterdroid.events;

public class RemovePostEvent {
    private final int position;

    public RemovePostEvent(final int position) {
        this.position = position;
    }

    public final int getPosition() {
        return this.position;
    }
}
