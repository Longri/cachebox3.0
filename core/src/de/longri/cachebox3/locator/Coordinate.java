/*
 * Copyright (C) 2011-2019 team-cachebox.de
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

import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.utils.GeoUtils;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.MathUtils.CalculationType;

import java.util.Date;
import java.util.Objects;

import static de.longri.cachebox3.utils.GeoUtils.*;

/**
 * Created by Longri on 27.07.2016.
 */
public class Coordinate {

    private final static double TOLERANCE = 0.000008;
    private final static String br = System.getProperty("line.separator");
    private final static float[] mResults = new float[2];


// private member

    private double latitude; // The latitude coordinate of this Coordinate in degrees.
    private double longitude; //The longitude coordinate of this Coordinate in degrees.
    private int accuracy = -1;
    private double elevation = 0;
    private int hash = 0;
    private double heading = 0.0;
    private boolean isGPSprovided = false;
    private double speed = 0.0;
    private Date date = null;

// constructors

    public Coordinate() {
        this(0, 0);
    }

    public Coordinate(double latitude, double longitude, double elevation, double heading, Date date) {
        this(latitude, longitude);
        this.elevation = elevation;
        this.heading = heading;
        this.date = date;
    }

    public Coordinate(Coordinate parent) {
        this(parent.latitude, parent.longitude);
        this.hash = parent.hash;
        this.elevation = parent.elevation;
        this.speed = parent.speed;
        this.heading = parent.heading;
        this.isGPSprovided = parent.isGPSprovided;
        this.accuracy = parent.accuracy;
        this.date = parent.date;
    }

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Coordinate(String text) {
        this(GeoUtils.parseCoordinate(text));
    }

    public Coordinate(double[] coordinate) {
        this(coordinate[0], coordinate[1]);
    }


// getter / setter

    public void set(Coordinate other) {
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.elevation = other.elevation;
        this.speed = other.speed;
        this.heading = other.heading;
        this.isGPSprovided = other.isGPSprovided;
        this.accuracy = other.accuracy;
        this.date = other.date;
        this.hash = other.hash;
    }

    public void setLatLon(double latitude, double longitude) {
        if (this.latitude != latitude || this.longitude != longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.hash = 0;
        }
    }

    public void setLatitude(double latitude) {
        if (this.latitude != latitude) {
            this.latitude = latitude;
            this.hash = 0;
        }
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLongitude(double longitude) {
        if (this.longitude != longitude) {
            this.longitude = longitude;
            this.hash = 0;
        }
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setAccuracy(float accuracy) {
        if (this.accuracy != accuracy) {
            this.accuracy = (int) accuracy;
            this.hash = 0;
        }
    }

    public int getAccuracy() {
        return this.accuracy;
    }

    public void setElevation(double elevation) {
        if (this.elevation != elevation) {
            this.elevation = elevation;
            this.hash = 0;
        }
    }

    public double getElevation() {
        return this.elevation;
    }

    public void setHeading(double heading) {
        if (this.heading != heading) {
            this.heading = heading;
            this.hash = 0;
        }
    }

    public double getHeading() {
        return this.heading;
    }

    public void setIsGpsProvided(boolean isGpsProvided) {
        if (this.isGPSprovided != isGpsProvided) {
            this.isGPSprovided = isGpsProvided;
            this.hash = 0;
        }
    }

    public boolean isGPSprovided() {
        return this.isGPSprovided;
    }

    public void setSpeed(double speed) {
        if (this.speed != speed) {
            this.speed = speed;
            this.hash = 0;
        }
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setDate(Date date) {
        if (date == null) {
            if (this.date == null) return;
        } else {
            if (this.date != null && this.date.compareTo(date) == 0) return;
        }
        this.date = date;
        this.hash = 0;
    }

    public Date getDate() {
        return this.date;
    }

// methods

    public void reset() {
        this.latitude = 0;
        this.longitude = 0;
        this.heading = 0;
        this.elevation = 0;
        this.date = null;
        this.speed = 0;
        this.accuracy = -1;
        this.isGPSprovided = false;

        this.hash = 0;
    }

    /**
     * A Coordinate is valid, if lat/lon in range and not 0,0!
     * 0,0 is in Range of max/min lat/lon, but we handle this as not valid
     */
    public boolean isValid() {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX || isZero()) return false;
        return !(longitude < LONGITUDE_MIN) && !(longitude > LONGITUDE_MAX);
    }

    public boolean isZero() {
        return ((latitude == 0) && (longitude == 0));
    }

    /**
     * Gibt einen formatierten String dieser Koordinate wieder
     * 54°47′15.96″N 5°21′12.32″O
     * N 48° 40.441 E 009° 23.470
     * 54.787767° 5.353422°
     * UTM: 32U E 528797 N 5391292
     *
     * @return
     */
    public String formatCoordinate() {
        if (isValid())
            return Formatter.FormatLatitudeDM(this.latitude) + " / " + Formatter.FormatLongitudeDM(this.longitude);
        else
            return "not Valid";
    }

    /**
     * Gibt einen formatierten String dieser Koordinate in zwei Zeilen wieder
     *
     * @return
     */
    public String formatCoordinateLineBreak() {
        if (isValid())
            return Formatter.FormatLatitudeDM(this.latitude) + br + Formatter.FormatLongitudeDM(this.longitude);
        else
            return "not Valid";
    }

    /**
     * Returns the approximate initial bearing in degrees East of true North when traveling along the shortest path between this location
     * and the given location. The shortest path is defined using the WGS84 ellipsoid. Locations that are (nearly) antipodal may produce
     * meaningless results.
     *
     * @param dest the destination location
     * @return the initial bearing in degrees
     */
    public float bearingTo(Coordinate dest, CalculationType type) {
        synchronized (mResults) {
            synchronized (mResults) {
                MathUtils.computeDistanceAndBearing(type, this.latitude, this.longitude, dest.latitude, dest.longitude, mResults);
                return mResults[1];
            }
        }
    }

    /**
     * Returns the distance to to refer Coordinate
     *
     * @param coord
     * @return
     */
    public float distance(Coordinate coord, CalculationType type) {
        if (coord == null) return -1;
        MathUtils.computeDistanceAndBearing(type, this.latitude, this.longitude, coord.latitude, coord.longitude, mResults);
        return mResults[0];
    }

    /**
     * Returns the distance to to last valid Position
     *
     * @return
     */
    public float distance(CalculationType type) {
        float[] dist = new float[1];
        Coordinate myPos = EventHandler.getMyPosition();
        MathUtils.computeDistanceAndBearing(type, this.latitude, this.longitude, myPos.latitude, myPos.longitude, dist);
        return dist[0];
    }

    /**
     * Returns the approximate distance in degrees between this location and the
     * given location, calculated in Euclidean space.
     */
    public double distance(Coordinate other) {
        return Math.hypot(this.longitude - other.longitude, this.latitude - other.latitude);
    }

    @Override
    public int hashCode() {
        if (hash != 0) return hash;
        hash = Objects.hash(this.latitude,
                this.longitude,
                this.accuracy,
                this.elevation,
                this.heading,
                this.speed,
                this.isGPSprovided,
                this.date);
        if (hash == 0) { //really?
            hash = 1;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (!(obj instanceof Coordinate)) {
            return false;
        }
        Coordinate other = (Coordinate) obj;
        if (other.hashCode() != this.hashCode()) return false;


        double la = this.latitude - other.latitude;
        double lo = this.longitude - other.longitude;

        if (la < 0)
            la *= -1;
        if (lo < 0)
            lo *= -1;

        if (la > TOLERANCE)
            return false;
        if (lo > TOLERANCE)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return formatCoordinate();
    }

}