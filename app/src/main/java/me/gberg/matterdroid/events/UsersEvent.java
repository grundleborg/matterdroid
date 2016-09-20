package me.gberg.matterdroid.events;

import me.gberg.matterdroid.model.Users;

public class UsersEvent {
    private final Users users;

    public UsersEvent(final Users users) {
        this.users = users;
    }

    public final Users getUsers() {
        return users;
    }
}
