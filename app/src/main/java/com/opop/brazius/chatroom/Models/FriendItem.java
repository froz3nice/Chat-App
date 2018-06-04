package com.opop.brazius.chatroom.Models;

/**
 * Created by Juozas on 2018.03.12.
 */

public class FriendItem {
    public FriendItem(String friendName) {
        this.friendName = friendName;
    }

    public FriendItem(String friendName, boolean online) {
        this.friendName = friendName;
        this.online = online;
    }

    String friendName;
    boolean online;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
}
