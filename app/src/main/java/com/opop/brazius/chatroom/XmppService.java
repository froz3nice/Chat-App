package com.opop.brazius.chatroom;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Juozas on 2018.03.12.
 */

public class XmppService extends Service implements ConnectionListener {

    AbstractXMPPConnection connection;
    private ConnectionInterface callback;
    private boolean connected;
    private boolean loggedin;
    private SharedPreferences prefs;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service", "created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("sevice", "bound");
        LocalBinder mBinder = new LocalBinder(this);
        return mBinder;
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

    public void disconnect() {
        if (connection.isConnected()) {
            connection.disconnect();
            prefs.edit().putBoolean("isAuthed",false).apply();
        } else {
            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_LONG).show();
        }
    }

    public void registerClient(AppCompatActivity activity) {
        callback = (ConnectionInterface) activity;
    }

    class RegisterUser extends AsyncTask<String, Void, Void> {

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
                AccountManager accountManager = AccountManager.getInstance(connection);
                accountManager.sensitiveOperationOverInsecureConnection(true);
                Map<String, String> attributes = new HashMap<String, String>(2);
                attributes.put("name", strings[2]);
                attributes.put("email", strings[3]);
                accountManager.createAccount(strings[0], strings[1], attributes);

                SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
                SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
                connection.login(strings[0], strings[1]);
                prefs.edit().putString("user",strings[0]).apply();
                prefs.edit().putString("pw",strings[1]).apply();
                ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
                reconnectionManager.enableAutomaticReconnection();
                ReconnectionManager.setEnabledPerDefault(true);
                callback.onLoggedIn();
            } catch (XMPPException | SmackException | IOException e) {
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
            Log.i(TAG, "Connecting to xmpp server finished...");
        }
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
                prefs.edit().putString("user",strings[0]).apply();
                prefs.edit().putString("pw",strings[1]).apply();
                ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
                reconnectionManager.enableAutomaticReconnection();
                ReconnectionManager.setEnabledPerDefault(true);
                callback.onLoggedIn();
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
            Log.i(TAG, "Connecting to xmpp server finished...");
        }
    }

    private void connect() throws IOException, XMPPException, SmackException {
        XMPPTCPConnectionConfiguration connConfig =
                XMPPTCPConnectionConfiguration.builder()
                        .setHost("pizokas.hopto.org")  // Name of your Host
                        .setServiceName("pizokas.hopto.org")
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setPort(5222)          // Your Port for accepting c2s connection
                        .setDebuggerEnabled(true)
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
            prefs.edit().putBoolean("isAuthed",true).apply();
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
