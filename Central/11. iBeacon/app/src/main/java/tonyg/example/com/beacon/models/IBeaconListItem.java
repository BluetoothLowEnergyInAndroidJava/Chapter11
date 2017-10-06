package tonyg.example.com.beacon.models;

import java.util.UUID;

import tonyg.example.com.beacon.ble.IBeacon;


/**
 * A visual representation of a list of discovered iBeacons
 * This is paired with a list_item_ibeacon.xml that lists the iBeacons found
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2016-12-09
 */
public class IBeaconListItem {
    private int mItemId;
    private int mRssi;
    private IBeacon mIBeacon;

    public IBeaconListItem(IBeacon iBeacon) {
        mIBeacon = iBeacon;
    }

    public void setItemId(int id) {  mItemId = id; }
    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public int getItemId() { return mItemId; }
    public UUID getUuid() { return mIBeacon.getUuid(); }
    public int getMajor() { return mIBeacon.getMajor(); }
    public int getMinor() {
        return mIBeacon.getMinor();
    }
    public int getRssi() { return mRssi; }
    public int getTransmissionPower() { return mIBeacon.getTransmissionPower(); }
    public double getDistance() { return mIBeacon.getDistance(); }
    public double getXLocation() { return mIBeacon.getXLocation(); }
    public double getYLocation() { return mIBeacon.getYLocation(); }
    public IBeacon getIBeacon() { return mIBeacon; }
}
