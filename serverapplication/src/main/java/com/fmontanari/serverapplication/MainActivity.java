package com.fmontanari.serverapplication;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements BluetoothConnectionService.BluetoothEventListener{

    private static final String TAG = "MainActivity";

    TextView txtLastMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtLastMessage = findViewById(R.id.txtLastMessage);
        Context context = getApplicationContext();

        Intent i = new Intent(context,ListeningService.class);
        context.startService(i);

        /*
        IntentFilter intentFilter = new IntentFilter(Constants.MESSAGE_RECEIVED);
        BluetoothBroadcastReceiver receiver = new BluetoothBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,intentFilter);
        receiver.addEventListener(this);*/
    }

    @Override
    public void onMessageReceived(final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtLastMessage.setText(message);
            }
        });

    }

   @Override
    public void onDeviceDisconnected() {
        Log.d(TAG, "onDeviceDisconnected: Device disconnected");
    }
}
