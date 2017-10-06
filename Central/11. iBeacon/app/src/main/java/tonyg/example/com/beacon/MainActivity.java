

package tonyg.example.com.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tonyg.example.com.beacon.adapters.IBeaconsListAdapter;
import tonyg.example.com.beacon.ble.IBeacon;
import tonyg.example.com.beacon.ble.BleCommManager;
import tonyg.example.com.beacon.ble.callbacks.BleScanCallbackv21;
import tonyg.example.com.beacon.models.IBeaconListItem;
import tonyg.example.com.beacon.utilities.IBeaconLocator;
import tonyg.example.com.beacon.models.IBeaconMapLayout;
import tonyg.example.com.beacon.ble.callbacks.BleScanCallbackv18;
import tonyg.example.com.beacon.utilities.DataConverter;

/**
 * Connect to a BLE Device, list its GATT services
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2015-12-21
 */
public class MainActivity extends AppCompatActivity {
    /** Constants **/
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    // Number of iBeacons required to find Central
    private static final int MIN_IBEACONS_FOR_TRILATERATION = 3;

    /** Bluetooth Stuff **/
    private BleCommManager mBleCommManager;
    private ArrayList<IBeacon> mFoundIBeacons = new ArrayList<IBeacon>();

    /** UI Stuff **/
    private MenuItem mProgressSpinner;
    private MenuItem mStartScanItem, mStopScanItem;
    private TextView mCentralPosition;
    private ListView mIBeaconsList;
    private IBeaconsListAdapter mIBeaconsListAdapter;
    private IBeaconMapLayout mIBeaconMap;

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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBleBroadcastReceiver);
    }


    public void loadUI() {
        mCentralPosition = (TextView) findViewById(R.id.central_position);
        mIBeaconsListAdapter = new IBeaconsListAdapter();
        mIBeaconsList = (ListView) findViewById(R.id.beacons_list);
        mIBeaconsList.setAdapter(mIBeaconsListAdapter);

        mIBeaconMap = (IBeaconMapLayout)findViewById(R.id.beacon_map);

    }

    /**
     * Create a menu
     * @param menu The menu
     * @return <b>true</b> if processed successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mStartScanItem = menu.findItem(R.id.action_start_scan);
        mStopScanItem =  menu.findItem(R.id.action_stop_scan);
        mProgressSpinner = menu.findItem(R.id.scan_progress_item);

        mStartScanItem.setVisible(true);
        mStopScanItem.setVisible(false);
        mProgressSpinner.setVisible(false);

        initializeBluetooth();

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start_scan:
                // User chose the "Scan" item
                startScan();
                return true;

            case R.id.action_stop_scan:
                // User chose the "Stop" item
                stopScan();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Trun on Bluetooth radio
     */
    public void initializeBluetooth() {
        try {
            mBleCommManager = new BleCommManager(this);
        } catch (Exception e) {
            Log.d(TAG, "Could not initialize bluetooth");
            Log.d(TAG, e.getMessage());
            finish();
        }

        // should prompt user to open settings if Bluetooth is not enabled.
        if (!mBleCommManager.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    /**
     * Start scanning for iBeacons
     */
    private void startScan() {
        mStartScanItem.setVisible(false);
        mStopScanItem.setVisible(true);
        mProgressSpinner.setVisible(true);
        mIBeaconsListAdapter.clear();
        mFoundIBeacons.clear();

        try {
            mBleCommManager.scanForPeripherals(mScanCallbackv18, mScanCallbackv21);
        } catch (Exception e) {
            Log.d(TAG, "Can't create Ble Device Scanner");
        }

        // to test mapping engine, add fake beacons
        addFakeBeacons();
    }

    /**
     * Stop scan
     */
    public void stopScan() {
        mBleCommManager.stopScanning(mScanCallbackv18, mScanCallbackv21);
    }

    public void onBleScanStopped() {
        Log.v(TAG, "Scan complete");
        mStartScanItem.setVisible(true);
        mStopScanItem.setVisible(false);
        mProgressSpinner.setVisible(false);

    }


    /**
     * To test the mapping engine, add fake beacons
     */
    public void addFakeBeacons() {
        byte[] scanRecord1 = DataConverter.hexToBytes("0201061AFF4C000215E20A39F473F54BC4A12F17D1AD07A96104610D10C80000000000000000000000000000000000000000000000000000000000000000");
        byte[] scanRecord2 = DataConverter.hexToBytes("0201061AFF4C000215E20A39F473F54BC4A12F17D1AD07A96104630D20C80000000000000000000000000000000000000000000000000000000000000000");
        byte[] scanRecord3 = DataConverter.hexToBytes("0201061AFF4C000215E20A39F473F54BC4A12F17D1AD07A96104640D30C80000000000000000000000000000000000000000000000000000000000000000");

        try {
            IBeacon iBeacon1 = IBeacon.fromScanRecord(scanRecord1);
            IBeacon iBeacon2 = IBeacon.fromScanRecord(scanRecord2);
            IBeacon iBeacon3 = IBeacon.fromScanRecord(scanRecord3);

            iBeacon1.setLocation(10, 10);
            iBeacon2.setLocation(50, 30);
            iBeacon3.setLocation(35, 50);

            int rssi1 = getRssi(iBeacon1.getTransmissionPower(), 13.2);
            int rssi2 = getRssi(iBeacon2.getTransmissionPower(), 10.8);
            int rssi3 = getRssi(iBeacon3.getTransmissionPower(), 7.7);

            iBeacon1.setRssi(rssi1);
            iBeacon2.setRssi(rssi2);
            iBeacon3.setRssi(rssi3);



            mFoundIBeacons.add(iBeacon1);
            mFoundIBeacons.add(iBeacon2);
            mFoundIBeacons.add(iBeacon3);


            for (IBeacon iBeacon : mFoundIBeacons) {
                mIBeaconMap.addBeacon(iBeacon);
                mIBeaconsListAdapter.addIBeacon(iBeacon);
            }

            mIBeaconMap.draw();
            mIBeaconsListAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.d(TAG, "fake Scan Records did not convert to iBeacon");
        }
    }

    /**
     * For adding fake iBeacons: calculate RSSI from a known distance
     *
     * @param txPower iBeacon Transmission Power
     * @param distance Distance between iBeacon and Central
     * @return
     */
    private int getRssi(double txPower, double distance) {
        double ratio = Math.log10(distance);
        double difference = ratio * (10 * IBeacon.RADIO_PROPAGATION_CONSTANT);
        double rssi = txPower - difference;
        return (int) rssi;
    }


    /**
     * Event trigger when new Peripheral is discovered
     */
    public void onIBeaconDiscovered(byte[] scanRecord, int rssi) {
        Log.v(TAG, "iBeacon discovered, GAP: " + DataConverter.bytesToHex(scanRecord));

        if (IBeacon.isIBeacon(scanRecord)) {
            try {
                final IBeacon iBeacon = IBeacon.fromScanRecord(scanRecord);

                // check if iBeacon is already in list
                boolean addBeacon = true;
                for (IBeaconListItem iBeaconListItem : mIBeaconsListAdapter.getItems()) {

                    if (iBeaconListItem.getIBeacon().equals(iBeacon)) {
                        addBeacon = false;
                        break;
                    }
                }

                if (addBeacon) {
                    // in real life, there is where to look up the beacon position
                    iBeacon.setRssi(rssi);
                    mFoundIBeacons.add(iBeacon);
                    mIBeaconsListAdapter.addIBeacon(iBeacon);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIBeaconsListAdapter.notifyDataSetChanged();

                        }
                    });


                    mIBeaconMap.addBeacon(iBeacon);
                    mIBeaconMap.draw();
                    if (mIBeaconsList.getCount() >= MIN_IBEACONS_FOR_TRILATERATION) {
                        triangulateCentral();
                    }
                }

            } catch (Exception e) {
                Log.d(TAG, "Could not convert scanRecord into iBeacon");
            }
        } else {
            Log.d(TAG, "Not a beacon");
        }

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
                        initializeBluetooth();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        initializeBluetooth();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };


    /**
     * Use this callback for Android API 21 (Lollipop) or greater
     */
    private final BleScanCallbackv21 mScanCallbackv21 = new BleScanCallbackv21() {
        /**
         * New Peripheral discovered
         *
         * @param callbackType int: Determines how this callback was triggered. Could be one of CALLBACK_TYPE_ALL_MATCHES, CALLBACK_TYPE_FIRST_MATCH or CALLBACK_TYPE_MATCH_LOST
         * @param result a Bluetooth Low Energy Scan Result, containing the Bluetooth Device, RSSI, and other information
         */
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int rssi = result.getRssi();

            // Get Scan Record byte array (Be warned, this can be null)
            if (result.getScanRecord() != null) {
                byte[] scanRecord = result.getScanRecord().getBytes();

                onIBeaconDiscovered(scanRecord, rssi);
            }
        }

        /**
         * Several peripherals discovered when scanning in low power mode
         *
         * @param results List: List of scan results that are previously scanned.
         */
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                int rssi = result.getRssi();

                // Get Scan Record byte array (Be warned, this can be null)
                if (result.getScanRecord() != null) {
                    byte[] scanRecord = result.getScanRecord().getBytes();

                    onIBeaconDiscovered(scanRecord, rssi);
                }
            }
        }

        /**
         * Scan failed to initialize
         *
         * @param errorCode	int: Error code (one of SCAN_FAILED_*) for scan failure.
         */
        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
                    Log.e(TAG, "Fails to start scan as BLE scan with the same settings is already started by the app.");
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.e(TAG, "Fails to start scan as app cannot be registered.");
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG, "Fails to start power optimized scan as this feature is not supported.");
                    break;
                default: // SCAN_FAILED_INTERNAL_ERROR
                    Log.e(TAG, "Fails to start scan due an internal error");

            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleScanStopped();
                }
            });
        }

        /**
         * Scan completed
         */
        public void onScanComplete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleScanStopped();
                }
            });

        }
    };



    private BleScanCallbackv18 mScanCallbackv18 = new BleScanCallbackv18() {
        /**
         *  Bluetooth LE Scan complete - timer expired out while searching for bluetooth devices
         */
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {

            onIBeaconDiscovered(scanRecord, rssi);
        }
        @Override
        public void onScanComplete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBleScanStopped();
                }
            });
        }
    };

    /**
     * plot the location of the Central on the map
     */
    public void triangulateCentral() {
        try {
            double[] centralPosition = IBeaconLocator.trilaterate(mFoundIBeacons);
            Log.d(TAG, "Central at "+centralPosition[0]+", "+centralPosition[1]);

            String centralPositionString = "";
            try {
                String xPosition = String.format("%.1f", centralPosition[0]);
                String yPosition = String.format("%.1f", centralPosition[1]);
                centralPositionString = String.format( getResources().getString(R.string.central_position), xPosition, yPosition);
            } catch (Exception e) {
                Log.d(TAG, "Could not convert central location to string");
            }
            mCentralPosition.setText(centralPositionString);

            mIBeaconMap.setCentralPosition(centralPosition[0], centralPosition[1]);
            mIBeaconMap.draw();
        } catch (Exception e) {
            Log.d(TAG, "Not enough Beacons to perform a triangulation.  Found " + mFoundIBeacons.size());
        }
    }
}
