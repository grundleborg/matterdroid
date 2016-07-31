package me.gberg.matterdroid.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.api.LoginAPI;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.LoginRequest;
import me.gberg.matterdroid.model.ServerConnectionParameters;
import me.gberg.matterdroid.model.User;
import me.gberg.matterdroid.utils.api.HttpHeaders;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.co_login_server)
    TextView serverView;

    @BindView(R.id.co_login_email)
    TextView emailView;

    @BindView(R.id.co_login_password)
    TextView passwordView;

    @BindView(R.id.co_login_submit)
    Button submitView;

    @Inject
    Gson gson;

    @Inject
    App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() called.");

        ((App) getApplication()).getLoginComponent().inject(this);

        setContentView(R.layout.ac_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        // When the password is entered and Done is clicked on the keyboard, submit the form.
        passwordView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitView.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.co_login_submit)
    void onSubmitClicked() {
        Timber.v("Submit Clicked");

        if (!validateForm()) {
            Timber.v("Form is not valid.");
            return;
        }

        Timber.v("Form is valid.");

        final String server = serverView.getText().toString();
        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        final ErrorParser errorParser = new ErrorParser(retrofit);

        LoginAPI loginService = retrofit.create(LoginAPI.class);
        LoginRequest loginRequest = new LoginRequest(email, password, null);
        Observable<Response<User>> loginObservable = loginService.login(loginRequest);
        loginObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<User>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        // Unhandled error. Log it.
                        Timber.e(e, e.getMessage());
                    }

                    @Override
                    public void onNext(Response<User> response) {

                        // Handle HTTP error response codes that we recognise.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            if (apiError.is(APIError.LOGIN_UNRECOGNISED_EMAIL)) {
                                // Invalid email address.
                                setUnrecognisedEmailError();
                                return;
                            } else if (apiError.is(APIError.LOGIN_WRONG_PASSWORD)) {
                                // Invalid password.
                                setWrongPasswordError();
                                return;
                            }
                            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode + " with error id " + apiError.id);
                            return;
                        }

                        // We have logged in successfully.
                        User user = response.body();
                        Timber.i("Logged in successfully with User ID: " + user.id);

                        // Create the UserComponent.
                        String token = response.headers().get(HttpHeaders.TOKEN);
                        ServerConnectionParameters serverConnectionParameters = new ServerConnectionParameters(server, token);
                        app.createUserComponent(user, serverConnectionParameters);

                        // Advance to the Choose Team activity and finalise this one.
                        Intent intent = new Intent(LoginActivity.this, ChooseTeamActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    /**
     * Validates the login form, and provides visual feedback of any problems.
     *
     * @return true if the form is valid, otherwise false.
     */
    private boolean validateForm() {
        boolean valid = true;

        // Check the server field contains a valid HTTP or HTTPS URL.
        final String serverText = serverView.getText().toString();
        if (!URLUtil.isHttpUrl(serverText) && !URLUtil.isHttpsUrl(serverText)) {
            valid = false;
            serverView.setError(getResources().getString(R.string.co_login_server_error));
        }

        // Check the email field contains a valid-looking email address.
        final Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        if (!emailPattern.matcher(emailView.getText()).matches()) {
            valid = false;
            emailView.setError(getResources().getString(R.string.co_login_email_error));
        }

        // Check the password field contains at least 1 character.
        final String passwordText = passwordView.getText().toString();
        if (passwordText.length() < 1) {
            valid = false;
            passwordView.setError(getResources().getString(R.string.co_login_password_error));
        }

        return valid;
    }

    private void setUnrecognisedEmailError() {
        emailView.setError(getResources().getString(R.string.co_login_api_error_unrecognised_email));
        emailView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(emailView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void setWrongPasswordError() {
        passwordView.setError(getResources().getString(R.string.co_login_api_error_wrong_password));
        passwordView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(passwordView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

}
