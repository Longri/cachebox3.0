package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.gui.map.layer.cluster.ClusterSymbol;
import de.longri.cachebox3.locator.Coordinate;
import org.oscim.core.GeoPoint;

import java.util.Arrays;

public class GeoCluster extends Coordinate {

    private int size;
    private GeoPoint center;
    private GeoBoundingBox bounds;
    private ClusteredList includedClusters = new ClusteredList();


    public GeoCluster(Coordinate pos) {
        super(pos.getLatitude(), pos.getLongitude());
        this.size = 1;
        this.center = new GeoPoint(pos.latitude, pos.longitude);
        this.bounds = new GeoBoundingBox(this.center);

    }

    public GeoCluster(int size, GeoPoint center, GeoBoundingBox bounds) {
        super(center.getLatitude(), center.getLongitude());
        this.size = size;
        this.center = center;
        this.bounds = bounds;
    }

    public GeoCluster merge(GeoCluster that) {
        int size = this.size + that.size();
        GeoPoint center = mean(this.center, size - that.size(), that.center(), that.size());
        GeoBoundingBox bounds = this.bounds.extend(that.bounds());
        GeoCluster cluster = new GeoCluster(size, center, bounds);
        cluster.includedClusters.addAll(includedClusters);
        cluster.includedClusters.addAll(that.includedClusters);
        return cluster;
    }

    private static GeoPoint mean(GeoPoint left, int leftWeight, GeoPoint right, int rightWeight) {
        double lat = (left.getLatitude() * leftWeight + right.getLatitude() * rightWeight) / (leftWeight + rightWeight);
        double lon = (left.getLongitude() * leftWeight + right.getLongitude() * rightWeight) / (leftWeight + rightWeight);
        return new GeoPoint(lat, lon);
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


    public void setCluster(ClusterSymbol Cluster) {
        clustersymbol = Cluster;
    }

    public void setDistanceBoundce(double distance) {


        double halfDistance = distance / 2;
        double bbDistance = Math.hypot(halfDistance, halfDistance);

        GeoPoint leftTop = this.center.destinationPoint(bbDistance, 315);
        GeoPoint rightBottom = this.center.destinationPoint(bbDistance, 135);

        this.bounds = new GeoBoundingBox(leftTop, rightBottom);


    }

    public boolean contains(GeoCluster testCluster) {
        return this.bounds.contains(testCluster.center);
    }

    public void addAll(ClusteredList list) {
        this.size += list.size();

        for (GeoCluster cluster : list) {
            this.size += cluster.includedClusters.size();
        }

        this.includedClusters.addAll(list);
    }

    public ClusteredList getClusters() {
        return includedClusters;
    }

    public void removeAll(ClusteredList list) {
        this.includedClusters.removeAll(list);
    }
}
