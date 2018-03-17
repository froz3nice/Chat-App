package com.opop.brazius.chatroom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.opop.brazius.chatroom.Models.MyMessage;
import com.opop.brazius.chatroom.Models.User;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements ChatManagerListener, ChatMessageListener {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private EditText chatBox;
    private List<MyMessage> myMessageList;
    private Button btnSend;
    private String friendName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle p = getIntent().getExtras();
        if(p != null) {
            friendName = p.getString("name");
        }

        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        chatBox = findViewById(R.id.edittext_chatbox);
        btnSend = findViewById(R.id.button_chatbox_send);
        setmMessageRecyclerView();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = chatBox.getText().toString();
                if(!text.equals("")) {
                    sendMessage(text);
                }
            }
        });

        Intent intent = new Intent(this, XmppService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        // mMessageRecycler.getRecycledViewPool().setMaxRecycledViews(0, 0);
    }

    private void setmMessageRecyclerView(){
        myMessageList = new ArrayList<>();
        myMessageList.add(new MyMessage("dalbajobas krw  dfnsfjk ndfjk sldn\n /n lopenzofndofjn sdjfn/n gds s/nusdfhsdjkfhsdkjghskjghsfjkghsjkghgjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjhfjk gbsakghsrugsdfygvnsfjgnskhgrbygwriyfhasduyfyfg sduyfgsknfgqifwvihybefvgeiugvhnoiuwndkgveiughsfffffffffffffffffffffffffffffffffffffffffgggggggggggggggggggg", new User("pizokas"),15));
        myMessageList.add(new MyMessage("dalbajob", new User("pikas"),20));

        mMessageAdapter = new MessageListAdapter(this, myMessageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setFocusable(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(manager);
    }

    private XmppService xmppService;
    private boolean isBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            XmppService.LocalBinder binder = (XmppService.LocalBinder) service;
            xmppService = binder.getService(); //<--------- from here on can access service!
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            xmppService = null;
            isBound = false;
        }
    };

    private void sendMessage(String body) {
        try {
            ChatManager.getInstanceFor(xmppService.getConnection()).addChatListener(this);
            String jid = friendName + "@pizokas.hopto.org";
            Chat chat = ChatManager.getInstanceFor(xmppService.getConnection())
                    .createChat(jid);
            chat.sendMessage(body);
            myMessageList.add(new MyMessage(body,new User(friendName),20));
            mMessageAdapter = new MessageListAdapter(ChatActivity.this, myMessageList);
            mMessageAdapter.notifyDataSetChanged();
            mMessageRecycler.setAdapter(mMessageAdapter);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        chat.addMessageListener(this);

    }

    @Override
    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
        if (message.getType().equals(Message.Type.chat) || message.getType().equals(Message.Type.normal)) {
            if (message.getBody() != null) {
                String jid = message.getFrom();
                Toast.makeText(xmppService, "balvonas", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


