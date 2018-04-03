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

/**
 * Created by Longri on 27.10.2017.
 */
public class CharSequenceUtil {

    public static boolean contains(CharSequence sorce, CharSequence target) {
        return indexOf(sorce, 0, sorce.length(),
                target, 0, target.length(), 0) >= 0;
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

    public static double parseDouble(char[] data, int offset, int length) {
        if (data == null || length == 0)
            throw new NumberFormatException("Number cannot be null/empty.");

        boolean isNegative = false;
        boolean hasDecimal = false;

        // check for operators
        switch (data[offset]) {
            case '+':
                offset++;
                break;

            case '-':
                offset++;
                isNegative = true;
                break;

            case '.':
                offset++;
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
        if (data == null || length == 0)
            throw new NumberFormatException("Number cannot be null/empty.");

        boolean isNegative = false;

        // check for operators
        switch (data[offset]) {
            case '+':
                offset++;
                break;

            case '-':
                offset++;
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
                break;

            case '-':
                offset++;
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

}
