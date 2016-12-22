package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.gui.map.layer.cluster.ClusterInterface;
import de.longri.cachebox3.gui.map.layer.cluster.ClusterSymbol;
import de.longri.cachebox3.locator.LatLong;
import org.oscim.core.GeoPoint;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GeoCluster implements ClusterInterface {

    private int size;
    private GeoPoint center;
    private GeoBoundingBox bounds;
    private List<GeoPoint> points = new LinkedList<GeoPoint>();


    protected ClusterSymbol mCluster;

    public GeoCluster(LatLong pos) {
        GeoPoint point = new GeoPoint(pos.getLatitude(), pos.getLongitude());
        this.size = 1;
        this.center = point;
        this.bounds = new GeoBoundingBox(point);
        points.add(center);
    }

    public GeoCluster(GeoPoint point) {
        this(1, point, new GeoBoundingBox(point));
        points.add(point);
    }

    public GeoCluster(int size, GeoPoint center, GeoBoundingBox bounds) {
        this.size = size;
        this.center = center;
        this.bounds = bounds;
    }


//    public GeoCluster(GeoPoint geoPoint) {
//        this.center = new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude());
//    }

    public void add(GeoPoint point) {
        ++size;
        center = mean(center, size - 1, point, 1);
        bounds = bounds.extend(point);
        points.add(point);
    }

    public GeoCluster merge(GeoCluster that) {
        int size = this.size + that.size();
        GeoPoint center = mean(this.center, size - that.size(), that.center(), that.size());
        GeoBoundingBox bounds = this.bounds.extend(that.bounds());
        GeoCluster cluster = new GeoCluster(size, center, bounds);
        cluster.points.addAll(points);
        cluster.points.addAll(that.points);
        return cluster;
    }

    private static GeoPoint mean(GeoPoint left, int leftWeight, GeoPoint right, int rightWeight) {
        double lat = (left.getLatitude() * leftWeight + right.getLatitude() * rightWeight) / (leftWeight + rightWeight);
        double lon = (left.getLongitude() * leftWeight + right.getLongitude() * rightWeight) / (leftWeight + rightWeight);
        return new GeoPoint(lat, lon);
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public int size() {
        return size;
    }

    public GeoPoint center() {
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

    @Override
    public GeoPoint getPoint() {
        return center;
    }

    @Override
    public ClusterSymbol getCluster() {
        return mCluster;
    }


    public void setCluster(ClusterSymbol Cluster) {
        mCluster = Cluster;
    }

}
