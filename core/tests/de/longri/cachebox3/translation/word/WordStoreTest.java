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
package de.longri.cachebox3.translation.word;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 26.10.2017.
 */
class WordStoreTest {
    @Test
    void add() {

        String t1 = "Test string with double used words like Test or string";
        String t2 = "Test string that contains non double words";
        WordStore store = new WordStore();

        StringSequence m1 = store.add(t1);
        assertThat("MutableString must equals  '" + t1 + "' : '" + m1 + "'", equals(t1, m1));
        assertThat("toString() must equals  '" + t1 + "' : '" + m1 + "'", t1.equals(m1.toString()));

        assertThat("Store length must be 33, but was:" + store.storage.size, store.storage.size == 33);

        StringSequence m2 = store.add(t2);
        assertThat("MutableString must equals  '" + t2 + "' : '" + m2 + "'", equals(t2, m2));
        assertThat("toString() must equals  '" + t2 + "' : '" + m2 + "'", t2.equals(m2.toString()));
        assertThat("Store length must be 48, but was:" + store.storage.size, store.storage.size == 48);

    }


    @Test
    void addTranslationFile() {

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