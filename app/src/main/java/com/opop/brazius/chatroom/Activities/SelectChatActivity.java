package com.opop.brazius.chatroom.Activities;

import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.opop.brazius.chatroom.Adapters.FriendsAdapter;
import com.opop.brazius.chatroom.IUpdateRecyclerView;
import com.opop.brazius.chatroom.MessageDatabase;
import com.opop.brazius.chatroom.Models.FriendItem;
import com.opop.brazius.chatroom.Models.MessagesDBModel;
import com.opop.brazius.chatroom.Models.Users;
import com.opop.brazius.chatroom.R;
import com.opop.brazius.chatroom.Utils;
import com.opop.brazius.chatroom.XmppService;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import java.util.ArrayList;
import java.util.Collection;

public class SelectChatActivity extends AppCompatActivity {

    private static final String DATABASE_NAME = "messages_db2";
    private MessageDatabase database;

    private Context context;
    private FloatingActionButton fab;
    private String friendName = "";
    private ListView friendList;
    private ArrayList<FriendItem> friends;
    private FriendsAdapter adapter;
    AbstractXMPPConnection connection;
    private final String DNS_NAME = "pizokas.hopto.org";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chat);
        context = this;
        database = Room.databaseBuilder(context,
                MessageDatabase.class, DATABASE_NAME)
                .build();

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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addUserToList();
                                    }
                                });
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
                xmppService.getConnection().disconnect();
                stopService(new Intent(this,XmppService.class));
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("user","").apply();
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pw","").apply();
                startActivity(new Intent(SelectChatActivity.this,FirstActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
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
            new FetchUsersAsync().execute();
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
                String userName = data.getRows().get(0).getValues("jid").get(0).split("@")[0];
                if (userName.equals(friendName)) {
                    userExists = true;
                }
            }
        } catch (SmackException | XMPPException e) {
            e.printStackTrace();
            userExists = false;
        }
        return userExists;
    }

    class FetchUsersAsync extends AsyncTask<Void, Void, ArrayList<Users>> {
        @Override
        protected ArrayList<Users> doInBackground(Void... params) {
            String currentUsername = xmppService.getConnection().getUser().split("@")[0];
            return (ArrayList) database.daoAccess().fetchAllUsersFriends(currentUsername);
        }

        @Override
        protected void onPostExecute(ArrayList<Users> users) {
            if (users != null) {
                Roster roste = Roster.getInstanceFor(xmppService.getConnection());
                roste.addRosterListener(new RosterListener() {
                    // Ignored events public void entriesAdded(Collection<String> addresses) {}
                    public void entriesDeleted(Collection<String> addresses) {
                    }

                    @Override
                    public void entriesAdded(Collection<String> addresses) {
                    }

                    public void entriesUpdated(Collection<String> addresses) {
                    }

                    public void presenceChanged(Presence presence) {
                        try {
                            Presence pres = new Presence(Presence.Type.subscribed);
                            pres.setTo(presence.getFrom());
                            xmppService.getConnection().sendStanza(pres);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Collection<RosterEntry> entries = roste.getEntries();
                for (RosterEntry entry : entries) {
                    System.out.println(entry);
                }
                for (Users user : users) {
                    if (!user.getMyUsername().equals(user.getNotMyUsername())) {
                        addUserWithPresence(user);
                    }
                }
            }
            setAdapter();
        }
    }

    private void addUserWithPresence(Users user){
        Roster roster = Roster.getInstanceFor(xmppService.getConnection());
        Presence presence = new Presence(Presence.Type.subscribe);
        String jid = user.getNotMyUsername() + "@" + DNS_NAME;
        presence.setMode(Presence.Mode.available);
        presence.setTo(jid);
        try {
            xmppService.getConnection().sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        Presence availability = roster.getPresence(jid);
        friends.add(new FriendItem(user.getUsername(), availability.isAvailable()));

    }

    private void insertUserToDB(final String userName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String currentUsername = connection.getUser().split("@")[0];
                if (validateAddedName(userName, currentUsername)) {
                    return;
                }
                database.daoAccess().insertUser(new Users(userName, currentUsername));
                friends.add(new FriendItem(friendName));
            }
        }).start();
    }

    private boolean validateAddedName(String userName, String currentUsername) {
        if (containsThisUserName(userName)) {
            createToast("Friend already in list");
            return true;
        }

        if (friendName.equals(currentUsername)) {
            createToast("Can't add yourself");
            return true;
        }
        return false;
    }

    private void createToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean containsThisUserName(String userName) {
        ArrayList<String> tempList = new ArrayList<>();
        for (FriendItem item : friends) {
            tempList.add(item.getFriendName());
            if (tempList.contains(userName)) {
                return true;
            }
        }
        return false;
    }

    private void addUserToList() {
        insertUserToDB(friendName);
        setAdapter();
    }

    private void setAdapter() {
        adapter = new FriendsAdapter(SelectChatActivity.this, R.layout.friend_list_item, friends);
        adapter.notifyDataSetChanged();
        friendList.setAdapter(adapter);
    }

    private void addOnListClickListener() {
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendItem o = friends.get(position);
                //Toast.makeText(getBaseContext(), o.getFriendName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SelectChatActivity.this, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", o.getFriendName());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


}
