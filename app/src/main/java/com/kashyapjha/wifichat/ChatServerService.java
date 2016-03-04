package com.kashyapjha.wifichat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class ChatServerService extends Service
{
    String TAG="WiFiChat_ChatServerService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
            return null;
    }
    public void onCreate()
    {
        Log.d(TAG,"Service Started");
    }
}