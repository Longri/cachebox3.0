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

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 17.10.2017.
 */
class CharSequenceArrayTest {
    @Test
    void length() {

        String testString = "Test String <?>";
        CharSequenceArray charSequenceArray = new CharSequenceArray(testString);

        assertThat("length must charSequenceEquals", testString.length() == charSequenceArray.length());

    }

    @Test
    void charAt() {
        String testString = "Test String <?>";
        CharSequenceArray charSequenceArray = new CharSequenceArray(testString);

        for (int i = 0, n = testString.length(); i < n; i++) {
            assertThat("Char at: " + i + " must charSequenceEquals",
                    testString.charAt(i) == charSequenceArray.charAt(i));
        }

    }

    @Test
    void subSequence() {
        String testString = "Test String <?>";
        CharSequenceArray charSequenceArray = new CharSequenceArray(testString);

        CharSequence subString = testString.subSequence(3, 6);
        CharSequence subCharArray = charSequenceArray.subSequence(3, 6);

        for (int i = 0, n = subString.length(); i < n; i++) {
            assertThat("Char at: " + i + " must charSequenceEquals",
                    subString.charAt(i) == subCharArray.charAt(i));
        }

    }

    @Test
    void chars() {

        String testString = "Test String <?>";
        CharSequenceArray charSequenceArray = new CharSequenceArray(testString);

        int[] stringInt = testString.chars().toArray();
        int[] charArrayInt = charSequenceArray.chars().toArray();

        for (int i = 0, n = stringInt.length; i < n; i++) {
            assertThat("Int at: " + i + " must charSequenceEquals",
                    stringInt[i] == charArrayInt[i]);
        }

    }

    @Test
    void codePoints() {
        String testString = "Test String <?>";
        CharSequenceArray charSequenceArray = new CharSequenceArray(testString);

        int[] stringInt = testString.codePoints().toArray();
        int[] charArrayInt = charSequenceArray.codePoints().toArray();

        for (int i = 0, n = stringInt.length; i < n; i++) {
            assertThat("Int at: " + i + " must charSequenceEquals",
                    stringInt[i] == charArrayInt[i]);
        }
    }


    @Test
    void hash() {
        String testString = "Test String <?>";
        CharSequenceArray charSequenceArray = new CharSequenceArray(testString);
        assertThat("must charSequenceEquals", charSequenceArray.hashCode() == testString.hashCode());
    }

    @Test
    void toStringTest() {
        String testString = "Test String <?>";
        CharSequenceArray charSequenceArray = new CharSequenceArray(testString);
        assertThat("must charSequenceEquals", charSequenceArray.toString().equals(testString));
    }


    @Test
    void equals() {
        String testString = "Test String <?>";
        CharSequenceArray charSequenceArray = new CharSequenceArray(testString);
        CharSequenceArray charSequenceArray2 = new CharSequenceArray(testString);

        assertThat("must charSequenceEquals", charSequenceArray.equals(testString));
        assertThat("must charSequenceEquals", charSequenceArray2.equals(charSequenceArray));
        assertThat("must charSequenceEquals", testString.equals(charSequenceArray.toString()));

        charSequenceArray = new CharSequenceArray("test");
        charSequenceArray2 = new CharSequenceArray("tested");

        assertThat("must not charSequenceEquals", !charSequenceArray.equals(testString));
        assertThat("must not charSequenceEquals", !charSequenceArray2.equals(charSequenceArray));
        assertThat("must not charSequenceEquals", !testString.equals(charSequenceArray.toString()));


    }
}