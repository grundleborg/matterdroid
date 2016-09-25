package me.gberg.matterdroid.utils.rx;

import me.gberg.matterdroid.model.IWebSocketMessage;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class Bus {

    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());
    private final Subject<IWebSocketMessage, IWebSocketMessage> webSocketBus = new SerializedSubject<IWebSocketMessage, IWebSocketMessage>(PublishSubject.<IWebSocketMessage>create());

    public void send(Object o) {
        bus.onNext(o);
    }

    public void sendWebSocketBus(IWebSocketMessage message) {
        webSocketBus.onNext(message);
    }

    public Observable<Object> toObserverable() {
        return bus;
    }

    public Observable<IWebSocketMessage> toWebSocketBusObservable() {
        return webSocketBus;
    }
}
