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


import de.longri.cachebox3.gui.utils.CharSequenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UnitFormatter {

    private UnitFormatter() {
    }

    final static Logger log = LoggerFactory.getLogger(UnitFormatter.class);
    private final static String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static String ROT13_LOOKUP = "nopqrstuvwxyzabcdefghijklmNOPQRSTUVWXYZABCDEFGHIJKLM";

    public static int getMiles(int kilometer) {
        return (int) Math.ceil(kilometer * 0.621371);
    }

    public static int getKilometer(int miles) {
        return (int) Math.ceil(miles * 1.609343502101154);
    }

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
    public static String distanceString(float distance, boolean ceil) {
        if (mUseImperialUnits)
            return distanceStringImperial(distance, ceil);
        else
            return distanceStringMetric(distance, ceil);
    }

    // / <summary>
    // / Erzeugt eine f�r den Menschen lesbare Form der Distanz
    // / </summary>
    // / <param name="distance"></param>
    // / <returns></returns>
    private static String distanceStringMetric(float distance, boolean ceil) {

        if (distance <= 500) {
            return String.format("%.0f", ceil ? Math.ceil(distance) : distance) + " m";
        }
        if (distance < 10000) {
            distance /= 1000;
            return String.format(ceil ? "%.1f" : "%.2f", distance) + " km";
        }
        distance /= 1000;
        return String.format("%.0f", ceil ? Math.ceil(distance) : distance) + " km";
    }

    // / <summary>
    // / Erzeugt eine f�r den Menschen lesbare Form der Distanz
    // / </summary>
    // / <param name="distance"></param>
    // / <returns></returns>
    private static String distanceStringImperial(float distance, boolean ceil) {

        float yards = distance / 0.9144f;
        float miles = yards / 1760;

        if (yards < 1000) {
            return String.format(ceil ? "%.0f" : "%.0f", ceil ? Math.ceil(yards) : yards) + " yd";
        }
        if (miles < 10)
            return String.format(ceil ? "%.1f" : "%.2f", miles) + " mi";

        return String.format(ceil ? "%.0f" : "%.1f", ceil ? Math.ceil(miles) : miles) + " mi";

    }

    public static String altString(float distance) {
        if (mUseImperialUnits)
            return altStringImperial(distance);
        else
            return altStringMetric(distance);
    }

    private static String altStringMetric(float alt) {
        return String.format("%.0f", alt) + " m";
    }

    private static String altStringImperial(float alt) {
        float yards = alt / 0.9144f;
        return String.format("%.0f", yards) + " yd";
    }

    public static String speedString(float kmh, boolean ceil) {
        if (mUseImperialUnits)
            return speedStringImperial(kmh, ceil);
        else
            return speedStringMetric(kmh, ceil);
    }

    private static String speedStringMetric(float kmh, boolean ceil) {
        return String.format(ceil ? "%.0f km/h" : "%.2f km/h", ceil ? Math.ceil(kmh) : kmh);
    }

    private static String speedStringImperial(float kmh, boolean ceil) {
        return String.format(ceil ? "%.0f mph" : "%.2f mph", ceil ? Math.ceil(kmh / 1.6093f) : kmh / 1.6093f);
    }

    private static String formatDM(double coord, String positiveDirection, String negativeDirection) {
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

    public static String formatLatitudeDM(double latitude) {
        return formatDM(latitude, "N", "S");
    }

    public static String formatLongitudeDM(double longitude) {
        return formatDM(longitude, "E", "W");
    }

    public static CharSequence rot13(CharSequence message) {

        if (message instanceof CharSequenceArray) {
            // change the char's and return it self
            for (int i = 0; i < message.length(); i++) {
                char curChar = message.charAt(i);
                int idx = ALPHABET.indexOf(curChar);

                if (idx < 0)
                    ((CharSequenceArray) message).set(i, curChar);
                else
                    ((CharSequenceArray) message).set(i, ROT13_LOOKUP.charAt(idx));
            }
            return message;
        }

        String result = "";
        for (int i = 0; i < message.length(); i++) {
            char curChar = message.charAt(i);
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
