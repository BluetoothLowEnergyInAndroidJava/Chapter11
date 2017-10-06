package tonyg.example.com.examplebleperipheral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import tonyg.example.com.examplebleperipheral.ble.IBeacon;


/**
 * Create a Bluetooth Peripheral.  Android 5 required
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2015-12-21
 */
public class MainActivity extends AppCompatActivity {
    /** Constants **/
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    /** Bluetooth Stuff **/
    private IBeacon mIBeacon;


    /** UI Stuff **/
    private Switch mBluetoothOnSwitch,
            mAdvertisingSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // notify when bluetooth is turned on or off
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBleBroadcastReceiver, filter);


        loadUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        // stop advertising when the activity pauses
        mIBeacon.stopAdvertising();
        mAdvertisingSwitch.setChecked(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeBluetooth();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBleBroadcastReceiver);
    }


    /**
     * Load UI components
     */
    public void loadUI() {
        mBluetoothOnSwitch = (Switch)findViewById(R.id.bluetooth_on);
        mAdvertisingSwitch = (Switch)findViewById(R.id.advertising);
    }



    /**
     * Initialize the Bluetooth Radio
     */
    public void initializeBluetooth() {
        // reset connection variables

        try {
            mIBeacon = new IBeacon(this);
        } catch (Exception e) {
            Toast.makeText(this, "Could not initialize bluetooth", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
            finish();
        }


        mBluetoothOnSwitch.setChecked(mIBeacon.getBluetoothAdapter().isEnabled());

        // should prompt user to open settings if Bluetooth is not enabled.
        if (!mIBeacon.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            startAdvertising();
        }

    }

    /**
     * Start advertising Peripheral
     */
    public void startAdvertising() {
        Log.v(TAG, "starting advertising...");
        mIBeacon.startAdvertising(mAdvertiseCallback);
    }



    /**
     * When the Bluetooth radio turns on, initialize the Bluetooth connection
     */
    private final BroadcastReceiver mBleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.v(TAG, "Bluetooth turned off");
                        initializeBluetooth();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.v(TAG, "Bluetooth turned on");
                        startAdvertising();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };



    public AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            mAdvertisingSwitch.setChecked(true);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            mAdvertisingSwitch.setChecked(false);
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    Log.e(TAG, "Failed to start advertising as the advertising is already started.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG, "This feature is not supported on this platform.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    Log.e(TAG, "Operation failed due to an internal error.");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    Log.e(TAG, "Failed to start advertising because no advertising instance is available.");
                    break;
                default:
                    Log.e(TAG, "unknown problem");
            }
        }
    };


}
