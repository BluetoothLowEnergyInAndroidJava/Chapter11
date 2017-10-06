package tonyg.example.com.examplebleperipheral.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.UUID;

import tonyg.example.com.examplebleperipheral.utilities.DataConverter;


/**
 * This class creates a local Bluetooth Peripheral
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2016-03-06
 */
public class IBeacon {
    /** Constants **/
    private static final String TAG = IBeacon.class.getSimpleName();

    /** Peripheral and GATT Profile **/
    public static final UUID I_BEACON_UUID = UUID.fromString("e20a39f4-73f5-4bc4-a12f-17d1ad07a961");


    private static final byte[] beaconHeader = { -1, 0x4c, 0x00, 0x02, 0x15 }; // -1 = 0xff in Java, except that 0xff produces an error
    //private static final byte[] beaconHeader = { 0x02, 0x01 }; //, 0x06 };

    // Major Number and Minor number identify the Beacon
    public static int MANUFACTURER_ID = 224; // Google Manufacturer ID
    public static int MAJOR_NUMBER = 1122;
    public static int MINOR_NUMBER = 3344;
    public static int TRANSMISSION_POWER_DB = -55;

    /** Data formatting **/
    public static final String CHARSET = "ASCII";

    /** Advertising settings **/
    int mAdvertisingMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
    int mTransmissionPower = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;



    /** Callback Handlers **/
    public AdvertiseCallback mAdvertiseCallback;

    /** Bluetooth Stuff **/
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothAdvertiser;


    /**
     * Construct a new Peripheral
     *
     * @param context The Application Context
     * @throws Exception Exception thrown if Bluetooth is not supported
     */
    public IBeacon(final Context context) throws Exception {

        // make sure Android device supports Bluetooth Low Energy
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw new Exception("Bluetooth Not Supported");
        }

        // get a reference to the Bluetooth Manager class, which allows us to talk to talk to the BLE radio
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();


        // Beware: this function doesn't work on some systems
        if(!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            throw new Exception ("Peripheral mode not supported");
        }

        mBluetoothAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        // Use this method instead for better support
        if (mBluetoothAdvertiser == null) {
            throw new Exception ("Peripheral mode not supported");
        }
    }

    /**
     * Get the system Bluetooth Adapter
     *
     * @return BluetoothAdapter
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }



    private byte[] getManufacturerData() {
        ByteBuffer manufacturerData = ByteBuffer.allocate(27);

        int i=0;
        while (i < beaconHeader.length) {
            manufacturerData.put(i, beaconHeader[i]);
            i++;
        }

        // add UUID
        byte[] uuid = DataConverter.hexToBytes(I_BEACON_UUID.toString().replace("-",""));

        Log.v(TAG, "uuid: "+I_BEACON_UUID.toString().replace("-","")+", length: "+uuid.length);
        for (int u = 0; i<16; u++) {
            i = u + beaconHeader.length;
            manufacturerData.put(i, uuid[u]);
        }



        manufacturerData.put(i++, (byte) (MAJOR_NUMBER >> 8)); // first byte of Major
        manufacturerData.put(i++, (byte) (MAJOR_NUMBER & 0xFF)); // second byte of Major

        manufacturerData.put(i++, (byte) (MINOR_NUMBER >> 8)); // first byte of Minor
        manufacturerData.put(i++, (byte) (MINOR_NUMBER & 0xFF)); // second byte of Minor

        manufacturerData.put(i++, (byte) TRANSMISSION_POWER_DB); // txPower

        byte[] manufacturerDataBytes = manufacturerData.array();

        Log.v(TAG, "iBeacon built, GAP: " + DataConverter.bytesToHex(manufacturerDataBytes));

        return manufacturerDataBytes;

    }


    /*
    private byte[] getManufacturerData() {
        ByteBuffer manufacturerData = ByteBuffer.allocate(24);

        manufacturerData.put(0, beaconHeader[0]); // iBeacon Identifier
        manufacturerData.put(1, beaconHeader[1]); // iBeacon Identifier

        // add UUID
        byte[] uuid = DataConverter.hexToBytes(I_BEACON_UUID.toString().replace("-",""));
        for (int i=2; i<=17; i++) {
            manufacturerData.put(i, uuid[i-2]);
        }



        manufacturerData.put(18, (byte) (MAJOR_NUMBER >> 8)); // first byte of Major
        manufacturerData.put(19, (byte) (MAJOR_NUMBER & 0xFF)); // second byte of Major

        manufacturerData.put(18, (byte) (MINOR_NUMBER >> 8)); // first byte of Minor
        manufacturerData.put(19, (byte) (MINOR_NUMBER & 0xFF)); // second byte of Minor

        manufacturerData.put(22, (byte) TRANSMISSION_POWER_DB); // txPower

        byte[] manufacturerDataBytes = manufacturerData.array();

        Log.v(TAG, "iBeacon built, GAP: " + DataConverter.bytesToHex(manufacturerDataBytes));

        return manufacturerDataBytes;

    }
*/


    /**
     * Start Advertising
     *
     * @throws Exception Exception thrown if Bluetooth Peripheral mode is not supported
     */
    public void startAdvertising(AdvertiseCallback advertiseCallback) {
        mAdvertiseCallback = advertiseCallback;


        // Build Advertise settings with transmission power and advertise speed
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(mAdvertisingMode)
                .setConnectable(false)
                .setTimeout(mTransmissionPower)
                .setTimeout(0)
                .build();


/*
        AdvertiseData.Builder scanResponseDataBuilder = new AdvertiseData.Builder();
        scanResponseDataBuilder.setIncludeDeviceName(false);
        scanResponseDataBuilder.addManufacturerData(MANUFACTURER_ID, getManufacturerData()); // using google's company ID
        AdvertiseData scanResponseData = scanResponseDataBuilder.build();
*/


        AdvertiseData.Builder advertiseBuilder = new AdvertiseData.Builder();
        advertiseBuilder.setIncludeDeviceName(false);
        advertiseBuilder.addManufacturerData(MANUFACTURER_ID, getManufacturerData()); // using google's company ID
        AdvertiseData advertiseData = advertiseBuilder.build();

        // begin advertising
        mBluetoothAdvertiser.startAdvertising(advertiseSettings, advertiseData, mAdvertiseCallback);
    }


    /**
     * Stop advertising
     */
    public void stopAdvertising() {
        if (mBluetoothAdvertiser != null) {
            mBluetoothAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
    }





}
