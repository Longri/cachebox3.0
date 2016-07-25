/*
 * Copyright (C) 2016 team-cachebox.de
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

import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UnitFormatter {

    private UnitFormatter() {
    }

    final static org.slf4j.Logger log = LoggerFactory.getLogger(UnitFormatter.class);
    private final static String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static String ROT13_LOOKUP = "nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM";

    private static boolean mUseImperialUnits = false;

    public static void setUseImperialUnits(boolean useImperialUnits) {
        mUseImperialUnits = useImperialUnits;
    }

    public static boolean getUseImperialUnits() {
        return mUseImperialUnits;
    }

    // / <summary>
    // / Erzeugt eine f�r den Menschen lesbare Form der Distanz
    // / </summary>
    // / <param name="distance"></param>
    // / <returns></returns>
    public static String DistanceString(float distance) {
        if (mUseImperialUnits)
            return DistanceStringImperial(distance);
        else
            return DistanceStringMetric(distance);
    }

    // / <summary>
    // / Erzeugt eine f�r den Menschen lesbare Form der Distanz
    // / </summary>
    // / <param name="distance"></param>
    // / <returns></returns>
    public static String DistanceStringMetric(float distance) {

        if (distance <= 500)
            return String.format("%.0f", distance) + " m";

        if (distance < 10000)
            return String.format("%.2f", distance / 1000) + " km";

        return String.format("%.0f", distance / 1000) + " km";
    }

    // / <summary>
    // / Erzeugt eine f�r den Menschen lesbare Form der Distanz
    // / </summary>
    // / <param name="distance"></param>
    // / <returns></returns>
    public static String DistanceStringImperial(float distance) {

        float yards = distance / 0.9144f;
        float miles = yards / 1760;

        if (yards < 1000)
            return String.format("%.0f", yards) + " yd";

        if (miles < 10)
            return String.format("%.2f", miles) + " mi";

        return String.format("%.1f", miles) + " mi";

    }

    public static String AltString(float distance) {
        if (mUseImperialUnits)
            return AltStringImperial(distance);
        else
            return AltStringMetric(distance);
    }

    public static String AltStringMetric(float alt) {
        return String.format("%.0f", alt) + " m";
    }

    public static String AltStringImperial(float alt) {
        float yards = alt / 0.9144f;
        return String.format("%.0f", yards) + " yd";
    }

    public static String SpeedString(float kmh) {
        if (mUseImperialUnits)
            return SpeedStringImperial(kmh);
        else
            return SpeedStringMetric(kmh);
    }

    public static String SpeedStringMetric(float kmh) {
        return String.format("%.2f km/h", kmh);
    }

    public static String SpeedStringImperial(float kmh) {
        return String.format("%.2f mph", kmh / 1.6093f);
    }

    private static String FormatDM(double coord, String positiveDirection, String negativeDirection) {
        int deg = (int) coord;
        double frac = coord - deg;
        double min = frac * 60;

        String result = Math.abs(deg) + "\u00B0  " + String.format("%.3f", Math.abs(min));

        result += " ";

        if (coord < 0)
            result += negativeDirection;
        else
            result += positiveDirection;

        return result;
    }

    public static String FormatLatitudeDM(double latitude) {
        return FormatDM(latitude, "N", "S");
    }

    public static String FormatLongitudeDM(double longitude) {
        return FormatDM(longitude, "E", "W");
    }

    public static String Rot13(String message) {
        String result = "";
        for (int i = 0; i < message.length(); i++) {
            String curChar = message.substring(i, i + 1);
            int idx = ALPHABET.indexOf(curChar);

            if (idx < 0)
                result += curChar;
            else
                result += ROT13_LOOKUP.substring(idx, idx + 1);
        }
        return result;
    }

    private final static String WRONG_DATE = "??.??.??";

    public static String getReadableDate(Date date) {
        if (date == null)
            return WRONG_DATE;

        String dateString = WRONG_DATE;
        try {
            SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
            dateString = postFormater.format(date);
        } catch (Exception e) {
            log.error("getReadableDate", e);
        }
        return dateString;
    }

}
