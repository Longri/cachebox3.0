/*
 * Copyright (C) 2011-2018 team-cachebox.de
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

public class Coordinate extends LatLong {

    static final String br = System.getProperty("line.separator");

    /**
     * Maximum possible latitude coordinate.
     */
    public static final double LATITUDE_MAX = 90;

    /**
     * Minimum possible latitude coordinate.
     */
    public static final double LATITUDE_MIN = -LATITUDE_MAX;

    /**
     * Maximum possible longitude coordinate.
     */
    public static final double LONGITUDE_MAX = 180;

    /**
     * Minimum possible longitude coordinate.
     */
    public static final double LONGITUDE_MIN = -LONGITUDE_MAX;

    private static final float[] mResults = new float[2];

    public Coordinate(LatLong latLon) {
        super(latLon);
    }

    public static Coordinate Project(Coordinate coord, double Direction, double Distance) {
        return Project(coord.getLatitude(), coord.getLongitude(), Direction, Distance);
    }

    /**
     * A Coordinate is valid, if lat/lon in range and not 0,0!
     * 0,0 is in Range of max/min lat/lon, but we handle this as not valid
     *
     * @return
     */
    public boolean isValid() {

        //we use getLatitude() and getLongitude() because some extended classes use their own value!
        double lat = getLatitude();
        double lon = getLongitude();

        if (lat < LATITUDE_MIN || lat > LATITUDE_MAX || lat == 0) return false;
        if (lon < LONGITUDE_MIN || lon > LONGITUDE_MAX || lon == 0) return false;

        return true;
    }

    public boolean isZero() {
        if (!isValid())
            return false;
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
    public String FormatCoordinate() {
        if (isValid())
            return Formatter.FormatLatitudeDM(getLatitude()) + " / " + Formatter.FormatLongitudeDM(getLongitude());
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
            return Formatter.FormatLatitudeDM(getLatitude()) + br + Formatter.FormatLongitudeDM(getLongitude());
        else
            return "not Valid";
    }

    private static final double EARTH_RADIUS = 6378137.0;

    public static Coordinate Project(double Latitude, double Longitude, double Direction, double Distance) {
        double dist = Distance / EARTH_RADIUS; // convert dist to angular distance in radians
        double brng = Direction * MathUtils.DEG_RAD; //
        double lat1 = Latitude * MathUtils.DEG_RAD;
        double lon1 = Longitude * MathUtils.DEG_RAD;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI; // normalise to -180°..+180°

        Coordinate result = new Coordinate(lat2 * MathUtils.RAD_DEG, lon2 * MathUtils.RAD_DEG);
        return result;

    }

    public static double Bearing(CalculationType type, Coordinate coord1, Coordinate coord2) {
        return Bearing(type, coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude());
    }

    public static double Bearing(CalculationType type, double froLatitude, double fromLongitude, double toLatitude, double toLongitude) {
        return new Coordinate(froLatitude, fromLongitude).bearingTo(new Coordinate(toLatitude, toLongitude), type);
    }

    public static Coordinate Intersection(Coordinate coord1, Coordinate coord2, Coordinate coord3, Coordinate coord4) {
        Coordinate result = null;

        double[] x = new double[4];
        double[] y = new double[4];
        x[0] = coord1.getLongitude();
        y[0] = coord1.getLatitude();
        x[1] = coord2.getLongitude();
        y[1] = coord2.getLatitude();
        x[2] = coord3.getLongitude();
        y[2] = coord3.getLatitude();
        x[3] = coord4.getLongitude();
        y[3] = coord4.getLatitude();

        // Steigungen
        double steig1 = (y[1] - y[0]) / (x[1] - x[0]);
        double steig2 = (y[3] - y[2]) / (x[3] - x[2]);
        // Nullwerte
        double null1 = y[0] - x[0] * steig1;
        double null2 = y[2] - x[2] * steig2;
        // Schnittpunkt
        double X = (null2 - null1) / (steig1 - steig2);
        double Y = steig1 * X + null1;
        // Konvertieren in Lat-Lon

        result = new Coordinate(Y, X);
        return result;
    }

    /**
     * Returns the approximate initial bearing in degrees East of true North when traveling along the shortest path between this location
     * and the given location. The shortest path is defined using the WGS84 ellipsoid. Locations that are (nearly) antipodal may produce
     * meaningless results.
     *
     * @param dest the destination location
     * @return the initial bearing in degrees
     */
    public float bearingTo(LatLong dest, CalculationType type) {
        synchronized (mResults) {
            // See if we already have the result
            // if (getLatitude() != mLat1 || getLongitude() != mLon1 || dest.getLatitude() != mLat2 || dest.getLongitude() != mLon2)
            // {
            // MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), dest.getLatitude(), dest.getLongitude(), mResults);
            // mLat1 = getLatitude();
            // mLon1 = getLongitude();
            // mLat2 = dest.getLatitude();
            // mLon2 = dest.getLongitude();
            // mInitialBearing = mResults[1];
            // }
            // return mInitialBearing;

            synchronized (mResults) {
                MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), dest.getLatitude(), dest.getLongitude(), mResults);
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
    public float distance(LatLong coord, CalculationType type) {
        if (coord == null) return -1;
        MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), coord.getLatitude(), coord.getLongitude(), mResults);
        return mResults[0];
    }

    /**
     * Returns the distance to to last valid Position
     *
     * @return
     */
    public float distance(CalculationType type) {
        float[] dist = new float[1];
        LatLong myPos = EventHandler.getMyPosition();
        MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), myPos.latitude, myPos.longitude, dist);
        return dist[0];
    }

    private final static double TOL = 0.000008;

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

        if (la > TOL)
            return false;
        if (lo > TOL)
            return false;

        return true;
    }

    public static Coordinate Crossbearing(CalculationType type, Coordinate coord1, double direction1, Coordinate coord2, double direction2) {
        float[] dist = new float[4];
        MathUtils.computeDistanceAndBearing(type, coord1.getLatitude(), coord1.getLongitude(), coord2.getLatitude(), coord2.getLongitude(), dist);
        double distance = dist[0];
        Coordinate coord3 = Project(coord1, direction1, distance);
        Coordinate coord4 = Project(coord2, direction2, distance);

        return Intersection(coord1, coord3, coord2, coord4);
    }

    public Coordinate(Coordinate parent) {
        super(parent.latitude, parent.longitude);
    }

    public Coordinate(double latitude, double longitude) {
        super(latitude, longitude);
    }

    public Coordinate(int latitude, int longitude) {
        super(latitude, longitude);
        if (latitude == 0 && longitude == 0)
            return;
    }

    public Coordinate(String text) {
        this(GeoUtils.parseCoordinate(text));
    }

    public Coordinate(double[] coordinate) {
        super(coordinate[0], coordinate[1]);
    }

    public Coordinate copy() {
        return new Coordinate(this);
    }

    @Override
    public String toString() {
        return FormatCoordinate();
    }

}