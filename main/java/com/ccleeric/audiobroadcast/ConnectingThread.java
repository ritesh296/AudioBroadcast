package com.ccleeric.audiobroadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ccleeric on 13/9/30.
 */
public class ConnectingThread extends Thread {
    private static final String TAG = "ConnectingThread";

    private BluetoothManager mBtManager;
    private BluetoothSocket mSocket;

    public ConnectingThread(BluetoothManager manager, String address) {
        this.mBtManager = manager;

        BluetoothAdapter adapter = mBtManager.getAdapter();
        BluetoothDevice mDevice = adapter.getRemoteDevice(address);
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            tmp = mDevice.createRfcommSocketToServiceRecord(
                    BluetoothManager.AUDIO_UUID_SECURE);
        } catch (IOException e) {
            Log.e(TAG, "Socket Type: Secure create() failed", e);
        }

        mSocket = tmp;
    }

    @Override
    public void run() {
        Log.i(TAG, "BEGIN mConnectThread SocketType: Secure");
        this.setName("ConnectThread Secure");

        // Always cancel discovery because it will slow down a connection
        mBtManager.getAdapter().cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            Log.d(TAG, "waiting connect server");
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mSocket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                mSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "unable to closeConnection() Secure + socket during connection failure", e2);
            }
            mBtManager.connectFailed();
            return;
        }

        Log.d(TAG, "Client Connect Success!");
        mBtManager.finishConnecting();
        // Start the connected thread
        mBtManager.connected(mSocket);
    }


    public void closeSocket() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "closeConnection() of connect socket failed", e);
        }
    }
}
