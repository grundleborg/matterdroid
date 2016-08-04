package me.gberg.matterdroid.api;

import me.gberg.matterdroid.model.Channels;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface TeamAPI {
    @GET("api/v3/teams/{teamId}/channels/")
    Observable<Response<Channels>> channels(@Path("teamId") String teamId);
}
