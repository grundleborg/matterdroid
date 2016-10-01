package me.gberg.matterdroid.utils.rx;

import java.util.ArrayList;

import me.gberg.matterdroid.events.PostsEvent;
import me.gberg.matterdroid.managers.WebSocketManager;
import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.model.IWebSocketMessage;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.model.Users;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class TeamBus {

    private final Subject<IWebSocketMessage, IWebSocketMessage> webSocketBus
            = new SerializedSubject<IWebSocketMessage, IWebSocketMessage>(PublishSubject.<IWebSocketMessage>create());

    private final BehaviorSubject<WebSocketManager.ConnectionState> connectionStateSubject
            = BehaviorSubject.create(WebSocketManager.ConnectionState.Disconnected);

    private final BehaviorSubject<Channels> channelsSubject
            = BehaviorSubject.create(Channels.builder().build());

    private final BehaviorSubject<PostsEvent> postsSubject
            = BehaviorSubject.create(new PostsEvent(new ArrayList<Post>()));

    private final BehaviorSubject<Users> usersSubject
            = BehaviorSubject.create(Users.builder().build());

    public Observable<IWebSocketMessage> toWebSocketBusObservable() {
        return webSocketBus;
    }

    public BehaviorSubject<WebSocketManager.ConnectionState> getConnectionStateSubject() {
        return connectionStateSubject;
    }

    public BehaviorSubject<Channels> getChannelsSubject() {
        return channelsSubject;
    }

    public BehaviorSubject<PostsEvent> getPostsSubject() {
        return postsSubject;
    }

    public BehaviorSubject<Users> getUsersSubject() {
        return usersSubject;
    }

    public void sendWebSocketBus(IWebSocketMessage message) {
        webSocketBus.onNext(message);
    }
}
