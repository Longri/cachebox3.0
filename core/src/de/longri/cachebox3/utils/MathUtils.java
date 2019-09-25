/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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

import org.oscim.core.Point;

/**
 * Implements some static methods!
 *
 * @author Longri
 */
public class MathUtils {
    /**
     * precalculation of Math.PI / 180.0
     */
    static public final double DEG_RAD = Math.PI / 180.0;

    /**
     * precalculation of 180.0 / Math.PI
     */
    static public final double RAD_DEG = 180.0 / Math.PI;

    /**
     * precalculation of Math.PI * 2
     */
    static public final double PI2 = Math.PI * 2;

    /**
     * precalculation of Math.PI / 2
     */
    static public final double HALF_PI = Math.PI / 2;

    /**
     * WGS84 major axis = 6378137.0
     */
    static public final double WGS84_MAJOR_AXIS = 6378137.0; // WGS84 major axis

    /**
     * WGS84 semi-major axis = 6356752.3142
     */
    static public final double WGS84_SEMI_MAJOR_AXIS = 6356752.3142; // WGS84 semi-major axis

    /**
     * Legalize the given degrees in to a value between 0 and 360!
     *
     * @param value
     * @return
     */
    static public float LegalizeDegreese(float value) {
        while (value > 360) {
            value = value - 360;
        }

        while (value < 0) {
            value += 360;
        }

        return value;
    }

    /**
     * Computes the approximate distance in meters between two locations, and optionally the initial and final bearings of the shortest path
     * between them. distance and bearing are defined using the WGS84 ellipsoid.
     * <p>
     * The computed distance is stored in results[0]. If results has length 2 or greater, the initial bearing is stored in results[1]. If
     * results has length 3 or greater, the final bearing is stored in results[2].
     *
     * @param startLatitude  the starting latitude
     * @param startLongitude the starting longitude
     * @param endLatitude    the ending latitude
     * @param endLongitude   the ending longitude
     * @param results        an array of floats to hold the results
     * @throws IllegalArgumentException if results is null or has length < 1
     */
    public static void computeDistanceAndBearing(CalculationType type, double startLatitude, double startLongitude, double endLatitude, double endLongitude, float[] results) {
        if (results == null || results.length < 1) {
            throw new IllegalArgumentException("results is null or has length < 1");
        }

        switch (type) {
            case ACCURATE:
                computeDistanceAndBearingAccurate(startLatitude, startLongitude, endLatitude, endLongitude, results);
                break;
            case FAST:
                computeDistanceAndBearingFast(startLatitude, startLongitude, endLatitude, endLongitude, results);
                break;
        }
    }

    public static void clampToMinMax(float[] mBox, int offset, short maxValue) {
        if (mBox[offset] < -maxValue) mBox[offset] = -maxValue;
        else if (mBox[offset] > maxValue) mBox[offset] = maxValue;
    }

    public static float hypo(float b, float c) {
        return (float) Math.sqrt((b * b) + (c * c));
    }

    public enum CalculationType {
        FAST, ACCURATE
    }

    /**
     * Fast calculation with Cos/Sin/Atan over LockUpTable
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param results
     */
    private static void computeDistanceAndBearingFast(double lat1, double lon1, double lat2, double lon2, float[] results) {
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);

        lat1 *= DEG_RAD;
        lon1 *= DEG_RAD;
        lat2 *= DEG_RAD;
        lon2 *= DEG_RAD;

        int IntWGS84_MAJOR_AXIS = (int) WGS84_MAJOR_AXIS;

        results[0] = (float) ((IntWGS84_MAJOR_AXIS) * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos((lon2 - lon1))));

        // results[0] = (float) distance;

        if (results.length > 1) {

            double longDiff = Math.toRadians(longitude2 - longitude1);
            double y = Math.sin(longDiff) * Math.cos(latitude2);
            double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

            double angle = Math.toDegrees(Math.atan2(y, x));
            results[1] = (float) (angle);
            if (results.length > 2) {
                results[2] = results[1];
            }
        }
    }

    static final double f = (WGS84_MAJOR_AXIS - WGS84_SEMI_MAJOR_AXIS) / WGS84_MAJOR_AXIS;
    static final double aSqMinusBSqOverBSq = (WGS84_MAJOR_AXIS * WGS84_MAJOR_AXIS - WGS84_SEMI_MAJOR_AXIS * WGS84_SEMI_MAJOR_AXIS) / (WGS84_SEMI_MAJOR_AXIS * WGS84_SEMI_MAJOR_AXIS);

    private static void computeDistanceAndBearingAccurate(double lat1, double lon1, double lat2, double lon2, float[] results) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)

        int MAXITERS = 20;
        // Convert lat/long to radians
        lat1 *= MathUtils.DEG_RAD;
        lat2 *= MathUtils.DEG_RAD;
        lon1 *= MathUtils.DEG_RAD;
        lon2 *= MathUtils.DEG_RAD;

        double L = lon2 - lon1;
        double A = 0.0;
        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));

        double cosU1 = Math.cos(U1);
        double cosU2 = Math.cos(U2);
        double sinU1 = Math.sin(U1);
        double sinU2 = Math.sin(U2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;

        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha = 0.0;
        double cos2SM = 0.0;
        double cosSigma = 0.0;
        double sinSigma = 0.0;
        double cosLambda = 0.0;
        double sinLambda = 0.0;

        double lambda = L; // initial guess
        for (int iter = 0; iter < MAXITERS; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
            sigma = Math.atan2(sinSigma, cosSigma); // (16)
            double sinAlpha = (sinSigma == 0) ? 0.0 : cosU1cosU2 * sinLambda / sinSigma; // (17)
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)

            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
            A = 1 + (uSquared / 16384.0) * // (3)
                    (4096.0 + uSquared * (-768 + uSquared * (320.0 - 175.0 * uSquared)));
            double B = (uSquared / 1024.0) * // (4)
                    (256.0 + uSquared * (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
            double C = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = B * sinSigma * // (6)
                    (cos2SM + (B / 4.0) * (cosSigma * (-1.0 + 2.0 * cos2SMSq) - (B / 6.0) * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0 + 4.0 * cos2SMSq)));

            lambda = L + (1.0 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SM + C * cosSigma * (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)

            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }

        float distance = (float) (WGS84_SEMI_MAJOR_AXIS * A * (sigma - deltaSigma));
        results[0] = distance;
        if (results.length > 1) {
            float initialBearing = (float) Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
            initialBearing *= MathUtils.RAD_DEG;
            results[1] = initialBearing;
            if (results.length > 2) {
                float finalBearing = (float) Math.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
                finalBearing *= MathUtils.RAD_DEG;
                results[2] = finalBearing;
            }
        }
    }


    public static float linearInterpolation(float min, float max, float min2, float max2, float value) {
        float range1 = max - min;
        float range2 = max2 - min2;
        return ((value - min) * range2 / range1) + min2;
    }


    public static boolean lineIntersect(float[] p, int p1, int p2, int p3, int p4, int intersect, int threeFloatTmp) {

        p[threeFloatTmp] = (p[p4 + 1] - p[p3 + 1]) * (p[p2] - p[p1]) - (p[p4] - p[p3]) * (p[p2 + 1] - p[p1 + 1]);
        if (p[threeFloatTmp] == 0.0) { // Lines are parallel.
            return false;
        }
        p[threeFloatTmp + 1] = ((p[p4] - p[p3]) * (p[p1 + 1] - p[p3 + 1]) - (p[p4 + 1] - p[p3 + 1]) * (p[p1] - p[p3])) / p[threeFloatTmp];
        p[threeFloatTmp + 2] = ((p[p2] - p[p1]) * (p[p1 + 1] - p[p3 + 1]) - (p[p2 + 1] - p[p1 + 1]) * (p[p1] - p[p3])) / p[threeFloatTmp];
        if (p[threeFloatTmp + 1] >= 0.0f && p[threeFloatTmp + 1] <= 1.0f && p[threeFloatTmp + 2] >= 0.0f && p[threeFloatTmp + 2] <= 1.0f) {
            // Set the intersection point.
            p[intersect + 0] = p[p1] + p[threeFloatTmp + 1] * (p[p2] - p[p1]);
            p[intersect + 1] = p[p1 + 1] + p[threeFloatTmp + 1] * (p[p2 + 1] - p[p1 + 1]);

            // check if on the line 1 and 2
            if (!(Math.min(p[p1], p[p2]) <= p[intersect + 0]
                    && p[intersect + 0] <= Math.max(p[p1], p[p2]))) return false;
            if (!(Math.min(p[p1 + 1], p[p2 + 1]) <= p[intersect + 1]
                    && p[intersect + 1] <= Math.max(p[p1 + 1], p[p2 + 1]))) return false;
            if (!(Math.min(p[p3], p[p4]) <= p[intersect + 0]
                    && p[intersect + 0] <= Math.max(p[p3], p[p4]))) return false;
            if (!(Math.min(p[p3 + 1], p[p4 + 1]) <= p[intersect + 1]
                    && p[intersect + 1] <= Math.max(p[p3 + 1], p[p4 + 1]))) return false;

            return true;
        }
        return false;
    }

    /**
     * Returns 0 if line outside!
     * Returns -1 if line inside
     * Returns 1 or 2 if line intersect
     *
     * @param r
     * @param offRec
     * @param offLine
     * @return
     */
    public static int clampLineToIntersectRect(float[] r, int offRec, int offLine, int intersectionPointOffset, int threeFloatTmp) {
        // if line completed into rec or outside

        boolean point1Inside = org.oscim.utils.geom.GeometryUtils.pointInPoly(r[0 + offLine], r[1 + offLine], r, 8, offRec);
        boolean point2Inside = org.oscim.utils.geom.GeometryUtils.pointInPoly(r[2 + offLine], r[3 + offLine], r, 8, offRec);
        boolean bothOutside = !point1Inside && !point2Inside;
        if (point1Inside && point2Inside) {
            return -1;
        }

        int intersectCount = 0;
        for (int i = 0; i < 8; i += 2) {
            if (lineIntersect(r, i, i > 4 ? 0 : i + 2, offLine, offLine + 2, intersectionPointOffset, threeFloatTmp)) {
                intersectCount++;
                if (!bothOutside) {
                    // can only one line intersect
                    if (point2Inside) {
                        r[offLine] = r[intersectionPointOffset + 0];
                        r[offLine + 1] = r[intersectionPointOffset + 1];
                    } else {
                        r[offLine + 2] = r[intersectionPointOffset + 0];
                        r[offLine + 3] = r[intersectionPointOffset + 1];
                    }
                    return 1;
                }
                if (intersectCount == 2) {
                    r[offLine] = r[intersectionPointOffset + 0];
                    r[offLine + 1] = r[intersectionPointOffset + 1];
                    r[offLine + 2] = r[intersectionPointOffset + 2];
                    r[offLine + 3] = r[intersectionPointOffset + 3];
                    return 2;
                }
                r[intersectionPointOffset + 2] = r[intersectionPointOffset + 0];
                r[intersectionPointOffset + 3] = r[intersectionPointOffset + 1];
            }
        }
        return 0;
    }
}
