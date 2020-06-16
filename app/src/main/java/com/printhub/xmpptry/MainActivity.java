package com.printhub.xmpptry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ArrayList<MessagesData> mMessagesData = new ArrayList<>();
    private AbstractXMPPConnection mConnection;
    public static String TAG = "ABCD";
    private EditText sendMessageEt;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rv);
        mAdapter = new Adapter(mMessagesData);
        sendMessageEt = findViewById(R.id.sendMessageEt);
        sendBtn = findViewById(R.id.send);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        DividerItemDecoration decoration = new DividerItemDecoration(this, manager.getOrientation());

        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        setConnection();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageSend = sendMessageEt.getText().toString();
                if(messageSend.length()>0){
                    sendMessage(messageSend,"subhashish@ec2-3-7-254-113.ap-south-1.compute.amazonaws.com");
                }
            }
        });

    }

    private void sendMessage(String messageSend, String entityBareId) {
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(entityBareId);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        if(mConnection != null) {
            ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
            Chat chat = chatManager.chatWith(jid);
            Message newMessage = new Message();
            newMessage.setBody(messageSend);
            try {
                chat.send(newMessage);
                MessagesData data = new MessagesData("send",messageSend);
                mMessagesData.add(data);
                mAdapter= new Adapter(mMessagesData);
                mRecyclerView.setAdapter(mAdapter);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setConnection(){
        // Create the configuration for this new connection

        new Thread(){
            @Override
            public void run() {


        InetAddress addr = null;
        try {
            addr = InetAddress.getByName("3.7.254.113");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return false;
            }
        };
        DomainBareJid serviceName = null;
        try {
            serviceName = JidCreate.domainBareFrom("3.7.254.113");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword("test@ec2-3-7-254-113.ap-south-1.compute.amazonaws.com","password")
                .setPort(5222)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setXmppDomain(serviceName)
                .setHostnameVerifier(verifier)
                .setHostAddress(addr)
                .setDebuggerEnabled(true)
                .build();
        mConnection = new XMPPTCPConnection(config);

        try {
            mConnection.connect();

            mConnection.login();
            if(mConnection.isAuthenticated() && mConnection.isConnected()){
                // Assume we've created an XMPPConnection name "connection"._
                Log.e(TAG,"run: auth done and connected successfully");
                ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
                chatManager.addIncomingListener(new IncomingChatMessageListener() {
                    @Override
                    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                        Log.e(TAG,"New message from " + from + ": " + message.getBody());

                        MessagesData data = new MessagesData("recieved", message.getBody().toString());
                        mMessagesData.add(data);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter = new Adapter(mMessagesData);
                                mRecyclerView.setAdapter(mAdapter);
                            }
                        });
                    }
                });
            }
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            }
        }.start();
    }
}