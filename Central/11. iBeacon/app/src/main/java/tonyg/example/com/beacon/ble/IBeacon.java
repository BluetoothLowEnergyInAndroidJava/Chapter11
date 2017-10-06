package tonyg.example.com.beacon.ble;

import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

import tonyg.example.com.beacon.utilities.DataConverter;


/**
 * This class describes iBeacons
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2016-12-09
 */
public class IBeacon {
    /** Constants **/
    private static String TAG = IBeacon.class.getSimpleName();

    public static final double RADIO_PROPAGATION_CONSTANT = 3.5;

    // distances
    public static final int DISTANCE_UNKNOWN = 0;
    public static final int DISTANCE_IMMEDIATE = 1;
    public static final int DISTANCE_NEAR = 3;
    public static final int DISTANCE_FAR = 10;

    private static final int RANGE_UNKNOWN = 0;
    private static final int RANGE_IMMEDIATE = 1;
    private static final int RANGE_NEAR = 3;

    /** iBeacon GAP HEader **/
    private static final byte[] IBEACON_HEADER = { 0x02, 0x01 };
    private static final int IBEACON_HEADER_POSITION = 0;
    private static final int MANUFACTURER_POSITION = 6;
    private static final int MANUFACTURER_LENGTH = 2;
    private static final int UUID_POSITION = 9;
    private static final int UUID_LENGTH = 16;
    private static final int MAJOR_NUMBER_POSITION = 25;
    private static final int MAJOR_NUMBER_LENGTH = 2;
    private static final int MINOR_NUMBER_POSITION = 27;
    private static final int MINOR_NUMBER_LENGTH = 2;
    private static final int TX_POWER_POSITION = 29;
    private static final int TX_POWER_LENGTH = 1;

    /** iBeacon Properties **/
    private UUID mUuid;
    private int mManufaturerId;
    private int mMajor;
    private int mMinor;
    private int mTransmissionPower;
    private int mRssi;
    private String mMacAddress;

    private double mX = 0;
    private double mY = 0;

    /** Get and set iBeacon Properties **/
    public void setUuid(UUID uuid) {  mUuid = uuid; }
    public void setMajor(int major) { mMajor = major; }
    public void setMinor(int minor) { mMinor = minor; }
    public void setTransmissionPower(int transmissionPower) { mTransmissionPower = transmissionPower; }
    public void setRssi(int rssi) { mRssi = rssi; }
    public void setMacAddress(String macAddress) { mMacAddress = macAddress; }
    public void setManufacturerId(int manufacturerId) { mManufaturerId = manufacturerId; }

    public UUID getUuid() { return mUuid; }
    public int getMajor() { return mMajor; }
    public int getMinor() { return mMinor; }
    public int getTransmissionPower() { return mTransmissionPower; }
    public int getRssi() { return mRssi; }
    public String getMacAddress() { return mMacAddress; }
    public int getManufacturerId() { return mManufaturerId; }

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
     * Test if two iBeacons are the same
     *
     * @param otherBeacon another iBeacon
     * @return <strong>true</strong> if two beacons are the same
     */
    public boolean equals(IBeacon otherBeacon) {
        // iBeacons are the same if they have the same UUID, Major, Minor, and Manufacturer ID
        boolean isSameBeacon = ((mUuid.equals(otherBeacon.getUuid())) && (mMajor == otherBeacon.getMajor()) && (mMinor == otherBeacon.mMinor) && (mManufaturerId == otherBeacon.getManufacturerId()));
        Log.v(TAG, "beacons same?: "+isSameBeacon);
        return isSameBeacon;
    }

    /**
     * Determine if a discovered Peripheral's GAP belongs to an iBeacon, based on its GAP header fingerprint
     *
     * @param scanRecord the byte array of the Peripheral's GAP Scan Record
     * @return <strong>true</strong> if Scan Record belongs to an iBeacon
     */
    static public boolean isIBeacon(final byte[] scanRecord) {
        byte[] scanRecordHeader = new byte[IBEACON_HEADER.length];
        System.arraycopy(scanRecord, IBEACON_HEADER_POSITION, scanRecordHeader, 0, IBEACON_HEADER.length);

        if (Arrays.equals(IBEACON_HEADER, scanRecordHeader)) { // || Arrays.equals(altBeaconHeader, scanRecordHeader)) {
            return true;
        }
        return false;
    }

    /**
     * Create an iBeacon from a GAP Scan Record
     *
     * Scan record header looks like this:
     *
     * Header:           0-1 (Little Endian)
     * MAC Address:      2-8
     * iBeacon Prefix:  9-19
     * Proximity UUID: 20-36
     * Major:          38-39 (Big Endian)
     * Minor:          40-41 (Big Endian)
     * TX Power:       42-43 (Two's complement negative)
     *
     * example: 4C00 02 15 B9407F30F5F8466EAFF925556B57FE6D ED4E 8931 B6
     *
     * @param scanRecord an incoming Scan Record
     * @return IBeacon
     */
    static public IBeacon fromScanRecord(byte[] scanRecord) throws Exception {
        if (!isIBeacon(scanRecord)) {
            throw new Exception("Scan Record does not represent an iBeacon");
        }

        IBeacon iBeacon = new IBeacon();

        iBeacon.setUuid(getUuidFromScanRecord(scanRecord));
        iBeacon.setManufacturerId(getManufacturerIdFromScanRecord(scanRecord));
        iBeacon.setMajor(getMajorNumberFromScanRecord(scanRecord));
        iBeacon.setMinor(getMinorNumberFromScanRecord(scanRecord));
        iBeacon.setTransmissionPower(getTransmissionPowerFromScanRecord(scanRecord));

        Log.v(TAG, "txPower: "+iBeacon.getTransmissionPower()+", major: "+iBeacon.getMajor()+", minor: "+iBeacon.getMinor()+", uuid: "+iBeacon.getUuid().toString());
        return iBeacon;
    }


    /**
     * Get iBeacon's UUID from an iBeacon's Scan Record
     *
     * @param scanRecord iBeacon Scan Record
     * @return UUID
     */
    static public UUID getUuidFromScanRecord(final byte[] scanRecord) {
        byte[] uuidBytes = new byte[16];
        System.arraycopy(scanRecord, UUID_POSITION, uuidBytes, 0, UUID_LENGTH);

        return DataConverter.bytesToUuid(uuidBytes);
    }

    /**
     * Get iBeacon's UUID from an iBeacon's Scan Record
     *
     * @param scanRecord iBeacon Scan Record
     * @return Minor number
     */
    public static int getMinorNumberFromScanRecord(final byte[] scanRecord) {
        byte[] minorBytes = new byte[2];
        System.arraycopy(scanRecord, MINOR_NUMBER_POSITION, minorBytes, 0, MINOR_NUMBER_LENGTH);
        return DataConverter.bytesToUnsignedInt(minorBytes);
    }

    /**
     * Get iBeacon's Major number from an iBeacon's Scan Record
     *
     * @param scanRecord iBeacon Scan Record
     * @return Major number
     */
    public static int getMajorNumberFromScanRecord(final byte[] scanRecord) {
        byte[] majorBytes = new byte[2];
        System.arraycopy(scanRecord, MAJOR_NUMBER_POSITION, majorBytes, 0, MAJOR_NUMBER_LENGTH);
        return DataConverter.bytesToUnsignedInt(majorBytes);
    }

    /**
     * Get iBeacon's transmission power from an iBeacon's Scan Record
     *
     * @param scanRecord iBeacon Scan Record
     * @return transmission power in decibels
     */
    public static int getTransmissionPowerFromScanRecord(final byte[] scanRecord) {
        byte[] txPowerBytes = new byte[1];
        System.arraycopy(scanRecord, TX_POWER_POSITION, txPowerBytes, 0, TX_POWER_LENGTH);
        return DataConverter.bytesToSignedInt(txPowerBytes);
    }

    /**
     * Get iBeacon's Manufacturer ID from an iBeacon's Scan Record
     *
     * @param scanRecord iBeacon Scan Record
     * @return Manufacturer ID number
     */
    public static int getManufacturerIdFromScanRecord(final byte[] scanRecord) {
        byte[] manufacturerBytes = new byte[MANUFACTURER_LENGTH];
        System.arraycopy(scanRecord, MANUFACTURER_POSITION, manufacturerBytes, 0, MANUFACTURER_LENGTH);
        return DataConverter.bytesToUnsignedInt(manufacturerBytes);

    }

    /**
     * Get iBeacon's distance from central based on an RSSI
     *
     * @return Minor number
     */
    public double getDistance() {
        if (mRssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = (mTransmissionPower - mRssi)/(10 * RADIO_PROPAGATION_CONSTANT);
        double distance = Math.pow(10, ratio);
        return distance;
    }

    /**
     * Get iBeacon's proximity range from a Central
     *
     * @return One of DISTANCE_UNKNOWN, DISTANCE_IMMEDIATE, DISTANCE_NEAR, or DISTANCE_FAR
     */
    public int getProximity() {
        double distance = getDistance();
        if (distance <= RANGE_UNKNOWN) {
            return DISTANCE_UNKNOWN;
        }
        if (distance < RANGE_IMMEDIATE) {
            return DISTANCE_IMMEDIATE;
        }
        if (distance < RANGE_NEAR) {
            return DISTANCE_NEAR;
        }
        return DISTANCE_FAR;
    }
}
