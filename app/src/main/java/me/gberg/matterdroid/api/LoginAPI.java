package me.gberg.matterdroid.api;

import me.gberg.matterdroid.model.LoginRequest;
import me.gberg.matterdroid.model.User;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by gberg on 29/07/16.
 */
public interface LoginAPI {
    @POST("api/v3/users/login")
    Observable<User> login(@Body LoginRequest body);
}
