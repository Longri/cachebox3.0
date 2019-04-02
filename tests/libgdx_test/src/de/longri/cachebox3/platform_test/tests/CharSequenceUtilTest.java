

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static de.longri.cachebox3.platform_test.Assert.assertThat;
import static de.longri.cachebox3.platform_test.Assert.assertEquals;

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
        assertThat("Length must equals  '" + shouldString.length() + "' : '" + m1.length() + "'", shouldString.length() == m1.length());
        assertThat("MutableString must equals  '" + shouldString + "' : '" + m1 + "'", equals(shouldString, m1));
        assertThat("toString() must equals  '" + shouldString + "' : '" + m1 + "'", shouldString.equals(m1.toString()));

        assertThat("Must contains 'Tes", CharSequenceUtil.contains(m1, "Tes"));
        assertThat("Must contains 'stStr", CharSequenceUtil.contains(m1, "stStr"));
        assertThat("Must contains 'rin", CharSequenceUtil.contains(m1, "rin"));
        assertThat("Must contains 'ngAdd", CharSequenceUtil.contains(m1, "ngAdd"));
        assertThat("Must contains 'stStringAdde", CharSequenceUtil.contains(m1, "stStringAdde"));
        assertThat("Must contains 'TestStringAdded", CharSequenceUtil.contains(m1, "TestStringAdded"));
    }

    @Test
    public void startsWith() throws PlatformAssertionError {
        assertThat("Length must equals  '" + shouldString.length() + "' : '" + m1.length() + "'", shouldString.length() == m1.length());
        assertThat("MutableString must equals  '" + shouldString + "' : '" + m1 + "'", equals(shouldString, m1));
        assertThat("toString() must equals  '" + shouldString + "' : '" + m1 + "'", shouldString.equals(m1.toString()));

        assertThat("Must starts with 'Tes", CharSequenceUtil.startsWith(m1, "Tes"));
        assertThat("Must not starts with 'stStr", !CharSequenceUtil.startsWith(m1, "stStr"));
        assertThat("Must not starts with 'rin", !CharSequenceUtil.startsWith(m1, "rin"));
        assertThat("Must not starts with 'ngAdd", !CharSequenceUtil.startsWith(m1, "ngAdd"));
        assertThat("Must not starts with 'stStringAdde", !CharSequenceUtil.startsWith(m1, "stStringAdde"));
        assertThat("Must starts with 'TestStringAdded", CharSequenceUtil.startsWith(m1, "TestStringAdded"));
    }

    @Test
    public void indexOf() throws PlatformAssertionError {
        assertThat("Length must equals  '" + shouldString.length() + "' : '" + m1.length() + "'", shouldString.length() == m1.length());
        assertThat("MutableString must equals  '" + shouldString + "' : '" + m1 + "'", equals(shouldString, m1));
        assertThat("toString() must equals  '" + shouldString + "' : '" + m1 + "'", shouldString.equals(m1.toString()));

        assertThat("Index of 'Tes' must be 0", CharSequenceUtil.indexOf(m1, "Tes") == 0);
        assertThat("Index of 'stStr' must be 2", CharSequenceUtil.indexOf(m1, "stStr") == 2);
        assertThat("Index of 'rin' must be 6", CharSequenceUtil.indexOf(m1, "rin") == 6);
        assertThat("Index of 'ngAdd' must be 8", CharSequenceUtil.indexOf(m1, "ngAdd") == 8);
        assertThat("Index of 'stStringAdde' must be 2", CharSequenceUtil.indexOf(m1, "stStringAdde") == 2);
        assertThat("Index of 'TestStringAdded' must be 0", CharSequenceUtil.indexOf(m1, "TestStringAdded") == 0);
    }


    private char[] PARSE_ARRAY = " 49.349817 219011721901171232  3810940 TRUE False".toCharArray();

    @Test
    public void parseDouble() throws PlatformAssertionError {
        double d = CharSequenceUtil.parseDouble(PARSE_ARRAY, 1, 9);
        assertThat("Value should be 49.349817", d == 49.349817);
    }

    @Test
    public void parseInteger() throws PlatformAssertionError {
        int i = CharSequenceUtil.parseInteger(PARSE_ARRAY, 31, 7);
        assertThat("Value should be 3810940", i == 3810940);

        boolean throwedException = false;
        try {
           CharSequenceUtil.parseInteger(PARSE_ARRAY, 11, 18);
        } catch (ArithmeticException e) {
            throwedException = true;
        }
        assertThat("must throw exception", throwedException);
    }

    @Test
    public void parseLong() throws PlatformAssertionError {
        long l = CharSequenceUtil.parseLong(PARSE_ARRAY, 11, 18);
        assertThat("Value should be 219011721901171232", l == 219011721901171232L);
    }

    @Test
    public void parseBoolean() throws PlatformAssertionError {
        boolean b = CharSequenceUtil.parseBoolean(PARSE_ARRAY, 39, 4);
        assertThat("Value should be true", b);

        b = CharSequenceUtil.parseBoolean(PARSE_ARRAY, 44, 5);
        assertThat("Value should be false", !b);
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
        Date actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 30, 27, STRING_PATTERN3.toCharArray());
        assertThat("Date should not NULL", actual != null);

        String expectedString = iso8601Format.format(expected);
        String actualString = iso8601Format.format(actual);

        assertEquals(expectedString, actualString, "Date String should be equals");

        //-------------------------------------------------------------------------------------------------------------------------------
        expected = DATE_PATTERN_2.parse("2011-04-16T07:00:00Z");
        actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 66, 20, STRING_PATTERN2.toCharArray());
        assertThat("Date should not NULL", actual != null);

        expectedString = iso8601Format.format(expected);
        actualString = iso8601Format.format(actual);

        assertEquals(expectedString, actualString, "Date String should be equals");

        //-------------------------------------------------------------------------------------------------------------------------------

        // is unparseable date, return dat must be null
        actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 66, 20, STRING_PATTERN3.toCharArray());
        assertThat("Date should not NULL", actual == null);

        //-------------------------------------------------------------------------------------------------------------------------------
        expected = DATE_PATTERN_2.parse("2011-04-17T03:39:24.4");
        actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 92, 21, STRING_PATTERN2.toCharArray());
        assertThat("Date should not NULL", actual != null);

        expectedString = iso8601Format.format(expected);
        actualString = iso8601Format.format(actual);

        assertEquals(expectedString, actualString, "Date String should be equals");

        //-------------------------------------------------------------------------------------------------------------------------------
        expected = DATE_PATTERN_3.parse("2011-04-17T03:39:24.4");
        actual = CharSequenceUtil.parseDate(locale, PARSE_DATE_ARRAY, 92, 21, STRING_PATTERN3.toCharArray());
        assertThat("Date should not NULL", actual != null);

        expectedString = iso8601Format.format(expected);
        actualString = iso8601Format.format(actual);

        assertEquals(expectedString, actualString, "Date String should be equals");

    }

    @Test
    public void getHtmlStringTest() throws PlatformAssertionError {

        char[] source = ("&lt;p&gt;Ein Spaziergang fÃ¼hrt Euch durch die Geschichte der\r\n" +
                "WollankstraÃŸe in Berlin, die auch durch die Berliner Mauer stark\r\n" +
                "geprÃ¤gt wurde &amp; Die angegebenen Koordinaten markieren den\r\n" +
                "Ausgangspunkt des Spazierganges, den S-Bahnhof WollankstraÃŸe.&lt;/p&gt;\r\n" +
                "&lt;!-- Ende Kurzbeschreibung --&gt;").toCharArray();

        String target = "<p>Ein Spaziergang fÃ¼hrt Euch durch die Geschichte der\n" +
                "WollankstraÃŸe in Berlin, die auch durch die Berliner Mauer stark\n" +
                "geprÃ¤gt wurde & Die angegebenen Koordinaten markieren den\n" +
                "Ausgangspunkt des Spazierganges, den S-Bahnhof WollankstraÃŸe.</p>\n" +
                "<!-- Ende Kurzbeschreibung -->";

        String result = CharSequenceUtil.getHtmlString(source, 0, source.length);

        assertEquals(target, result, "Should be equals");

    }


    //##################################################################
    //# Helper
    //##################################################################

    public static boolean equals(CharSequence s1, CharSequence s2) {
        if (s1 == null || s2 == null) return false;
        if (s1.length() != s2.length()) return false;
        int n = s1.length();
        while (n-- > 0) {
            if (s1.charAt(n) != s2.charAt(n)) return false;
        }
        return true;
    }


}
