/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.gpx;

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.Import.GPXFileImporter;
import de.longri.cachebox3.types.AbstractCache;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Longri on 29.03.2018.
 */
class GpxFileImporterTest {

    static {
        TestUtils.initialGdx();
    }

    @Test
    public void testGpxImport() throws Exception {

        Database TEST_DB = TestUtils.getTestDB(true);
        FileHandle gpxFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC2T9RW.gpx");
        ImportHandler importHandler = new ImportHandler() {
        };

        GPXFileImporter importer = new GPXFileImporter(TEST_DB, gpxFile, importHandler);
        importer.doImport(importHandler, 0);

//  TODO      CacheTest.assertCache_GC2T9RW_with_details(true);

        TEST_DB.close();
        TEST_DB.getFileHandle().delete();
    }

    @Test
    public void testGpxImportShortDesc() throws Exception {

        Database TEST_DB = TestUtils.getTestDB(true);
        FileHandle gpxFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC52BKF.gpx");
        ImportHandler importHandler = new ImportHandler() {
        };

        GPXFileImporter importer = new GPXFileImporter(TEST_DB, gpxFile, importHandler);
        importer.doImport(importHandler, 0);

        AbstractCache cache = TEST_DB.getFromDbByGcCode("GC52BKF", false);
        assertEquals("", cache.getLongDescription(TEST_DB));

        String sd = "<p>Drive In. Eine nette Zusatzeule. Bewohner ist informiert. Dennoch oft ï¿½muggelig. Das Grundstï¿½ck muss nicht betreten werden!ï¿½<img alt=\"enlightened\" src=\"http://www.geocaching.com/static/js/CKEditor/4.1.2/plugins/smiley/images/lightbulb.gif\" title=\"enlightened\" style=\"height:20px;width:20px;\" /></p>";
        assertEquals(sd, cache.getShortDescription(TEST_DB));

    }


}