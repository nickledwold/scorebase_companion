package com.nickledwold.scorebase_companion;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

public class SignalRService extends Service {
    public HubConnection mHubConnection;
    public HubProxy mHubProxy;
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    private SharedPreferences SP;
    private String ipAddress;
    private String panelNumber;
    private String roleType;

    public SignalRService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        startSignalR();
        return result;
    }

    @Override
    public void onDestroy() {
        mHubConnection.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        startSignalR();
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }

    /**
     * method for clients (activities)
     */
    public void sendMessage(String message) {
        String SERVER_METHOD_SEND = "send";
        mHubProxy.invoke(SERVER_METHOD_SEND, message);
    }

    /**
     * method for clients (activities)
     */
    public void sendMessage_To(String receiverName, String message) {
        String SERVER_METHOD_SEND_TO = "Send";
        mHubProxy.invoke(SERVER_METHOD_SEND_TO, receiverName, message);
    }

    public void startSignalR() {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        ipAddress = SP.getString("ipAddress","192.168.0.18");

        String serverUrl = "http://"+ipAddress+":8081/signalr";
        mHubConnection = new HubConnection(serverUrl);
        String SERVER_HUB_CHAT = "SimpleHub";
        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);
        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        final SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);
        try {
            signalRFuture.get(1, TimeUnit.SECONDS);
            Toast.makeText(getApplicationContext(), "Connected to Panel Manager", Toast.LENGTH_LONG).show();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Log.e("SimpleSignalR", e.toString());
            return;
        }
        panelNumber = SP.getString("panelNumber","0");
        roleType = SP.getString("roleType","1");
        mHubProxy.invoke("SetUserName","P" + panelNumber + "|" + roleType);
        mHubProxy.invoke("JoinGroup","Judges");


    }

}
