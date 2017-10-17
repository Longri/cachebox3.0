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
package de.longri.cachebox3.gui.utils;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 17.10.2017.
 */
class CharArrayTest {
    @Test
    void length() {

        String testString = "Test String <?>";
        CharArray charArray = new CharArray(testString);

        assertThat("length must equals", testString.length() == charArray.length());

    }

    @Test
    void charAt() {
        String testString = "Test String <?>";
        CharArray charArray = new CharArray(testString);

        for (int i = 0, n = testString.length(); i < n; i++) {
            assertThat("Char at: " + Integer.toString(i) + " must equals",
                    testString.charAt(i) == charArray.charAt(i));
        }

    }

    @Test
    void subSequence() {
        String testString = "Test String <?>";
        CharArray charArray = new CharArray(testString);

        CharSequence subString = testString.subSequence(3, 6);
        CharSequence subCharArray = charArray.subSequence(3, 6);

        for (int i = 0, n = subString.length(); i < n; i++) {
            assertThat("Char at: " + Integer.toString(i) + " must equals",
                    subString.charAt(i) == subCharArray.charAt(i));
        }

    }

    @Test
    void chars() {

        String testString = "Test String <?>";
        CharArray charArray = new CharArray(testString);

        int[] stringInt = testString.chars().toArray();
        int[] charArrayInt = charArray.chars().toArray();

        for (int i = 0, n = stringInt.length; i < n; i++) {
            assertThat("Int at: " + Integer.toString(i) + " must equals",
                    stringInt[i] == charArrayInt[i]);
        }

    }

    @Test
    void codePoints() {
        String testString = "Test String <?>";
        CharArray charArray = new CharArray(testString);

        int[] stringInt = testString.codePoints().toArray();
        int[] charArrayInt = charArray.codePoints().toArray();

        for (int i = 0, n = stringInt.length; i < n; i++) {
            assertThat("Int at: " + Integer.toString(i) + " must equals",
                    stringInt[i] == charArrayInt[i]);
        }
    }
    
}