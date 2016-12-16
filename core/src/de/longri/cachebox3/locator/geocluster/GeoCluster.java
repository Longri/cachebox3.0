package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.locator.LatLong;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GeoCluster {

    private int size;
    private LatLong center;
    private GeoBoundingBox bounds;
    private List<LatLong> points = new LinkedList<LatLong>();

    public GeoCluster(LatLong point) {
        this(1, point, new GeoBoundingBox(point));
        points.add(point);
    }

    public GeoCluster(int size, LatLong center, GeoBoundingBox bounds) {
        this.size = size;
        this.center = center;
        this.bounds = bounds;
    }

    public void add(LatLong point) {
        ++size;
        center = mean(center, size - 1, point, 1);
        bounds = bounds.extend(point);
        points.add(point);
    }

    public GeoCluster merge(GeoCluster that) {
        int size = this.size + that.size();
        LatLong center = mean(this.center, size - that.size(), that.center(), that.size());
        GeoBoundingBox bounds = this.bounds.extend(that.bounds());
        GeoCluster cluster = new GeoCluster(size, center, bounds);
        cluster.points.addAll(points);
        cluster.points.addAll(that.points);
        return cluster;
    }

    private static LatLong mean(LatLong left, int leftWeight, LatLong right, int rightWeight) {
        double lat = (left.latitude * leftWeight + right.latitude * rightWeight) / (leftWeight + rightWeight);
        double lon = (left.longitude * leftWeight + right.longitude * rightWeight) / (leftWeight + rightWeight);
        return new LatLong(lat, lon);
    }

    public List<LatLong> getPoints() {
        return points;
    }

    public int size() {
        return size;
    }

    public LatLong center() {
        return center;
    }

    public GeoBoundingBox bounds() {
        return bounds;
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof GeoCluster &&
                equals((GeoCluster) that);
    }

    private boolean equals(GeoCluster that) {
        return size == that.size() && center.equals(that.center()) && bounds.equals(that.bounds());
    }

    @Override
    public int hashCode() {
        return hashCode(size, center.toString(), bounds);
    }

    private static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", center.toString(), size);
    }
}
