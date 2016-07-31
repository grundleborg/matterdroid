package me.gberg.matterdroid.di.modules;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.di.scopes.UserScope;
import me.gberg.matterdroid.model.ServerConnectionParameters;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    Gson providesGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Provides
    @UserScope
    Retrofit providesRetrofit(final Gson gson,
                              final ServerConnectionParameters serverConnectionParameters) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(final Chain chain) throws IOException {
                        Request request = chain.request();
                        request = request.newBuilder()
                                .header("Token", serverConnectionParameters.token)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(serverConnectionParameters.server)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit;
    }
}
