package com.kashyapjha.wifichat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG ="WiFiChat" ;
    private static final String SERVICE_TYPE ="_http._tcp." ;
    NsdManager.RegistrationListener mRegistrationListener;
    String mServiceName;
    String ip;
    NsdManager mNsdManager;
    public ListView lvUsers;
    public ArrayList<String> ipList=new ArrayList<String>();
    public ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i=new Intent(getApplicationContext(),ChatServerService.class);
        startService(i);
        lvUsers=(ListView)findViewById(R.id.lvUsers);
        lvUsers.setBackgroundColor(Color.BLACK);
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        ip = "/"+Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1,ipList);
        initializeRegistrationListener();
        registerService(60000);
    }

    public void initializeRegistrationListener()
    {
        mRegistrationListener = new NsdManager.RegistrationListener()
        {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo)
            {
                mServiceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode)
            {
                Log.d(TAG,"Registration failed");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0)
            {
                Log.d(TAG,"Service unregistered");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode)
            {
                Log.d(TAG,"Unregistration failed");
            }
        };
    }

    public void registerService(int port)
    {
        Log.d(TAG, "Registering Service");
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(TAG);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);
        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        Log.d(TAG, "Registered Service");
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
        switch(id)
        {
            case R.id.refresh:
            {
                Toast.makeText(getApplicationContext(),"Discovering devices", Toast.LENGTH_SHORT);
                discover();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void discover()
    {
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener()
        {
            @Override
            public void onDiscoveryStarted(String regType)
            {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(final NsdServiceInfo service)
            {
                Log.d(TAG, "Service found " + service);
                String serviceType=service.getServiceType();
                String serviceName=service.getServiceName();
                if (!serviceType.equals(SERVICE_TYPE))
                {
                    Log.d(TAG, "Unknown Service Type: " + serviceType);
                }

                else if(serviceName.contains("WiFiChat") || serviceName.equals(mServiceName))
                {
                    mNsdManager.resolveService(service, new NsdManager.ResolveListener()
                    {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode)
                        {
                            Log.e(TAG,"Error: "+errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo)
                        {
                            String host=serviceInfo.getHost().toString();
                            int port=serviceInfo.getPort();
                            if(port==60000 && (!host.equals(ip) && !ipList.contains(host)))
                            {
                                ipList.add(host);
                                ipList.remove("/10.225.250.250");
                            }

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            Log.d(TAG, String.valueOf(ipList.size()));
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {

                                    lvUsers.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    lvUsers.setClickable(true);
                                    lvUsers.setLongClickable(true);
                                    lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                    {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                        {
                                            String IP = adapter.getItem(position);
                                            Intent i=new Intent(getApplicationContext(),ChatActivity.class);
                                            i.putExtra("IP",IP);
                                            startActivity(i);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service)
            {
                Log.e(TAG, "Service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType)
            {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode)
            {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode)
            {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        });
    }
}
