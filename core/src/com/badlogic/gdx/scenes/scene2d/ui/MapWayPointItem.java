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
package com.badlogic.gdx.scenes.scene2d.ui;

import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.geocluster.GeoBoundingBoxInt;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import org.oscim.core.GeoPoint;
import org.oscim.renderer.atlas.TextureRegion;

import java.util.Arrays;

public class MapWayPointItem extends Coordinate {
    private final static Logger log = LoggerFactory.getLogger(MapWayPointItem.class);

    private final Object dataObject; // Cache.class or WayPoint.class

    final GeoPoint geoPoint;
    private GeoBoundingBoxInt bounds;
    private final int cachedHash;
    private final TextureRegion small, middle, large;


    public MapWayPointItem(Coordinate pos, Object obj, TextureRegion small, TextureRegion middle, TextureRegion large) {
        super(pos.latitude, pos.longitude);
        geoPoint = new GeoPoint(pos.latitude, pos.longitude);
        this.dataObject = obj;
        this.small = small;
        this.middle = middle;
        this.large = large;


        cachedHash = hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof MapWayPointItem &&
                equals((MapWayPointItem) that);
    }

    private boolean equals(MapWayPointItem that) {
        return that.cachedHash == this.cachedHash
                && geoPoint.equals(that.geoPoint)
                && dataObject.equals(that.dataObject);
    }

    public TextureRegion getMapSymbol(int zoomLevel) {
        if ((zoomLevel >= 13) && (zoomLevel <= 14)) {
            return middle; // 13x13
        } else if (zoomLevel > 14) {
            return large; // default Images
        }
        return small;
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

    void setDistanceBoundce(double distance) {
        double halfDistance = distance / 2;
        double bbDistance = Math.hypot(halfDistance, halfDistance);

        this.bounds = new GeoBoundingBoxInt(this.geoPoint.destinationPoint(bbDistance, 315)
                , this.geoPoint.destinationPoint(bbDistance, 135));
    }

    public boolean contains(MapWayPointItem testCluster) {
        if (this.bounds == null) return false;
        return this.bounds.contains(testCluster.geoPoint);
    }
}
