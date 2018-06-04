package com.opop.brazius.chatroom.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.opop.brazius.chatroom.ConnectionInterface;
import com.opop.brazius.chatroom.R;
import com.opop.brazius.chatroom.XmppService;

public class FirstActivity extends AppCompatActivity implements ConnectionInterface {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = new Intent(FirstActivity.this, XmppService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
       // xmppService.connect()
        setContentView(R.layout.activity_first);
        Button login = findViewById(R.id.login);
        Button signup = findViewById(R.id.signup);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,LogInActivity.class));
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,RegisterActivity.class));
            }
        });
    }

    private XmppService xmppService;
    private boolean isBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            XmppService.LocalBinder binder = (XmppService.LocalBinder) service;
            xmppService = binder.getService(); //<--------- from here on can access service!
            String username = prefs.getString("user","");
            String pw = prefs.getString("pw","");
            if(!username.equals("") && !pw.equals("")){
                xmppService.registerClient(FirstActivity.this);
                xmppService.login(username,pw);
            }
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

    @Override
    public void onLoginException() {

    }

    @Override
    public void onConnectionExcepion() {

    }

    @Override
    public void onLoggedIn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(FirstActivity.this, SelectChatActivity.class));
            }
        });
    }

    @Override
    public void onRegisterException() {

    }
}
