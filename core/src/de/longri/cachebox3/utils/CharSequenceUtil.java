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

}
