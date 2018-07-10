package com.fmontanari.serverapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    private ArrayList<BluetoothConnectionService.BluetoothEventListener> listeners;

    public BluetoothBroadcastReceiver()
    {
        listeners = new ArrayList<>();
    }

    public void addEventListener(BluetoothConnectionService.BluetoothEventListener listener)
    {
        listeners.add(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra(Constants.MESSAGE);
        for (BluetoothConnectionService.BluetoothEventListener listener :
                listeners) {
            listener.onMessageReceived(message);
        }
    }
}
