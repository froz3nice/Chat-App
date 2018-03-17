package com.opop.brazius.chatroom;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.io.IOException;
import java.util.ArrayList;

public class SelectChatActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private String friendName = "";
    private ListView friendList;
    private ArrayList<FriendItem> friends;
    private FriendsAdapter adapter;
    AbstractXMPPConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chat);
        fab = findViewById(R.id.fab);
        friendList = findViewById(R.id.list_friends);
        friends = new ArrayList<>();
        Intent intent = new Intent(this, XmppService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        addOnListClickListener();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectChatActivity.this);
                builder.setTitle("Type friend username");

                final EditText input = new EditText(SelectChatActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Add user", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        friendName = input.getText().toString().toLowerCase();

                        if (isBound) {
                            connection = xmppService.getConnection();
                            if (userExists(connection)) {
                                addUserToList();
                            } else {
                                Toast.makeText(SelectChatActivity.this, "This username does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_logout:
                xmppService.disconnect();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null) {
            unbindService(mConnection);
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

    private boolean userExists(AbstractXMPPConnection con) {
        boolean userExists = false;

        try {
            UserSearchManager search = new UserSearchManager(con);
            Form searchForm = search
                    .getSearchForm("search." + con.getServiceName());

            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", friendName);
            ReportedData data = search
                    .getSearchResults(answerForm, "search." + con.getServiceName());

            if (data.getRows().size() != 0) {
                userExists = true;
            }
        } catch (SmackException | XMPPException e) {
            e.printStackTrace();
            userExists = false;
        }
        return userExists;
    }


    private void addUserToList() {
        friends.add(new FriendItem(friendName));
        adapter = new FriendsAdapter(SelectChatActivity.this, R.layout.friend_list_item, friends);
        friendList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void addOnListClickListener() {
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendItem o = friends.get(position);
                Toast.makeText(getBaseContext(), o.getFriendName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SelectChatActivity.this, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", o.getFriendName());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


}
