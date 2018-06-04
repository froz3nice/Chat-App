package com.opop.brazius.chatroom.Models;

/**
 * Created by Juozas on 2018.03.08.
 */

public class MyMessage {
    String message;
    Users sender;
    String createdAt;

    public MyMessage(String message, Users sender, String createdAt) {
        this.message = message;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Users getSender() {
        return sender;
    }

    public void setSender(Users sender) {
        this.sender = sender;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String setCreatedAt(String createdAt) {
       return this.createdAt = createdAt;
    }
}
