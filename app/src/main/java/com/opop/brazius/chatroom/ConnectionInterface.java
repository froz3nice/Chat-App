package com.opop.brazius.chatroom;

import org.jivesoftware.smack.AbstractXMPPConnection;

/**
 * Created by Juozas on 2018.03.12.
 */

public interface ConnectionInterface {
    void onLoginException();
    void onConnectionExcepion();
    void onLoggedIn();
}
