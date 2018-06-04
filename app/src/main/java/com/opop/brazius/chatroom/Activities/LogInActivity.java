package com.opop.brazius.chatroom.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.opop.brazius.chatroom.ConnectionInterface;
import com.opop.brazius.chatroom.Models.MessagesDBModel;
import com.opop.brazius.chatroom.R;
import com.opop.brazius.chatroom.XmppService;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;


public class LogInActivity extends AppCompatActivity implements ConnectionInterface {

    private EditText _usernameText;
    private EditText _passwordText;
    private Button _loginButton;
    private AbstractXMPPConnection connection;
    private String TAG = "connection";
    private String username;
    private String password;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        _usernameText = findViewById(R.id.input_username);
        _passwordText = findViewById(R.id.input_password);
        _loginButton = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.bringToFront();
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                attemptLogin();
            }
        });
        Intent intent = new Intent(this, XmppService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }


    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
    }

    private void attemptLogin() {
        username = _usernameText.getText().toString().toLowerCase();
        password = _passwordText.getText().toString();

        if (!validFields()) {
            //This is where the login login is fired up
            //new XmppLogic().setConnection(this);

            if (isBound) {
                xmppService.registerClient(this);
                xmppService.login(username, password);
            }

            _loginButton.setEnabled(false);
        }else{
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private XmppService xmppService;
    private boolean isBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            XmppService.LocalBinder binder = (XmppService.LocalBinder) service;
            xmppService = binder.getService(); //<--------- from here on can access service!
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            xmppService = null;
            isBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    private boolean validFields() {
        // Reset errors.
        _usernameText.setError(null);
        _passwordText.setError(null);

        // Store values at the time of the login attempt.


        boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            _passwordText.setError(getString(R.string.error_invalid_password));
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            _usernameText.setError(getString(R.string.error_field_required));
            cancel = true;
        }
        return cancel;
    }

    @Override
    public void onLoginException() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                _loginButton.setEnabled(true);
                Toast.makeText(xmppService, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectionExcepion() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                _loginButton.setEnabled(true);
                Toast.makeText(xmppService, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLoggedIn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(LogInActivity.this, SelectChatActivity.class));
            }
        });

    }

    @Override
    public void onRegisterException() {

    }
}
