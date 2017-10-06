package tonyg.example.com.beacon.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tonyg.example.com.beacon.R;
import tonyg.example.com.beacon.ble.IBeacon;
import tonyg.example.com.beacon.models.IBeaconListItem;

/**
 * Manages the IBeaconDeviceListItems so that we can populate the list
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2015-12-17
 */
public class IBeaconsListAdapter extends BaseAdapter {
    private static String TAG = IBeaconsListAdapter.class.getSimpleName();

    private ArrayList<IBeaconListItem> mBeaconListItems = new ArrayList<IBeaconListItem>(); // list of Peripherals

    /**
     * How many items are in the ListView
     * @return the number of items in this ListView
     */
    @Override
    public int getCount() {
        return mBeaconListItems.size();
    }

    /**
     * Add a new Peripheral to the ListView
     *
     * @param iBeacon iBeacon device information
     * @param rssi Periheral's RSSI, indicating its radio signal quality
     */
    public void addIBeacon(IBeacon iBeacon) {
        // update UI stuff
        int listItemId = mBeaconListItems.size();
        IBeaconListItem listItem = new IBeaconListItem(iBeacon);
        listItem.setItemId(listItemId);
        listItem.setRssi(iBeacon.getRssi());

        // add to list
        mBeaconListItems.add(listItem);
    }

    /**
     * Get current state of ListView
     * @return ArrayList of BlePeripheralListItems
     */
    public ArrayList<IBeaconListItem> getItems() {
        return mBeaconListItems;
    }

    /**
     * Clear all items from the ListView
     */
    public void clear() {
        mBeaconListItems.clear();
    }

    /**
     * Get the IBeaconListItem held at some position in the ListView
     *
     * @param position the position of a desired item in the list
     * @return the IBeaconListItem at some position
     */
    @Override
    public IBeaconListItem getItem(int position) {
        return mBeaconListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mBeaconListItems.get(position).getItemId();
    }

    /**
     * This ViewHolder represents what UI components are in each List Item in the ListView
     */
    public static class ViewHolder{
        public TextView mUuidTV;
        public TextView mRssiTV;
        public TextView mTransmissionPowerTV;
        public TextView mLocationTV;
        public TextView mDistanceTV;
        public TextView mMajorTV;
        public TextView mMinorTV;
    }

    /**
     * Generate a new ListItem for some known position in the ListView
     *
     * @param position the position of the ListItem
     * @param convertView An existing List Item
     * @param parent The Parent ViewGroup
     * @return The List Item
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder peripheralListItemView;

        Context context = parent.getContext();
        Resources resources = context.getResources();

        // if this ListItem does not exist yet, generate it
        // otherwise, use it
        if(convertView == null) {

            // convert list_item_ibeacon.xmla View
            LayoutInflater inflater = LayoutInflater.from(context);
            v = inflater.inflate(R.layout.list_item_ibeacon, null);

            // match the UI stuff in the list Item to what's in the xml file
            peripheralListItemView = new ViewHolder();
            peripheralListItemView.mUuidTV = (TextView) v.findViewById(R.id.uuid);
            peripheralListItemView.mRssiTV = (TextView) v.findViewById(R.id.rssi);
            peripheralListItemView.mTransmissionPowerTV = (TextView) v.findViewById(R.id.transmission_power);
            peripheralListItemView.mMajorTV = (TextView) v.findViewById(R.id.major_number);
            peripheralListItemView.mMinorTV = (TextView) v.findViewById(R.id.minor_number);
            peripheralListItemView.mDistanceTV = (TextView) v.findViewById(R.id.distance);
            peripheralListItemView.mLocationTV = (TextView) v.findViewById(R.id.location);

            v.setTag( peripheralListItemView );
        } else {
            peripheralListItemView = (ViewHolder) v.getTag();
        }
        Log.v(TAG, "ListItem size: "+ mBeaconListItems.size());
        // if there are known Peripherals, create a ListItem that says so
        // otherwise, display a ListItem with Bluetooth Periheral information
        if (mBeaconListItems.size() <= 0) {
            peripheralListItemView.mUuidTV.setText(R.string.beacon_list_empty);
        } else {
            IBeaconListItem item = mBeaconListItems.get(position);

            peripheralListItemView.mUuidTV.setText(item.getUuid().toString());

            peripheralListItemView.mMajorTV.setText(String.format(resources.getString(R.string.major_number), item.getMajor()));
            peripheralListItemView.mMinorTV.setText(String.format(resources.getString(R.string.minor_number), item.getMinor()));

            peripheralListItemView.mRssiTV.setText(String.format(resources.getString(R.string.rssi), item.getRssi()));

            String distance_m = "";
            try {
                distance_m = String.format(resources.getString(R.string.distance), String.format("%.1f",item.getDistance()));
            } catch (Exception e) {
                Log.d(TAG, "Could not convert distance to string");
            }
            peripheralListItemView.mDistanceTV.setText(distance_m);

            String location = "";
            try {
                String xLocation = String.format("%.1f", item.getXLocation());
                String yLocation = String.format("%.1f", item.getYLocation());
                location = String.format( resources.getString(R.string.location), xLocation, yLocation);
            } catch (Exception e) {
                Log.d(TAG, "Could not convert location to string");
            }
            peripheralListItemView.mLocationTV.setText(location);


            String transmissionPower = "";
            try {
                transmissionPower = String.format(resources.getString(R.string.transmission_power), item.getTransmissionPower());

            } catch (Exception e) {
                Log.d(TAG, "Could not convert reference rssi to string");
            }
            peripheralListItemView.mTransmissionPowerTV.setText(transmissionPower);


        }
        return v;
    }
}
