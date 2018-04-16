/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Longri on 27.10.2017.
 */
public class CharSequenceUtil {

    private final static Logger log = LoggerFactory.getLogger(CharSequenceUtil.class);

    public static boolean contains(CharSequence source, CharSequence target) {
        return indexOf(source, 0, source.length(),
                target, 0, target.length(), 0) >= 0;
    }

    public static boolean contains(char[] source, int sourceOffset, int sourceCount,
                                   char[] target, int targetOffset, int targetCount) {
        return indexOf(source, sourceOffset, sourceCount,
                target, targetOffset, targetCount, 0) >= 0;
    }

    public static boolean startsWith(CharSequence sorce, CharSequence target) {
        return indexOf(sorce, 0, sorce.length(),
                target, 0, target.length(), 0) == 0;
    }

    public static int indexOf(CharSequence sorce, CharSequence target) {
        return indexOf(sorce, 0, sorce.length(),
                target, 0, target.length(), 0);
    }

    public static int indexOf(CharSequence source, int sourceOffset, int sourceCount,
                              CharSequence target, int targetOffset, int targetCount,
                              int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target.charAt(targetOffset);
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source.charAt(i) != first) {
                while (++i <= max && source.charAt(i) != first) ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source.charAt(j)
                        == target.charAt(k); j++, k++)
                    ;

                if (j == end) {
                    /* Found whole storage. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    public static int indexOf(char[] source, int sourceOffset, int sourceCount,
                              char[] target, int targetOffset, int targetCount,
                              int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first) ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j]
                        == target[k]; j++, k++)
                    ;

                if (j == end) {
                    /* Found whole storage. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    public static boolean equals(CharSequence s1, CharSequence s2) {
        if (s1.length() != s2.length()) return false;
        int n = s1.length();
        while (n-- > 0) {
            if (s1.charAt(n) != s2.charAt(n)) return false;
        }
        return true;
    }

    public static boolean equals(char[] s1, char[] s2) {
        if (s1 == s2) return true;
        if (s1.length != s2.length) return false;
        for (int i = 0; i < s1.length; i++)
            if (s1[i] != s2[i]) return false;
        return true;
    }

    public static boolean equals(char[] s1, int offset1, int length1, char[] s2, int offset2, int length2) {
        if (length1 != length2) return false;
        int j = offset2;
        for (int i = offset1, n = offset1 + length1; i < n; i++)
            if (s1[i] != s2[j++]) return false;
        return true;
    }

    public static double parseDouble(char[] data, int offset, int length) {
        if (data == null || length == 0)
            throw new NumberFormatException("Number cannot be null/empty.");

        boolean isNegative = false;
        boolean hasDecimal = false;

        // check for operators
        switch (data[offset]) {
            case '+':
                offset++;
                length--;
                break;

            case '-':
                offset++;
                length--;
                isNegative = true;
                break;

            case '.':
                offset++;
                length--;
                hasDecimal = true;
                break;
        }

        double ip = 0.0, dp = 0.0;
        double fd = 1.0;

        for (int i = offset, n = length + offset; i < n; i++) {
            int digit = data[i] - '0';

            if (isNumeric(data, i) && digit != '0') {
                if (!hasDecimal) {
                    ip *= 10;
                    ip += digit;
                } else {
                    dp *= 10;
                    dp += digit;
                    fd *= 10;
                }
            } else if (data[i] == '.') {
                if (hasDecimal)
                    throw new NumberFormatException("Number is malformed: " + new String(data, offset, length));
                hasDecimal = true;
            } else {
                throw new NumberFormatException("Number is malformed: " + new String(data, offset, length));
            }
        }
        dp = dp / fd;
        return isNegative ? (ip + dp) * -1 : ip + dp;
    }

    public static int parseInteger(char[] data, int offset, int length) {
        if (data == null || length <= 0)
            throw new NumberFormatException("Number cannot be null/empty.");

        boolean isNegative = false;

        // check for operators
        switch (data[offset]) {
            case '+':
                offset++;
                length--;
                break;

            case '-':
                offset++;
                length--;
                isNegative = true;
                break;
        }


        int ip = 0, lastIp = 0;
        for (int i = offset, n = length + offset; i < n; i++) {
            if (!isNumeric(data, i))
                throw new NumberFormatException("Number is malformed: " + new String(data, offset, length));
            int digit = data[i] - '0';
            ip *= 10;
            ip += digit;

            if (ip < lastIp)
                throw new ArithmeticException("Number is overflow: " + new String(data, offset, length));

            lastIp = ip;
        }
        return isNegative ? ip * -1 : ip;
    }

    public static long parseLong(char[] data, int offset, int length) {
        if (data == null || length == 0)
            throw new NumberFormatException("Number cannot be null/empty.");

        boolean isNegative = false;

        // check for operators
        switch (data[offset]) {
            case '+':
                offset++;
                length--;
                break;

            case '-':
                offset++;
                length--;
                isNegative = true;
                break;
        }


        long ip = 0, lastIp = 0;
        for (int i = offset, n = length + offset; i < n; i++) {
            if (!isNumeric(data, i))
                throw new NumberFormatException("Number is malformed: " + new String(data, offset, length));
            int digit = data[i] - '0';
            ip *= 10;
            ip += digit;

            if (ip < lastIp)
                throw new ArithmeticException("Number is overflow: " + new String(data, offset, length));

            lastIp = ip;
        }
        return isNegative ? ip * -1 : ip;
    }

    public static boolean parseBoolean(char[] data, int offset, int length) {
        if (data == null || length == 0)
            throw new NumberFormatException("Number cannot be null/empty.");

        // TRUE.length=4 / FALSE.length=5
        if (length == 4) {
            if (data[offset] == 'T' || data[offset] == 't') {
                return true;
            }
        } else if (length == 5) {
            if (data[offset] == 'F' || data[offset] == 'f') {
                return false;
            }
        }
        throw new NumberFormatException("Number is malformed: " + new String(data, offset, length));
    }

    private static boolean isNumeric(char[] data, int offset) {
        return '0' <= data[offset] && data[offset] <= '9';
    }


    /**
     * see https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     * <p>
     * Letter	Date or Time Component	Presentation	Examples
     * G		Era designator				Text			AD
     * y		Year						Year		1996; 96
     * Y		Week year					Year		2009; 09
     * M		Month in year				Month	July; Jul; 07
     * w		Week in year				Number		27
     * W		Week in month				Number		2
     * D		Day in year				Number	189
     * d		Day in month				Number	10
     * F		Day of week in month			Number	2
     * E		Day name in week			Text		Tuesday; Tue
     * u		Day number of week 			Number	1
     * a		Am/pm marker				Text		PM
     * H		Hour in day (0-23)			Number	0
     * k		Hour in day (1-24)			Number	24
     * K		Hour in am/pm (0-11)		Number	0
     * h		Hour in am/pm (1-12)		Number	12
     * m		Minute in hour				Number	30
     * s		Second in minute			Number	55
     * S		Millisecond					Number	978
     * z		Time zone	General time zone	Pacific Standard Time; PST; GMT-08:00
     * Z		Time zone	RFC 822 time zone	-0800
     * X		Time zone	ISO 8601 time zone	-08; -0800; -08:00
     *
     * @param locale
     * @param data
     * @param offset
     * @param length
     * @param patterns
     * @return
     */
    public static Date parseDate(Locale locale, char[] data, int offset, int length, char[]... patterns) {

        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), locale);

        int year = 0, yearEnd = 0, yearStart = -1;
        int month = 0, monthEnd = 0, monthStart = -1;
        int day = 0, dayEnd = 0, dayStart = -1;
        int hour = 0, hourEnd = 0, hourStart = -1;
        int minute = 0, minuteEnd = 0, minuteStart = -1;
        int second = 0, secondEnd = 0, secondStart = -1;
        int millisecond = 0, startMillisecond = -1;

        boolean dayInYear = false;
        boolean amPmMarker = false;
        boolean pm = false;
        boolean hourZeroIndex = false;
        boolean unescape = false;
        int unescapeIndex = 0;
        int parseLength = 0;

        for (char[] pattern : patterns) {
            yearStart = -1;
            monthStart = -1;
            dayStart = -1;
            hourStart = -1;
            minuteStart = -1;
            secondStart = -1;
            startMillisecond = -1;

            dayInYear = false;
            amPmMarker = false;
            pm = false;
            hourZeroIndex = false;
            unescape = false;
            unescapeIndex = 0;
            parseLength = 0;

            for (int i = 0; i < pattern.length; i++) {

                if (unescape) {
                    if (pattern[i] == '\'') {
                        unescape = false;
                        unescapeIndex++;
                    }
                    continue;
                }

                switch (pattern[i]) {
                    case 'y':
                    case 'Y':
                        if (yearStart == -1) yearStart = i - unescapeIndex;
                        else yearEnd = i - unescapeIndex + 1;
                        break;
                    case 'M':
                        if (monthStart == -1) monthStart = i - unescapeIndex;
                        else monthEnd = i - unescapeIndex + 1;
                        break;
                    case 'D':
                        dayInYear = true;
                    case 'd':
                        if (dayStart == -1) dayStart = i - unescapeIndex;
                        else dayEnd = i - unescapeIndex + 1;
                        break;
                    case 'a':
                        if (!amPmMarker) amPmMarker = true;
                        else if (true/*TODO*/) pm = true;
                        break;
                    case 'H':
                    case 'K':
                        hourZeroIndex = true;
                    case 'k':
                    case 'h':
                        if (hourStart == -1) hourStart = i - unescapeIndex;
                        else hourEnd = i - unescapeIndex + 1;
                        break;
                    case 'm':
                        if (minuteStart == -1) minuteStart = i - unescapeIndex;
                        else minuteEnd = i - unescapeIndex + 1;
                        break;
                    case 's':
                        if (secondStart == -1) secondStart = i - unescapeIndex;
                        else secondEnd = i - unescapeIndex + 1;
                        break;
                    case '\'':
                        unescape = true;
                        unescapeIndex++;
                        break;
                    case 'S':
                        startMillisecond = i - unescapeIndex;
                        break;
                }
            }
            try {
                year = parseInteger(data, offset + yearStart, yearEnd - yearStart);
                month = parseInteger(data, offset + monthStart, monthEnd - monthStart);
                day = parseInteger(data, offset + dayStart, dayEnd - dayStart);
                hour = parseInteger(data, offset + hourStart, hourEnd - hourStart);
                minute = parseInteger(data, offset + minuteStart, minuteEnd - minuteStart);
                second = parseInteger(data, offset + secondStart, secondEnd - secondStart);

                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month - 1);
                if (dayInYear) {
                    cal.set(Calendar.DAY_OF_YEAR, day);
                } else {
                    cal.set(Calendar.DAY_OF_MONTH, day);
                }
                if (amPmMarker) {
                    if (!hourZeroIndex) hour++;
                    cal.set(Calendar.AM, hour);
                    cal.set(Calendar.PM, hour);
                } else {
                    if (!hourZeroIndex) hour++;
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                }
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, second);

                if (startMillisecond >= 0) {
                    // search end of millisecond
                    int i = startMillisecond + offset;
                    while (isNumeric(data, i)) {
                        i++;
                    }
                    parseLength = i - (offset + startMillisecond);
                    if (parseLength <= 0) continue; // unparsable milliseconds
                    millisecond = parseInteger(data, offset + startMillisecond, parseLength);
                    cal.set(Calendar.MILLISECOND, millisecond);
                } else {
                    cal.set(Calendar.MILLISECOND, 0);
                }

                return cal.getTime();
            } catch (Exception e) {
                log.warn("Can't parse Date: {} with DatePattern {}", new String(data, offset, length), new String(pattern));
            }

        }
        log.error("Can't parse Date: {} with given DatePattern");

        return null;
    }


    private final static char[] AMP = "&amp;".toCharArray();
    private final static char[] LT = "&lt;".toCharArray();
    private final static char[] GT = "&gt;".toCharArray();
    private final static char[] LINE_BREAK = "\r\n".toCharArray();

    public static String getHtmlString(char[] data, int offset, int length) {
        char[] stringData = new char[length];
        System.arraycopy(data, offset, stringData, 0, length);

        int newLength = length;
        int startIndex = 0;
        while (true) {
            startIndex = replace(stringData, 0, newLength, AMP, '&', startIndex);
            if (startIndex >= 0) {
                newLength -= AMP.length - 1;
            } else {
                break;
            }
        }

        startIndex = 0;
        while (true) {
            startIndex = replace(stringData, 0, newLength, LT, '<', startIndex);
            if (startIndex >= 0) {
                newLength -= LT.length - 1;
            } else {
                break;
            }
        }

        startIndex = 0;
        while (true) {
            startIndex = replace(stringData, 0, newLength, GT, '>', startIndex);
            if (startIndex >= 0) {
                newLength -= GT.length - 1;
            } else {
                break;
            }
        }

        startIndex = 0;
        while (true) {
            startIndex = replace(stringData, 0, newLength, LINE_BREAK, '\n', startIndex);
            if (startIndex >= 0) {
                newLength -= LINE_BREAK.length - 1;
            } else {
                break;
            }
        }
        return new String(stringData, 0, newLength);
    }

    public static int replace(char[] data, int offset, int length, char[] searchChar, char replChar, int fromIndex) {
        int indexOf = indexOf(data, offset, length, searchChar, 0, searchChar.length, fromIndex);
        if (indexOf >= 0) {
            //set replace char to array indexOf
            data[indexOf] = replChar;

            //copy rest of chars
            System.arraycopy(data, indexOf + searchChar.length, data, indexOf + 1, length - (indexOf + searchChar.length));

            //return index of replacement
            return indexOf + 1;
        }
        return -1;
    }

}
