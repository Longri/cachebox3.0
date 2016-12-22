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

    public GeoBoundingBox extend(GeoPoint point) {
        return extend(point, point);
    }

    public GeoBoundingBox extend(GeoBoundingBox bounds) {
        return extend(bounds.topLeft(), bounds.bottomRight());
    }

    public GeoBoundingBox extend(double factor) {
        double distance = size(GeoDistanceUnit.KILOMETERS) * factor;
        return extend(distance, GeoDistanceUnit.KILOMETERS);
    }

    public GeoBoundingBox extend(double distance, GeoDistanceUnit unit) {
        double offsetValue= unit.toKm(distance);
        return new GeoBoundingBox(offsetBy(topLeft(),offsetValue, 315.0, unit), offsetBy(bottomRight(),offsetValue, 135.0, unit));
    }

    private GeoBoundingBox extend(GeoPoint topLeft, GeoPoint bottomRight) {
        return contains(topLeft) && contains(bottomRight) ? this : new GeoBoundingBox(
                new GeoPoint(Math.max(topLeft().getLatitude(), topLeft.getLatitude()), Math.min(topLeft().getLongitude(), topLeft.getLongitude())),
                new GeoPoint(Math.min(bottomRight().getLatitude(), bottomRight.getLatitude()), Math.max(bottomRight().getLongitude(), bottomRight.getLongitude())));
    }

    public double size(GeoDistanceUnit unit) {
        return distanceTo(topLeft,bottomRight, unit);
    }

    /**
     * Destination point given distance and bearing from start point
     *
     * Given a start point, initial bearing, and distance, this will calculate the destination point and final bearing travelling along a (shortest distance) great circle arc.
     *
     * @param distance
     * @param bearing
     * @param unit
     * @return
     */
    public static GeoPoint offsetBy(GeoPoint point,double distance, double bearing, GeoDistanceUnit unit) {

        double radLat = Math.toRadians(point.getLatitude());
        double radLon = Math.toRadians(point.getLongitude());

        double d = unit.toKm(distance) / EARTH_RADIUS;
        double b = Math.toRadians(bearing);

        double lat = Math.asin(Math.sin(radLat) * Math.cos(d) +
                Math.cos(radLat) * Math.sin(d) * Math.cos(b));
        double lon = radLon + Math.atan2(Math.sin(b) * Math.sin(d) * Math.cos(radLat),
                Math.cos(d) - Math.sin(radLat) * Math.sin(lat));

        lon = (lon + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        return new GeoPoint(Math.toDegrees(lat), Math.toDegrees(lon));
    }



    /**
     * Computes the great circle distance between this GeoPoint instance and {point} argument.
     *
     */
    public static double distanceTo(GeoPoint point,GeoPoint point1, GeoDistanceUnit unit) {
        double radLat = Math.toRadians(point.getLatitude());
        double radLon = Math.toRadians(point.getLongitude());
        double pointRadLat = Math.toRadians(point1.getLatitude());
        double pointRadLon = Math.toRadians(point1.getLongitude());

        double rad = Math.sin(radLat) * Math.sin(pointRadLat) +
                Math.cos(radLat) * Math.cos(pointRadLat) *
                        Math.cos(radLon - pointRadLon);

        // Valid result is in range -1.0..+1.0
        rad = (rad < -1.0) ? -1.0 : (rad > 1.0) ? 1.0 : rad;

        return unit.fromKm(Math.acos(rad) * EARTH_RADIUS);
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
