package me.gberg.matterdroid.utils.picasso;

import android.content.Context;
import android.widget.ImageView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

public class ProfileImagePicasso {
    private final static String ENDPOINT_START = "/api/v3/users/";
    private final static String ENDPOINT_END = "/image";

    private final String server;
    private Picasso picasso;

    public ProfileImagePicasso(final String server, final Context context, final OkHttpClient httpClient) {
        this.server = server;
        this.picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(httpClient))
                .build();
    }

    public void loadInto(final String userId, final ImageView imageView) {
        this.picasso.load(server + ENDPOINT_START + userId + ENDPOINT_END).into(imageView);
    }
}
