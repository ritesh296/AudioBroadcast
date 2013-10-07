package com.ccleeric.audiobroadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ccleeric on 13/10/2.
 */
public class ListenThread extends Thread {
    private static final String TAG = "ListenThread";
    public final boolean D = true;

    private BluetoothManager mBtManager;

    // The local server socket
    private final BluetoothServerSocket mmServerSocket;

    public ListenThread(BluetoothManager manager) {
        mBtManager = manager;
        BluetoothAdapter adapter = mBtManager.getAdapter();
        BluetoothServerSocket tmp = null;

        // Create a new listening server socket
        try {
                tmp = adapter.listenUsingRfcommWithServiceRecord(BluetoothManager.NAME_SECURE,
                        BluetoothManager.AUDIO_UUID_SECURE);

        } catch (IOException e) {
            Log.e(TAG, "Socket Type: secure listen() failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        if (D) Log.d(TAG, "BEGIN ListenThread");
        setName("ListenThread");

        BluetoothSocket socket = null;

        // Listen to the server socket if we're not connected
        int state = mBtManager.getState();
        while (state != BluetoothManager.STATE_CONNECTED) {
            try {
                Log.i(TAG, "Waiting connecting ...");
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: secure accept() failed", e);
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                //synchronized (BluetoothChatService.this) {
                    switch (state) {
                        case BluetoothManager.STATE_LISTEN:
                        case BluetoothManager.STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            mBtManager.connected(socket);
                            break;
                        case BluetoothManager.STATE_NONE:
                        case BluetoothManager.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not closeConnection unwanted socket", e);
                            }
                            break;
                    }
                //}
            }
            state = mBtManager.getState();
        }
        if (D) Log.i(TAG, "END mAcceptThread, socket Type: Secure");

    }

    public void closeSocket() {
        if (D) Log.d(TAG, "Socket Type Secure cancel " + this);
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Socket Type Secure closeConnection() of server failed", e);
        }
    }

}
