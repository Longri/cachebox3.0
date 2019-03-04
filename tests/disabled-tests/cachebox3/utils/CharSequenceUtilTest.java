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

import com.badlogic.gdx.utils.CharArray;
import de.longri.cachebox3.translation.word.MutableString;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Longri on 27.10.2017.
 */
public class CharSequenceUtilTest {

    static CharArray buffer = new CharArray();

    static String t1 = "Test";
    static String t2 = "String";
    static String t3 = "Added";
    static MutableString m1 = new MutableString(buffer, t1);
    static MutableString m3 = new MutableString(buffer, t3);
    static MutableString m2 = new MutableString(buffer, t2);
    static String shouldString = t1 + t2 + t3;

    static {
        m1.add(m2).add(m3);
    }


    @Test
    void contains() {
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
    void startsWith() {
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
    void indexOf() {
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


    char[] PARSE_ARRAY = " 49.349817 219011721901171232  3810940 TRUE False".toCharArray();

    @Test
    void parseDouble() {
        double d = CharSequenceUtil.parseDouble(PARSE_ARRAY, 1, 9);
        assertThat("Value should be 49.349817", d == 49.349817);
    }

    @Test
    void parseInteger() {
        int i = CharSequenceUtil.parseInteger(PARSE_ARRAY, 31, 7);
        assertThat("Value should be 3810940", i == 3810940);

        boolean throwedException = false;
        try {
            int j = CharSequenceUtil.parseInteger(PARSE_ARRAY, 11, 18);
        } catch (ArithmeticException e) {
            throwedException = true;
        }
        assertThat("must throw exception", throwedException);
    }

    @Test
    void parseLong() {
        long l = CharSequenceUtil.parseLong(PARSE_ARRAY, 11, 18);
        assertThat("Value should be 219011721901171232", l == 219011721901171232L);
    }

    @Test
    void parseBoolean() {
        boolean b = CharSequenceUtil.parseBoolean(PARSE_ARRAY, 39, 4);
        assertThat("Value should be true", b == true);

        b = CharSequenceUtil.parseBoolean(PARSE_ARRAY, 44, 5);
        assertThat("Value should be false", b == false);
    }


    char[] PARSE_DATE_ARRAY = " 49.349817 219011721901171232 2011-07-05T12:54:02.308107Z 3810940 2011-04-16T07:00:00Z TRUE 2011-04-17T03:39:24.4 False".toCharArray();
    private final Locale locale = Locale.getDefault();
    private final String STRING_PATTERN1 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final String STRING_PATTERN2 = "yyyy-MM-dd'T'HH:mm:ss";
    private final String STRING_PATTERN3 = "yyyy-MM-dd'T'HH:mm:ss.S";

    private final SimpleDateFormat DATE_PATTERN_1 = new SimpleDateFormat(STRING_PATTERN1, locale);
    private final SimpleDateFormat DATE_PATTERN_2 = new SimpleDateFormat(STRING_PATTERN2, locale);
    private final SimpleDateFormat DATE_PATTERN_3 = new SimpleDateFormat(STRING_PATTERN3, locale);

    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Test
    void parseDate() throws ParseException {
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
    public void getHtmlStringTest() {

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