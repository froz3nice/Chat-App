package com.opop.brazius.chatroom;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isAuth = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isAuthed",false);
        if(isAuth){
            startActivity(new Intent(this,SelectChatActivity.class));
        }
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
}
