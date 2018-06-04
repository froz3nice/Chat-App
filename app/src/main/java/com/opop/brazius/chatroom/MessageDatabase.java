package com.opop.brazius.chatroom;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.opop.brazius.chatroom.Models.MessagesDBModel;
import com.opop.brazius.chatroom.Models.Users;

@Database(entities = {MessagesDBModel.class, Users.class}, version = 1, exportSchema = false)
public abstract class MessageDatabase extends RoomDatabase {
    public abstract DaoAccess daoAccess() ;

}
