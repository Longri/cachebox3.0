package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.locator.LatLong;

import java.util.LinkedList;
import java.util.List;

public class GeoClusterBuilder {

    private final double factor;
    private final List<GeoCluster> clusters = new LinkedList<GeoCluster>();
    private GeoBoundingBox bounds = null;
    private double maxDistance = 0.0;

    public GeoClusterBuilder(double factor, GeoBoundingBox bounds) {
        this.factor = factor;
        this.bounds = bounds;
        if (bounds != null) {
            maxDistance = factor * bounds.size(GeoDistanceUnit.KILOMETERS);
        }
    }

    public GeoClusterBuilder(double factor) {
        this.factor = factor;
    }

    public GeoClusterBuilder add(LatLong point) {
        if (bounds == null) {
            bounds = new GeoBoundingBox(point);
        } else if (!bounds.contains(point)) {
            bounds = bounds.extend(point);
            maxDistance = factor * bounds.size(GeoDistanceUnit.KILOMETERS);
        }
        GeoCluster nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (GeoCluster cluster : clusters) {
            double distance = cluster.center().distanceTo(point, GeoDistanceUnit.KILOMETERS);
            if (distance < minDistance && distance <= maxDistance && cluster.bounds().extend(point).size(GeoDistanceUnit.KILOMETERS) <= maxDistance) {
                minDistance = distance;
                nearest = cluster;
            }
        }
        if (nearest == null) {
            nearest = new GeoCluster(point);
            clusters.add(nearest);
        } else {
            nearest.add(point);
        }
        return this;
    }

    public List<GeoCluster> build() {
        return clusters;
    }
}
