package com.kashyapjha.wifichat;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG ="WiFiChat" ;
    private static final String SERVICE_TYPE ="_http._tcp." ;
    NsdManager.RegistrationListener mRegistrationListener;
    String mServiceName;
    NsdManager mNsdManager;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.ResolveListener mResolveListener;
    public ListView lvUsers;
    public ArrayList<String> ipList=new ArrayList<String>();
    public ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lvUsers=(ListView)findViewById(R.id.lvUsers);
        adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, ipList);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Start new chat
            }
        });
        initializeRegistrationListener();
        initializeDiscoveryListener();
        registerService(60000);
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        mResolveListener=new NsdManager.ResolveListener()
        {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode)
            {
                Log.e(TAG,"Error: "+errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo)
            {
                Log.d(TAG, serviceInfo.getHost().toString());
                Log.d(TAG, String.valueOf(serviceInfo.getPort()));
                if(serviceInfo.getPort()==60000/* && !ipList.contains(serviceInfo.getHost().toString())*/)
                    ipList.add(serviceInfo.getHost().toString());
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
        };
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

    public void initializeDiscoveryListener()
    {
        mDiscoveryListener = new NsdManager.DiscoveryListener()
        {
            @Override
            public void onDiscoveryStarted(String regType)
            {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service)
            {
                Log.d(TAG, "Service found " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE))
                {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                }
                else if (service.getServiceName().equals(mServiceName))
                {

                }
                else if (service.getServiceName().contains("WiFiChat"))
                {
                    mNsdManager.resolveService(service, mResolveListener);
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
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
