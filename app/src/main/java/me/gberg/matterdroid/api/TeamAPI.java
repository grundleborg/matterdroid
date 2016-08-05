package me.gberg.matterdroid.api;

import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.model.Posts;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface TeamAPI {
    @GET("api/v3/teams/{teamId}/channels/")
    Observable<Response<Channels>> channels(@Path("teamId") String teamId);

    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/page/{page}/{count}")
    Observable<Response<Posts>> posts(
            @Path("teamId") String teamId,
            @Path("channelId") String channelId,
            @Path("page") int page,
            @Path("count") int count
    );
}
