package com.fmontanari.wearable;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.btwiz.library.BTSocket;
import com.btwiz.library.BTWiz;
import com.btwiz.library.DeviceMajorComparator;
import com.btwiz.library.DeviceNotSupportBluetooth;
import com.btwiz.library.GetAllDevicesListener;
import com.btwiz.library.IDeviceConnectionListener;
import com.btwiz.library.IDeviceLookupListener;
import com.btwiz.library.MarkCompletionListener;
import com.btwiz.library.SecureMode;
import com.btwiz.library.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothComms {
    private static final String TAG = "BluetoothComms";

    // UUID necessary for creating socket
    private static final UUID MY_UUID_INSECURE =
               UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static BluetoothComms instance = null;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;





    public static BluetoothComms getInstance()
    {
        if(instance == null)
        {
            instance = new BluetoothComms();
        }
        return instance;
    }

    /**
     * Test looking up and connecting to a single device, identified by major number & name, via getBTDeviceAsync.
     * Note that if device is not part of the bonded list a discovery process will be initiated.
     *  Set name to null to disable comparison by name
     *  Set major to -1 to disable comparison by major
     */
    public static void connectToDevice(final Context context, final int major,
                                       final String name, final SecureMode secureMode, final UUID serviceUuid) {
        try {
            if (!BTWiz.isEnabled(context)) {
                // TODO call startActivity with BTWiz.enableBTIntent() allowing user to enable BT
                context.startActivity(BTWiz.enableBTIntent());
                return;
            }
        } catch (DeviceNotSupportBluetooth e) {
            // TODO disable BT functionality in your app
            return;
        }

        final IDeviceConnectionListener deviceConnectionListener = new IDeviceConnectionListener() {
            @Override
            public void onConnectionError(Exception exception, String where) {
                // TODO handle connection error
                Log.e("Tester", "Connection error: " + exception + " at " + where);
            }
            @Override
            public void onConnectSuccess(BTSocket clientSocket) {
                // TODO work with new connection e.g. using
                // async IO methods: readAsync() & writeAsync()
                // or synchronous read() & write()
                Log.d("Tester", "Connected to new device");
            }
        };


        // declare a connecting listener
        IDeviceLookupListener lookupListener = new IDeviceLookupListener() {
            @Override
            public boolean onDeviceFound(BluetoothDevice device, boolean byDiscovery) {
                // log
                String name = device.getName();
                String addr = device.getAddress();
                int major = device.getBluetoothClass().getMajorDeviceClass();
                String majorStr = Utils.majorToString(major);
                Log.d("Tester", "Discovered device: " + name + ", " + addr + ", " + majorStr);
                // and connect to the newly found device
                BTWiz.connectAsClientAsync(context, device, deviceConnectionListener, secureMode, serviceUuid);
                return false; // and terminate discovery
            }
            @Override
            public void onDeviceNotFound(boolean byDiscovery) {
                // TODO handle discovery failure
                Log.d("Tester", "Failed to discover device");
            }
        };

        final boolean DISCOVER_IF_NEEDED = true; // start discovery if device not found in bonded-devices list
        DeviceMajorComparator comparator = new DeviceMajorComparator(major, name);

        BTWiz.lookupDeviceAsync(context, comparator, lookupListener, DISCOVER_IF_NEEDED);

        // TODO call BTWiz.cleanup() at end of BT processing
    }




    public void connectToBTDevice(BluetoothDevice device) {
        String name = device.getName();
        String addr = device.getAddress();
        int major = device.getBluetoothClass().getMajorDeviceClass();
        String majorStr = Utils.majorToString(major);
        Log.d(TAG, "Discovered device: " + name + ", " + addr + ", " + majorStr);

        UUID SERIAL_UUID = device.getUuids()[0].getUuid();

        BluetoothSocket socket = null;

        try {
            socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Error creating socket");
        }

        try {
            socket.connect();
            Log.e(TAG, "Connected");
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            try {
                Log.e(TAG, "trying fallback...");

                socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                socket.connect();

                Log.e(TAG, "Connected");
            } catch (Exception e2) {
                Log.e(TAG, "Couldn't establish Bluetooth connection!");
            }
        }
    }

    public void startClient(Context mContext, BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        //init progress dialog
/*        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth"
                ,"Please Wait...",true);*/
        Toast.makeText(mContext,"Connecting to bluetooth",Toast.LENGTH_SHORT);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }

            //will talk about this in the 3rd video
            connected(mmSocket,mmDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }
    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try{
                //
                // mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }


}
