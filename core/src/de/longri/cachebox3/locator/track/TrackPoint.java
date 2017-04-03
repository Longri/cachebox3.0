/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.locator.track;

import de.longri.cachebox3.locator.LatLong;

import java.util.Date;

public class TrackPoint extends LatLong {
    public double Elevation;
    public double Direction;
    public Date TimeStamp;

    public TrackPoint(double latitude, double longitude, double elevation, double direction, Date time) {
        super(latitude, longitude);
        this.Elevation = elevation;
        this.Direction = direction;
        this.TimeStamp = time;
    }
}
