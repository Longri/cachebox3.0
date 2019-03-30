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

import java.util.Arrays;

public class GeoBoundingBoxDouble {
    private final Coordinate topLeft, bottomRight;


    public GeoBoundingBoxDouble(Coordinate topLeft, Coordinate bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        validate();
    }


    public GeoBoundingBoxDouble(double lat, double lon, double extend) {
        double half = extend / 2;
        topLeft = new Coordinate(lat + half, lon - half);
        bottomRight = new Coordinate(lat - half, lon + half);
        validate();
    }

    public boolean contains(Coordinate point) {
        return point.getLatitude()<= topLeft.getLatitude()&& point.getLatitude()>= bottomRight.getLatitude()&&
                point.getLongitude()>= topLeft.getLongitude()&& point.getLongitude()<= bottomRight.getLongitude();
    }

    public boolean contains(double longitude, double latitude) {
        return latitude <= topLeft.getLatitude()&& latitude >= bottomRight.getLatitude()&&
                longitude >= topLeft.getLongitude()&& longitude <= bottomRight.getLongitude();
    }

    private void validate() {
        if (topLeft.getLatitude()< bottomRight.getLatitude()|| topLeft.getLongitude()> bottomRight.getLongitude()) {
            throw new IllegalArgumentException("Wrong leftTop to rightBottom \n" +
                    topLeft.getLatitude()+ " < " + bottomRight.getLatitude()+ " || " + topLeft.getLongitude()+ " > " + bottomRight.getLongitude());
        }

    }

    @Override
    public boolean equals(Object that) {
        return that instanceof GeoBoundingBoxDouble &&
                equals((GeoBoundingBoxDouble) that);
    }

    private boolean equals(GeoBoundingBoxDouble that) {
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
