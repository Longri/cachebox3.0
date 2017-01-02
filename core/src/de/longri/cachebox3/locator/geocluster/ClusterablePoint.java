package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.gui.map.layer.cluster.ClusterSymbol;
import de.longri.cachebox3.locator.Coordinate;
import org.oscim.core.GeoPoint;

import java.util.Arrays;

public class ClusterablePoint extends Coordinate {

    protected final String debugName;

    final private GeoPoint geoPoint;
    private GeoBoundingBox bounds;
    private final int cachedHash;


    public ClusterablePoint(Coordinate pos, String name) {
        super(pos.latitude, pos.longitude);
        geoPoint = new GeoPoint(pos.latitude, pos.longitude);
        this.debugName = name;

        cachedHash = hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof ClusterablePoint &&
                equals((ClusterablePoint) that);
    }

    private boolean equals(ClusterablePoint that) {

        if (that.cachedHash != this.cachedHash) return false;
        if (!geoPoint.equals(that.geoPoint)) return false;
        if (!debugName.equals(that.debugName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hashCode(geoPoint, debugName);
    }

    private static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }

    @Override
    public String toString() {
        return "Cluster : " + debugName;
    }

    public void setClusterSymbol(ClusterSymbol Cluster) {
        clustersymbol = Cluster;
    }

    public void setDistanceBoundce(double distance) {
        double halfDistance = distance / 2;
        double bbDistance = Math.hypot(halfDistance, halfDistance);

        this.bounds = new GeoBoundingBox(this.geoPoint.destinationPoint(bbDistance, 315)
                , this.geoPoint.destinationPoint(bbDistance, 135));
    }

    public boolean contains(ClusterablePoint testCluster) {
        return this.bounds.contains(testCluster.geoPoint);
    }


}
