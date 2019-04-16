

//  Don't modify this file, it's created by tool 'extract_libgdx_test

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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.utils.*;

import com.badlogic.gdx.utils.CharArray;
import de.longri.cachebox3.translation.word.MutableString;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.Platform;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static de.longri.cachebox3.platform_test.Assert.assertEquals;
import static de.longri.cachebox3.platform_test.Assert.assertTrue;


/**
 * Created by Longri on 27.10.2017.
 */
public class CharSequenceUtilTest {

    private static CharArray buffer = new CharArray();

    private static String t1 = "Test";
    private static String t2 = "String";
    private static String t3 = "Added";
    private static MutableString m1 = new MutableString(buffer, t1);
    private static MutableString m3 = new MutableString(buffer, t3);
    private static MutableString m2 = new MutableString(buffer, t2);
    private static String shouldString = t1 + t2 + t3;

    static {
        m1.add(m2).add(m3);
    }


    @Test
    public void contains() throws PlatformAssertionError {
        assertTrue(shouldString.length() == m1.length(), "Length must charSequenceEquals  '" + shouldString.length() + "' : '" + m1.length() + "'");
        assertTrue(CharSequenceUtil.equals(shouldString, m1), "MutableString must charSequenceEquals  '" + shouldString + "' : '" + m1 + "'");
        assertTrue(shouldString.equals(m1.toString()), "toString() must charSequenceEquals  '" + shouldString + "' : '" + m1 + "'");

        assertTrue(CharSequenceUtil.contains(m1, "Tes"), "Must contains 'Tes");
        assertTrue(CharSequenceUtil.contains(m1, "stStr"), "Must contains 'stStr");
        assertTrue(CharSequenceUtil.contains(m1, "rin"), "Must contains 'rin");
        assertTrue(CharSequenceUtil.contains(m1, "ngAdd"), "Must contains 'ngAdd");
        assertTrue(CharSequenceUtil.contains(m1, "stStringAdde"), "Must contains 'stStringAdde");
        assertTrue(CharSequenceUtil.contains(m1, "TestStringAdded"), "Must contains 'TestStringAdded");
    }

    @Test
    public void startsWith() throws PlatformAssertionError {
        assertTrue(shouldString.length() == m1.length(), "Length must charSequenceEquals  '" + shouldString.length() + "' : '" + m1.length() + "'");
        assertTrue(CharSequenceUtil.equals(shouldString, m1), "MutableString must charSequenceEquals  '" + shouldString + "' : '" + m1 + "'");
        assertTrue(shouldString.equals(m1.toString()), "toString() must charSequenceEquals  '" + shouldString + "' : '" + m1 + "'");

        assertTrue(CharSequenceUtil.startsWith(m1, "Tes"), "Must starts with 'Tes");
        assertTrue(!CharSequenceUtil.startsWith(m1, "stStr"), "Must not starts with 'stStr");
        assertTrue(!CharSequenceUtil.startsWith(m1, "rin"), "Must not starts with 'rin");
        assertTrue(!CharSequenceUtil.startsWith(m1, "ngAdd"), "Must not starts with 'ngAdd");
        assertTrue(!CharSequenceUtil.startsWith(m1, "stStringAdde"), "Must not starts with 'stStringAdde");
        assertTrue(CharSequenceUtil.startsWith(m1, "TestStringAdded"), "Must starts with 'TestStringAdded");
    }

    @Test
    public void indexOf() throws PlatformAssertionError {
        assertTrue(shouldString.length() == m1.length(), "Length must charSequenceEquals  '" + shouldString.length() + "' : '" + m1.length() + "'");
        assertTrue(CharSequenceUtil.equals(shouldString, m1), "MutableString must charSequenceEquals  '" + shouldString + "' : '" + m1 + "'");
        assertTrue(shouldString.equals(m1.toString()), "toString() must charSequenceEquals  '" + shouldString + "' : '" + m1 + "'");

        assertTrue(CharSequenceUtil.indexOf(m1, "Tes") == 0, "Index of 'Tes' must be 0");
        assertTrue(CharSequenceUtil.indexOf(m1, "stStr") == 2, "Index of 'stStr' must be 2");
        assertTrue(CharSequenceUtil.indexOf(m1, "rin") == 6, "Index of 'rin' must be 6");
        assertTrue(CharSequenceUtil.indexOf(m1, "ngAdd") == 8, "Index of 'ngAdd' must be 8");
        assertTrue(CharSequenceUtil.indexOf(m1, "stStringAdde") == 2, "Index of 'stStringAdde' must be 2");
        assertTrue(CharSequenceUtil.indexOf(m1, "TestStringAdded") == 0, "Index of 'TestStringAdded' must be 0");
    }


    private char[] PARSE_ARRAY = " 49.349817 219011721901171232  3810940 TRUE False".toCharArray();

    @Test
    public void parseDouble() throws PlatformAssertionError {
        double d = CharSequenceUtil.parseDouble(PARSE_ARRAY, 1, 9);
        assertTrue(d == 49.349817, "Value should be 49.349817");
    }

    @Test
    public void parseInteger() throws PlatformAssertionError {
        int i = CharSequenceUtil.parseInteger(PARSE_ARRAY, 31, 7);
        assertTrue(i == 3810940, "Value should be 3810940");

        boolean throwedException = false;
        try {
            CharSequenceUtil.parseInteger(PARSE_ARRAY, 11, 18);
        } catch (ArithmeticException e) {
            throwedException = true;
        }
        assertTrue(throwedException, "must throw exception");
    }

    @Test
    public void parseLong() throws PlatformAssertionError {
        long l = CharSequenceUtil.parseLong(PARSE_ARRAY, 11, 18);
        assertTrue(l == 219011721901171232L, "Value should be 219011721901171232");
    }

    @Test
    public void parseBoolean() throws PlatformAssertionError {
        boolean b = CharSequenceUtil.parseBoolean(PARSE_ARRAY, 39, 4);
        assertTrue(b, "Value should be true");

        b = CharSequenceUtil.parseBoolean(PARSE_ARRAY, 44, 5);
        assertTrue(!b, "Value should be false");
    }


    private char[] PARSE_DATE_ARRAY = " 49.349817 219011721901171232 2011-07-05T12:54:02.308107Z 3810940 2011-04-16T07:00:00Z TRUE 2011-04-17T03:39:24.4 False".toCharArray();
    private final Locale locale = Locale.getDefault();
    private final String STRING_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss";
    private final String STRING_PATTERN3 = "yyyy-MM-dd'T'HH:mm:ss.S";

    private final SimpleDateFormat DATE_PATTERN_2 = new SimpleDateFormat(STRING_PATTERN2, locale);
    private final SimpleDateFormat DATE_PATTERN_3 = new SimpleDateFormat(STRING_PATTERN3, locale);

    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Test
    public void parseDate() throws ParseException, PlatformAssertionError {
        Date expected = DATE_PATTERN_3.parse("2011-07-05T12:54:02.308107Z");
        if (CanvasAdapter.platform == Platform.ANDROID) {
            //Android ignored parsing of milliseconds, so we add manuel!
            Calendar cal = Calendar.getInstance();
            cal.setTime(expected);
            cal.add(Calendar.MILLISECOND, 308107);
            expected = cal.getTime();
        }
        Date actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 30, 27, STRING_PATTERN3.toCharArray());
        assertTrue(actual != null, "Date should not NULL");

        String expectedString = iso8601Format.format(expected);
        String actualString = iso8601Format.format(actual);

        assertEquals(expectedString, actualString, "Date String should be charSequenceEquals");

        //-------------------------------------------------------------------------------------------------------------------------------
        expected = DATE_PATTERN_2.parse("2011-04-16T07:00:00Z");
        actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 66, 20, STRING_PATTERN2.toCharArray());
        assertTrue(actual != null, "Date should not NULL");

        expectedString = iso8601Format.format(expected);
        actualString = iso8601Format.format(actual);

        assertEquals(expectedString, actualString, "Date String should be charSequenceEquals");

        //-------------------------------------------------------------------------------------------------------------------------------

        // is unparsable date, return date must be null
        actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 66, 20, STRING_PATTERN3.toCharArray());
        assertTrue(actual == null, "Date must be NULL");

        //-------------------------------------------------------------------------------------------------------------------------------
        expected = DATE_PATTERN_2.parse("2011-04-17T03:39:24.4");
        actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 92, 21, STRING_PATTERN2.toCharArray());
        assertTrue(actual != null, "Date should not NULL");

        expectedString = iso8601Format.format(expected);
        actualString = iso8601Format.format(actual);

        assertEquals(expectedString, actualString, "Date String should be charSequenceEquals");

        //-------------------------------------------------------------------------------------------------------------------------------
        expected = DATE_PATTERN_3.parse("2011-04-17T03:39:24.4");
        actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 92, 21, STRING_PATTERN3.toCharArray());
        assertTrue(actual != null, "Date should not NULL");

        expectedString = iso8601Format.format(expected);
        actualString = iso8601Format.format(actual);

        assertEquals(expectedString, actualString, "Date String should be charSequenceEquals");

    }

    @Test
    public void getHtmlStringTest() throws PlatformAssertionError {

        char[] source = ("&lt;p&gt;Ein Spaziergang führt Euch durch die Geschichte der\r\n" +
                "Wollankstraße in Berlin, die auch durch die Berliner Mauer stark\r\n" +
                "geprägt wurde &amp; Die angegebenen Koordinaten markieren den\r\n" +
                "Ausgangspunkt des Spazierganges, den S-Bahnhof Wollankstraße.&lt;/p&gt;\r\n" +
                "&lt;!-- Ende Kurzbeschreibung --&gt;").toCharArray();

        String target = "<p>Ein Spaziergang führt Euch durch die Geschichte der\n" +
                "Wollankstraße in Berlin, die auch durch die Berliner Mauer stark\n" +
                "geprägt wurde & Die angegebenen Koordinaten markieren den\n" +
                "Ausgangspunkt des Spazierganges, den S-Bahnhof Wollankstraße.</p>\n" +
                "<!-- Ende Kurzbeschreibung -->";

        String result = CharSequenceUtil.getHtmlString(source, 0, source.length);

        assertEquals(target, result, "Should be charSequenceEquals");

    }
}
