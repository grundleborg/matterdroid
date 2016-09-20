package me.gberg.matterdroid.managers;

import java.util.HashMap;

import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.events.MembersEvent;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.ExtraInfo;
import me.gberg.matterdroid.model.Member;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MembersManager {

    private final ErrorParser errorParser;
    private final Bus bus;
    private final Team team;
    private final TeamAPI teamApi;

    private Channel channel;
    private HashMap<String, Member> members = new HashMap<>();
    private int membersCount;

    public MembersManager(final ErrorParser errorParser,
                          final Bus bus,
                          final Team team,
                          final TeamAPI teamAPI) {
        this.errorParser = errorParser;
        this.bus = bus;
        this.team = team;
        this.teamApi = teamAPI;
    }

    public void setChannel(final Channel channel) {
        this.channel = channel;
        if (this.members != null) {
            this.members.clear();
            this.membersCount = 0;
        }

        // Load the extra info for this channel.
        Observable<Response<ExtraInfo>> initialLoadObservable = teamApi.extraInfo(team.id, channel.id);
        initialLoadObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<ExtraInfo>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        bus.send(new MembersEvent(e));
                    }

                    @Override
                    public void onNext(final Response<ExtraInfo> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            bus.send(new MembersEvent(apiError));
                        }

                        // If the channel ID doesn't match, throw away the stale response.
                        final ExtraInfo body = response.body();
                        if (!body.id.equals(channel.id)) {
                            return;
                        }

                        // Save to the manager.
                        membersCount = body.memberCount;
                        for (final Member member: body.members) {
                            members.put(member.id, member);
                        }

                        // Request is successful.
                        bus.send(new MembersEvent(body.id, body.memberCount, body.members));
                    }
                });
    }

    public final Member getMember(final String id) {
        return members.get(id);
    }
}
