package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.locator.LatLong;
import org.oscim.core.GeoPoint;

/**
 * Created by Longri on 20.12.2016.
 */
public class FastGeoBoundingBoxContains {

    private final int mSquareLength;
    private int latLeftTop, lonLeftTop, latRightButtom, lonRightButtom;


    public FastGeoBoundingBoxContains(double mSquareLength) {
        this.mSquareLength = (int)(mSquareLength * 1000000.0D);
    }

    public void setCenter(GeoPoint center) {
        latLeftTop = center.latitudeE6 + mSquareLength;
        lonLeftTop = center.longitudeE6 - mSquareLength;
        latRightButtom = center.latitudeE6 - mSquareLength;
        lonRightButtom = center.longitudeE6 + mSquareLength;
    }

    public boolean contains(GeoPoint point) {
        return point.latitudeE6 <= latLeftTop && point.latitudeE6 >= latRightButtom &&
                point.longitudeE6 >= lonLeftTop && point.longitudeE6 <= lonRightButtom;
    }
}
