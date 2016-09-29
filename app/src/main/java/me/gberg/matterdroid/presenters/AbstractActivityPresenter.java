package me.gberg.matterdroid.presenters;

import android.os.Bundle;

import com.trello.navi.Event;
import com.trello.navi.Listener;

import me.gberg.matterdroid.activities.PresentedActivity;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public abstract class AbstractActivityPresenter<T extends PresentedActivity> {
    private T view;
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    public void takeView(final T view, final Bundle savedInstanceState) {
        this.view = view;

        // Set up lifecycle callbacks.
        this.view.addListener(Event.DESTROY, new Listener<Void>() {
            @Override
            public void call(final Void aVoid) {
                dropView(view);
            }
        });

        this.view.addListener(Event.PAUSE, new Listener<Void>() {
            @Override
            public void call(final Void aVoid) {
                onPaused();
            }
        });

        this.view.addListener(Event.RESUME, new Listener<Void>() {
            @Override
            public void call(final Void aVoid) {
                onResumed();
            }
        });

        this.view.addListener(Event.START, new Listener<Void>() {
            @Override
            public void call(final Void aVoid) {
                onStarted();
            }
        });

        this.view.addListener(Event.STOP, new Listener<Void>() {
            @Override
            public void call(final Void aVoid) {
                onStopped();
            }
        });

        this.view.addListener(Event.SAVE_INSTANCE_STATE, new Listener<Bundle>() {
            @Override
            public void call(final Bundle bundle) {
                onSaveInstanceState(bundle);
            }
        });

        // Signal that the new view has been created.
        onCreated(savedInstanceState);
    }

    public void dropView(final T view) {
        if (this.view == view) {
            this.view = null;
            onDestroyed();
        }
    }

    protected T getView() {
        return view;
    }

    protected abstract void onCreated(final Bundle savedInstanceState);
    protected void onDestroyed() {}
    protected void onPaused() {}
    protected void onResumed() {}
    protected void onStopped() {}
    protected void onStarted() {}
    protected void onSaveInstanceState(final Bundle bundle) {}

    public void leaveScope() {
        // This method exists because I am struggling to see how to get the presenter to drop its
        // references to other things that are keeping it hanging around after it goes out of scope
        // and thus stopping it being garbage collected.
        this.subscriptions.unsubscribe();
    }

    public void addSubscription(final Subscription subscription) {
        this.subscriptions.add(subscription);
    }
}
