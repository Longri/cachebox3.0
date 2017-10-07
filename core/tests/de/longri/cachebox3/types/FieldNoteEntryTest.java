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
package de.longri.cachebox3.types;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 05.10.2017
 */
public class FieldNoteEntryTest {

    static {
        TestUtils.initialGdx();

        //initial a empty FieldNote DB
        try {


            FileHandle resourcesFolder= Gdx.files.absolute("testsResources");
            if(!resourcesFolder.exists()){
                //try set /core path
                resourcesFolder= Gdx.files.absolute("core/testsResources");
            }

            FileHandle fieldNotesFileHandle =   resourcesFolder.child("fieldNotes.db3");

            if (fieldNotesFileHandle.exists()) {
                fieldNotesFileHandle.delete();
            }

            Database.FieldNotes = new Database(Database.DatabaseType.FieldNotes);
            Database.FieldNotes.startUp(fieldNotesFileHandle);
        } catch (SQLiteGdxException e) {
            assertThat("Can't open fieldNotes.db3", false);
        }
    }

    @Test
    void fieldNoteTest() {
        //Store FieldNote into DB

        FieldNoteEntry fne = new FieldNoteEntry(LogTypes.found);

        // Set FieldNote values
        fne.CacheId = 100;
        fne.gcCode = "GCCODE";
        fne.CacheName = "CacheName";
        fne.timestamp = new Date();
        fne.foundNumber = 12;
        fne.comment = "Comment";
        fne.cacheType = CacheTypes.Traditional;
        fne.CacheUrl = "URL";
        fne.uploaded = false;
        fne.gc_Vote = 7;
        fne.isTbFieldNote = false;
        fne.TbName = "TB Name";
        fne.TbIconUrl = "TB icon url";
        fne.TravelBugCode = "TravelBugCode";
        fne.TrackingNumber = "TrackingNumber";
        fne.isDirectLog = false;
        fne.fillType();

        try {
            fne.writeToDatabase();
        } catch (Exception e) {
            assertThat("Can't write FieldNote to DB", false);
        }


        // read FieldNote from DB
        FieldNoteList fieldNoteEntries = new FieldNoteList();
        fieldNoteEntries.loadFieldNotes("", FieldNoteList.LoadingType.LOAD_NEW_LAST_LENGTH);
        assertThat("FieldNoteList size must be 1", fieldNoteEntries.size == 1);
        FieldNoteEntry fne2 = fieldNoteEntries.get(0);
        assertThat("FieldNotes must be equals", fne2.equals(fne));

        //read non uploaded FieldNote from DB
        FieldNoteList lFieldNotes = new FieldNoteList();
        lFieldNotes.loadFieldNotes("(Uploaded=0 or Uploaded is null)", FieldNoteList.LoadingType.LOAD_ALL);

        assertThat("FieldNoteList size must be 1", fieldNoteEntries.size == 1);
        FieldNoteEntry fne3 = fieldNoteEntries.get(0);
        assertThat("FieldNotes must be equals", fne3.equals(fne));

    }


}
