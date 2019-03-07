/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.utils;

import de.longri.cachebox3.utils.converter.UTMConvert;

import java.util.ArrayList;

/**
 * Created by Longri on 20.06.2017.
 */
public class GeoUtils {

    /**
     * Conversion factor from degrees to microdegrees.
     */
    private static final double CONVERSION_FACTOR = 1000000.0;

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

        text = text.replace("'", " ");
        text = text.replace("\\U0022", "");
        text = text.replace("\"", " ");
        text = text.replace("\r", "");
        text = text.replace("\n", "");
        text = text.replace("/", "");
        text = text.replace((char) 8242, ' ');
        text = text.replace((char) 8243, ' ');
        text = text.replace((char) 176, ' ');
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

        //round values to six digits
        values[0] = (double) Math.round(values[0] * 1000000d) / 1000000d;
        values[1] = (double) Math.round(values[1] * 1000000d) / 1000000d;


        return values;

    }

    /**
     * Converts a coordinate from degrees to microdegrees (degrees * 10^6). No validation is performed.
     *
     * @param coordinate the coordinate in degrees.
     * @return the coordinate in microdegrees (degrees * 10^6).
     */
    public static int degreesToMicrodegrees(double coordinate) {
        return (int) (coordinate * CONVERSION_FACTOR);
    }

    /**
     * Converts a coordinate from microdegrees (degrees * 10^6) to degrees. No validation is performed.
     *
     * @param coordinate the coordinate in microdegrees (degrees * 10^6).
     * @return the coordinate in degrees.
     */
    public static double microdegreesToDegrees(int coordinate) {
        return coordinate / CONVERSION_FACTOR;
    }
}
