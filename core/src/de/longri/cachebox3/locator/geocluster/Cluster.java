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

import de.longri.cachebox3.locator.Coordinate;

/**
 * Created by Longri on 31.12.16.
 */
public class Cluster extends ClusterablePoint {

    final ClusteredList includedClusters = new ClusteredList();

    public Cluster(Coordinate pos, String name) {
        super(pos, name);
    }


    void addAll(ClusteredList list) {
        for (ClusterablePoint cluster : list) {
            if (cluster instanceof Cluster) {
                this.includedClusters.addAll(((Cluster) cluster).includedClusters);
                ((Cluster) cluster).includedClusters.clear();
            } else {
                this.includedClusters.add(cluster);
            }
        }
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

    void removeAll(ClusteredList list) {
        this.includedClusters.removeAll(list);
    }

    public int size() {
        return includedClusters.size();
    }

    @Override
    public String toString() {
        return "Cluster [" + this.includedClusters.size() + "]: ";
    }
}
