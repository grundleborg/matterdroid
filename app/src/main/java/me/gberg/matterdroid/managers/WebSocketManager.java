package me.gberg.matterdroid.managers;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.gberg.matterdroid.model.PostDeletedMessage;
import me.gberg.matterdroid.model.PostEditedMessage;
import me.gberg.matterdroid.model.PostedMessage;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.model.WebSocketMessage;
import me.gberg.matterdroid.utils.api.HttpHeaders;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import timber.log.Timber;

public class WebSocketManager implements WebSocketListener {

    private Bus bus;
    private Team team;
    private OkHttpClient httpClient;
    private Gson gson;
    private SessionManager sessionManager;
    private ErrorParser errorParser;

    // FIXME: is this accessed from multiple threads?
    boolean connected = false;

    public WebSocketManager(Bus bus, Team team, Gson gson, SessionManager sessionManager,
                            ErrorParser errorParser) {
        this.bus = bus;
        this.team = team;
        this.gson = gson;
        this.sessionManager = sessionManager;
        this.errorParser = errorParser;

        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.SECONDS)
                .build();
    }

    public void connect() {
        Timber.d("connect()");
        if (!connected) {
            Timber.d("Attempting to connect to web socket.");

            Request request = new Request.Builder()
                    .url(sessionManager.getServer() + "/api/v3/users/websocket")
                    .header(HttpHeaders.AUTHORIZATION,
                            HttpHeaders.buildAuthorizationHeader(sessionManager.getToken()))
                    .build();

            WebSocketCall.create(httpClient, request).enqueue(this);
            connected = true;
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Timber.v("onOpen().");
    }

    @Override
    public void onMessage(ResponseBody responseBody) throws IOException {
        Timber.v("onMessage()");
        String payload = responseBody.string();
        Timber.v(payload);

        // Message received. Parse it and emit the appropriate event on the bus.
        WebSocketMessage message = gson.fromJson(payload, WebSocketMessage.class);
        if (message.action != null) {
            switch (message.action) {
                case "posted":
                    PostedMessage postedMessage = PostedMessage.create(gson, payload);
                    bus.sendWebSocketBus(postedMessage);
                    break;
                case "post_edited":
                    PostEditedMessage postEditedMessage = PostEditedMessage.create(gson, payload);
                    bus.sendWebSocketBus(postEditedMessage);
                    break;
                case "post_deleted":
                    PostDeletedMessage postDeletedMessage = PostDeletedMessage.create(gson, payload);
                    bus.sendWebSocketBus(postDeletedMessage);
                    break;
                default:
                    Timber.w("Message received with unhandled action: " + message.action);
                    break;
            }
        }

        responseBody.close();
    }

    @Override
    public void onPong(Buffer payload) {
        Timber.v("onPong()");
    }

    @Override
    public void onClose(int code, String reason) {
        Timber.v("onClose()");
    }

    @Override
    public void onFailure(IOException e, Response response) {
        Timber.v("onFailure()");
        Timber.w(e);
    }

}
