package me.gberg.matterdroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.di.components.TeamComponent;
import me.gberg.matterdroid.events.ChannelsEvent;
import me.gberg.matterdroid.managers.ChannelsManager;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.utils.rx.Bus;
import rx.functions.Action1;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    Bus bus;

    @Inject
    ChannelsManager channelsManager;

    Drawer drawer;

    private IItemAdapter<IDrawerItem> drawerAdapter;
    private Channels channels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() Called");

        // Redirect to Choose Team activity if we can't inject the Team Component.
        TeamComponent teamComponent = ((App) getApplication()).getTeamComponent();
        if (teamComponent == null) {
            ChooseTeamActivity.launch(this);
            finish();
            return;
        }
        teamComponent.inject(this);

        // Subscribe to the event bus.
        // TODO: Unsubscribe at the correct lifecycle events.
        bus.toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof ChannelsEvent) {
                            handleChannelsEvent((ChannelsEvent) event);
                        }
                    }
                });

        setContentView(R.layout.ac_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Set up the drawer.
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .build();

        drawerAdapter = drawer.getItemAdapter();

        // TODO: Initialise the rest of this activity properly.

        channelsManager.loadChannels();
    }

    private void handleChannelsEvent(final ChannelsEvent event) {
        Timber.v("handleChannelsEvent()");
        if (event.isApiError()) {
            APIError apiError = event.getApiError();
            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode + " with error id " + apiError.id);
            return;
        } else if (event.isError()) {
            // Unhandled error. Log it.
            Throwable e = event.getThrowable();
            Timber.e(e, e.getMessage());
            return;
        }

        // Success.
        this.channels = event.getChannels();

        for (Channel channel: channels.channels) {
            drawerAdapter.add(new PrimaryDrawerItem()
                    .withIdentifier(channels.channels.indexOf(channel))
                    .withName(channel.displayName));
        }
    }

    public static void launch(final Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }
}
