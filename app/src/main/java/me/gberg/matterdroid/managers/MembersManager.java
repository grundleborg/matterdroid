package me.gberg.matterdroid.managers;

import java.util.HashMap;

import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.ExtraInfo;
import me.gberg.matterdroid.model.Member;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.TeamBus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MembersManager {

    private final ErrorParser errorParser;
    private final TeamBus bus;
    private final Team team;
    private final TeamAPI teamApi;

    private Channel channel;
    private HashMap<String, Member> members;

    public MembersManager(final ErrorParser errorParser,
                          final TeamBus bus,
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
            this.members = null;
        }

        // Load the extra info for this channel.
        Observable<Response<ExtraInfo>> initialLoadObservable = teamApi.extraInfo(team.id(), channel.id());
        initialLoadObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<ExtraInfo>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Timber.e(e, "etra info API returned an error.");
                    }

                    @Override
                    public void onNext(final Response<ExtraInfo> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            Timber.e("Extra Info Error: " + apiError.statusCode() + apiError.detailedError());
                        }

                        // If the channel ID doesn't match, throw away the stale response.
                        final ExtraInfo body = response.body();
                        if (!body.id().equals(MembersManager.this.channel.id())) {
                            return;
                        }

                        // Save to the manager.
                        members = new HashMap<String, Member>();
                        for (final Member member: body.members()) {
                            members.put(member.id(), member);
                        }

                        // TODO: Emit a message once someone cares.
                    }
                });
    }
}
