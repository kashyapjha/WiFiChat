package com.kashyapjha.wifichat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServerService extends Service
{
    static final int socketServerPort = 2048;
    String TAG="WiFiChat_ChatServerService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
            return null;
    }
    public void onCreate()
    {
        Log.d(TAG, "onCreate: Service Started");
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "WiFiChat_Lock");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WiFiChat_Lock");

        wifiLock.acquire();
        wakeLock.acquire();

        Log.d(TAG, "onCreate: Acquired Wakelocks");

    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId)
    {
        //final Thread socketServerThread=new Thread(new SocketServerThread());
        Log.d(TAG, "onStartCommand: Starting socketServerThread");
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                new SocketServerThread().run();
            }
        }).start();
        return Service.START_STICKY;
    }

    private class SocketServerThread extends Thread
    {
        @Override
        public void run()
        {
            Log.d(TAG,"SocketServerThread.run: socketServerThread running");

            try
            {
                ServerSocket serverSocket=null;
                DataInputStream dataInputStream=null;

                while (true)
                {
                    serverSocket = new ServerSocket(socketServerPort);

                    Log.d(TAG,"SocketServerThread.run: Server started on port "+serverSocket.getLocalPort());

                    Socket socket = serverSocket.accept();

                    Log.d(TAG, "SocketServerThread.run: Message received");

                    dataInputStream = new DataInputStream(socket.getInputStream());
                    String messageFromClient=dataInputStream.readUTF();

                    final JSONObject msgObject=new JSONObject(messageFromClient);

                    String path=getApplicationContext().getDir("chats",MODE_PRIVATE).getPath()+socket.getInetAddress();

                    Log.d(TAG, path);

                    File file = new File(path);
                    if(!file.exists())
                    {
                        file.createNewFile();
                        FileWriter fw=new FileWriter(file);
                        fw.append("Them: "+msgObject.getString("Message"));
                        fw.append("\n");
                        fw.flush();
                        fw.close();
                        // write code for saving data to the file
                    }
                    else
                    {
                        FileWriter fw=new FileWriter(file,true);
                        fw.append("Them: "+msgObject.getString("Message"));
                        fw.append("\n");
                        fw.flush();
                        fw.close();
                    }

                    Log.d(TAG, "SocketServerThread.run: Message " + messageFromClient + " received from " + socket.getInetAddress() + ":" + socket.getPort());

                    sendMessage();

                    Handler h = new Handler(getMainLooper());

                    h.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Toast.makeText(getApplicationContext(), "Message received: "+msgObject.getString("Message"), Toast.LENGTH_SHORT).show();
                            }
                            catch (JSONException e)
                            {
                                Log.d(TAG, "SocketServerThread.run: "+e.toString());
                            }
                        }
                    });

                    Log.d(TAG, "SocketServerThread.run: Closing socket");
                    socket.close();
                    Log.d(TAG, "SocketServerThread.run: Closed socket");

                    Log.d(TAG, "SocketServerThread.run: Closing serverSocket");
                    serverSocket.close();
                    Log.d(TAG, "SocketServerThread.run: serverSocket closed");
                }
            }

            catch (IOException e)
            {
                Log.d(TAG, "SocketServerThread.run: "+e.toString());
            }

            catch (JSONException e)
            {
                Log.d(TAG, "SocketServerThread.run: "+e.toString());
            }
        }
    }
    private void sendMessage()
    {
        Intent intent = new Intent("my-event");
        intent.putExtra("message", "data");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}