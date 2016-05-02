package com.kashyapjha.wifichat;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileServerService extends Service
{
    String imgpath= "/sdcard/wifichat/Images/Sent";
    public String ip;
    static final int socketServerPort = 4096;
    String TAG="WiFiChat_FileServerService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    public void onCreate()
    {
        Log.d(TAG, "onCreate: File Service Started");
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
        Log.d(TAG, "onStartCommand: Starting fileSocketServerThread");
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                new FileSocketServerThread().run();
            }
        }).start();
        return Service.START_STICKY;
    }

    private class FileSocketServerThread extends Thread
    {
        @Override
        public void run()
        {
            Log.d(TAG, "FileSocketServerThread.run: socketServerThread running");

            try
            {
                ServerSocket serverSocket=null;
                DataInputStream dis=null;
                FileOutputStream fos=null;
                while (true)
                {
                    String filename= new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".jpg";

                    File path=new File("/sdcard/wifichat/images/sent");
                    path.mkdirs();

                    serverSocket = new ServerSocket(socketServerPort);
                    Log.d(TAG, "FileSocketServerThread.run: Server started on port " + serverSocket.getLocalPort());
                    Socket socket = serverSocket.accept();
                    //sleep(10000);
                    dis=new DataInputStream(socket.getInputStream());
                    int len=dis.readInt();
                    Log.d(TAG, "Size of InputStream: "+len);
                    if(len>0)
                    {
                        int i=0;
                        byte[] bytes =new byte[len];
                        dis.readFully(bytes,0,bytes.length);
                        Log.d(TAG, "FileSocketServerThread.run: Size of received byte array: " + bytes.length);
                        Log.d(TAG, "FileSocketServerThread.run: Received byte array: ");
                        for(i=0;i<bytes.length-1;i++);
                        {
                            Log.d(TAG, "bytes: "+bytes[i]);
                        }
                        fos=new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/wifichat/images/"+filename);
                        fos.write(bytes);
                        fos.close();
                    }

                    ip=socket.getLocalAddress().toString();
                    Log.d(TAG, "FileSocketServerThread.run: Started accepting connections");

                    sendMessage();
                    notifyMessage();
                    socket.close();
                    serverSocket.close();
                    Log.d(TAG, "FileSocketServerThread.run: Closed socket");
                }
            }

            catch (IOException e)
            {
                Log.d(TAG, "Printing Stacktrace: ");
                e.printStackTrace();
            } //catch (InterruptedException e) {
                //e.printStackTrace();
            //}
        }
    }
    private void sendMessage()
    {
        Intent intent = new Intent("my-event");
        intent.putExtra("message", "data");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void notifyMessage()
    {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.message_icon);
        mBuilder.setContentTitle("Message from "+ip);
        mBuilder.setContentText("Image received");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1,mBuilder.build());
    }
}