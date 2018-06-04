package com.opop.brazius.chatroom.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.opop.brazius.chatroom.Adapters.MessageListAdapter;
import com.opop.brazius.chatroom.IUpdateRecyclerView;
import com.opop.brazius.chatroom.MessageDatabase;
import com.opop.brazius.chatroom.Models.MessagesDBModel;
import com.opop.brazius.chatroom.R;
import com.opop.brazius.chatroom.Utils;
import com.opop.brazius.chatroom.XmppService;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity  implements IUpdateRecyclerView {
    private static final String DATABASE_NAME = "messages_db2";
    private MessageDatabase database;

    private static final String DNS_NAME = "pizokas.hopto.org";
    private Context context;
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private EditText chatBox;
    private List<MessagesDBModel> myMessageList;
    private Button btnSend;
    private String notMyUsername;
    private String myUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String currentDBPath = getDatabasePath("messages_db2").getAbsolutePath();
        database = Room.databaseBuilder(context,
                MessageDatabase.class, DATABASE_NAME)
                .build();
        Bundle p = getIntent().getExtras();
        if (p != null) {
            notMyUsername = p.getString("name");
            setTitle(notMyUsername);
        }
        NotificationManager notifManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifManager != null) {
            notifManager.cancelAll();
        }
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);

        chatBox = findViewById(R.id.edittext_chatbox);
        btnSend = findViewById(R.id.button_chatbox_send);
        setMessageRecyclerView();


        Intent intent = new Intent(this, XmppService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        // mMessageRecycler.getRecycledViewPool().setMaxRecycledViews(0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
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

    private void fetchMessages() {
        OfflineMessageManager mOfflineMessageManager =
                new OfflineMessageManager(xmppService.getConnection());
        // Load all messages from the storage
        List<Message> messages = null;
        try {
            messages = mOfflineMessageManager.getMessages();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        for(Message msg : messages){
            String notMyUsername = msg.getFrom().split("@")[0];
            String myUsername = msg.getTo().split("@")[0];

            database.daoAccess().insertOnlySingleMessage(
                    new MessagesDBModel(msg.getBody(),
                            Utils.getCurrentTime(),
                            false,
                            myUsername,
                            notMyUsername));
        }
        Presence presence123 = new Presence(Presence.Type.available);
        presence123.setStatus("Available");
        try {
            xmppService.getConnection().sendStanza(presence123);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        new FetchMessagesAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    class FetchMessagesAsync extends AsyncTask<Void, List<MessagesDBModel>, List<MessagesDBModel>> {
        @Override
        protected List<MessagesDBModel> doInBackground(Void... voids) {
            return database.daoAccess().fetchChatMessages(myUsername, notMyUsername);
        }

        @Override
        protected void onPostExecute(List<MessagesDBModel> messages) {
            super.onPostExecute(messages);
            myMessageList = messages;
            mMessageAdapter = new MessageListAdapter(context, myMessageList);
            mMessageRecycler.setAdapter(mMessageAdapter);
            mMessageAdapter.notifyDataSetChanged();
        }
    }



    private void setMessageRecyclerView() {
        myMessageList = new ArrayList<>();
        //myMessageList.add(new MyMessage("dalbajobas kr", new Users("pizokas"), 15));
        // myMessageList.add(new MyMessage("dalbajob", new Users("pikas"), 20));

        mMessageAdapter = new MessageListAdapter(this, myMessageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setFocusable(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(manager);
    }

    private XmppService xmppService;
    private boolean isBound;
    private boolean isListenerRegistered;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            XmppService.LocalBinder binder = (XmppService.LocalBinder) service;
            xmppService = binder.getService(); //<--------- from here on can access service!
            xmppService.registerViewUpdater(ChatActivity.this);
            myUsername = xmppService.getConnection().getUser().split("@")[0];
            fetchMessages();
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = chatBox.getText().toString();
                    if (!text.equals("")) {
                        sendMessage(text);
                        chatBox.setText("");
                    }
                }
            });
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            xmppService = null;
            isBound = false;
        }
    };

    @Override
    public void updateRecyclerView(String body, boolean b, String myUsername, String notMyUsername) {
        mMessageAdapter.addMessage(new MessagesDBModel(body, Utils.getCurrentTime(), false, myUsername, notMyUsername));
        mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
    }

    private void insertMessage(final String message, final String time,
                               final boolean isSender, final String myUserName, final String notMyUsername) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MessagesDBModel messageObj = new MessagesDBModel();
                messageObj.setContent(message);
                messageObj.setTime(time);
                messageObj.setSender(isSender);
                messageObj.setMyUsername(myUserName);
                messageObj.setNotMyUsername(notMyUsername);

                database.daoAccess().insertOnlySingleMessage(messageObj);
            }
        }).start();
    }

    private void createToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String body) {
        try {
            String jid = notMyUsername + "@" + DNS_NAME;
            Chat chat = ChatManager.getInstanceFor(xmppService.getConnection())
                    .createChat(jid);
            chat.sendMessage(body);
            insertMessage(body, Utils.getCurrentTime(), true, myUsername, notMyUsername);
            mMessageAdapter.addMessage(new MessagesDBModel(body, Utils.getCurrentTime(), true, myUsername, notMyUsername));
            mMessageRecycler.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            Toast.makeText(context, "Not connected", Toast.LENGTH_SHORT).show();
        }
    }
}


