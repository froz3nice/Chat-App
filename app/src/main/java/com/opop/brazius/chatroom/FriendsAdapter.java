package com.opop.brazius.chatroom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juozas on 2018.03.12.
 */

public class FriendsAdapter extends ArrayAdapter<FriendItem>
{
    private final List<FriendItem> mItems;
    private final Context mContext;
    private final LayoutInflater inflater;

    public FriendsAdapter (Context context, int resourceId, ArrayList<FriendItem> items)
    {
        super(context, resourceId, items);
        mContext = context;
        mItems = items;
        inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.friend_list_item, parent,false);
            holder = new ViewHolder();
            holder.friendName = convertView.findViewById(R.id.friend_name);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        FriendItem item = getItem(position);
        if (item != null) {
            holder.friendName.setText(item.getFriendName());
        }

        return convertView;
    }

    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public FriendItem getItem(int position)
    {
        return mItems.get(position);
    }

    public class ViewHolder
    {
        TextView friendName;
    }
}
