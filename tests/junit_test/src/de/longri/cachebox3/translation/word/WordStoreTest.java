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

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 26.10.2017.
 */
class WordStoreTest {

    @BeforeAll
    static void setUp() throws SQLiteGdxException {
        TestUtils.initialGdx();
    }


    @Test
    void add() {

        String t1 = "Test storage with double used words like Test or storage";
        String t2 = "Test storage that contains non double words";
        String t3 = "Test storage that contains non double words!";

        WordStore store = new WordStore();

        StringSequence m1 = store.add(t1);
        assertThat("MutableString must charSequenceEquals  '" + t1 + "' : '" + m1 + "'", CharSequenceUtil.equals(t1, m1));
        assertThat("toString() must charSequenceEquals  '" + t1 + "' : '" + m1 + "'", t1.equals(m1.toString()));


        StringSequence m2 = store.add(t2);
        assertThat("MutableString must charSequenceEquals  '" + t2 + "' : '" + m2 + "'", CharSequenceUtil.equals(t2, m2));
        assertThat("toString() must charSequenceEquals  '" + t2 + "' : '" + m2 + "'", t2.equals(m2.toString()));

        StringSequence m3 = store.add(t3);
        assertThat("MutableString must charSequenceEquals  '" + t3 + "' : '" + m3 + "'", CharSequenceUtil.equals(t3, m3));
        assertThat("toString() must charSequenceEquals  '" + t3 + "' : '" + m3 + "'", t3.equals(m3.toString()));

    }


    @Test
    void addTranslationFile() throws FileNotFoundException {

        WordStore.count = 0;
        FileHandle fileHandle = TestUtils.getResourceFileHandle("testsResources/strings.ini", true);
        WordStore store = new WordStore();

        String fileString = fileHandle.readString();
        String[] lines = fileString.split("\r\n");

        String[] clearedLines = new String[lines.length];

        for (int i = 0, n = lines.length; i < n; i++) {
            String line = lines[i];
            if (line == null || line.isEmpty()) continue;
            int pos = line.indexOf("=");
            if (pos < 0) continue;
            clearedLines[i] = line.substring(pos + 1);
        }

        // store lines
        StringSequence[] sequences = new StringSequence[lines.length];
        for (int i = 0, n = lines.length; i < n; i++) {
            String line = clearedLines[i];
            if (line == null || line.isEmpty()) continue;
            sequences[i] = store.add(line);
        }

        // test
        for (int i = 0, n = lines.length; i < n; i++) {
            String line = clearedLines[i];
            StringSequence sequence = sequences[i];
            if (line == null || line.isEmpty()) continue;
            assertThat("MutableString must charSequenceEquals  '" + line + "' : '" + sequence + "'", CharSequenceUtil.equals(line, sequence));
            assertThat("toString() must charSequenceEquals  '" + line + "' : '" + sequence + "'", line.equals(sequence.toString()));
        }
    }
}