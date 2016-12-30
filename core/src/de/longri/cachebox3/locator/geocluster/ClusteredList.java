package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.utils.lists.CB_List;


/**
 * Created by Longri on 25.12.16.
 */
public class ClusteredList extends CB_List<GeoCluster> {


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
        while (index < this.size()) {

            GeoCluster cluster = this.get(index);
            cluster.setDistanceBoundce(distance);

            //search all cluster inside
            ClusteredList inside = new ClusteredList();

            for (int i = index + 1; i < this.size(); i++) {
                GeoCluster testCluster = this.get(i);
                if (cluster.contains(testCluster)) {
                    inside.add(testCluster);
                }
            }

            if (inside.size() > 0) {
                this.removeAll(inside);
                cluster.addAll(inside);

                int clusterSize = cluster.size();

                if (WaypointLayer.CLUSTER1_SYMBOL != null) { //is NULL in case of JUnit test
                    if (clusterSize > 1 && clusterSize <= 10) {
                        cluster.setCluster(WaypointLayer.CLUSTER1_SYMBOL);
                    } else if (clusterSize > 10 && clusterSize <= 100) {
                        cluster.setCluster(WaypointLayer.CLUSTER10_SYMBOL);
                    } else if (clusterSize > 100) {
                        cluster.setCluster(WaypointLayer.CLUSTER100_SYMBOL);
                    }
                }
            }
            index++;
        }
    }

    private void expand(double distance) {
        ClusteredList outsideList = new ClusteredList();
        for (GeoCluster cluster : this) {
            cluster.setDistanceBoundce(distance);
            for (GeoCluster includedCluster : cluster.getClusters()) {

                if (this.contains(includedCluster)) continue;

                if (!cluster.contains(includedCluster)) {
                    outsideList.add(includedCluster);
                }
            }

            cluster.removeAll(outsideList);

            int clusterSize = cluster.size();

            if (WaypointLayer.CLUSTER1_SYMBOL != null) { //is NULL in case of JUnit test
                if (clusterSize > 1 && clusterSize <= 10) {
                    cluster.setCluster(WaypointLayer.CLUSTER1_SYMBOL);
                } else if (clusterSize > 10 && clusterSize <= 100) {
                    cluster.setCluster(WaypointLayer.CLUSTER10_SYMBOL);
                } else if (clusterSize > 100) {
                    cluster.setCluster(WaypointLayer.CLUSTER100_SYMBOL);
                }
            }
        }
        this.addAll(outsideList);
    }
}
