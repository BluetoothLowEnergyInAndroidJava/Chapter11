package tonyg.example.com.beacon.utilities;

import java.util.ArrayList;

import tonyg.example.com.beacon.ble.BleBeacon;
import tonyg.example.com.beacon.ble.IBeacon;


/**
 * Get the position of a known central from 3 known iBeacon positions
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2016-12-09
 */
public class IBeaconLocator {

    public static double[] trilaterate(ArrayList<IBeacon> beaconList) throws Exception {

        if (beaconList.size() < 3) {
            throw new Exception("Not enough points to perform a triangulation");
        }

        IBeacon p1 = beaconList.get(0);
        IBeacon p2 = beaconList.get(1);
        IBeacon p3 = beaconList.get(2);
        double r1 = beaconList.get(0).getDistance();
        double r2 = beaconList.get(1).getDistance();
        double r3 = beaconList.get(2).getDistance();


        //unit vector in a direction from point1 to point 2
        double p2p1Distance = Math.sqrt(
                Math.pow(p2.getXLocation() - p1.getXLocation(), 2) +
                Math.pow(p2.getYLocation() - p1.getYLocation(), 2)
        );
        double exx = (p2.getXLocation() - p1.getXLocation()) / p2p1Distance;
        double exy = (p2.getYLocation() - p1.getYLocation()) / p2p1Distance;

        //signed magnitude of the x component
        double i = exx * (p3.getXLocation() - p1.getXLocation()) + exy * (p3.getYLocation() - p1.getYLocation());

        //double ix = exx*(p3.getXLocation() - p1.getXLocation());
        //double iy = exy*(p3.getYLocation() - p1.getYLocation());

        //the unit vector in the y direction.
        double eyx = (p3.getXLocation() - p1.getXLocation() - i * exx) /
                Math.sqrt(
                        Math.pow(p3.getXLocation() - p1.getXLocation() - i * exx, 2) +
                        Math.pow(p3.getYLocation() - p1.getYLocation() - i * exy, 2)
                );
        double eyy = (p3.getYLocation() - p1.getYLocation() - i * exy) /
                Math.sqrt(
                        Math.pow(p3.getXLocation() - p1.getXLocation() - i * exx, 2) +
                        Math.pow(p3.getYLocation() - p1.getYLocation() - i * exy, 2)
                );

        //the signed magnitude of the y component
        double j = eyx * (p2.getXLocation() - p3.getYLocation()) + eyy * (p3.getYLocation() - p1.getYLocation());

        //double jx = eyx * (p3.getXLocation() - p1.getXLocation());
        //double jy = eyy * (p3.getYLocation() - p1.getYLocation());

        //coordinates
        double x = (Math.pow(r1, 2) - Math.pow(r2,2) + Math.pow(p2p1Distance,2))/ (2 * p2p1Distance);
        double y = (Math.pow(r1, 2) - Math.pow(r3,2) + Math.pow(i,2) + Math.pow(j ,2)) / (2 * j) - i * x / j;

        //result coordinates
        double finalX = p1.getXLocation()+ x * exx + y*eyx;
        double finalY = p1.getYLocation()+ x * exy + y*eyy;

        return new double[] {finalX, finalY};

    }
}
