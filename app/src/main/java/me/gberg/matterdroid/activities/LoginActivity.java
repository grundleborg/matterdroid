package me.gberg.matterdroid.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.gberg.matterdroid.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() called.");

        setContentView(R.layout.ac_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.co_login_submit)
    void onSubmitClicked() {
        Timber.v("Submit Clicked");
    }

}
