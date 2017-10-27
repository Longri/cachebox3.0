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

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 27.10.2017.
 */
class CharSequenceUtilTest {

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

    //##################################################################
    //# Helper
    //##################################################################

    private boolean equals(CharSequence s1, CharSequence s2) {
        if (s1.length() != s2.length()) return false;
        int n = s1.length();
        while (n-- > 0) {
            if (s1.charAt(n) != s2.charAt(n)) return false;
        }
        return true;
    }


}