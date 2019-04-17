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

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.utils.CharSequenceUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 28.10.2017.
 */
class SequenceTranslationHandlerTest {


    static SequenceTranslationHandler translationHandler;


    @BeforeAll
    public static void loadTranslation() throws IOException {
        TestUtils.initialGdx();
        FileHandle workPath = TestUtils.getResourceFileHandle("testsResources/lang", true);
        translationHandler = new SequenceTranslationHandler(workPath, "en-GB");
        translationHandler.loadTranslation("de");
        assertThat("TranslationCount must be 1142", translationHandler.getCount() == 1142);

    }

    @Test
    void getTranslation() {

        CompoundCharSequence trans = translationHandler.getTranslation("abort");
        String txt = "Abbrechen";
        assertThat("Translation must '" + txt + "' but was: '" + trans + "'", CharSequenceUtil.equals(txt, trans));

        trans = translationHandler.getTranslation("Description");
        txt = "Description";//default
        assertThat("Translation must '" + txt + "' but was: '" + trans + "'", CharSequenceUtil.equals(txt, trans));

        //hint=Hinweis
        trans = translationHandler.getTranslation("hint", "-Test");
        txt = "Hinweis-Test";
        assertThat("Translation must '" + txt + "' but was: '" + trans + "'", CharSequenceUtil.equals(txt, trans));

        //draftNoteNoDelete= ????
        trans = translationHandler.getTranslation("draftNoteNoDelete");
        txt = "$ID:draftNoteNoDelete";
        assertThat("Translation must '" + txt + "' but was: '" + trans + "'", CharSequenceUtil.equals(txt, trans));


        trans = translationHandler.getTranslation("cacheOtherDb", "Name");
        trans.add("\n", translationHandler.getTranslation("draftNoDelete"));
        txt = "Der Cache [Name] ist nicht in der aktuellen DB.\nThis Draft can not be deleted";
        assertThat("Translation must '" + txt + "' but was: '" + trans + "'", CharSequenceUtil.equals(txt, trans));


        //solverErrParamType=Fehler: Funktion {1}/Parameter {2} ({3}) ist keine gültige {4}: [{5}]
        trans = translationHandler.getTranslation("solverErrParamType", "first", "second", "third", "fourth", "fifth");
        txt = "Fehler: Funktion first/Parameter second (third) ist keine gültige fourth: [fifth]";
        assertThat("Translation must '" + txt + "' but was: '" + trans + "'", CharSequenceUtil.equals(txt, trans));

        //solverErrParamType=Fehler: Funktion {1}/Parameter {2} ({3}) ist keine gültige {4}: [{5}]
        trans = translationHandler.getTranslation("solverErrParamType", "first", "second", "third");
        txt = "Fehler: Funktion first/Parameter second (third) ist keine gültige : []";
        assertThat("Translation must '" + txt + "' but was: '" + trans + "'", CharSequenceUtil.equals(txt, trans));

        //solverErrParamType=Fehler: Funktion {1}/Parameter {2} ({3}) ist keine gültige {4}: [{5}]
        trans = translationHandler.getTranslation("solverErrParamType", "first", "second", "third", "fourth", "fifth", "sixth", "seventh");
        txt = "Fehler: Funktion first/Parameter second (third) ist keine gültige fourth: [fifth]sixthseventh";
        assertThat("Translation must '" + txt + "' but was: '" + trans + "'", CharSequenceUtil.equals(txt, trans));

    }

}