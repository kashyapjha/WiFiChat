package com.kashyapjha.wifichat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class ChatActivity extends AppCompatActivity
{
    ArrayList<Message> messagesList;
    CustomAdapter adapter;
    String messages;
    int serverSocketPort=2048;
    int fileServerSocketPort=4096;
    String chatIP;
    ListView lvChat;
    EditText etChatbox;
    Button btnSend;
    String TAG="WiFiChat";
    String ownIP;
    String path;

    public void log(String msg)
    {
        Log.d(TAG,msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messagesList=new ArrayList<Message>();

        adapter=new CustomAdapter(getApplicationContext(),messagesList);

        lvChat=(ListView)findViewById(R.id.lvChat);
        lvChat.setDividerHeight(0);
        lvChat.setDivider(null);
        lvChat.setAdapter(adapter);

        Intent i=getIntent();
        chatIP =i.getStringExtra("IP").substring(1);

        path=getApplicationContext().getDir("chats",MODE_PRIVATE).getPath()+"/"+chatIP;

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        ownIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        log("onCreate: Chat with host "+chatIP);

        etChatbox=(EditText)findViewById(R.id.etChatbox);

        btnSend=(Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                log("btnSend.onClick: Send button clicked");
                String message = etChatbox.getText().toString();
                etChatbox.setText("");
                String senderIP = ownIP;
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                JSONObject msgObject = new JSONObject();
                try
                {
                    msgObject.put("Message", message);
                    msgObject.put("Sender IP", senderIP);
                    msgObject.put("Timestamp", timeStamp);
                    msgObject.put("isMe",true);
                    log("btnSend.onClick: Message object created");
                }
                catch (JSONException e)
                {
                    log("btnSend.onClick: " + e.toString());
                    log("btnSend.onClick: Message object not created");
                }
                MessageWriter.writeSentMessage(msgObject, path);
                sendMsg(msgObject);
                readMessages();
                lvChat.setSelection(adapter.getCount() - 1);
            }
        });
        readMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendMsg(JSONObject msgObject)
    {
        log("sendMsg: Start sendMsg");
        new SocketServerTask().execute(msgObject);
        readMessages();
    }

    public void readMessages()
    {
        log("readMessages started");
        File file = new File(path);
        if (file.exists())
        {
            try
            {
                Scanner fr=new Scanner(file);
                    fr.useDelimiter("\\Z"); // \Z means EOF.
                messages = fr.next();
                fr.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        else
        {
            messages="No chats to display\n";
        }
        String[] messagesArray=messages.split("\n");
        messagesList.clear();
        int count;
        try
        {
            log("Iterating through messages array");
            for(count=0;count<messagesArray.length;count++)
            {
                log("Parsing "+messagesArray[count]);
                if (messagesArray[count].startsWith("Me"))
                    messagesList.add(new Message(messagesArray[count],true));

                else messagesList.add(new Message(messagesArray[count],false));

            }
            log("messagesList populated");

            adapter.notifyDataSetChanged();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private class SocketServerTask extends AsyncTask<JSONObject, Void, Void>
    {
        private JSONObject jsonData;
        private boolean success;

        @Override
        protected Void doInBackground(JSONObject... params)
        {
            log("SocketServerTask.doInBackground: doInBackground started");
            DataOutputStream dataOutputStream = null;
            Socket socket = null;
            jsonData = params[0];
            try
            {
                socket = new Socket(chatIP, serverSocketPort);
                log("SocketServerTask.doInBackground: Created socket");
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(jsonData.toString());
                dataOutputStream.flush();
                dataOutputStream.close();
                socket.close();
                log("SocketServerTask.doInBackground: Sending data " + jsonData.toString());
                success=true;
            }
            catch (IOException e)
            {
                log("SocketServerTask.doInBackground: "+e.toString());
                success = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if(success)
                log("onPostExecute: Message "+jsonData.toString()+" sent");
            else log("onPostExecute: Message"+jsonData.toString()+" not sent");
        }
    }

    public void onResume()
    {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            readMessages();
            lvChat.setSelection(adapter.getCount()-1);
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}
