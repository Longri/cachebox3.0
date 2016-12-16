package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.locator.LatLong;

import java.util.Arrays;

public class GeoBoundingBox {
    private final LatLong topLeft, bottomRight;

    public GeoBoundingBox(LatLong point) {
        this(point, point);
    }

    public GeoBoundingBox(LatLong topLeft, LatLong bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public LatLong topLeft() {
        return topLeft;
    }

    public LatLong bottomRight() {
        return bottomRight;
    }

    public boolean contains(LatLong point) {
        return point.latitude <= topLeft.latitude && point.latitude >= bottomRight.latitude &&
                point.longitude >= topLeft.longitude && point.longitude <= bottomRight.longitude;
    }

    public GeoBoundingBox extend(LatLong point) {
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
        double offsetBy = unit.toKm(distance);
        return new GeoBoundingBox(topLeft().offsetBy(offsetBy, 315.0, unit), bottomRight().offsetBy(offsetBy, 135.0, unit));
    }

    private GeoBoundingBox extend(LatLong topLeft, LatLong bottomRight) {
        return contains(topLeft) && contains(bottomRight) ? this : new GeoBoundingBox(
                new LatLong(Math.max(topLeft().latitude, topLeft.latitude), Math.min(topLeft().longitude, topLeft.longitude)),
                new LatLong(Math.min(bottomRight().latitude, bottomRight.latitude), Math.max(bottomRight().longitude, bottomRight.longitude)));
    }

    public double size(GeoDistanceUnit unit) {
        return topLeft.distanceTo(bottomRight, unit);
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
