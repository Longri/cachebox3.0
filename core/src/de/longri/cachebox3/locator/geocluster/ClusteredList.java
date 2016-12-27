package de.longri.cachebox3.locator.geocluster;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Longri on 25.12.16.
 */
public class ClusterdList extends ArrayList<GeoCluster> {


    double lastDistance = -1;


    public ClusterdList getClustertByDistancs(double distance) {

        if (lastDistance == -1 || lastDistance > distance) {
            return reduce(distance);
        } else {
            return expand(distance);
        }


    }

    private ClusterdList reduce(double distance) {

        int index = 0;

        while (index < this.size()) {

            GeoCluster cluster = this.get(index);
            cluster.setDistanceBoundce(distance);


            //search all cluster inside
            ClusterdList inside = new ClusterdList();

            for (int i = index + 1; i < this.size(); i++) {
                GeoCluster testCluster = this.get(i);
                if (cluster.contains(testCluster)) {
                    inside.add(testCluster);
                }
            }

            if (inside.size() > 0) {
                this.removeAll(inside);
                cluster.addAll(inside);
            }
            index++;
        }

        return this;

    }

    private ClusterdList expand(double distance) {
        return this;
    }


}
