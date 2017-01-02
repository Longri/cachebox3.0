package de.longri.cachebox3.locator.geocluster;

import de.longri.cachebox3.locator.Coordinate;

/**
 * Created by Longri on 31.12.16.
 */
public class Cluster extends ClusterablePoint {

    final ClusteredList includedClusters = new ClusteredList();

    public Cluster(Coordinate pos, String name) {
        super(pos, name);
    }


    public void addAll(ClusteredList list) {


        for (ClusterablePoint cluster : list) {
            if (cluster instanceof Cluster) {
                this.includedClusters.addAll(((Cluster) cluster).includedClusters);
                ((Cluster) cluster).includedClusters.clear();
            } else {
                this.includedClusters.add(cluster);
            }
        }
//        this.includedClusters.addAll(list);
    }


    public void add(ClusterablePoint cluster) {
        if (cluster instanceof Cluster) {
            this.includedClusters.addAll(((Cluster) cluster).includedClusters);
        } else {
            this.includedClusters.add(cluster);
        }
    }

    public void add(Cluster cluster) {
        this.includedClusters.addAll(cluster.includedClusters);
        cluster.includedClusters.clear();
        this.includedClusters.add(cluster);
    }

    public void removeAll(ClusteredList list) {
        this.includedClusters.removeAll(list);
    }

    public int size() {
        return includedClusters.size();
    }

    @Override
    public String toString() {
        return "Cluster [" + this.includedClusters.size() + "]: " + debugName;
    }
}
