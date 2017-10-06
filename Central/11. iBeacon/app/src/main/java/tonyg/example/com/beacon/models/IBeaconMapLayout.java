package tonyg.example.com.beacon.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.ArrayList;

import tonyg.example.com.beacon.R;
import tonyg.example.com.beacon.ble.BleBeacon;
import tonyg.example.com.beacon.ble.IBeacon;

/**
 * This class represents a the visual Beacon Map
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2016-03-06
 */
public class IBeaconMapLayout extends LinearLayout {
    private static final String TAG = IBeaconMapLayout.class.getSimpleName();

    /** Graphic properties **/
    private static final int BITMAP_WIDTH = 1000;
    private static final int BITMAP_HEIGHT = 800;
    private static final String PAINT_COLOR = "#CD5C5C";
    private static final int STROKE_COLOR = 5;

    private static final int X_OFFSET = 50;
    private static final int Y_OFFSET = 50;
    private static final int M_PX_MULTIPLIER = 400;

    private Bitmap mIBeaconIcon, mCentralIcon; // icons
    private Canvas mCanvas = new Canvas();
    private Paint mPaint = new Paint(); // paint properties
    private Bitmap mMapBitmap; // rendered map

    private ArrayList<IBeacon> mIBeaconList = new ArrayList<IBeacon>(); // list of iBeacons
    private boolean mIsCentralPositionSet = false;
    private double[] mCentralPosition; // central position

    /**
     * Create a new BeaconMapLayout
     *
     * @param context the Activity context
     */
    public IBeaconMapLayout(Context context) {
        super(context);
        initialize();
    }
    /**
     * Create a new BeaconMapLayout
     *
     * @param context the Activity context
     * @param attrs
     */
    public IBeaconMapLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    /**
     * Create a new BeaconMapLayout
     *
     * @param context the Activity context
     * @param attrs
     * @param defStyle
     */
    public IBeaconMapLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    /**
     * Initialize the map
     */
    public void initialize() {
        mIBeaconIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.peripheral);

        mCentralIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.central);


        mPaint.setColor(Color.parseColor(PAINT_COLOR));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_COLOR);
        mMapBitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mMapBitmap);
    }

    /**
     * Add a new Beacon
     *
     * @param iBeacon
     */
    public void addBeacon(IBeacon iBeacon) {
        mIBeaconList.add(iBeacon);
    }

    /**
     * Position the central
     *
     * @param x x location
     * @param y y location
     */
    public void setCentralPosition(double x, double y) {
        mIsCentralPositionSet = true;
        mCentralPosition = new double[]{x, y};
    }

    /**
     * Draw the Beacon Position
     *
     * @param beacon
     */
    private void drawBeaconPosition(IBeacon iBeacon) {
        Log.d(TAG, "Drawing point: " + iBeacon);
        Rect sourceRect = new Rect(0, 0, mIBeaconIcon.getWidth(), mIBeaconIcon.getHeight());
        Rect destRect = new Rect((int) (iBeacon.getXLocation() * M_PX_MULTIPLIER - 50 + X_OFFSET), (int) (iBeacon.getYLocation() * M_PX_MULTIPLIER - 50 + Y_OFFSET), (int) (iBeacon.getXLocation() * M_PX_MULTIPLIER + 50 + X_OFFSET), (int) (iBeacon.getYLocation() * M_PX_MULTIPLIER + 50 + Y_OFFSET));
        mCanvas.drawBitmap(mIBeaconIcon, sourceRect, destRect, null);
        mCanvas.drawCircle((float) iBeacon.getXLocation() * M_PX_MULTIPLIER + X_OFFSET, (float) iBeacon.getYLocation() * M_PX_MULTIPLIER + Y_OFFSET, (float) iBeacon.getDistance() * M_PX_MULTIPLIER, mPaint);


    }

    /**
     * Draw the Central onscreen
     *
     * @param location
     */
    public void drawCentralPosition(double[] location) {
        Rect sourceRect = new Rect(0, 0, mCentralIcon.getWidth(), mCentralIcon.getHeight());
        Rect destRect = new Rect((int) (location[0] * M_PX_MULTIPLIER - 36 + X_OFFSET), (int) (location[1] * M_PX_MULTIPLIER - 71 + Y_OFFSET), (int) (location[0] * M_PX_MULTIPLIER + 37 + X_OFFSET), (int) (location[1] * M_PX_MULTIPLIER + 72 + Y_OFFSET));
        mCanvas.drawBitmap(mCentralIcon, sourceRect, destRect, null);
    }

    /**
     * Draw the frame
     */
    public void draw() {
        // clear canvas
        mCanvas.drawColor(Color.WHITE);
        // draw each beacon
        for (IBeacon iBeacon : mIBeaconList) {
            drawBeaconPosition(iBeacon);
        }
        // draw central position
        if (mIsCentralPositionSet) {
            drawCentralPosition(mCentralPosition);
        }
        this.setBackgroundDrawable(new BitmapDrawable(mMapBitmap));
    }

}

