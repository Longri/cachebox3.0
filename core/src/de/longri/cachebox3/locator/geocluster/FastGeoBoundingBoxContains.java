package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.locator.LatLong;

/**
 * Created by Longri on 20.12.2016.
 */
public class FastGeoBoundingBoxContains {

    private final double mSquareLength;
    private double latLeftTop, lonLeftTop, latRightButtom, lonRightButtom;


    public FastGeoBoundingBoxContains(double mSquareLength) {
        this.mSquareLength = mSquareLength;
    }

    public void setCenter(LatLong center) {
        latLeftTop = center.latitude + mSquareLength;
        lonLeftTop = center.longitude - mSquareLength;
        latRightButtom = center.latitude - mSquareLength;
        lonRightButtom = center.longitude + mSquareLength;
    }

    public boolean contains(LatLong point) {
        return point.latitude <= latLeftTop && point.latitude >= latRightButtom &&
                point.longitude >= lonLeftTop && point.longitude <= lonRightButtom;
    }
}
