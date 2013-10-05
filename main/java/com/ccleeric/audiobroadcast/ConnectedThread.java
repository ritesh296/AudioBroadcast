package com.ccleeric.audiobroadcast;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ccleeric on 13/10/3.
 */
public class ConnectedThread extends Thread {
    public final String TAG = "Connected Thread";
    private BluetoothManager mBtManager;
    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    public ConnectedThread(BluetoothManager manager, BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                Log.d(TAG, "1111 Before Read stream!");
                bytes = mmInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                mBtManager.connectLost();
                break;
            }
        }
    }

    public void closeSocket() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "closeConnection() of connect socket failed", e);
        }
    }
}
