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
package de.longri.cachebox3.translation;

import com.badlogic.gdx.utils.CharArray;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 26.10.2017.
 */
class MutableStringTest {

    @Test
    void constructor() {
        CharArray buffer = new CharArray();
        CharArray buffer2 = new CharArray();
        String t1 = "TestString";

        MutableString mutable1 = new MutableString(buffer, t1);
        MutableString mutable2 = new MutableString(buffer2, mutable1);

        assertThat("MutableString must equals", equals(t1, mutable1));
        assertThat("MutableString must equals", equals(t1, mutable2));
        assertThat("MutableString must equals", equals(mutable1, mutable2));

    }

    @Test
    void length() {
        CharArray buffer = new CharArray();
        String t1 = "TestString";
        MutableString mutable1 = new MutableString(buffer, t1);
        MutableString mutable2 = new MutableString(buffer, mutable1);

        assertThat("Length must: " + t1.length() + "but was: " + mutable1.length(), t1.length() == mutable1.length());
        assertThat("Length must: " + t1.length() + "but was: " + mutable2.length(), t1.length() == mutable2.length());
    }

    @Test
    void charAt() {
        CharArray buffer = new CharArray();
        String t1 = "TestString";
        MutableString mutable1 = new MutableString(buffer, t1);
        MutableString mutable2 = new MutableString(buffer, mutable1);

        assertThat("MutableString must equals", equals(t1, mutable1));
        assertThat("MutableString must equals", equals(t1, mutable2));
        assertThat("MutableString must equals", equals(mutable1, mutable2));
    }

    @Test
    void subSequence() {
        CharArray buffer = new CharArray();
        String t1 = "TestString";
        CharSequence sub = t1.subSequence(4, 10);
        MutableString mutable1 = new MutableString(buffer, t1);
        MutableString mutable2 = new MutableString(buffer, mutable1);

        MutableString sub1 = mutable1.subSequence(4, 10);
        MutableString sub2 = mutable2.subSequence(4, 10);

        assertThat("MutableSubString must equals", equals(sub, sub1));
        assertThat("MutableSubString must equals", equals(sub, sub2));
    }


    @Test
    void toStringTest() {
        CharArray buffer = new CharArray();
        String t1 = "TestString";
        MutableString mutable1 = new MutableString(buffer, t1);
        MutableString mutable2 = new MutableString(buffer, mutable1);

        assertThat("MutableString must equals", t1.equals(mutable1.toString()));
        assertThat("MutableString must equals", t1.equals(mutable2.toString()));
        assertThat("MutableString must equals", mutable1.toString().equals(mutable2.toString()));

        assertThat("MutableString must equals", mutable1.toString().equals(t1));
        assertThat("MutableString must equals", mutable2.toString().equals(t1));
        assertThat("MutableString must equals", mutable1.toString().equals(mutable2.toString()));
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