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

import org.oscim.core.GeoPoint;

import java.util.Arrays;

public class GeoBoundingBox {
    private final GeoPoint topLeft, bottomRight;

    public GeoBoundingBox(GeoPoint point) {
        this(point, point);
    }

    public GeoBoundingBox(GeoPoint topLeft, GeoPoint bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public boolean contains(GeoPoint point) {
        return point.getLatitude() <= topLeft.getLatitude() && point.getLatitude() >= bottomRight.getLatitude() &&
                point.getLongitude() >= topLeft.getLongitude() && point.getLongitude() <= bottomRight.getLongitude();
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof GeoBoundingBox &&
                equals((GeoBoundingBox) that);
    }

    private boolean equals(GeoBoundingBox that) {
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
