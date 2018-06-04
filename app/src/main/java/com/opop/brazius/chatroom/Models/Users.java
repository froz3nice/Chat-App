package com.opop.brazius.chatroom.Models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "users")
public class Users {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String getMyUsername() {
        return myUsername;
    }

    public Users(String notMyUsername, String myUsername) {
        this.myUsername = myUsername;
        this.notMyUsername = notMyUsername;

    }

    public String getUsername() {
        return notMyUsername;
    }

    @ColumnInfo(name = "myUsername")
    public String myUsername;

    public String getNotMyUsername() {
        return notMyUsername;
    }

    @ColumnInfo(name = "notMyUsername")
    public String notMyUsername;
}
