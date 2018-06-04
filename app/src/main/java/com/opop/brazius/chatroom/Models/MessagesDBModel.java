package com.opop.brazius.chatroom.Models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "messages")
public class MessagesDBModel {
    @PrimaryKey(autoGenerate = true)
    private int messageId;

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setUsername(String username) {
        this.myUsername = username;
    }

    public int getMessageId() {
        return messageId;
    }

    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "time")
    private String time;
    @ColumnInfo(name = "isSender")
    private boolean isSender;

    @ColumnInfo(name = "myUsername")
    public String myUsername;

    @ColumnInfo(name = "notMyUsername")
    public String notMyUsername;

    public String getMyUsername() {
        return myUsername;
    }

    public void setMyUsername(String myUsername) {
        this.myUsername = myUsername;
    }

    public String getNotMyUsername() {
        return notMyUsername;
    }

    public void setNotMyUsername(String notMyUsername) {
        this.notMyUsername = notMyUsername;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public MessagesDBModel() {
    }

    public MessagesDBModel(String content, String time, boolean isSender, String myUsername, String notMyUsername) {
        this.content = content;
        this.time = time;
        this.isSender = isSender;
        this.myUsername = myUsername;
        this.notMyUsername = notMyUsername;
    }

    public MessagesDBModel(String content) {
        this.content = content;
    }

    public int getId() {
        return messageId;
    }

    public void setId(int id) {
        this.messageId = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
