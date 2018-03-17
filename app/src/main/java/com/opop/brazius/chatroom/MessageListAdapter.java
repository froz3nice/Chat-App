package com.opop.brazius.chatroom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.opop.brazius.chatroom.Models.MyMessage;

import java.util.List;

/**
 * Created by Juozas on 2018.03.08.
 */

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<MyMessage> mMyMessageList;

    public MessageListAdapter(Context context, List<MyMessage> myMessageList) {
        mContext = context;
        mMyMessageList = myMessageList;
    }

    @Override
    public int getItemCount() {
        return mMyMessageList.size();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        MyMessage myMessage = mMyMessageList.get(position);

        if (true) {
            // If the current user is the sender of the me
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the myMessage
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

       // if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
       // } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
       //     view = LayoutInflater.from(parent.getContext())
       //             .inflate(R.layout.item_message_received, parent, false);
       //     return new ReceivedMessageHolder(view);
       // }

       //return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyMessage myMessage = (MyMessage) mMyMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(myMessage);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(myMessage);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
        }

        void bind(MyMessage myMessage) {
            messageText.setText(myMessage.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(String.valueOf(myMessage.getCreatedAt()));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.text_message_name);
            profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }

        void bind(MyMessage myMessage) {
            messageText.setText(myMessage.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(String.valueOf(myMessage.getCreatedAt()));

            nameText.setText(myMessage.getSender().getNickname());

            // Insert the profile image from the URL into the ImageView.
            //Utils.displayRoundImageFromUrl(mContext, myMessage.getProfileUrl(), profileImage);
        }
    }
}
