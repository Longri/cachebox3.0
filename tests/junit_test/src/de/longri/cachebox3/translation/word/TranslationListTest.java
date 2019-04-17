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

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 27.10.2017.
 */
class TranslationListTest {

    @BeforeAll
    static void setUp() throws SQLiteGdxException {
        TestUtils.initialGdx();
    }


    @Test
    void load() {
        FileHandle fileHandle = TestUtils.getResourceFileHandle("testsResources/strings.ini", true);

        TranslationList translationList = new TranslationList();
        translationList.load(fileHandle);

        assertThat("TranslationList.length() must be  '" + 1142 + "' : '" + translationList.length() + "'", 1142 == translationList.length());

        CharSequence sequence = translationList.get("ErrDbFNStartup");
        String translation = "Error during Fieldnote Database Startup!";
        assertThat("Translation must '" + translation + "' but was: '" + sequence + "'", CharSequenceUtil.equals(sequence, translation));

        int id = "solverDescCrosstotal".hashCode();
        translation = "Sum of all individual Digits in the Number. \nExample: CS(123456)=21";
        sequence = translationList.get(id);
        assertThat("Translation must '" + translation + "' but was: '" + sequence + "'", CharSequenceUtil.equals(sequence, translation));


    }
}