package me.gberg.matterdroid.api;

import java.util.Map;

import me.gberg.matterdroid.model.InitialLoad;
import me.gberg.matterdroid.model.User;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface UserAPI {
    @GET("api/v3/users/initial_load")
    Observable<Response<InitialLoad>> initialLoad();

    @GET("api/v3/users/me")
    Observable<Response<User>> me();

    @GET("api/v3/users/profiles/{teamId}")
    Observable<Response<Map<String, User>>> users(@Path("teamId") String teamId);
}
