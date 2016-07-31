package me.gberg.matterdroid.di.modules;

import com.google.gson.Gson;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.di.scopes.UserScope;
import me.gberg.matterdroid.model.ServerConnectionParameters;
import me.gberg.matterdroid.utils.api.HttpHeaders;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gberg on 31/07/16.
 */
@Module
public class APIModule {

    @Provides
    @UserScope
    public Retrofit providesRetrofit(final Gson gson,
                              final ServerConnectionParameters serverConnectionParameters) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(final Chain chain) throws IOException {
                        Request request = chain.request();
                        request = request.newBuilder()
                                //.header("Token", serverConnectionParameters.token)
                                .header(HttpHeaders.COOKIE, new StringBuilder()
                                        .append(HttpHeaders.AUTH_COOKIE_NAME)
                                        .append("=")
                                        .append(serverConnectionParameters.token)
                                        .toString())
                                .build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(logging)
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(serverConnectionParameters.server)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit;
    }

    @Provides
    @UserScope
    public UserAPI providesUserAPI(final Retrofit retrofit) {
        return retrofit.create(UserAPI.class);
    }

    @Provides
    @UserScope
    public ErrorParser providesErrorParser(final Retrofit retrofit) {
        return new ErrorParser(retrofit);
    }
}
