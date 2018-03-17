package com.opop.brazius.chatroom.Models;

/**
 * Created by Juozas on 2018.03.08.
 */

public class User {
    String nickname;
    String profileUrl;

    public User(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
