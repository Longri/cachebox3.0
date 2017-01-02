package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.utils.lists.CB_List;


/**
 * Created by Longri on 25.12.16.
 */
public class ClusteredList extends CB_List<ClusterablePoint> {


    double lastDistance = -1;


    public void clusterByDistance(double distance) {

        if (lastDistance == -1 || lastDistance < distance) {
            lastDistance = distance;
            reduce(distance);
        } else {
            lastDistance = distance;
            expand(distance);
        }


    }

    private void reduce(final double distance) {
        int index = 0;
        while (index < this.size) {

            ClusterablePoint cluster = this.get(index);
            cluster.setDistanceBoundce(distance);

            //search all cluster inside
            ClusteredList inside = new ClusteredList();

            for (int i = index + 1; i < this.size(); i++) {
                ClusterablePoint testCluster = this.get(i);
                if (testCluster == cluster) continue;
                if (cluster.contains(testCluster)) {
                    inside.add(testCluster);
                }
            }

            if (inside.size() > 0) {
                this.removeAll(inside);

                Cluster newCluster = new Cluster(cluster, "New Cluster");
                newCluster.add(cluster);
                newCluster.addAll(inside);
                this.set(index, newCluster);

                int clusterSize = newCluster.size();

                if (WaypointLayer.CLUSTER1_SYMBOL != null) { //is NULL in case of JUnit test
                    if (clusterSize > 1 && clusterSize <= 10) {
                        newCluster.setClusterSymbol(WaypointLayer.CLUSTER1_SYMBOL);
                    } else if (clusterSize > 10 && clusterSize <= 100) {
                        newCluster.setClusterSymbol(WaypointLayer.CLUSTER10_SYMBOL);
                    } else if (clusterSize > 100) {
                        newCluster.setClusterSymbol(WaypointLayer.CLUSTER100_SYMBOL);
                    }
                }
            }
            index++;
        }
    }


    /**
     * Calculate the center coordinate of all included Coordinates
     *
     * @return
     */
    private Coordinate getCenter() {
        double avLat = 0;
        double avLon = 0;

        for (ClusterablePoint cluster : this) {
            avLat += cluster.latitude;
            avLon += cluster.longitude;
        }
        return new Coordinate(avLat / this.size, avLon / this.size);
    }

    private void expand(final double distance) {
        ClusteredList outsideList = new ClusteredList();
        ClusteredList emptyList = new ClusteredList();
        ClusteredList clusterOutList = new ClusteredList();
        for (ClusterablePoint obj : this) {

            if (!(obj instanceof Cluster)) continue;

            Cluster cluster = (Cluster) obj;

            clusterOutList.clear();
            cluster.setDistanceBoundce(distance);
            for (ClusterablePoint includedCluster : cluster.includedClusters) {

                if (this.contains(includedCluster)) {
                    continue;
                }

                if (!cluster.contains(includedCluster)) {
                    clusterOutList.add(includedCluster);
                }
            }

            cluster.removeAll(clusterOutList);

            if (cluster.size() == 1) {
                clusterOutList.add(cluster.includedClusters.get(0));
                emptyList.add(cluster);
            }

            outsideList.addAll(clusterOutList);

            int clusterSize = cluster.size();

            if (WaypointLayer.CLUSTER1_SYMBOL != null) { //is NULL in case of JUnit test
                if (clusterSize > 1 && clusterSize <= 10) {
                    cluster.setClusterSymbol(WaypointLayer.CLUSTER1_SYMBOL);
                } else if (clusterSize > 10 && clusterSize <= 100) {
                    cluster.setClusterSymbol(WaypointLayer.CLUSTER10_SYMBOL);
                } else if (clusterSize > 100) {
                    cluster.setClusterSymbol(WaypointLayer.CLUSTER100_SYMBOL);
                }
            }
        }
        this.addAll(outsideList);
        this.removeAll(emptyList);
        this.reduce(distance);
    }
}
