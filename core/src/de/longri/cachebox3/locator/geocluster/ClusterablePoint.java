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
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;

import java.util.Arrays;

public class ClusterablePoint extends Coordinate {

    private final Object dataObject; // Cache.class or WayPoint.class

    final GeoPoint geoPoint;
    private GeoBoundingBox bounds;
    private final int cachedHash;


    public ClusterablePoint(Coordinate pos, Object obj) {
        super(pos.latitude, pos.longitude);
        geoPoint = new GeoPoint(pos.latitude, pos.longitude);
        this.dataObject = obj;

        cachedHash = hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof ClusterablePoint &&
                equals((ClusterablePoint) that);
    }

    private boolean equals(ClusterablePoint that) {
        return that.cachedHash == this.cachedHash
                && geoPoint.equals(that.geoPoint)
                && dataObject.equals(that.dataObject);
    }

    @Override
    public int hashCode() {
        return hashCode(geoPoint, dataObject);
    }

    private static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }

    @Override
    public String toString() {
        return "Cluster : " + dataObject;
    }

    public void setClusterSymbol(Bitmap Cluster) {
        mapSymbol = Cluster;
    }

    void setDistanceBoundce(double distance) {
        double halfDistance = distance / 2;
        double bbDistance = Math.hypot(halfDistance, halfDistance);

        this.bounds = new GeoBoundingBox(this.geoPoint.destinationPoint(bbDistance, 315)
                , this.geoPoint.destinationPoint(bbDistance, 135));
    }

    public boolean contains(ClusterablePoint testCluster) {
        return this.bounds.contains(testCluster.geoPoint);
    }


}
