package de.longri.cachebox3.locator.geocluster;



import org.oscim.core.GeoPoint;

import java.util.Arrays;

public class GeoBoundingBox {
    private final GeoPoint topLeft, bottomRight;
    private static final double EARTH_RADIUS = 6371.01d;

    public GeoBoundingBox(GeoPoint point) {
        this(point, point);
    }

    public GeoBoundingBox(GeoPoint topLeft, GeoPoint bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }


    public GeoPoint topLeft() {
        return topLeft;
    }

    public GeoPoint bottomRight() {
        return bottomRight;
    }

    public boolean contains(GeoPoint point) {
        return point.getLatitude() <= topLeft.getLatitude() && point.getLatitude() >= bottomRight.getLatitude() &&
                point.getLongitude() >= topLeft.getLongitude() && point.getLongitude() <= bottomRight.getLongitude();
    }



    public GeoBoundingBox extend(GeoBoundingBox bounds) {
        return extend(bounds.topLeft(), bounds.bottomRight());
    }




    private GeoBoundingBox extend(GeoPoint topLeft, GeoPoint bottomRight) {
        return contains(topLeft) && contains(bottomRight) ? this : new GeoBoundingBox(
                new GeoPoint(Math.max(topLeft().getLatitude(), topLeft.getLatitude()), Math.min(topLeft().getLongitude(), topLeft.getLongitude())),
                new GeoPoint(Math.min(bottomRight().getLatitude(), bottomRight.getLatitude()), Math.max(bottomRight().getLongitude(), bottomRight.getLongitude())));
    }


    @Override
    public boolean equals(Object that) {
        return that instanceof GeoBoundingBox &&
                equals((GeoBoundingBox) that);
    }

    private boolean equals(GeoBoundingBox that) {
        return topLeft.equals(that.topLeft) && bottomRight.equals(that.bottomRight);
    }

    @Override
    public int hashCode() {
        return hashCode(topLeft.toString(), bottomRight.toString());
    }

    private static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }

    @Override
    public String toString() {
        return topLeft.toString() + ".." + bottomRight.toString();
    }
}
