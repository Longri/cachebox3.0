package de.longri.cachebox3.locator.geocluster;

import java.util.LinkedList;
import java.util.List;

public class GeoClusterReducer {

    private final double factor;

    public GeoClusterReducer(double factor) {
        this.factor = factor;
    }

    public List<GeoCluster> reduce(List<GeoCluster> clusters) {
        GeoBoundingBox bounds = getBounds(clusters);
        double maxDistance = bounds != null ? factor * bounds.size(GeoDistanceUnit.KILOMETERS) : 0.0;
        List<GeoCluster> reduced = new LinkedList<GeoCluster>();
        reduced.addAll(clusters);
        REDUCE:
        while (true) {
            for (int i = 0; i < reduced.size(); ++i) {
                for (int j = i + 1; j < reduced.size(); ++j) {
                    GeoCluster a = reduced.get(i);
                    GeoCluster b = reduced.get(j);

                    if (a.center().distanceTo(b.center(), GeoDistanceUnit.KILOMETERS) <= maxDistance) {
                        reduced.remove(a);
                        reduced.remove(b);
                        reduced.add(a.merge(b));
                        continue REDUCE;
                    }
                }
            }
            break;
        }
        return reduced;
    }

    public List<GeoCluster> reduceSquare(List<GeoCluster> clusters) {
        double maxDistance = this.factor * 5;
        FastGeoBoundingBoxContains boundingBoxContains=new FastGeoBoundingBoxContains(maxDistance);
        List<GeoCluster> reduced = new LinkedList<GeoCluster>();
        reduced.addAll(clusters);
        REDUCE:
        while (true) {
            for (int i = 0; i < reduced.size(); ++i) {
                for (int j = i + 1; j < reduced.size(); ++j) {
                    GeoCluster a = reduced.get(i);
                    GeoCluster b = reduced.get(j);

                   // GeoBoundingBox abb = new GeoBoundingBox(a.center(), maxDistance);

                    boundingBoxContains.setCenter(a.center());

                    if (boundingBoxContains.contains(b.center())) {
                        reduced.remove(a);
                        reduced.remove(b);
                        reduced.add(a.merge(b));
                        continue REDUCE;
                    }
                }
            }
            break;
        }
        return reduced;
    }

    private static GeoBoundingBox getBounds(List<GeoCluster> clusters) {
        GeoBoundingBox bounds = null;
        for (GeoCluster cluster : clusters) {
            if (bounds != null) {
                bounds = bounds.extend(cluster.bounds());
            } else {
                bounds = cluster.bounds();
            }
        }
        return bounds;
    }
}
