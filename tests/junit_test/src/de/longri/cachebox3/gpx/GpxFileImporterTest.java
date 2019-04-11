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
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.test_caches.AbstractTestCache;
import de.longri.cachebox3.types.test_caches.TEST_CACHES;
import de.longri.cachebox3.types.test_caches.TestCache_GC52BKF_without_logs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Longri on 29.03.2018.
 */
class GpxFileImporterTest {

    static {
        TestUtils.initialGdx();
    }


    @BeforeEach
    public void setUp() {
        try {
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitSec();
    }

    @Test
    public void testGpxStreamImport_GC2T9RW() {
        waitSec();
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        FileHandle gpxFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC2T9RW.gpx", true);
        final AtomicInteger cacheCount = new AtomicInteger();
        final AtomicInteger waypointCount = new AtomicInteger();
        final AtomicInteger logCount = new AtomicInteger();
        final Array<String> mysteryList = new Array<>();
        ImportHandler importHandler = new ImportHandler() {
            @Override
            public void incrementCaches(String mysteryGcCode) {
                cacheCount.incrementAndGet();
                if (mysteryGcCode != null) mysteryList.add(mysteryGcCode);
            }

            @Override
            public void incrementWaypoints() {
                waypointCount.incrementAndGet();
            }

            @Override
            public void incrementLogs() {
                logCount.incrementAndGet();
            }
        };

        new GroundspeakGpxStreamImporter(TEST_DB, importHandler).doImport(gpxFile);
        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);
        AbstractCache cache = TEST_DB.getFromDbByGcCode("GC2T9RW", true, true);
        TEST_CACHES.GC2T9RW.assertCache(cache, TEST_DB);

        assertEquals(cacheCount.get(), 1, "Imported Cache count is wrong");
        assertEquals(waypointCount.get(), 1, "Imported Waypoint count is wrong");
        assertEquals(logCount.get(), 20, "Imported Log count is wrong");
        assertEquals(mysteryList.size, 0, "Imported Mystery count is Wrong");


        TEST_DB.close();

        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("Gpx Stream import time: " + elapseTime + "ms");
    }

    @Test
    public void testGpxStreamImport_GC52BKF() {
        waitSec();
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        FileHandle gpxFile = TestUtils.getResourceFileHandle("testsResources/gpx/GC52BKF.gpx", true);
        final AtomicInteger cacheCount = new AtomicInteger();
        final AtomicInteger waypointCount = new AtomicInteger();
        final AtomicInteger logCount = new AtomicInteger();
        final Array<String> mysteryList = new Array<>();
        ImportHandler importHandler = new ImportHandler() {
            @Override
            public void incrementCaches(String mysteryGcCode) {
                cacheCount.incrementAndGet();
                if (mysteryGcCode != null) mysteryList.add(mysteryGcCode);
            }

            @Override
            public void incrementWaypoints() {
                waypointCount.incrementAndGet();
            }

            @Override
            public void incrementLogs() {
                logCount.incrementAndGet();
            }
        };

        new GroundspeakGpxStreamImporter(TEST_DB, importHandler).doImport(gpxFile);
        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);
        AbstractCache cache = TEST_DB.getFromDbByGcCode("GC52BKF", true, true);
        TEST_CACHES.GC52BKF.assertCache(cache, TEST_DB);

        assertEquals(cacheCount.get(), 1, "Imported Cache count is wrong");
        assertEquals(waypointCount.get(), 0, "Imported Waypoint count is wrong");
        assertEquals(logCount.get(), 20, "Imported Log count is wrong");
        assertEquals(mysteryList.size, 0, "Imported Mystery count is Wrong");

        // set Favorite and Found! check Conflict handling with reimport
        AbstractTestCache CHANGED_FAV_FOUND = new TestCache_GC52BKF_without_logs() {
            protected void setValues() {
                super.setValues();
                this.found = true;
                this.favorite = true;
            }
        };

        //store changed
        cache.setFound(true);
        cache.setFavorite(true);
        cache.updateBooleanStore(TEST_DB);


        //check if changes stored in DB
        cache = TEST_DB.getFromDbByGcCode("GC52BKF", true, true);
        CHANGED_FAV_FOUND.assertCache(cache, TEST_DB);

        //reimport
        new GroundspeakGpxStreamImporter(TEST_DB, importHandler).doImport(gpxFile);
        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);
        cache = TEST_DB.getFromDbByGcCode("GC52BKF", true, true);
        CHANGED_FAV_FOUND.assertCache(cache, TEST_DB);


        TEST_DB.close();
        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("Gpx Stream import time: " + elapseTime + "ms");
    }

    @Test
    public void testGpxStreamImport_PQ() {
        waitSec();
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        FileHandle gpxFile = TestUtils.getResourceFileHandle("testsResources/gpx/GS_PQ/6004539_HomeZone.gpx", true);
        FileHandle gpxFile2 = TestUtils.getResourceFileHandle("testsResources/gpx/GS_PQ/6004539_HomeZone-wpts.gpx", true);
        final AtomicInteger cacheCount = new AtomicInteger();
        final AtomicInteger waypointCount = new AtomicInteger();
        final AtomicInteger logCount = new AtomicInteger();
        final Array<String> mysteryList = new Array<>();
        ImportHandler importHandler = new ImportHandler() {
            @Override
            public void incrementCaches(String mysteryGcCode) {
                cacheCount.incrementAndGet();
                if (mysteryGcCode != null) mysteryList.add(mysteryGcCode);
            }

            @Override
            public void incrementWaypoints() {
                waypointCount.incrementAndGet();
            }

            @Override
            public void incrementLogs() {
                logCount.incrementAndGet();
            }
        };

        GroundspeakGpxStreamImporter importer = new GroundspeakGpxStreamImporter(TEST_DB, importHandler);
        importer.doImport(gpxFile);
        waitSec();
        importer.doImport(gpxFile2);
        waitSec();

        assertThat("Cache count must be 500", TEST_DB.getCacheCountOnThisDB() == 500);
        AbstractCache cache = TEST_DB.getFromDbByGcCode("GC2TNPV", true, true);
        TEST_CACHES.GC2TNPV.assertCache(cache, TEST_DB);

        cache = TEST_DB.getFromDbByGcCode("GCV272", true, true);
        TEST_CACHES.GCV272.assertCache(cache, TEST_DB);


        TEST_DB.close();

        assertEquals(cacheCount.get(), 500, "Imported Cache count is wrong");
        assertEquals(waypointCount.get(), 183, "Imported Waypoint count is wrong");
        assertEquals(logCount.get(), 2534, "Imported Log count is wrong");
        assertEquals(mysteryList.size, 167, "Imported Mystery count is Wrong");

        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("PQ Stream import time: " + elapseTime + "ms");
    }

    @Test
    public void testGpxStreamImport_GSAK_correctedCoords() {
        waitSec();
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        final AtomicInteger cacheCount = new AtomicInteger();
        final AtomicInteger waypointCount = new AtomicInteger();
        final AtomicInteger logCount = new AtomicInteger();
        final Array<String> mysteryList = new Array<>();
        ImportHandler importHandler = new ImportHandler() {
            @Override
            public void incrementCaches(String mysteryGcCode) {
                cacheCount.incrementAndGet();
                if (mysteryGcCode != null) mysteryList.add(mysteryGcCode);
            }

            @Override
            public void incrementWaypoints() {
                waypointCount.incrementAndGet();
            }

            @Override
            public void incrementLogs() {
                logCount.incrementAndGet();
            }
        };

        GroundspeakGpxStreamImporter importer = new GroundspeakGpxStreamImporter(TEST_DB, importHandler);
        AbstractCache cache;
        //=================================================================================
        importer.doImport(TestUtils.getResourceFileHandle("testsResources/gpx/gsak-correctedCoords.gpx", true));
        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);
        cache = TEST_DB.getFromDbByGcCode("GC250Q2", true, true);
        TEST_CACHES.GC250Q2.assertCache(cache, TEST_DB);
        assertEquals(cacheCount.get(), 1, "Imported Cache count is wrong");
        assertEquals(waypointCount.get(), 1, "Imported Waypoint count is wrong");
        assertEquals(logCount.get(), 0, "Imported Log count is wrong");
        assertEquals(mysteryList.size, 1, "Imported Mystery count is Wrong");
        //=================================================================================

        //=================================================================================
        importer.doImport(TestUtils.getResourceFileHandle("testsResources/gpx/TestCache3_WP_Parents_1_1.gpx", true));
        assertThat("Cache count must be 2", TEST_DB.getCacheCountOnThisDB() == 2);
        cache = TEST_DB.getFromDbByGcCode("ACWP003", true, true);
        TEST_CACHES.ACWP003.assertCache(cache, TEST_DB);
        assertEquals(cacheCount.get(), 2, "Imported Cache count is wrong");
        assertEquals(waypointCount.get(), 2, "Imported Waypoint count is wrong");
        assertEquals(logCount.get(), 2, "Imported Log count is wrong");
        assertEquals(mysteryList.size, 1, "Imported Mystery count is Wrong");
        //=================================================================================


        TEST_DB.close();

        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("Gpx Stream import time: " + elapseTime + "ms");
    }

    @Test
    public void testGpxStreamImport_GSAK_correctedCoords_1_0() {
        waitSec();
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        final AtomicInteger cacheCount = new AtomicInteger();
        final AtomicInteger waypointCount = new AtomicInteger();
        final AtomicInteger logCount = new AtomicInteger();
        final Array<String> mysteryList = new Array<>();
        ImportHandler importHandler = new ImportHandler() {
            @Override
            public void incrementCaches(String mysteryGcCode) {
                cacheCount.incrementAndGet();
                if (mysteryGcCode != null) mysteryList.add(mysteryGcCode);
            }

            @Override
            public void incrementWaypoints() {
                waypointCount.incrementAndGet();
            }

            @Override
            public void incrementLogs() {
                logCount.incrementAndGet();
            }
        };

        GroundspeakGpxStreamImporter importer = new GroundspeakGpxStreamImporter(TEST_DB, importHandler);
        AbstractCache cache;

        //=================================================================================
        importer.doImport(TestUtils.getResourceFileHandle("testsResources/gpx/TestCache3_WP_Parents_1_0.gpx", true));
        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);
        cache = TEST_DB.getFromDbByGcCode("ACWP003", true, true);
        TEST_CACHES.ACWP003.assertCache(cache, TEST_DB);
        assertEquals(cacheCount.get(), 1, "Imported Cache count is wrong");
        assertEquals(waypointCount.get(), 1, "Imported Waypoint count is wrong");
        assertEquals(logCount.get(), 2, "Imported Log count is wrong");
        assertEquals(mysteryList.size, 0, "Imported Mystery count is Wrong");
        //=================================================================================


        TEST_DB.close();

        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("Gpx Stream import time: " + elapseTime + "ms");
    }

    @Test
    public void testGpxStreamImport_cachebox_extension() {
        waitSec();
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        final AtomicInteger cacheCount = new AtomicInteger();
        final AtomicInteger waypointCount = new AtomicInteger();
        final AtomicInteger logCount = new AtomicInteger();
        final Array<String> mysteryList = new Array<>();
        ImportHandler importHandler = new ImportHandler() {
            @Override
            public void incrementCaches(String mysteryGcCode) {
                cacheCount.incrementAndGet();
                if (mysteryGcCode != null) mysteryList.add(mysteryGcCode);
            }

            @Override
            public void incrementWaypoints() {
                waypointCount.incrementAndGet();
            }

            @Override
            public void incrementLogs() {
                logCount.incrementAndGet();
            }
        };

        GroundspeakGpxStreamImporter importer = new GroundspeakGpxStreamImporter(TEST_DB, importHandler);
        AbstractCache cache;

        //=================================================================================
        importer.doImport(TestUtils.getResourceFileHandle("testsResources/gpx/acb_export.gpx", true));
        assertThat("Cache count must be 350", TEST_DB.getCacheCountOnThisDB() == 350);
        cache = TEST_DB.getFromDbByGcCode("GC2V0NP", true, true);
        TEST_CACHES.GC2V0NP.assertCache(cache, TEST_DB);
        assertEquals(cacheCount.get(), 350, "Imported Cache count is wrong");
        assertEquals(waypointCount.get(), 435, "Imported Waypoint count is wrong");
        assertEquals(logCount.get(), 1460, "Imported Log count is wrong");
        assertEquals(mysteryList.size, 137, "Imported Mystery count is Wrong");
        //=================================================================================


        TEST_DB.close();

        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("Gpx Stream import time: " + elapseTime + "ms");
    }

    @Test
    public void testGpxStreamImport_OC() {
        waitSec();
        long start = System.currentTimeMillis();

        Database TEST_DB = TestUtils.getTestDB(true);
        final AtomicInteger cacheCount = new AtomicInteger();
        final AtomicInteger waypointCount = new AtomicInteger();
        final AtomicInteger logCount = new AtomicInteger();
        final Array<String> mysteryList = new Array<>();
        ImportHandler importHandler = new ImportHandler() {
            @Override
            public void incrementCaches(String mysteryGcCode) {
                cacheCount.incrementAndGet();
                if (mysteryGcCode != null) mysteryList.add(mysteryGcCode);
            }

            @Override
            public void incrementWaypoints() {
                waypointCount.incrementAndGet();
            }

            @Override
            public void incrementLogs() {
                logCount.incrementAndGet();
            }
        };

        GroundspeakGpxStreamImporter importer = new GroundspeakGpxStreamImporter(TEST_DB, importHandler);
        AbstractCache cache;

        //=================================================================================
        importer.doImport(TestUtils.getResourceFileHandle("testsResources/gpx/OCF19A.gpx", true));
        assertThat("Cache count must be 1", TEST_DB.getCacheCountOnThisDB() == 1);
        cache = TEST_DB.getFromDbByGcCode("OCF19A", true, true);
        TEST_CACHES.OCF19A.assertCache(cache, TEST_DB);
        assertEquals(cacheCount.get(), 1, "Imported Cache count is wrong");
        assertEquals(waypointCount.get(), 3, "Imported Waypoint count is wrong");
        assertEquals(logCount.get(), 2, "Imported Log count is wrong");
        assertEquals(mysteryList.size, 0, "Imported Mystery count is Wrong");
        //=================================================================================


        TEST_DB.close();

        long elapseTime = System.currentTimeMillis() - start;
        System.out.println("Gpx Stream import time: " + elapseTime + "ms");
    }

    private void waitSec() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}