package me.gberg.matterdroid.api;

import me.gberg.matterdroid.model.InitialLoad;
import me.gberg.matterdroid.model.User;
import retrofit2.Response;
import retrofit2.http.GET;
import rx.Observable;

public interface UserAPI {
    @GET("api/v3/users/initial_load")
    Observable<Response<InitialLoad>> initialLoad();

    @GET("api/v3/users/me")
    Observable<Response<User>> me();
}
