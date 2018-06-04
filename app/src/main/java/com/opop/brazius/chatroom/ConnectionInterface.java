package com.opop.brazius.chatroom;

import org.jivesoftware.smack.AbstractXMPPConnection;


public interface ConnectionInterface {
    void onLoginException();
    void onConnectionExcepion();
    void onLoggedIn();

    void onRegisterException();
}
