package com.opop.brazius.chatroom;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.opop.brazius.chatroom.Activities.ChatActivity;
import com.opop.brazius.chatroom.Activities.FirstActivity;
import com.opop.brazius.chatroom.Models.MessagesDBModel;
import com.opop.brazius.chatroom.Models.Users;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jivesoftware.smackx.xdata.Form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Juozas on 2018.03.12.
 */

public class XmppService extends Service implements ConnectionListener {

    private static final String DATABASE_NAME = "messages_db2";
    private MessageDatabase database;
    private static final String CHANNEL_ID = "notific";

    AbstractXMPPConnection connection;
    private ConnectionInterface callback;
    private IUpdateRecyclerView updateCallback;

    private boolean connected;
    private boolean loggedin;
    private final int notificationId = 420;

    private SharedPreferences prefs;
    private boolean isListenerRegistered = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service", "created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        database = Room.databaseBuilder(this,
                MessageDatabase.class, DATABASE_NAME)
                .build();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("sevice", "bound");
        LocalBinder mBinder = new LocalBinder(this);
        return mBinder;
    }

    public void createNotification(String body, String notMyUsername) {
        int notifyID = 1;
        CharSequence name = getString(R.string.channel_name);// The user-visible name of the channel.
        NotificationChannel mChannel = null;
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notMyUsername)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", notMyUsername);
        intent.putExtras(bundle);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        if (mNotificationManager != null) {
            mNotificationManager.notify(notificationId, mBuilder.build());
        }
    }

    public class LocalBinder extends Binder {
        XmppService service;

        public LocalBinder(XmppService service) {
            this.service = service;
        }

        public XmppService getService() {
            return service;
        }
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    public void regiser(String username, String password, String name, String email) {
        String[] myTaskParams = {username, password, name, email};
        new RegisterUser().execute(myTaskParams);
    }

    public void login(final String user, final String pass) {
        String[] myTaskParams = {user, pass};
        new LoginToXmppServer().execute(myTaskParams);
    }

    public void registerViewUpdater(AppCompatActivity activity){
        updateCallback = (IUpdateRecyclerView) activity;
    }

    public void registerClient(AppCompatActivity activity) {
        callback = (ConnectionInterface) activity;
    }

    class RegisterUser extends AsyncTask<String, Void, Void> {

        private final String TAG = "IFConnected";

        @Override
        protected Void doInBackground(String... strings) {
            if (android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();
            boolean connected = false;
            try {
                connect();
                connected = true;
            } catch (XMPPException | SmackException | IOException e) {
                e.printStackTrace();
                callback.onConnectionExcepion();
                connected = false;
            }
            try {
                AccountManager accountManager = AccountManager.getInstance(connection);
                accountManager.sensitiveOperationOverInsecureConnection(true);
                Map<String, String> attributes = new HashMap<String, String>(2);
                attributes.put("name", strings[2]);
                attributes.put("email", strings[3]);
                accountManager.createAccount(strings[0], strings[1], attributes);

                SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
                SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
                connection.login(strings[0], strings[1]);
                prefs.edit().putString("user", strings[0]).apply();
                prefs.edit().putString("pw", strings[1]).apply();
                ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
                reconnectionManager.enableAutomaticReconnection();
                ReconnectionManager.setEnabledPerDefault(true);
                String currentUsername = connection.getUser().split("@")[0];

                database.daoAccess().insertUser(new Users(strings[0], currentUsername));
                callback.onLoggedIn();
            } catch (XMPPException | SmackException | IOException e) {
                e.printStackTrace();
                if (connected) {
                    callback.onRegisterException();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i(TAG, "Connecting to xmpp server finished...");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SERVICE STOP","sERVICE STOP");
    }

    class LoginToXmppServer extends AsyncTask<String, Void, Void> {

        private final String TAG = "IFConnected";

        @Override
        protected Void doInBackground(String... strings) {
            try {
                connect();
            } catch (XMPPException | SmackException | IOException e) {
                e.printStackTrace();
                callback.onConnectionExcepion();
            }
            try {
                SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
                SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
                connection.login(strings[0], strings[1]);
                ServiceDiscoveryManager sdm= ServiceDiscoveryManager.getInstanceFor(connection);

                Presence presence123 = new Presence(Presence.Type.available);
                presence123.setStatus("Available");
                try {
                    connection.sendStanza(presence123);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                prefs.edit().putString("user", strings[0]).apply();
                prefs.edit().putString("pw", strings[1]).apply();
                ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
                reconnectionManager.enableAutomaticReconnection();
                ReconnectionManager.setEnabledPerDefault(true);
                callback.onLoggedIn();

                if (database.daoAccess().getUserName(strings[0], strings[0]) == null
                        || database.daoAccess().getUserName(strings[0], strings[0]).equals("")) {
                    database.daoAccess().insertUser(new Users(strings[0], strings[0]));
                }

            } catch (XMPPException | IOException | SmackException e) {
                e.printStackTrace();
                callback.onLoginException();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(getConnection().isConnected()){
                registerMessageReceiver();
            }
        }
    }

    private void registerMessageReceiver(){
        if (!isListenerRegistered) {

            ChatManager.getInstanceFor(getConnection()).addChatListener(new ChatManagerListener() {
                @Override
                public void chatCreated(Chat chat, boolean createdLocally) {
                    //isListenerRegistered = true;
                    chat.addMessageListener(new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message message) {

                            if (message.getType() == Message.Type.chat && message.getBodies().size() != 0) {
                                //createToast(message.getBody());
                                String myUsername = getConnection().getUser().split("@")[0];
                                String notMyUsername = message.getFrom().split("@")[0];
                                insertMessage(message.getBody(), Utils.getCurrentTime(), false, myUsername, notMyUsername);
                                Log.d("MESSAGE: ",message.getBody()+"   "+notMyUsername);
                                createNotification(message.getBody(),notMyUsername);
                                if(updateCallback != null) {
                                    updateCallback.updateRecyclerView(message.getBody(), false, myUsername, notMyUsername);
                                }
                            }
                        }
                    });
                }
            });
        }
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

    private void connect() throws IOException, XMPPException, SmackException {
        XMPPTCPConnectionConfiguration connConfig =
                XMPPTCPConnectionConfiguration.builder()
                        .setHost("pizokas.hopto.org")  // Name of your Host
                        .setServiceName("pizokas.hopto.org")
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setPort(5222)          // Your Port for accepting c2s connection
                        .setDebuggerEnabled(true)
                        //.setSendPresence(false)
                        .build();
        //setXmppDomain(DomainBareJid xmppServiceDomain)

        connection = new XMPPTCPConnection(connConfig);
        //connection.setPacketReplyTimeout(1000);
        connection.addConnectionListener(XmppService.this);
        connection.connect();
    }

    @Override
    public void connected(XMPPConnection connection) {
        connected = true;
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.d("XmppAuth", "xmpp Type Main Authenticated() :" + connection.isAuthenticated());

        if (connection.isAuthenticated()) {
            prefs.edit().putBoolean("isAuthed", true).apply();
            ServerPingWithAlarmManager.getInstanceFor(connection).setEnabled(true);

            PingManager pingManager = PingManager.getInstanceFor(connection);
            pingManager.setPingInterval(10);

            try {
                pingManager.pingMyServer();
                pingManager.pingMyServer(true, 10);
                pingManager.pingServerIfNecessary();
                pingManager.registerPingFailedListener(new PingFailedListener() {
                    @Override
                    public void pingFailed() {
                        Log.d("Ping", "pingFailed");
                        //disconnect();
                        try {
                            connect();
                        } catch (XMPPException | IOException | SmackException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (SmackException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connectionClosed() {
        connected = false;
        loggedin = false;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        connected = false;
        loggedin = false;
    }

    @Override
    public void reconnectionSuccessful() {
        connected = true;
        loggedin = false;
    }

    @Override
    public void reconnectingIn(int seconds) {
        loggedin = false;
    }

    @Override
    public void reconnectionFailed(Exception e) {
        connected = false;
        // chat_created = false;
        loggedin = false;
        try {
            connection.connect();

        } catch (SmackException | IOException | XMPPException ex) {
            ex.printStackTrace();
        }
    }

}
