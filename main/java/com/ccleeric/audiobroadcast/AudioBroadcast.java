package com.ccleeric.audiobroadcast;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AudioBroadcast extends Activity {

    private final String TAG = "AudioBroadcast";

    public static final String TOAST = "toast";
    private static final int REQUEST_ENABLE_BT = 3;

    private AudioBroadcastController mBroadcastController;
    private ListView mNewDevicesListView;
    private ListDevicesAdapter mNewDevicesArrayAdapter;
    private ProgressBar mSearchProgress;

    private Intent mPlayerIntent;

    private boolean mSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case AudioPlayer.ACTION_STOP:
                if (resultCode == Activity.RESULT_OK) {
                    mSender = false;
                    mBroadcastController.setHandler(mHandler);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    mBroadcastController.waitConnection();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mBroadcastController.getConnectionState() == BluetoothManager.STATE_NONE &&
                                         BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            mBroadcastController.waitConnection();
        }
    }

    public void init() {

        mNewDevicesListView = (ListView) findViewById(R.id.new_devices);
        mNewDevicesArrayAdapter = new ListDevicesAdapter(this, R.layout.list_item, new ArrayList<String>());
        mNewDevicesListView.setAdapter(mNewDevicesArrayAdapter);

        mNewDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckBox box = (CheckBox)view.findViewById(R.id.checkbox);
                TextView address = (TextView)view.findViewById(R.id.mac_address);
                mSender = true;
                Intent intent = new Intent(AudioBroadcast.this, PlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("Sender", true);
                intent.putExtras(bundle);
                startActivityForResult(intent, AudioPlayer.ACTION_STOP);

                mBroadcastController.connectDevice((String) address.getText());
                box.toggle();
            }
        });

        BluetoothManager mBtManager = new BluetoothManager();
        mBroadcastController = AudioBroadcastController.getInstance();
        mBroadcastController.setHandler(mHandler);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        mSearchProgress = (ProgressBar) findViewById(R.id.search_progress);
    }

    @Override
    protected void onDestroy() {
        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_menu, menu);


        // Locate MenuItem with ShareActionProvider
        //MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        //mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        //mShareActionProvider.setShareIntent(getDefaultIntent());


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                mNewDevicesArrayAdapter.clear();
                mNewDevicesArrayAdapter.notifyDataSetChanged();
                mSearchProgress.setVisibility(View.VISIBLE);
                mBroadcastController.search();
                return true;
            case R.id.action_share:
                enableDiscoverability();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    /*
     * Enable bluetooth to be discoverable.
     */
    public void enableDiscoverability() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
        startActivity(discoverableIntent);
    }

    // Create a BroadcastReceiver for ACTION_FOUND & ACTION_DISCOVERY_FINISHED
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String title = device.getName() + "," + device.getAddress();

                // Check the found device is exist in the list
                if(mNewDevicesArrayAdapter.getPosition(title) < 0) {
                    // Add the name and address to an array adapter to show in a ListView
                    mNewDevicesArrayAdapter.add(title);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mSearchProgress.setVisibility(View.GONE);
            }
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case AudioPlayer.ACTION_PLAY:
                    mSearchProgress.setVisibility(View.GONE);
                    Intent intent = new Intent(AudioBroadcast.this, PlayerActivity.class);
                    startActivityForResult(intent, AudioPlayer.ACTION_STOP);
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
            // Otherwise, setup the chat session
        }
    }
}
