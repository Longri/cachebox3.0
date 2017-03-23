/* 
 * Copyright (C) 2011-2014 team-cachebox.de
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

import java.io.Serializable;
import java.util.ArrayList;


import de.longri.cachebox3.locator.events.newT.EventHandler;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.MathUtils.CalculationType;
import de.longri.cachebox3.utils.converter.UTMConvert;
import org.oscim.backend.canvas.Bitmap;

public class Coordinate extends LatLong implements Serializable {
    private static final long serialVersionUID = 4288564255686705L;

    public static final String br = System.getProperty("line.separator");

    protected boolean valid;

    private static final float[] mResults = new float[2];

    public static Coordinate Project(Coordinate coord, double Direction, double Distance) {
        return Project(coord.getLatitude(), coord.getLongitude(), Direction, Distance);
    }

    public boolean isValid() {
        return valid;
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
        if (valid)
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
        if (valid)
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
        result.valid = true;
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
    public float bearingTo(Coordinate dest, CalculationType type) {
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
    public float distance(Coordinate coord, CalculationType type) {
        // float[] dist = new float[1];
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
        Coordinate coord = EventHandler.getCoordinate();
        if (coord == null) return -1;
        MathUtils.computeDistanceAndBearing(type, getLatitude(), getLongitude(), coord.getLatitude(), coord.getLongitude(), dist);
        return dist[0];
    }

    private final static double TOL = 0.000008;

    @Override
    public boolean equals(Object other) {
        if (other instanceof Coordinate) {
            Coordinate coord = (Coordinate) other;

            double la = (this.getLatitude() > coord.getLatitude()) ? this.getLatitude() - coord.getLatitude() : coord.getLatitude() - this.getLatitude();
            double lo = (this.getLongitude() > coord.getLongitude()) ? this.getLongitude() - coord.getLongitude() : coord.getLongitude() - this.getLongitude();

            if (la < 0)
                la *= -1;
            if (lo < 0)
                la *= -1;

            if (la > TOL)
                return false;
            if (lo > TOL)
                return false;

            return true;
        }
        return false;
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
        this.valid = parent.valid;
    }

    public Coordinate(double latitude, double longitude) {
        super(latitude, longitude);
        if (latitude == 0 && longitude == 0)
            return;
        valid = true;
    }

    public Coordinate(int latitude, int longitude) {
        super(latitude, longitude);
        if (latitude == 0 && longitude == 0)
            return;
        valid = true;
    }

    public Coordinate(String text) {
        this(parseCoordinate(text));
    }

    public Coordinate(double[] coordinate) {
        super(coordinate[0], coordinate[1]);
        if (coordinate.length > 2) {
            this.valid = coordinate[2] == 1;
        }
    }

    public static double[] parseCoordinate(String text) {
        double[] values = new double[3];
        text = text.toUpperCase();
        text = text.replace(",", ".");
        values[2] = 0;

        // UTM versuche
        String[] utm = text.trim().split(" ");
        if (utm.length == 3) {
            {
                String zone = utm[0];
                String seasting = utm[1];
                String snording = utm[2];
                try {
                    snording = snording.replace(",", ".");
                    seasting = seasting.replace(",", ".");
                    double nording = Double.valueOf(snording);
                    double easting = Double.valueOf(seasting);
                    UTMConvert convert = new UTMConvert();
                    double ddlat = 0;
                    double ddlon = 0;
                    convert.iUTM2LatLon(nording, easting, zone);
                    ddlat = convert.dLat;
                    ddlon = convert.dLon;
                    // Ergebnis runden, da sonst Koordinaten wie 47� 60' herauskommen!
                    ddlat = Math.rint(ddlat * 1000000) / 1000000;
                    ddlon = Math.rint(ddlon * 1000000) / 1000000;
                    values[2] = 1;
                    values[0] = ddlat;
                    values[1] = ddlon;
                    return values;
                } catch (Exception ex) {

                }
            }
        }

        text = text.replace("'", "");
        text = text.replace("\\U0022", "");
        text = text.replace("\"", "");
        text = text.replace("\r", "");
        text = text.replace("\n", "");
        text = text.replace("/", "");
        // NumberFormatInfo ni = new NumberFormatInfo();
        // text = text.Replace(".", Global.DecimalSeparator);
        text = text.replace(",", ".");
        double lat = 0;
        double lon = 0;
        int ilat = text.indexOf('N');
        if (ilat < 0)
            ilat = text.indexOf('S');

        int ilon = text.indexOf('E');
        if (ilon < 0)
            ilon = text.indexOf('W');

        if (ilat < 0) {
            String[] latlon = text.split(" ");
            if (latlon.length == 2) {
                try {
                    values[0] = Double.valueOf(latlon[0]);
                    values[1] = Double.valueOf(latlon[1]);
                    values[2] = 1;
                } catch (Exception e) {
                }
            }
            return values;
        }
        if (ilon < 0)
            return values;
        if (ilat > ilon)
            return values;
        char dlat = text.charAt(ilat);
        char dlon = text.charAt(ilon);
        String slat = "";
        String slon = "";
        if (ilat < 2) {
            slat = text.substring(ilat + 1, ilon).trim().replace("\u00B0", " ");
            slon = text.substring(ilon + 1, text.length()).trim().replace("\u00B0", " ");
        } else {
            slat = text.substring(0, ilat).trim().replace("\u00B0", " ");
            slon = text.substring(ilat + 1, text.length() - 1).trim().replace("\u00B0", " ");
        }

        String[] clat = slat.split(" ");
        String[] clon = slon.split(" ");
        ArrayList<String> llat = new ArrayList<String>(clat.length);
        ArrayList<String> llon = new ArrayList<String>(clon.length);
        for (String ss : clat) {
            if (!ss.equals("")) {
                llat.add(ss);
            }

        }
        for (String ss : clon) {
            if (!ss.equals("")) {
                llon.add(ss);
            }

        }

        try {
            if ((llat.size() == 1) && (llon.size() == 1)) {
                // Decimal
                lat = Double.valueOf(llat.get(0));
                lon = Double.valueOf(llon.get(0));
            } else if ((llat.size() == 2) && (llon.size() == 2)) {
                // Decimal Minute
                lat = Integer.valueOf(llat.get(0));
                lat += Double.valueOf(llat.get(1)) / 60;
                lon = Integer.valueOf(llon.get(0));
                lon += Double.valueOf(llon.get(1)) / 60;
            } else if ((llat.size() == 3) && (llon.size() == 3)) {
                // Decimal - Minute - Second
                lat = Integer.valueOf(llat.get(0));
                lat += Double.valueOf(llat.get(1)) / 60;
                lat += Double.valueOf(llat.get(2)) / 3600;
                lon = Integer.valueOf(llon.get(0));
                lon += Double.valueOf(llon.get(1)) / 60;
                lon += Double.valueOf(llon.get(2)) / 3600;
            } else {
                values[2] = 0;
                return values;
            }
        } catch (Exception exc) {
            values[2] = 0;
            return values;
        }
        values[0] = lat;
        values[1] = lon;
        if (dlat == 'S')
            values[0] = -values[0];
        if (dlon == 'W')
            values[1] = -values[1];
        values[2] = 1;
        if (values[0] > 180.00001)
            values[2] = 0;
        if (values[0] < -180.00001)
            values[2] = 0;
        if (values[1] > 180.00001)
            values[2] = 0;
        if (values[1] < -180.00001)
            values[2] = 0;

        return values;

    }

    public Coordinate copy() {
        return new Coordinate(this);
    }

    public void setValid(boolean b) {
        valid = b;
    }

    @Override
    public String toString() {
        return FormatCoordinate();
    }


}