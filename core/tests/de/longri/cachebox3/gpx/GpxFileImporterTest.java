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
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.test_caches.TEST_CACHES;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
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
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        FileHandle gpxFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC2T9RW.gpx");
        ImportHandler importHandler = new ImportHandler() {
        };

        new GroundspeakGpxFileImporter(TEST_DB, importHandler).doImport(gpxFile);
        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);
        AbstractCache cache = TEST_DB.getFromDbByGcCode("GC2T9RW", true);
        TEST_CACHES.GC2T9RW.assertCache(cache, TEST_DB);
        TEST_DB.close();

        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("Gpx import time: " + elapseTime + "ms");
    }

    @Test
    public void testGpxStreamImport() throws Exception {
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        FileHandle gpxFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC2T9RW.gpx");
        ImportHandler importHandler = new ImportHandler() {
        };

        new GroundspeakGpxStreamImporter(TEST_DB, importHandler).doImport(gpxFile);
        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);
        AbstractCache cache = TEST_DB.getFromDbByGcCode("GC2T9RW", true);
        TEST_CACHES.GC2T9RW.assertCache(cache, TEST_DB);

        TEST_DB.close();

        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("Gpx Stream import time: " + elapseTime + "ms");
    }

    @Test
    public void testGpxImportShortDesc() throws Exception {

        Database TEST_DB = TestUtils.getTestDB(true);
        FileHandle gpxFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC52BKF.gpx");
        ImportHandler importHandler = new ImportHandler() {
        };

        new GroundspeakGpxFileImporter(TEST_DB, importHandler).doImport(gpxFile);

        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);


        AbstractCache cache = TEST_DB.getFromDbByGcCode("GC52BKF", false);
        assertThat("Cache can't be NULL", cache != null);
        assertEquals("\n", cache.getLongDescription(TEST_DB));

        String sd = "&lt;p&gt;Drive In. Eine nette Zusatzeule. Bewohner ist informiert. Dennoch oft  muggelig. Das Grundstück muss nicht betreten werden! &lt;img alt=\"enlightened\" src=\"http://www.geocaching.com/static/js/CKEditor/4.1.2/plugins/smiley/images/lightbulb.gif\" title=\"enlightened\" style=\"height:20px;width:20px;\" /&gt;&lt;/p&gt;\n" +
                "\n";
        assertEquals(sd, cache.getShortDescription(TEST_DB));

    }


}