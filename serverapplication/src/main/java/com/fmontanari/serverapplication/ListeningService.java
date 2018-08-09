package com.fmontanari.serverapplication;

import android.app.Instrumentation;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;

public class ListeningService extends Service implements BluetoothConnectionService.BluetoothEventListener {

    private BluetoothConnectionService mBluetoothService = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mBluetoothService == null){
            mBluetoothService = new BluetoothConnectionService(getApplicationContext());
            mBluetoothService.addEventListener(this);
        }
        return Service.START_STICKY;
    }


    @Override
    public void onMessageReceived(String message) {
        AudioManager mAudioManager;
        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(message)
        {
            case "pause":
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                mAudioManager.dispatchMediaKeyEvent(event);
                event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                mAudioManager.dispatchMediaKeyEvent(event);
                break;
            case "next":
                event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
                mAudioManager.dispatchMediaKeyEvent(event);
                event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT);
                mAudioManager.dispatchMediaKeyEvent(event);
                break;
            case "prev":
                event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                mAudioManager.dispatchMediaKeyEvent(event);
                event = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                mAudioManager.dispatchMediaKeyEvent(event);
                break;
        }

        Intent i = new Intent(Constants.MESSAGE_RECEIVED).putExtra(Constants.MESSAGE,message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    public void onDeviceDisconnected()
    {
        // Reset listening service.
        mBluetoothService.reset();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
