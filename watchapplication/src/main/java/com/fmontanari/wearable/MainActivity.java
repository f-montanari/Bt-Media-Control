package com.fmontanari.wearable;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.btwiz.library.SecureMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "BluetoothActivity";
    public static final int REQUEST_ENABLE_BT = 134;

    // Variables necessary for querying and setting MAC addresses
    private BluetoothAdapter mBluetoothAdapter;
    private ListPopupWindow popupWindow;
    private String[] addresses = new String[]{};
    private String[] names = new String[]{};
    private TextView mTextMessage;
    private BluetoothDevice device;
    final BluetoothComms coms = new BluetoothComms();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    ConnectBluetoothClicked();
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void ConnectBluetoothClicked()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Context context = getApplicationContext();
            String msg = context.getResources().getString(R.string.bluetooth_error);
            Log.w(TAG, msg);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            showPairedDevices(getBaseContext());
        }
    }


    private void showPairedDevices(Context context) {
        if (mBluetoothAdapter == null) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        addresses = new String[pairedDevices.size()];
        names = new String[pairedDevices.size()];
        int i = 0;

        for (BluetoothDevice device : pairedDevices) {
            addresses[i] = device.getAddress();
            names[i] = device.getName() + " (" + addresses[i] + ")";
            ++i;
        }

        popupWindow = new ListPopupWindow(context);
        popupWindow.setAdapter(new ArrayAdapter(context, R.layout.list_item, names));
        popupWindow.setAnchorView(mTextMessage);

        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < addresses.length) {
                    // Selected item listener
                    selectedDevice(mBluetoothAdapter.getRemoteDevice(addresses[position]));
                }
                popupWindow.dismiss();
            }
        });
        popupWindow.show();
    }

    // Method that gets called once request to enable bluetooth completes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            showPairedDevices(getBaseContext());
        }
    }


    public void selectedDevice(BluetoothDevice device)
    {
        mTextMessage.setText(device.getAddress());
        this.device = device;
        coms.startClient(getApplicationContext(),device, UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66"));
    }

    public void nextButtonClicked(View view) {
        coms.write(("next").getBytes(Charset.defaultCharset()));
    }

    public void pauseButtonClicked(View view) {
        coms.write(("pause").getBytes(Charset.defaultCharset()));
    }

    public void previousButtonClicked(View view) {
        coms.write(("prev").getBytes(Charset.defaultCharset()));
    }
}
