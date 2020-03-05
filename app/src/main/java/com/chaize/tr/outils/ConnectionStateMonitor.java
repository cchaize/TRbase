package com.chaize.tr.outils;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;

public class ConnectionStateMonitor extends LiveData<Boolean> {

    private Context mContext;
    private ConnectivityManager.NetworkCallback networkCallback = null;
    private NetworkReceiver networkReceiver;
    private ConnectivityManager connectivityManager;

    public ConnectionStateMonitor(Context context) {
        mContext = context;
        connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = new NetworkCallback(this);
        } else {
            networkReceiver = new NetworkReceiver();
        }
    }

    @Override
    protected void onActive() {
        super.onActive();
        updateConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build();
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } else {
            mContext.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } else {
            mContext.unregisterReceiver(networkReceiver);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class NetworkCallback extends ConnectivityManager.NetworkCallback {

        private ConnectionStateMonitor mConnectionStateMonitor;

        public NetworkCallback(ConnectionStateMonitor connectionStateMonitor) {
            mConnectionStateMonitor = connectionStateMonitor;
        }

        @Override
        public void onAvailable(Network network) {
            if (network != null) {
                mConnectionStateMonitor.postValue(true);
            }
        }

        @Override
        public void onLost(Network network) {
            mConnectionStateMonitor.postValue(false);
        }
    }

    private void updateConnection() {
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                postValue(true);
            }else{
                postValue(false);
            }
        }

    }

    class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                updateConnection();
            }
        }
    }

    @TargetApi(23)
    public static boolean checkNetworkConnection(Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >=23) {

                final Network network = connectivityManager.getActiveNetwork();
                final NetworkCapabilities capabilities = connectivityManager
                        .getNetworkCapabilities(network);

                return capabilities != null
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {

                final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                return networkInfo != null
                        && networkInfo.isConnected();

            }
        } catch (Exception e) {
            return false;
        }
    }

}