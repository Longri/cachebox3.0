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
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.gdx.sqlite.SQLiteGdxException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 05.10.2017
 */
public class DraftEntryTest {

    static {
        TestUtils.initialGdx();

        //initial a empty Draft DB
        try {


            FileHandle resourcesFolder = Gdx.files.absolute("testsResources");
            if (!resourcesFolder.exists()) {
                //try set /core path
                resourcesFolder = Gdx.files.absolute("tests/testsResources");
            }

            FileHandle fieldNotesFileHandle = resourcesFolder.child("fieldNotes.db3");

            if (fieldNotesFileHandle.exists()) {
                fieldNotesFileHandle.delete();
            }

            Database.Drafts = new Database(Database.DatabaseType.Drafts);
            Database.Drafts.startUp(fieldNotesFileHandle);
        } catch (SQLiteGdxException e) {
            assertThat("Can't open fieldNotes.db3", false);
        }
    }

//    @Test
//    void fieldNoteTest() {
//        //Store Draft into DB
//
//        DraftEntry fne = new DraftEntry(LogTypes.found);
//
//        // Set Draft values
//        fne.CacheId = 100;
//        fne.gcCode = "GCCODE";
//        fne.CacheName = "CacheName";
//        fne.timestamp = new Date();
//        fne.foundNumber = 12;
//        fne.comment = "Comment";
//        fne.cacheType = CacheTypes.Traditional;
//        fne.CacheUrl = "URL";
//        fne.uploaded = false;
//        fne.gc_Vote = 7;
//        fne.isTbDraft = false;
//        fne.TbName = "TB Name";
//        fne.TbIconUrl = "TB icon url";
//        fne.TravelBugCode = "TravelBugCode";
//        fne.TrackingNumber = "TrackingNumber";
//        fne.isDirectLog = false;
//        fne.fillType();
//
//        try {
//            fne.writeToDatabase();
//        } catch (Exception e) {
//            assertThat("Can't write Draft to DB", false);
//        }
//
//
//        // read Draft from DB
//        DraftList fieldNoteEntries = new DraftList();
//        fieldNoteEntries.loadDrafts("", DraftList.LoadingType.LOAD_NEW_LAST_LENGTH);
//        assertThat("DraftList size must be 1", fieldNoteEntries.size == 1);
//        DraftEntry fne2 = fieldNoteEntries.get(0);
//        assertThat("Drafts must be charSequenceEquals", fne2.charSequenceEquals(fne));
//
//        //read non uploaded Draft from DB
//        DraftList lDrafts = new DraftList();
//        lDrafts.loadDrafts("(Uploaded=0 or Uploaded is null)", DraftList.LoadingType.LOAD_ALL);
//
//        assertThat("DraftList size must be 1", lDrafts.size == 1);
//        DraftEntry fne3 = lDrafts.get(0);
//        assertThat("Drafts must be charSequenceEquals", fne3.charSequenceEquals(fne));
//
//
//        // set uploaded flag and write to DB
//        fne3.uploaded = true;
//        fne3.writeToDatabase();
//
//        DraftList lDrafts2 = new DraftList();
//        lDrafts2.loadDrafts("", DraftList.LoadingType.LOAD_ALL);
//
//        assertThat("DraftList size must be 1", lDrafts2.size == 1);
//        DraftEntry fne4 = lDrafts2.get(0);
//        assertThat("Drafts must not charSequenceEquals", !fne4.charSequenceEquals(fne));
//
//        assertThat("Drafts must charSequenceEquals", fne4.charSequenceEquals(fne3));
//
//        assertThat("Drafts must have uploaded flag", fne4.uploaded);
//    }


}
