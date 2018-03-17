package com.opop.brazius.chatroom;

/**
 * Created by Juozas on 2018.03.12.
 */

class FriendItem {
    public FriendItem(String friendName) {
        this.friendName = friendName;
    }

    String friendName;

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
}
