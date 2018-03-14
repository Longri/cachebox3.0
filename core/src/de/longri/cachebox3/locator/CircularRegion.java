/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.locator;

import de.longri.cachebox3.utils.MathUtils;

/**
 * Created by Longri on 13.03.18.
 */
public class CircularRegion extends Region {

    public final LatLong center;
    public final double radius;
    private final float[] results = new float[1];


    public CircularRegion(LatLong center, double radius) {
        this.center = center;
        this.radius = radius;
    }


    @Override
    public boolean contains(LatLong latLong) {
        MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, latLong.getLatitude(), latLong.getLongitude(), center.getLatitude(), center.getLongitude(), results);
        return results[0] < radius;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof CircularRegion) {
            CircularRegion circularRegion = (CircularRegion) other;
            if (!circularRegion.center.equals(this.center)) return false;
            if (circularRegion.radius != this.radius) return false;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Region center:" + center.latitude + " , " + center.longitude + " radius:" + radius;
    }
}
