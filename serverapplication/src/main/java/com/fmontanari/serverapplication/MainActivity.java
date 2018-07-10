package com.fmontanari.serverapplication;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.btwiz.library.BTSocket;
import com.btwiz.library.BTWiz;
import com.btwiz.library.DeviceNotSupportBluetooth;
import com.btwiz.library.IAcceptListener;
import com.btwiz.library.SecureMode;
import com.btwiz.library.Utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*try {
            if (!BTWiz.isEnabled(getApplicationContext())) {
                startActivity(BTWiz.enableBTIntent());
                return;
            }
        } catch (DeviceNotSupportBluetooth e) {
            // TODO disable Bluetooth functionality in your app
            return;
        }

        IAcceptListener acceptListener = new IAcceptListener() {
            @Override
            public void onNewConnectionAccepted(BTSocket newConnection) {
                // log
                BluetoothDevice device = newConnection.getRemoteDevice();
                String name = device.getName();
                String addr = device.getAddress();
                int major = device.getBluetoothClass().getMajorDeviceClass();
                String majorStr = Utils.majorToString(major);
                Log.d("Tester", "New connection: " + name + ", " + addr + ", " + majorStr);

                // TODO work with new connection e.g. using
                // async IO methods: readAsync() & writeAsync()
                // or synchronous read() & write()
            }
            @Override
            public void onError(Exception e, String where) {
                // TODO handle error
                Log.e("Tester", "Connection error " + e + " at " + where);
            }
        };


        BTWiz.listenForConnectionsAsync("MyServerName", acceptListener, SecureMode.INSECURE);

        Log.d("Tester", "Async listener activated");
*/
        BluetoothConnectionService mBluetoothService = new BluetoothConnectionService(getApplicationContext());

    }
}
