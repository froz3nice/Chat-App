package com.opop.brazius.chatroom.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.opop.brazius.chatroom.ConnectionInterface;
import com.opop.brazius.chatroom.R;
import com.opop.brazius.chatroom.XmppService;

import org.jivesoftware.smack.AbstractXMPPConnection;


public class RegisterActivity extends AppCompatActivity implements ConnectionInterface {

    private static final String TAG = "SignupActivity";

    EditText _nameText, _emailText, _passwordText, _usernameText;
    String username = "", name = "", email = "", password = "";
    Button _signupButton;
    TextView _loginLink;
    private ProgressBar progressBar;
    AbstractXMPPConnection connection;
    private BroadcastReceiver receiverRegistered;
    private BroadcastReceiver receiverException;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        _nameText = findViewById(R.id.input_name);
        _emailText = findViewById(R.id.input_email);
        _usernameText = findViewById(R.id.input_username);
        _passwordText = findViewById(R.id.input_password);
        _signupButton = findViewById(R.id.btn_signup);
        _loginLink = findViewById(R.id.link_login);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.bringToFront();
        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
                finish();
            }
        });
        Intent intent = new Intent(this, XmppService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    public void signup() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Signup");
        username = _usernameText.getText().toString().toLowerCase();
        name = _nameText.getText().toString().toLowerCase();
        email = _emailText.getText().toString().toLowerCase();
        password = _passwordText.getText().toString().toLowerCase();

        if (!validate()) {
            onSignupFailed();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        _signupButton.setEnabled(false);
        if (isBound) {
            xmppService.registerClient(this);
            xmppService.regiser(username, password, name, email);
        }

        //xmppLogic.setConnection(this);
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

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (username.isEmpty()) {
            _usernameText.setError("Please enter username");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() ) {
            _passwordText.setError("Please enter password");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _signupButton.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        _nameText.setText("");
        _usernameText.setText("");
        _emailText.setText("");
        _passwordText.setText("");
        if(mConnection != null){
            unbindService(mConnection);
        }
    }

    @Override
    public void onLoginException() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                _signupButton.setEnabled(true);
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
                _signupButton.setEnabled(true);
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
                startActivity(new Intent(RegisterActivity.this, SelectChatActivity.class));
            }
        });
    }

    @Override
    public void onRegisterException() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                _signupButton.setEnabled(true);
                Toast.makeText(xmppService, "Unable to register try different username", Toast.LENGTH_LONG).show();
            }
        });
    }
}
