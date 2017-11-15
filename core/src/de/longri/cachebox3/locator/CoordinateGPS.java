/* 
 * Copyright (C) 2011-2016 team-cachebox.de
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

import org.oscim.core.MapPosition;

import java.io.Serializable;

/**
 * @author Longri
 */
public class CoordinateGPS extends Coordinate implements Serializable {

    private static final long serialVersionUID = 1235642315487L;

    private double Elevation = 0;

    /**
     * Die Genauigkeit dieser Coordinate! Wird beim Messen benutzt
     */
    protected int Accuracy = -1;
    private double speed;
    private double heading;
    private boolean isGPSprovided;

    public CoordinateGPS(double latitude, double longitude) {
        super(latitude, longitude);
        this.setElevation(0);
        if (latitude == 0 && longitude == 0)
            return;
    }

    public CoordinateGPS(double latitude, double longitude, int accuracy) {
        super(latitude, longitude);
        this.setElevation(0);
        this.Accuracy = accuracy;
        if (latitude == 0 && longitude == 0)
            return;
    }

    public CoordinateGPS(int latitude, int longitude, int accuracy) {
        super(latitude, longitude);
        this.setElevation(0);
        this.Accuracy = accuracy;
        if (latitude == 0 && longitude == 0)
            return;
    }

    public CoordinateGPS(CoordinateGPS parent) {
        super(parent.latitude, parent.longitude);
        this.setElevation(parent.getElevation());
        this.Accuracy = parent.getAccuracy();
    }

    public CoordinateGPS(MapPosition mapPosition) {
        super(mapPosition.getLatitude(), mapPosition.getLongitude());
    }

    public boolean hasAccuracy() {
        if (Accuracy == -1)
            return false;
        return true;
    }


    public int getAccuracy() {
        return Accuracy;
    }

    /**
     * Parse Coordinates from String
     *
     * @param text
     */
    public CoordinateGPS(String text) {
        super(text);
    }


    public double getElevation() {
        return Elevation;
    }


    public void setElevation(double elevation) {
        Elevation = elevation;
    }

    public void setAccuracy(float accuracy) {
        Accuracy = (int) accuracy;
    }

    public boolean isGPSprovided() {
        return this.isGPSprovided;
    }

    public double getHeading() {
        return this.heading;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setIsGpsProvided(boolean isGpsProvided) {
        this.isGPSprovided = isGpsProvided;
    }
}