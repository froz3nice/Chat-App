package com.opop.brazius.chatroom;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.opop.brazius.chatroom.Models.MessagesDBModel;
import com.opop.brazius.chatroom.Models.Users;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface DaoAccess {
    @Insert
    void insertOnlySingleMessage (MessagesDBModel messageObj);

    @Insert
    void insertMultipleMessages (ArrayList<MessagesDBModel> moviesList);

    @Query("SELECT * FROM users where myUsername = :myUsername")
    List<Users> fetchAllUsersFriends (String myUsername);

    @Query("SELECT * FROM messages where myUsername = :myUsername and notMyUsername = :notMyUsername")
    List<MessagesDBModel> fetchChatMessages (String myUsername,String notMyUsername);

    //checking if username exists
    @Query("SELECT notMyUsername FROM users where notMyuserName = :notMyuserName" +
            " and myUsername = :myUsername")
    String getUserName (String notMyuserName,String myUsername);

    @Update
    void updateMessage (MessagesDBModel movies);

    @Delete
    void deleteMessage (MessagesDBModel movies);

    @Insert
    void insertUser(Users users);
}
