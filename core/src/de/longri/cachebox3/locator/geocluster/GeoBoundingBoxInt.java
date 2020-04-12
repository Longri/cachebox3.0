/*
 * Copyright (C) 2016-2020 team-cachebox.de
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

import org.oscim.core.Box;
import org.oscim.core.GeoPoint;

import java.util.Arrays;

public class GeoBoundingBoxInt {
    private final GeoPoint topLeft, bottomRight;

    public GeoBoundingBoxInt(GeoPoint point) {
        this(point, point);
    }

    public GeoBoundingBoxInt(GeoPoint topLeft, GeoPoint bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        validate();
    }

    public GeoBoundingBoxInt(Box box) {
        bottomRight = new GeoPoint(box.ymin, box.xmax);
        topLeft = new GeoPoint(box.ymax, box.xmin);
        validate();
    }

    public GeoBoundingBoxInt(double lat, double lon, double extend) {
        double half = extend / 2;
        topLeft = new GeoPoint(lat - half, lon);
        bottomRight = new GeoPoint(lat, lon + half);
        validate();
    }

    public boolean contains(GeoPoint point) {
        return point.latitudeE6 <= topLeft.latitudeE6 && point.latitudeE6 >= bottomRight.latitudeE6 &&
                point.longitudeE6 >= topLeft.longitudeE6 && point.longitudeE6 <= bottomRight.longitudeE6;
    }

    private void validate() {
        if (topLeft.latitudeE6 < bottomRight.latitudeE6 || topLeft.longitudeE6 > bottomRight.longitudeE6)
            throw new IllegalArgumentException("Wrong leftTop to rightBottom");
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof GeoBoundingBoxInt &&
                equals((GeoBoundingBoxInt) that);
    }

    private boolean equals(GeoBoundingBoxInt that) {
        return topLeft.equals(that.topLeft) && bottomRight.equals(that.bottomRight);
    }

    @Override
    public int hashCode() {
        return hashCode(topLeft.toString(), bottomRight.toString());
    }

    private static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }

    @Override
    public String toString() {
        return topLeft.toString() + ".." + bottomRight.toString();
    }
}
