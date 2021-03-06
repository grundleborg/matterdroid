package me.gberg.matterdroid.api;

import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.model.ExtraInfo;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.model.Posts;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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

    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/{lastPostId}/before/{page}/{count}")
    Observable<Response<Posts>> postsBefore(
            @Path("teamId") String teamId,
            @Path("channelId") String channelId,
            @Path("lastPostId") String lastPostId,
            @Path("page") int page,
            @Path("count") int count
    );

    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/since/{time}")
    Observable<Response<Posts>> postsSince(
            @Path("teamId") String teamId,
            @Path("channelId") String channelId,
            @Path("time") long time
    );

    @GET("api/v3/teams/{teamId}/channels/{channelId}/extra_info")
    Observable<Response<ExtraInfo>> extraInfo(
            @Path("teamId") String teamId,
            @Path("channelId") String channelId
    );

    @POST("api/v3/teams/{teamId}/channels/{channelId}/posts/create")
    Observable<Response<Post>> createPost(
            @Path("teamId") String teamId,
            @Path("channelId") String channelId,
            @Body Post post
    );
}
