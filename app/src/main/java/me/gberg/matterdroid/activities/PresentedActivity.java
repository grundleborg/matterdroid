package me.gberg.matterdroid.activities;

import com.trello.navi.component.support.NaviAppCompatActivity;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.navi.NaviLifecycle;

public class PresentedActivity extends NaviAppCompatActivity {

    private final LifecycleProvider<ActivityEvent> lifecycleProvider
            = NaviLifecycle.createActivityLifecycleProvider(this);

    public LifecycleProvider<ActivityEvent> getLifecycleProvider() {
        return lifecycleProvider;
    }
}
