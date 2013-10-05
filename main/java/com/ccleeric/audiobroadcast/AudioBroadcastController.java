package com.ccleeric.audiobroadcast;

import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ccleeric on 13/9/25.
 */
public class AudioBroadcastController implements Observer {

    private final String TAG = "AudioBroadcastController";

    private volatile static AudioBroadcastController mInstance;

    private BluetoothManager mBtManager;
    private AudioRecorder mAudioRecorder;
    private AudioPlayer mAudioPlayer;
    private Handler mHandler;
    private boolean mSender;
    private boolean mPressedBack;

    private AudioBroadcastController() {
        mBtManager = new BluetoothManager();
        mBtManager.attachObserver(this);

        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.attachObserver(this);

        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.attachObserver(this);
        mSender = false;
    }

    public static synchronized AudioBroadcastController getInstance() {
        if(mInstance == null) {
            mInstance = new AudioBroadcastController();
        }
        return mInstance;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void search() {
        mBtManager.doDiscovery();
    }

    public int getConnectionState() {
        return mBtManager.getState();
    }

    public void waitConnection() {
        mPressedBack = false;
        mBtManager.listen();
    }

    public void connectDevice(String address) {
        Log.d(TAG, "1111 ConnectDevice in Controller!");
        mSender = true;
        mBtManager.connect(address);
    }

    public void stopAudio() {
        if(mSender) {
            mAudioRecorder.stop();
        }
        mBtManager.disconnect();
    }

    public void playAudio() {
        mSender = false;
        mAudioRecorder.start(mBtManager.getAudioOutStream());
    }

    public void closeConnection() {
        mPressedBack = true;
        mBtManager.stop();
        mBtManager.disconnect();
    }

    public void sendMessage(int action, String text) {
        Message msg = mHandler.obtainMessage(action);
        Bundle bundle = new Bundle();
        bundle.putString(AudioBroadcast.TOAST, text);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    @Override
    public void update(int action, Object args) {

        switch(action) {
            case AudioPlayer.ACTION_PLAY:
                mAudioPlayer.play((InputStream) args);
                sendMessage(action, null);
                if(mSender) {
                    playAudio();
                }
                break;
            case BluetoothManager.ACTION_CONNECT_FAILED:
                sendMessage(action, (String)args);
                break;
            case BluetoothManager.ACTION_CONNECT_LOST:
                mBtManager.connectLost();
                sendMessage(action, (String)args);
                break;
            default:
                break;
        }

    }
}
