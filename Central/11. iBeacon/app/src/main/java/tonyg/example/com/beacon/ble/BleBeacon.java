package tonyg.example.com.beacon.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * This class allows to talk to Beacons
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2016-03-06
 */
public class BleBeacon {
    public boolean isFake = false;
    private String mMacAddress = "";

    public static final String TAG = BleBeacon.class.getSimpleName();
    public static final String BROADCAST_NAME = "MyBeacon";

    public static final double RADIO_PROPAGATION_CONSTANT = 3.5;

    /** GATT Profile  **/
    public static final UUID SERVICE_UUID = UUID.fromString("0000180c-0000-1000-8000-00805f9b34fb");
    public static final UUID RSSI_CHARACTERISTIC_UUID = UUID.fromString("00002a56-0000-1000-8000-00805f9b34fa");
    public static final UUID X_CHARACTERISTIC_UUID = UUID.fromString("00002a56-0000-1000-8000-00805f9b34fb");
    public static final UUID Y_CHARACTERISTIC_UUID = UUID.fromString("00002a56-0000-1000-8000-00805f9b34fc");

    private Context mContext;
    private int mItemId;
    private int mRssi = 0;
    private int mReferenceRssi;
    private double mDistance_m;
    private double mX = 0;
    private double mY = 0;

    private boolean mReferenceRssiRead = false;
    private boolean mXLocationRead = false;
    private boolean mYLocationRead = false;


    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;

    /**
     * Create a BleBeacon
     *
     * @param context the Activity context
     * @param rssi RSSI
     */
    public BleBeacon(Context context, int rssi) {
        mContext = context;
        isFake = true;
        mRssi = rssi;
    }

    /**
     * Create a BleBeacon
     *
     * @param context the Activity context
     * @param bluetoothDevice the Bluetooth Device
     * @param rssi RSSI
     */
    public BleBeacon(Context context, BluetoothDevice bluetoothDevice, int rssi) {
        mContext = context;
        mBluetoothDevice = bluetoothDevice;
        mRssi = rssi;
    }

    /**
     * Calculate the distance to the BleBeacon based on a reference RSSI and a radio propagation constant
     *
     * @param referenceRssi the reference RSSI at 1 meter
     * @param propagationConstant the radio propagation constant for the physical location
     * @param rssi the RSSI from the Ble Beacon
     * @return
     */
    public static double getDistanceFromRSSI(int referenceRssi, double propagationConstant, int rssi) {
        double exponent = (referenceRssi - rssi)/(10*propagationConstant);
        double distance = Math.pow(10, exponent);
        return distance;
    }

    /**
     * Set the List Item ID
     * @param id
     */
    public void setItemId(int id) {  this.mItemId = id; }

    /**
     * Set the Ble Beacon RSSI
     * @param rssi
     */
    public void setRssi(int rssi) { this.mRssi = rssi; }

    /**
     * Set the distance to the BleBeacon from the Central
     * @param distance_m distance in meters
     */
    public void setDistance(double distance_m) { this.mDistance_m = distance_m; }

    /**
     * Set the X and Y location of the BleBeacon
     *
     * @param x x location
     * @param y y location
     */
    public void setLocation(double x, double y) {
        setXLocation(x);
        setYLocation(y);
    }

    /**
     * Set the X location of the BleBeacon
     *
     * @param x x location
     */
    public void setXLocation(double x) {
        mXLocationRead = true;
        mX = x;
    }

    /**
     * Get the X location
     *
     * @return x location
     */
    public double getXLocation()  {
        return mX;
    }

    /**
     * Set the Y location of the BleBeacon
     *
     * @param y y location
     */
    public void setYLocation(double y) {
        mYLocationRead = true;
        mY = y;
    }

    /**
     * Get the Y location
     *
     * @return y location
     */
    public double getYLocation() {
        return mY;
    }

    /**
     * The BleBeacon is done reading variables variables
     *
     * @return <b>true</b> if the reference RSSI, x and y location have been read from the beacon
     */
    public boolean isDoneSettingValues() {
        boolean isDoneSettingValues = !( (mReferenceRssiRead) || (mXLocationRead) || (mYLocationRead) );
        Log.d(TAG, "Is done reading values? "+Boolean.toString(isDoneSettingValues));
        return isDoneSettingValues;
    }

    /**
     * Get the List Item ID
     *
     * @return list item id
     */
    public int getItemId() { return mItemId; }

    /**
     * Set the MAC address of the BleBeacon
     *
     * @param macAddress
     */
    public void setAddress(String macAddress) { mMacAddress = macAddress; }
    public String getAddress() {
        if (mBluetoothDevice != null)
            return mBluetoothDevice.getAddress();
        return mMacAddress;
    }

    /**
     * Get the RSSI of the BleBeacon
     *
     * @return RSSI
     */
    public int getRssi() {
        return mRssi;
    }

    /**
     * Distance to the BleBeacon
     *
     * @return distance
     */
    public double getDistance() { return mDistance_m; }

    /**
     * Get the reference RSSI for this BleBeacon
     *
     * @return reference RSSI
     */
    public int getReferenceRssi() { return mReferenceRssi; }

    /**
     * Set the Reference RSSI for this BleBeacon
     *
     * @param referenceRssi reference RSSI
     */
    public void setReferenceRssi(int referenceRssi) {
        mReferenceRssiRead = true;
        mReferenceRssi = referenceRssi;
    }

    /**
     * Get the Bluetooth Device
     *
     * @return
     */
    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    /**
     * Get the GATT profile for this device
     * @return
     */
    public BluetoothGatt getGatt() { return mBluetoothGatt; }

    /**
     * Connect to a Peripheral
     *
     * @param bluetoothDevice the Bluetooth Device
     * @param callback The connection callback
     * @return a connection to the BluetoothGatt
     * @throws Exception if no device is given
     */
    public BluetoothGatt connect(BluetoothDevice bluetoothDevice, BluetoothGattCallback callback) throws Exception {
        if (bluetoothDevice == null) {
            throw new Exception("No bluetooth device provided");
        }
        mBluetoothDevice = bluetoothDevice;
        mBluetoothGatt = bluetoothDevice.connectGatt(mContext, false, callback);
        refreshDeviceCache();
        return mBluetoothGatt;
    }

    /**
     * Disconnect from a Peripheral
     */
    public void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }



    /**
     * Request a data/value read from a Ble Characteristic
     *
     * @param characteristic
     */
    public void readValueFromCharacteristic(final BluetoothGattCharacteristic characteristic) {
        // Reading a characteristic requires both requesting the read and handling the callback that is
        // sent when the read is successful
        // http://stackoverflow.com/a/20020279
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Clear the GATT Service cache.
     *
     * @return <b>true</b> if the device cache clears successfully
     * @throws Exception
     */
    public boolean refreshDeviceCache() throws Exception {
        Method localMethod = mBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
        if (localMethod != null) {
            return ((Boolean) localMethod.invoke(mBluetoothGatt, new Object[0])).booleanValue();
        }

        return false;
    }



    /**
     * Check if a Characetristic supports write permissions
     * @return Returns <b>true</b> if property is writable
     */
    public static boolean isCharacteristicWritable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    /**
     * Check if a Characetristic has read permissions
     *
     * @return Returns <b>true</b> if property is Readable
     */
    public static boolean isCharacteristicReadable(BluetoothGattCharacteristic characteristic) {
        return ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }

    /**
     * Check if a Characteristic supports Notifications
     *
     * @return Returns <b>true</b> if property is supports notification
     */
    public static boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }


}
