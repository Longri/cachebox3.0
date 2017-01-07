/*
 * Copyright (C) 2016-2017 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.gui.map.layer.WaypointLayer;
import de.longri.cachebox3.utils.lists.CB_List;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Longri on 25.12.16.
 */
public class ClusteredList extends CB_List<ClusterablePoint> {

    private int allItemSize = 0;

    private GeoBoundingBox clusterBoundingBox;
    private boolean anyClusterOutsideBoundingBox = true;
    private AtomicBoolean cancel = new AtomicBoolean(false);

    public void cancel() {
        cancel.set(true);
    }


    public void clusterByDistance(double distance, GeoBoundingBox boundingBox, ClusterRunnable.Task task, boolean all) {
        clusterBoundingBox = boundingBox;
        switch (task) {
            case expand:
                expand(distance, all);
                break;
            case reduce:
                reduce(distance);
                break;
        }
    }

    private void reduce(final double distance) {
        int index = 0;
        anyClusterOutsideBoundingBox = false;
        while (!cancel.get() && index < this.size) {

            ClusterablePoint cluster = this.get(index);

            if (clusterBoundingBox != null) {
                if (!clusterBoundingBox.contains(cluster.geoPoint)) {
                    index++;
                    anyClusterOutsideBoundingBox = true;
                    continue;
                }
            }

            cluster.setDistanceBoundce(distance);

            //search all cluster inside
            ClusteredList inside = new ClusteredList();

            for (int i = index + 1; i < this.size(); i++) {

                if (cancel.get()) {
                    break;
                }

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

    private void expand(final double distance, boolean all) {
        ClusteredList outsideList = new ClusteredList();
        ClusteredList emptyList = new ClusteredList();
        ClusteredList clusterOutList = new ClusteredList();
        for (ClusterablePoint obj : this) {

            if (cancel.get()) {
                break;
            }

            if (!(obj instanceof Cluster)) continue;

            Cluster cluster = (Cluster) obj;

            clusterOutList.clear();
            if (!all) cluster.setDistanceBoundce(distance);
            for (ClusterablePoint includedCluster : cluster.getIncludedClusters()) {

                if (cancel.get()) {
                    break;
                }

                if (includedCluster == null || this.contains(includedCluster)) {
                    continue;
                }

                if (all || !cluster.contains(includedCluster)) {
                    clusterOutList.add(includedCluster);
                }
            }

            cluster.removeAll(clusterOutList);

            if (cluster.size() == 1) {
                clusterOutList.add(cluster.getIncludedClusters().get(0));
                emptyList.add(cluster);
            }

            if (cluster.size() == 0) {
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
        if (!all) this.reduce(distance); //don't reduce with show all
    }

    public void setFinishFill() {
        allItemSize = this.size;
    }

    public boolean isExpandToAll() {
        return allItemSize == this.size;
    }

    public int getAllSize() {
        return allItemSize;
    }

    public boolean isAnyClusterNotVisible() {
        return anyClusterOutsideBoundingBox;
    }
}
