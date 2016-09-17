package me.gberg.matterdroid.utils.rx;

import me.gberg.matterdroid.model.WebSocketMessage;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class Bus {

    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());
    private final Subject<WebSocketMessage, WebSocketMessage> webSocketBus = new SerializedSubject<WebSocketMessage, WebSocketMessage>(PublishSubject.<WebSocketMessage>create());

    public void send(Object o) {
        bus.onNext(o);
    }

    public void sendWebSocketBus(WebSocketMessage message) {
        webSocketBus.onNext(message);
    }

    public Observable<Object> toObserverable() {
        return bus;
    }

    public Observable<WebSocketMessage> toWebSocketBusObservable() {
        return webSocketBus;
    }
}
