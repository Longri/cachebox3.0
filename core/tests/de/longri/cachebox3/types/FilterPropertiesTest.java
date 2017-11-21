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


import com.badlogic.gdx.sql.SQLiteGdxException;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheList3DAO;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Longri on 19.02.17.
 */
class FilterPropertiesTest {
    static Database testDb;

    static {
        TestUtils.initialGdx();
        testDb = new Database(Database.DatabaseType.CacheBox3);
        try {
            testDb.startUp(TestUtils.getResourceFileHandle("testsResources/cacheboxTestDB.db3"));
        } catch (SQLiteGdxException e) {
            e.printStackTrace();
        }
    }

    @Test
    void checkPresetALL() {


        // chk FilterInstances.ALL 'All Caches'
        assertThat("FilterInstances.ALL 'All Caches' => Finds", 0 == FilterInstances.ALL.Finds.get());
        assertThat("FilterInstances.ALL 'All Caches' => NotAvailable", 0 == FilterInstances.ALL.NotAvailable.get());
        assertThat("FilterInstances.ALL 'All Caches' => Archived", 0 == FilterInstances.ALL.Archived.get());
        assertThat("FilterInstances.ALL 'All Caches' => Own", 0 == FilterInstances.ALL.Own.get());
        assertThat("FilterInstances.ALL 'All Caches' => ContainsTravelbugs", 0 == FilterInstances.ALL.ContainsTravelbugs.get());
        assertThat("FilterInstances.ALL 'All Caches' => Favorites", 0 == FilterInstances.ALL.Favorites.get());
        assertThat("FilterInstances.ALL 'All Caches' => HasUserData", 0 == FilterInstances.ALL.HasUserData.get());
        assertThat("FilterInstances.ALL 'All Caches' => ListingChanged", 0 == FilterInstances.ALL.ListingChanged.get());
        assertThat("FilterInstances.ALL 'All Caches' => WithManualWaypoint", 0 == FilterInstances.ALL.WithManualWaypoint.get());
        assertThat("FilterInstances.ALL 'All Caches' => MinDifficulty", 1.0f == FilterInstances.ALL.MinDifficulty);
        assertThat("FilterInstances.ALL 'All Caches' => MaxDifficulty", 5.0f == FilterInstances.ALL.MaxDifficulty);
        assertThat("FilterInstances.ALL 'All Caches' => MinTerrain", 1.0f == FilterInstances.ALL.MinTerrain);
        assertThat("FilterInstances.ALL 'All Caches' => MaxTerrain", 5.0f == FilterInstances.ALL.MaxTerrain);
        assertThat("FilterInstances.ALL 'All Caches' => MinContainerSize", 0.0f == FilterInstances.ALL.MinContainerSize);
        assertThat("FilterInstances.ALL 'All Caches' => MaxContainerSize", 4.0f == FilterInstances.ALL.MaxContainerSize);
        assertThat("FilterInstances.ALL 'All Caches' => MinRating", 0.0f == FilterInstances.ALL.MinRating);
        assertThat("FilterInstances.ALL 'All Caches' => MaxRating", 5.0f == FilterInstances.ALL.MaxRating);

        // CacheTypes
        assertThat("FilterInstances.ALL 'All Caches' => cacheType.length", 23 == FilterInstances.ALL.mCacheTypes.length);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[0]''?", FilterInstances.ALL.mCacheTypes[0]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[1]''?", FilterInstances.ALL.mCacheTypes[1]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[2]''?", FilterInstances.ALL.mCacheTypes[2]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[3]''?", FilterInstances.ALL.mCacheTypes[3]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[4]''?", FilterInstances.ALL.mCacheTypes[4]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[5]''?", FilterInstances.ALL.mCacheTypes[5]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[6]''?", FilterInstances.ALL.mCacheTypes[6]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[7]''?", FilterInstances.ALL.mCacheTypes[7]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[8]''?", FilterInstances.ALL.mCacheTypes[8]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[9]''?", FilterInstances.ALL.mCacheTypes[9]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[10]''?", FilterInstances.ALL.mCacheTypes[10]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[12]'GIGA'", FilterInstances.ALL.mCacheTypes[21]);

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("FilterInstances.ALL 'All Caches' => attributesFilter.length", AtributeLength ==
                FilterInstances.ALL.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("FilterInstances.ALL 'All Caches' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.ALL.mAttributes[i]);
        }

        assertThat("FilterInstances.ALL 'All Caches' => GPXFilenameIds.size", 0 == FilterInstances.ALL.GPXFilenameIds.size);
        assertThat("FilterInstances.ALL 'All Caches' => Categories.size", 0 == FilterInstances.ALL.Categories.size);
        assertEquals("", FilterInstances.ALL.filterName, "FilterInstances.ALL 'All Caches' => filterName");
        assertEquals("", FilterInstances.ALL.filterGcCode, "FilterInstances.ALL 'All Caches' => filterGcCode");
        assertEquals("", FilterInstances.ALL.filterOwner, "FilterInstances.ALL 'All Caches' => filterOwner");


        assertEquals("{\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.ALL.toString(),
                "FilterInstances.ALL 'All Caches' =>toString");

        assertFilteredReadedDB(FilterInstances.ALL);

    }

    @Test
    void checkPresetACTIVE() {
        assertThat("presets[1] 'All Caches to find' => Finds", -1 == FilterInstances.ACTIVE.Finds.get());
        assertThat("presets[1] 'All Caches to find' => NotAvailable", -1 == FilterInstances.ACTIVE.NotAvailable.get());
        assertThat("presets[1] 'All Caches to find' => Archived", -1 == FilterInstances.ACTIVE.Archived.get());
        assertThat("presets[1] 'All Caches to find' => Own", -1 == FilterInstances.ACTIVE.Own.get());
        assertThat("presets[1] 'All Caches to find' => ContainsTravelbugs", 0 == FilterInstances.ACTIVE.ContainsTravelbugs.get());
        assertThat("presets[1] 'All Caches to find' => Favorites", 0 == FilterInstances.ACTIVE.Favorites.get());
        assertThat("presets[1] 'All Caches to find' => HasUserData", 0 == FilterInstances.ACTIVE.HasUserData.get());
        assertThat("presets[1] 'All Caches to find' => ListingChanged", 0 == FilterInstances.ACTIVE.ListingChanged.get());
        assertThat("presets[1] 'All Caches to find' => WithManualWaypoint", 0 == FilterInstances.ACTIVE.WithManualWaypoint.get());
        assertThat("presets[1] 'All Caches to find' => MinDifficulty", 1.0f == FilterInstances.ACTIVE.MinDifficulty);
        assertThat("presets[1] 'All Caches to find' => MaxDifficulty", 5.0f == FilterInstances.ACTIVE.MaxDifficulty);
        assertThat("presets[1] 'All Caches to find' => MinTerrain", 1.0f == FilterInstances.ACTIVE.MinTerrain);
        assertThat("presets[1] 'All Caches to find' => MaxTerrain", 5.0f == FilterInstances.ACTIVE.MaxTerrain);
        assertThat("presets[1] 'All Caches to find' => MinContainerSize", 0.0f == FilterInstances.ACTIVE.MinContainerSize);
        assertThat("presets[1] 'All Caches to find' => MaxContainerSize", 4.0f == FilterInstances.ACTIVE.MaxContainerSize);
        assertThat("presets[1] 'All Caches to find' => MinRating", 0.0f == FilterInstances.ACTIVE.MinRating);
        assertThat("presets[1] 'All Caches to find' => MaxRating", 5.0f == FilterInstances.ACTIVE.MaxRating);

        // CacheTypes
        assertThat("presets[1] 'All Caches to find' => cacheType.length", 23 == FilterInstances.ACTIVE.mCacheTypes.length);
        assertThat("presets[1] 'All Caches to find' => cacheType[0]''?", FilterInstances.ACTIVE.mCacheTypes[0]);
        assertThat("presets[1] 'All Caches to find' => cacheType[1]''?", FilterInstances.ACTIVE.mCacheTypes[1]);
        assertThat("presets[1] 'All Caches to find' => cacheType[2]''?", FilterInstances.ACTIVE.mCacheTypes[2]);
        assertThat("presets[1] 'All Caches to find' => cacheType[3]''?", FilterInstances.ACTIVE.mCacheTypes[3]);
        assertThat("presets[1] 'All Caches to find' => cacheType[4]''?", FilterInstances.ACTIVE.mCacheTypes[4]);
        assertThat("presets[1] 'All Caches to find' => cacheType[5]''?", FilterInstances.ACTIVE.mCacheTypes[5]);
        assertThat("presets[1] 'All Caches to find' => cacheType[6]''?", FilterInstances.ACTIVE.mCacheTypes[6]);
        assertThat("presets[1] 'All Caches to find' => cacheType[7]''?", FilterInstances.ACTIVE.mCacheTypes[7]);
        assertThat("presets[1] 'All Caches to find' => cacheType[8]''?", FilterInstances.ACTIVE.mCacheTypes[8]);
        assertThat("presets[1] 'All Caches to find' => cacheType[9]''?", FilterInstances.ACTIVE.mCacheTypes[9]);
        assertThat("presets[1] 'All Caches to find' => cacheType[10]''?", FilterInstances.ACTIVE.mCacheTypes[10]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[12]'GIGA'", FilterInstances.ACTIVE.mCacheTypes[21]);

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[1] 'All Caches to find' => attributesFilter.length", AtributeLength ==
                FilterInstances.ACTIVE.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[1] 'All Caches to find' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.ACTIVE.mAttributes[i]);
        }

        assertThat("presets[1] 'All Caches to find' => GPXFilenameIds.size", 0 == FilterInstances.ACTIVE.GPXFilenameIds.size);
        assertThat("presets[1] 'All Caches to find' => Categories.size", 0 == FilterInstances.ACTIVE.Categories.size);
        assertEquals("", FilterInstances.ACTIVE.filterName, "presets[1] 'All Caches to find' => filterName");
        assertEquals("", FilterInstances.ACTIVE.filterGcCode, "presets[1] 'All Caches to find' => filterGcCode");
        assertEquals("", FilterInstances.ACTIVE.filterOwner, "presets[1] 'All Caches to find' => filterOwner");

        assertEquals("{\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.ALL.toString(),
                "FilterInstances.ALL 'All Caches' =>toString");

        assertFilteredReadedDB(FilterInstances.ACTIVE);
    }

    @Test
    void checkPresetQUICK() {
        assertThat("presets[2] 'Quick Cache' => Finds", -1 == FilterInstances.QUICK.Finds.get());
        assertThat("presets[2] 'Quick Cache' => NotAvailable", -1 == FilterInstances.QUICK.NotAvailable.get());
        assertThat("presets[2] 'Quick Cache' => Archived", -1 == FilterInstances.QUICK.Archived.get());
        assertThat("presets[2] 'Quick Cache' => Own", -1 == FilterInstances.QUICK.Own.get());
        assertThat("presets[2] 'Quick Cache' => ContainsTravelbugs", 0 == FilterInstances.QUICK.ContainsTravelbugs.get());
        assertThat("presets[2] 'Quick Cache' => Favorites", 0 == FilterInstances.QUICK.Favorites.get());
        assertThat("presets[2] 'Quick Cache' => HasUserData", 0 == FilterInstances.QUICK.HasUserData.get());
        assertThat("presets[2] 'Quick Cache' => ListingChanged", 0 == FilterInstances.QUICK.ListingChanged.get());
        assertThat("presets[2] 'Quick Cache' => WithManualWaypoint", 0 == FilterInstances.QUICK.WithManualWaypoint.get());
        assertThat("presets[2] 'Quick Cache' => MinDifficulty", 1.0f == FilterInstances.QUICK.MinDifficulty);
        assertThat("presets[2] 'Quick Cache' => MaxDifficulty", 2.5f == FilterInstances.QUICK.MaxDifficulty);
        assertThat("presets[2] 'Quick Cache' => MinTerrain", 1.0f == FilterInstances.QUICK.MinTerrain);
        assertThat("presets[2] 'Quick Cache' => MaxTerrain", 2.5f == FilterInstances.QUICK.MaxTerrain);
        assertThat("presets[2] 'Quick Cache' => MinContainerSize", 0.0f == FilterInstances.QUICK.MinContainerSize);
        assertThat("presets[2] 'Quick Cache' => MaxContainerSize", 4.0f == FilterInstances.QUICK.MaxContainerSize);
        assertThat("presets[2] 'Quick Cache' => MinRating", 0.0f == FilterInstances.QUICK.MinRating);
        assertThat("presets[2] 'Quick Cache' => MaxRating", 5.0f == FilterInstances.QUICK.MaxRating);

        // CacheTypes
        assertThat("presets[2] 'Quick Cache' => cacheType.length", 23 == FilterInstances.QUICK.mCacheTypes.length);
        assertThat("presets[2] 'Quick Cache' => cacheType[0]''?", FilterInstances.QUICK.mCacheTypes[0]);
        assertThat("presets[2] 'Quick Cache' => cacheType[1]''?", !FilterInstances.QUICK.mCacheTypes[1]);
        assertThat("presets[2] 'Quick Cache' => cacheType[2]''?", !FilterInstances.QUICK.mCacheTypes[2]);
        assertThat("presets[2] 'Quick Cache' => cacheType[3]''?", FilterInstances.QUICK.mCacheTypes[3]);
        assertThat("presets[2] 'Quick Cache' => cacheType[4]''?", FilterInstances.QUICK.mCacheTypes[4]);
        assertThat("presets[2] 'Quick Cache' => cacheType[5]''?", !FilterInstances.QUICK.mCacheTypes[5]);
        assertThat("presets[2] 'Quick Cache' => cacheType[6]''?", !FilterInstances.QUICK.mCacheTypes[6]);
        assertThat("presets[2] 'Quick Cache' => cacheType[7]''?", !FilterInstances.QUICK.mCacheTypes[7]);
        assertThat("presets[2] 'Quick Cache' => cacheType[8]''?", !FilterInstances.QUICK.mCacheTypes[8]);
        assertThat("presets[2] 'Quick Cache' => cacheType[9]''?", !FilterInstances.QUICK.mCacheTypes[9]);
        assertThat("presets[2] 'Quick Cache' => cacheType[10]''?", !FilterInstances.QUICK.mCacheTypes[10]);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[12]'GIGA'", !FilterInstances.QUICK.mCacheTypes[21]);


        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[2] 'Quick Cache' => attributesFilter.length", AtributeLength ==
                FilterInstances.QUICK.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[2] 'Quick Cache' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.QUICK.mAttributes[i]);
        }

        assertThat("presets[2] 'Quick Cache' => GPXFilenameIds.size", 0 == FilterInstances.QUICK.GPXFilenameIds.size);
        assertThat("presets[2] 'Quick Cache' => Categories.size", 0 == FilterInstances.QUICK.Categories.size);
        assertEquals("", FilterInstances.QUICK.filterName, "presets[2] 'Quick Cache' => filterName");
        assertEquals("", FilterInstances.QUICK.filterGcCode, "presets[2] 'Quick Cache' => filterGcCode");
        assertEquals("", FilterInstances.QUICK.filterOwner, "presets[2] 'Quick Cache' => filterOwner");


        assertEquals(
                "{\"types\":\"true,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,2.5,1.0,2.5,0.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.QUICK.toString(),
                "presets[2] 'Quick Cache' =>toString");

        assertEquals("BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and (not Owner='NAME') and Difficulty >= 2.0 and Difficulty <= 5.0 and Terrain >= 2.0 and Terrain <= 5.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,3,4) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                FilterInstances.QUICK.getSqlWhere("NAME"), "SqlWhere must Equals");

        assertFilteredReadedDB(FilterInstances.QUICK);
    }


    @Test
    void checkPresetBEGINNER() {

    }

    @Test
    void checkPresetWITHTB() {
        assertThat("presets[3] 'Fetch some Travelbugs' => Finds", 0 == FilterInstances.WITHTB.Finds.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => NotAvailable", -1 == FilterInstances.WITHTB.NotAvailable.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => Archived", -1 == FilterInstances.WITHTB.Archived.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => Own", 0 == FilterInstances.WITHTB.Own.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => ContainsTravelbugs", 1 == FilterInstances.WITHTB.ContainsTravelbugs.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => Favorites", 0 == FilterInstances.WITHTB.Favorites.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => HasUserData", 0 == FilterInstances.WITHTB.HasUserData.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => ListingChanged", 0 == FilterInstances.WITHTB.ListingChanged.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => WithManualWaypoint", 0 == FilterInstances.WITHTB.WithManualWaypoint.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => MinDifficulty", 1.0f == FilterInstances.WITHTB.MinDifficulty);
        assertThat("presets[3] 'Fetch some Travelbugs' => MaxDifficulty", 3.0f == FilterInstances.WITHTB.MaxDifficulty);
        assertThat("presets[3] 'Fetch some Travelbugs' => MinTerrain", 1.0f == FilterInstances.WITHTB.MinTerrain);
        assertThat("presets[3] 'Fetch some Travelbugs' => MaxTerrain", 3.0f == FilterInstances.WITHTB.MaxTerrain);
        assertThat("presets[3] 'Fetch some Travelbugs' => MinContainerSize", 0.0f == FilterInstances.WITHTB.MinContainerSize);
        assertThat("presets[3] 'Fetch some Travelbugs' => MaxContainerSize", 4.0f == FilterInstances.WITHTB.MaxContainerSize);
        assertThat("presets[3] 'Fetch some Travelbugs' => MinRating", 0.0f == FilterInstances.WITHTB.MinRating);
        assertThat("presets[3] 'Fetch some Travelbugs' => MaxRating", 5.0f == FilterInstances.WITHTB.MaxRating);

        // CacheTypes
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType.length", 23 == FilterInstances.WITHTB.mCacheTypes.length);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[0]''?", FilterInstances.WITHTB.mCacheTypes[0]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[1]''?", FilterInstances.WITHTB.mCacheTypes[1]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[2]''?", FilterInstances.WITHTB.mCacheTypes[2]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[3]''?", !FilterInstances.WITHTB.mCacheTypes[3]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[4]''?", !FilterInstances.WITHTB.mCacheTypes[4]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[5]''?", FilterInstances.WITHTB.mCacheTypes[5]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[6]''?", FilterInstances.WITHTB.mCacheTypes[6]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[7]''?", FilterInstances.WITHTB.mCacheTypes[7]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[8]''?", !FilterInstances.WITHTB.mCacheTypes[8]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[9]''?", FilterInstances.WITHTB.mCacheTypes[9]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[10]''?", FilterInstances.WITHTB.mCacheTypes[10]);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[22]'GIGA'", FilterInstances.WITHTB.mCacheTypes[21]);


        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[3] 'Fetch some Travelbugs' => attributesFilter.length", AtributeLength ==
                FilterInstances.QUICK.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[3] 'Fetch some Travelbugs' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.QUICK.mAttributes[i]);
        }

        assertThat("presets[3] 'Fetch some Travelbugs' => GPXFilenameIds.size", 0 == FilterInstances.WITHTB.GPXFilenameIds.size);
        assertThat("presets[3] 'Fetch some Travelbugs' => Categories.size", 0 == FilterInstances.WITHTB.Categories.size);
        assertEquals("", FilterInstances.WITHTB.filterName, "presets[3] 'Fetch some Travelbugs' => filterName");
        assertEquals("", FilterInstances.WITHTB.filterGcCode, "presets[3] 'Fetch some Travelbugs' => filterGcCode");
        assertEquals("", FilterInstances.WITHTB.filterOwner, "presets[3] 'Fetch some Travelbugs' => filterOwner");


        assertEquals(
                "{\"types\":\"true,true,true,false,false,true,true,true,false,true,true,false,false,false,false,false,false,false,false,false,false,true,false\",\"caches\":\"0,-1,-1,0,1,0,0,0,0,1.0,3.0,1.0,3.0,0.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.WITHTB.toString(), "presets[3] 'Fetch some Travelbugs' =>toString");

        assertFilteredReadedDB(FilterInstances.WITHTB);
    }

    @Test
    void checkPresetDROPTB() {
        assertThat("presets[4] 'Drop off Travelbugs' => Finds", 0 == FilterInstances.DROPTB.Finds.get());
        assertThat("presets[4] 'Drop off Travelbugs' => NotAvailable", -1 == FilterInstances.DROPTB.NotAvailable.get());
        assertThat("presets[4] 'Drop off Travelbugs' => Archived", -1 == FilterInstances.DROPTB.Archived.get());
        assertThat("presets[4] 'Drop off Travelbugs' => Own", 0 == FilterInstances.DROPTB.Own.get());
        assertThat("presets[4] 'Drop off Travelbugs' => ContainsTravelbugs", 0 == FilterInstances.DROPTB.ContainsTravelbugs.get());
        assertThat("presets[4] 'Drop off Travelbugs' => Favorites", 0 == FilterInstances.DROPTB.Favorites.get());
        assertThat("presets[4] 'Drop off Travelbugs' => HasUserData", 0 == FilterInstances.DROPTB.HasUserData.get());
        assertThat("presets[4] 'Drop off Travelbugs' => ListingChanged", 0 == FilterInstances.DROPTB.ListingChanged.get());
        assertThat("presets[4] 'Drop off Travelbugs' => WithManualWaypoint", 0 == FilterInstances.DROPTB.WithManualWaypoint.get());
        assertThat("presets[4] 'Drop off Travelbugs' => MinDifficulty", 1.0f == FilterInstances.DROPTB.MinDifficulty);
        assertThat("presets[4] 'Drop off Travelbugs' => MaxDifficulty", 3.0f == FilterInstances.DROPTB.MaxDifficulty);
        assertThat("presets[4] 'Drop off Travelbugs' => MinTerrain", 1.0f == FilterInstances.DROPTB.MinTerrain);
        assertThat("presets[4] 'Drop off Travelbugs' => MaxTerrain", 3.0f == FilterInstances.DROPTB.MaxTerrain);
        assertThat("presets[4] 'Drop off Travelbugs' => MinContainerSize", 2.0f == FilterInstances.DROPTB.MinContainerSize);
        assertThat("presets[4] 'Drop off Travelbugs' => MaxContainerSize", 4.0f == FilterInstances.DROPTB.MaxContainerSize);
        assertThat("presets[4] 'Drop off Travelbugs' => MinRating", 0.0f == FilterInstances.DROPTB.MinRating);
        assertThat("presets[4] 'Drop off Travelbugs' => MaxRating", 5.0f == FilterInstances.DROPTB.MaxRating);

        // CacheTypes
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType.length", 23 == FilterInstances.DROPTB.mCacheTypes.length);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[0]''?", FilterInstances.DROPTB.mCacheTypes[0]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[1]''?", FilterInstances.DROPTB.mCacheTypes[1]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[2]''?", FilterInstances.DROPTB.mCacheTypes[2]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[3]''?", !FilterInstances.DROPTB.mCacheTypes[3]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[4]''?", !FilterInstances.DROPTB.mCacheTypes[4]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[5]''?", FilterInstances.DROPTB.mCacheTypes[5]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[6]''?", FilterInstances.DROPTB.mCacheTypes[6]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[7]''?", FilterInstances.DROPTB.mCacheTypes[7]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[8]''?", !FilterInstances.DROPTB.mCacheTypes[8]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[9]''?", FilterInstances.DROPTB.mCacheTypes[9]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[10]''?", FilterInstances.DROPTB.mCacheTypes[10]);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[11]'Giga'", FilterInstances.DROPTB.mCacheTypes[21]);

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[4] 'Drop off Travelbugs' => attributesFilter.length", AtributeLength ==
                FilterInstances.DROPTB.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[4] 'Drop off Travelbugs' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.DROPTB.mAttributes[i]);
        }

        assertThat("presets[4] 'Drop off Travelbugs' => GPXFilenameIds.size", 0 == FilterInstances.DROPTB.GPXFilenameIds.size);
        assertThat("presets[4] 'Drop off Travelbugs' => Categories.size", 0 == FilterInstances.DROPTB.Categories.size);
        assertEquals("", FilterInstances.DROPTB.filterName, "presets[4] 'Drop off Travelbugs' => filterName");
        assertEquals("", FilterInstances.DROPTB.filterGcCode, "presets[4] 'Drop off Travelbugs' => filterGcCode");
        assertEquals("", FilterInstances.DROPTB.filterOwner, "presets[4] 'Drop off Travelbugs' => filterOwner");


        assertEquals("{\"types\":\"true,true,true,false,false,true,true,true,false,true,true,false,false,false,false,false,false,false,false,false,false,true,false\",\"caches\":\"0,-1,-1,0,0,0,0,0,0,1.0,3.0,1.0,3.0,2.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.DROPTB.toString(),
                "presets[4] 'Drop off Travelbugs' =>toString");

        assertFilteredReadedDB(FilterInstances.DROPTB);
    }

    @Test
    void checkPresetHIGHLIGHTS() {
        assertThat("presets[5] 'Highlights' => Finds", -1 == FilterInstances.HIGHLIGHTS.Finds.get());
        assertThat("presets[5] 'Highlights' => NotAvailable", -1 == FilterInstances.HIGHLIGHTS.NotAvailable.get());
        assertThat("presets[5] 'Highlights' => Archived", -1 == FilterInstances.HIGHLIGHTS.Archived.get());
        assertThat("presets[5] 'Highlights' => Own", 0 == FilterInstances.HIGHLIGHTS.Own.get());
        assertThat("presets[5] 'Highlights' => ContainsTravelbugs", 0 == FilterInstances.HIGHLIGHTS.ContainsTravelbugs.get());
        assertThat("presets[5] 'Highlights' => Favorites", 0 == FilterInstances.HIGHLIGHTS.Favorites.get());
        assertThat("presets[5] 'Highlights' => HasUserData", 0 == FilterInstances.HIGHLIGHTS.HasUserData.get());
        assertThat("presets[5] 'Highlights' => ListingChanged", 0 == FilterInstances.HIGHLIGHTS.ListingChanged.get());
        assertThat("presets[5] 'Highlights' => WithManualWaypoint", 0 == FilterInstances.HIGHLIGHTS.WithManualWaypoint.get());
        assertThat("presets[5] 'Highlights' => MinDifficulty", 1.0f == FilterInstances.HIGHLIGHTS.MinDifficulty);
        assertThat("presets[5] 'Highlights' => MaxDifficulty", 5.0f == FilterInstances.HIGHLIGHTS.MaxDifficulty);
        assertThat("presets[5] 'Highlights' => MinTerrain", 1.0f == FilterInstances.HIGHLIGHTS.MinTerrain);
        assertThat("presets[5] 'Highlights' => MaxTerrain", 5.0f == FilterInstances.HIGHLIGHTS.MaxTerrain);
        assertThat("presets[5] 'Highlights' => MinContainerSize", 0.0f == FilterInstances.HIGHLIGHTS.MinContainerSize);
        assertThat("presets[5] 'Highlights' => MaxContainerSize", 4.0f == FilterInstances.HIGHLIGHTS.MaxContainerSize);
        assertThat("presets[5] 'Highlights' => MinRating", 3.5f == FilterInstances.HIGHLIGHTS.MinRating);
        assertThat("presets[5] 'Highlights' => MaxRating", 5.0f == FilterInstances.HIGHLIGHTS.MaxRating);

        // CacheTypes
        assertThat("presets[5] 'Highlights' => cacheType.length", 23 == FilterInstances.HIGHLIGHTS.mCacheTypes.length);
        assertThat("presets[5] 'Highlights' => cacheType[0]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[0]);
        assertThat("presets[5] 'Highlights' => cacheType[1]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[1]);
        assertThat("presets[5] 'Highlights' => cacheType[2]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[2]);
        assertThat("presets[5] 'Highlights' => cacheType[3]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[3]);
        assertThat("presets[5] 'Highlights' => cacheType[4]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[4]);
        assertThat("presets[5] 'Highlights' => cacheType[5]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[5]);
        assertThat("presets[5] 'Highlights' => cacheType[6]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[6]);
        assertThat("presets[5] 'Highlights' => cacheType[7]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[7]);
        assertThat("presets[5] 'Highlights' => cacheType[8]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[8]);
        assertThat("presets[5] 'Highlights' => cacheType[9]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[9]);
        assertThat("presets[5] 'Highlights' => cacheType[10]''?", FilterInstances.HIGHLIGHTS.mCacheTypes[10]);
        assertThat("presets[5] 'Highlights' => cacheType[11]'Munzee'", FilterInstances.HIGHLIGHTS.mCacheTypes[11]);

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[5] 'Highlights' => attributesFilter.length", AtributeLength ==
                FilterInstances.HIGHLIGHTS.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[5] 'Highlights' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.HIGHLIGHTS.mAttributes[i]);
        }

        assertThat("presets[5] 'Highlights' => GPXFilenameIds.size", 0 == FilterInstances.HIGHLIGHTS.GPXFilenameIds.size);
        assertThat("presets[5] 'Highlights' => Categories.size", 0 == FilterInstances.HIGHLIGHTS.Categories.size);
        assertEquals("", FilterInstances.HIGHLIGHTS.filterName, "presets[5] 'Highlights' => filterName");
        assertEquals("", FilterInstances.HIGHLIGHTS.filterGcCode, "presets[5] 'Highlights' => filterGcCode");
        assertEquals("", FilterInstances.HIGHLIGHTS.filterOwner, "presets[5] 'Highlights' => filterOwner");


        assertEquals(
                "{\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"-1,-1,-1,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,3.5,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.HIGHLIGHTS.toString(),
                "presets[5] 'Highlights' =>toString");

        assertFilteredReadedDB(FilterInstances.HIGHLIGHTS);
    }

    @Test
    void checkPresetVAFORITES() {
        assertThat("presets[6] 'Favoriten' => Finds", 0 == FilterInstances.FAVORITES.Finds.get());
        assertThat("presets[6] 'Favoriten' => NotAvailable", 0 == FilterInstances.FAVORITES.NotAvailable.get());
        assertThat("presets[6] 'Favoriten' => Archived", 0 == FilterInstances.FAVORITES.Archived.get());
        assertThat("presets[6] 'Favoriten' => Own", 0 == FilterInstances.FAVORITES.Own.get());
        assertThat("presets[6] 'Favoriten' => ContainsTravelbugs", 0 == FilterInstances.FAVORITES.ContainsTravelbugs.get());
        assertThat("presets[6] 'Favoriten' => Favorites", 1 == FilterInstances.FAVORITES.Favorites.get());
        assertThat("presets[6] 'Favoriten' => HasUserData", 0 == FilterInstances.FAVORITES.HasUserData.get());
        assertThat("presets[6] 'Favoriten' => ListingChanged", 0 == FilterInstances.FAVORITES.ListingChanged.get());
        assertThat("presets[6] 'Favoriten' => WithManualWaypoint", 0 == FilterInstances.FAVORITES.WithManualWaypoint.get());
        assertThat("presets[6] 'Favoriten' => MinDifficulty", 1.0f == FilterInstances.FAVORITES.MinDifficulty);
        assertThat("presets[6] 'Favoriten' => MaxDifficulty", 5.0f == FilterInstances.FAVORITES.MaxDifficulty);
        assertThat("presets[6] 'Favoriten' => MinTerrain", 1.0f == FilterInstances.FAVORITES.MinTerrain);
        assertThat("presets[6] 'Favoriten' => MaxTerrain", 5.0f == FilterInstances.FAVORITES.MaxTerrain);
        assertThat("presets[6] 'Favoriten' => MinContainerSize", 0.0f == FilterInstances.FAVORITES.MinContainerSize);
        assertThat("presets[6] 'Favoriten' => MaxContainerSize", 4.0f == FilterInstances.FAVORITES.MaxContainerSize);
        assertThat("presets[6] 'Favoriten' => MinRating", 0.0f == FilterInstances.FAVORITES.MinRating);
        assertThat("presets[6] 'Favoriten' => MaxRating", 5.0f == FilterInstances.FAVORITES.MaxRating);

        // CacheTypes
        assertThat("presets[6] 'Favoriten' => cacheType.length", 23 == FilterInstances.FAVORITES.mCacheTypes.length);
        assertThat("presets[6] 'Favoriten' => cacheType[0]''?", FilterInstances.FAVORITES.mCacheTypes[0]);
        assertThat("presets[6] 'Favoriten' => cacheType[1]''?", FilterInstances.FAVORITES.mCacheTypes[1]);
        assertThat("presets[6] 'Favoriten' => cacheType[2]''?", FilterInstances.FAVORITES.mCacheTypes[2]);
        assertThat("presets[6] 'Favoriten' => cacheType[3]''?", FilterInstances.FAVORITES.mCacheTypes[3]);
        assertThat("presets[6] 'Favoriten' => cacheType[4]''?", FilterInstances.FAVORITES.mCacheTypes[4]);
        assertThat("presets[6] 'Favoriten' => cacheType[5]''?", FilterInstances.FAVORITES.mCacheTypes[5]);
        assertThat("presets[6] 'Favoriten' => cacheType[6]''?", FilterInstances.FAVORITES.mCacheTypes[6]);
        assertThat("presets[6] 'Favoriten' => cacheType[7]''?", FilterInstances.FAVORITES.mCacheTypes[7]);
        assertThat("presets[6] 'Favoriten' => cacheType[8]''?", FilterInstances.FAVORITES.mCacheTypes[8]);
        assertThat("presets[6] 'Favoriten' => cacheType[9]''?", FilterInstances.FAVORITES.mCacheTypes[9]);
        assertThat("presets[6] 'Favoriten' => cacheType[10]''?", FilterInstances.FAVORITES.mCacheTypes[10]);
        assertThat("presets[6] 'Favoriten' => cacheType[11]'Munzee'", FilterInstances.FAVORITES.mCacheTypes[11]);

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[6] 'Favoriten' => attributesFilter.length", AtributeLength ==
                FilterInstances.FAVORITES.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[6] 'Favoriten' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.FAVORITES.mAttributes[i]);
        }

        assertThat("presets[6] 'Favoriten' => GPXFilenameIds.size", 0 == FilterInstances.FAVORITES.GPXFilenameIds.size);
        assertThat("presets[6] 'Favoriten' => Categories.size", 0 == FilterInstances.FAVORITES.Categories.size);
        assertEquals("", FilterInstances.FAVORITES.filterName, "presets[6] 'Favoriten' => filterName");
        assertEquals("", FilterInstances.FAVORITES.filterGcCode, "presets[6] 'Favoriten' => filterGcCode");
        assertEquals("", FilterInstances.FAVORITES.filterOwner, "presets[6] 'Favoriten' => filterOwner");


        assertEquals(
                "{\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,1,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.FAVORITES.toString(),
                "presets[6] 'Favoriten' =>toString");

        assertFilteredReadedDB(FilterInstances.FAVORITES);
    }

    @Test
    void checkPresetTOARCHIVE() {
        assertThat("presets[7] 'prepare to archive' => Finds", 0 == FilterInstances.TOARCHIVE.Finds.get());
        assertThat("presets[7] 'prepare to archive' => NotAvailable", 0 == FilterInstances.TOARCHIVE.NotAvailable.get());
        assertThat("presets[7] 'prepare to archive' => Archived", -1 == FilterInstances.TOARCHIVE.Archived.get());
        assertThat("presets[7] 'prepare to archive' => Own", -1 == FilterInstances.TOARCHIVE.Own.get());
        assertThat("presets[7] 'prepare to archive' => ContainsTravelbugs", 0 == FilterInstances.TOARCHIVE.ContainsTravelbugs.get());
        assertThat("presets[7] 'prepare to archive' => Favorites", -1 == FilterInstances.TOARCHIVE.Favorites.get());
        assertThat("presets[7] 'prepare to archive' => HasUserData", -1 == FilterInstances.TOARCHIVE.HasUserData.get());
        assertThat("presets[7] 'prepare to archive' => ListingChanged", -1 == FilterInstances.TOARCHIVE.ListingChanged.get());
        assertThat("presets[7] 'prepare to archive' => WithManualWaypoint", 0 == FilterInstances.TOARCHIVE.WithManualWaypoint.get());
        assertThat("presets[7] 'prepare to archive' => MinDifficulty", 1.0f == FilterInstances.TOARCHIVE.MinDifficulty);
        assertThat("presets[7] 'prepare to archive' => MaxDifficulty", 5.0f == FilterInstances.TOARCHIVE.MaxDifficulty);
        assertThat("presets[7] 'prepare to archive' => MinTerrain", 1.0f == FilterInstances.TOARCHIVE.MinTerrain);
        assertThat("presets[7] 'prepare to archive' => MaxTerrain", 5.0f == FilterInstances.TOARCHIVE.MaxTerrain);
        assertThat("presets[7] 'prepare to archive' => MinContainerSize", 0.0f == FilterInstances.TOARCHIVE.MinContainerSize);
        assertThat("presets[7] 'prepare to archive' => MaxContainerSize", 4.0f == FilterInstances.TOARCHIVE.MaxContainerSize);
        assertThat("presets[7] 'prepare to archive' => MinRating", 0.0f == FilterInstances.TOARCHIVE.MinRating);
        assertThat("presets[7] 'prepare to archive' => MaxRating", 5.0f == FilterInstances.TOARCHIVE.MaxRating);

        // CacheTypes
        assertThat("presets[7] 'prepare to archive' => cacheType.length", 23 == FilterInstances.TOARCHIVE.mCacheTypes.length);
        assertThat("presets[7] 'prepare to archive' => cacheType[0]''?", FilterInstances.TOARCHIVE.mCacheTypes[0]);
        assertThat("presets[7] 'prepare to archive' => cacheType[1]''?", FilterInstances.TOARCHIVE.mCacheTypes[1]);
        assertThat("presets[7] 'prepare to archive' => cacheType[2]''?", FilterInstances.TOARCHIVE.mCacheTypes[2]);
        assertThat("presets[7] 'prepare to archive' => cacheType[3]''?", FilterInstances.TOARCHIVE.mCacheTypes[3]);
        assertThat("presets[7] 'prepare to archive' => cacheType[4]''?", FilterInstances.TOARCHIVE.mCacheTypes[4]);
        assertThat("presets[7] 'prepare to archive' => cacheType[5]''?", FilterInstances.TOARCHIVE.mCacheTypes[5]);
        assertThat("presets[7] 'prepare to archive' => cacheType[6]''?", FilterInstances.TOARCHIVE.mCacheTypes[6]);
        assertThat("presets[7] 'prepare to archive' => cacheType[7]''?", FilterInstances.TOARCHIVE.mCacheTypes[7]);
        assertThat("presets[7] 'prepare to archive' => cacheType[8]''?", FilterInstances.TOARCHIVE.mCacheTypes[8]);
        assertThat("presets[7] 'prepare to archive' => cacheType[9]''?", FilterInstances.TOARCHIVE.mCacheTypes[9]);
        assertThat("presets[7] 'prepare to archive' => cacheType[10]''?", FilterInstances.TOARCHIVE.mCacheTypes[10]);
        assertThat("presets[7] 'prepare to archive' => cacheType[11]'Munzee'", FilterInstances.TOARCHIVE.mCacheTypes[11]);

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[7] 'prepare to archive' => attributesFilter.length", AtributeLength ==
                FilterInstances.TOARCHIVE.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[7] 'prepare to archive' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.TOARCHIVE.mAttributes[i]);
        }

        assertThat("presets[7] 'prepare to archive' => GPXFilenameIds.size", 0 == FilterInstances.TOARCHIVE.GPXFilenameIds.size);
        assertThat("presets[7] 'prepare to archive' => Categories.size", 0 == FilterInstances.TOARCHIVE.Categories.size);
        assertEquals("", FilterInstances.TOARCHIVE.filterName, "presets[7] 'prepare to archive' => filterName");
        assertEquals("", FilterInstances.TOARCHIVE.filterGcCode, "presets[7] 'prepare to archive' => filterGcCode");
        assertEquals("", FilterInstances.TOARCHIVE.filterOwner, "presets[7] 'prepare to archive' => filterOwner");


        assertEquals(
                "{\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,-1,-1,0,-1,-1,-1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.TOARCHIVE.toString(), "presets[7] 'prepare to archive' =>toString");

        assertEquals("~BooleanStore & 788= 788 and (not Owner='NAME') and Difficulty >= 2.0 and Difficulty <= 10.0 and Terrain >= 2.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                FilterInstances.TOARCHIVE.getSqlWhere("NAME"), "SqlWhere must Equals");

        assertFilteredReadedDB(FilterInstances.TOARCHIVE);
    }

    @Test
    void checkPresetLISTINGCHANGED() {
        assertThat("presets[8] 'Listing Changed' => Finds", 0 == FilterInstances.LISTINGCHANGED.Finds.get());
        assertThat("presets[8] 'Listing Changed' => NotAvailable", 0 == FilterInstances.LISTINGCHANGED.NotAvailable.get());
        assertThat("presets[8] 'Listing Changed' => Archived", 0 == FilterInstances.LISTINGCHANGED.Archived.get());
        assertThat("presets[8] 'Listing Changed' => Own", 0 == FilterInstances.LISTINGCHANGED.Own.get());
        assertThat("presets[8] 'Listing Changed' => ContainsTravelbugs", 0 == FilterInstances.LISTINGCHANGED.ContainsTravelbugs.get());
        assertThat("presets[8] 'Listing Changed' => Favorites", 0 == FilterInstances.LISTINGCHANGED.Favorites.get());
        assertThat("presets[8] 'Listing Changed' => HasUserData", 0 == FilterInstances.LISTINGCHANGED.HasUserData.get());
        assertThat("presets[8] 'Listing Changed' => ListingChanged", 1 == FilterInstances.LISTINGCHANGED.ListingChanged.get());
        assertThat("presets[8] 'Listing Changed' => WithManualWaypoint", 0 == FilterInstances.LISTINGCHANGED.WithManualWaypoint.get());
        assertThat("presets[8] 'Listing Changed' => MinDifficulty", 1.0f == FilterInstances.LISTINGCHANGED.MinDifficulty);
        assertThat("presets[8] 'Listing Changed' => MaxDifficulty", 5.0f == FilterInstances.LISTINGCHANGED.MaxDifficulty);
        assertThat("presets[8] 'Listing Changed' => MinTerrain", 1.0f == FilterInstances.LISTINGCHANGED.MinTerrain);
        assertThat("presets[8] 'Listing Changed' => MaxTerrain", 5.0f == FilterInstances.LISTINGCHANGED.MaxTerrain);
        assertThat("presets[8] 'Listing Changed' => MinContainerSize", 0.0f == FilterInstances.LISTINGCHANGED.MinContainerSize);
        assertThat("presets[8] 'Listing Changed' => MaxContainerSize", 4.0f == FilterInstances.LISTINGCHANGED.MaxContainerSize);
        assertThat("presets[8] 'Listing Changed' => MinRating", 0.0f == FilterInstances.LISTINGCHANGED.MinRating);
        assertThat("presets[8] 'Listing Changed' => MaxRating", 5.0f == FilterInstances.LISTINGCHANGED.MaxRating);

        // CacheTypes
        assertThat("presets[8] 'Listing Changed' => cacheType.length", 23 == FilterInstances.LISTINGCHANGED.mCacheTypes.length);
        assertThat("presets[8] 'Listing Changed' => cacheType[0]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[0]);
        assertThat("presets[8] 'Listing Changed' => cacheType[1]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[1]);
        assertThat("presets[8] 'Listing Changed' => cacheType[2]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[2]);
        assertThat("presets[8] 'Listing Changed' => cacheType[3]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[3]);
        assertThat("presets[8] 'Listing Changed' => cacheType[4]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[4]);
        assertThat("presets[8] 'Listing Changed' => cacheType[5]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[5]);
        assertThat("presets[8] 'Listing Changed' => cacheType[6]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[6]);
        assertThat("presets[8] 'Listing Changed' => cacheType[7]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[7]);
        assertThat("presets[8] 'Listing Changed' => cacheType[8]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[8]);
        assertThat("presets[8] 'Listing Changed' => cacheType[9]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[9]);
        assertThat("presets[8] 'Listing Changed' => cacheType[10]''?", FilterInstances.LISTINGCHANGED.mCacheTypes[10]);
        assertThat("presets[8] 'Listing Changed' => cacheType[11]'Munzee'", FilterInstances.LISTINGCHANGED.mCacheTypes[11]);

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[8] 'Listing Changed' => attributesFilter.length", AtributeLength ==
                FilterInstances.LISTINGCHANGED.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[8] 'Listing Changed' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.LISTINGCHANGED.mAttributes[i]);
        }

        assertThat("presets[8] 'Listing Changed' => GPXFilenameIds.size", 0 == FilterInstances.LISTINGCHANGED.GPXFilenameIds.size);
        assertThat("presets[8] 'Listing Changed' => Categories.size", 0 == FilterInstances.LISTINGCHANGED.Categories.size);
        assertEquals("", FilterInstances.LISTINGCHANGED.filterName, "presets[8] 'Listing Changed' => filterName");
        assertEquals("", FilterInstances.LISTINGCHANGED.filterGcCode, "presets[8] 'Listing Changed' => filterGcCode");
        assertEquals("", FilterInstances.LISTINGCHANGED.filterOwner, "presets[8] 'Listing Changed' => filterOwner");


        assertEquals(
                "{\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,0,0,1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.LISTINGCHANGED.toString(), "presets[8] 'Listing Changed' =>toString");

        assertFilteredReadedDB(FilterInstances.LISTINGCHANGED);

    }

    @Test
    void checkDefault() {
        FilterProperties defaultCtor = new FilterProperties();

        assertThat("default constructor => Finds", 0 == defaultCtor.Finds.get());
        assertThat("default constructor => NotAvailable", 0 == defaultCtor.NotAvailable.get());
        assertThat("default constructor => Archived", 0 == defaultCtor.Archived.get());
        assertThat("default constructor => Own", 0 == defaultCtor.Own.get());
        assertThat("default constructor => ContainsTravelbugs", 0 == defaultCtor.ContainsTravelbugs.get());
        assertThat("default constructor => Favorites", 0 == defaultCtor.Favorites.get());
        assertThat("default constructor => HasUserData", 0 == defaultCtor.HasUserData.get());
        assertThat("default constructor => ListingChanged", 0 == defaultCtor.ListingChanged.get());
        assertThat("default constructor => WithManualWaypoint", 0 == defaultCtor.WithManualWaypoint.get());
        assertThat("default constructor => MinDifficulty", 1.0f == defaultCtor.MinDifficulty);
        assertThat("default constructor => MaxDifficulty", 5.0f == defaultCtor.MaxDifficulty);
        assertThat("default constructor => MinTerrain", 1.0f == defaultCtor.MinTerrain);
        assertThat("default constructor => MaxTerrain", 5.0f == defaultCtor.MaxTerrain);
        assertThat("default constructor => MinContainerSize", 0.0f == defaultCtor.MinContainerSize);
        assertThat("default constructor => MaxContainerSize", 4.0f == defaultCtor.MaxContainerSize);
        assertThat("default constructor => MinRating", 0.0f == defaultCtor.MinRating);
        assertThat("default constructor => MaxRating", 5.0f == defaultCtor.MaxRating);

        // CacheTypes
        assertThat("default constructor => cacheType.length", 23 == defaultCtor.mCacheTypes.length);
        assertThat("default constructor => cacheType[0]''?", defaultCtor.mCacheTypes[0]);
        assertThat("default constructor => cacheType[1]''?", defaultCtor.mCacheTypes[1]);
        assertThat("default constructor => cacheType[2]''?", defaultCtor.mCacheTypes[2]);
        assertThat("default constructor => cacheType[3]''?", defaultCtor.mCacheTypes[3]);
        assertThat("default constructor => cacheType[4]''?", defaultCtor.mCacheTypes[4]);
        assertThat("default constructor => cacheType[5]''?", defaultCtor.mCacheTypes[5]);
        assertThat("default constructor => cacheType[6]''?", defaultCtor.mCacheTypes[6]);
        assertThat("default constructor => cacheType[7]''?", defaultCtor.mCacheTypes[7]);
        assertThat("default constructor => cacheType[8]''?", defaultCtor.mCacheTypes[8]);
        assertThat("default constructor => cacheType[9]''?", defaultCtor.mCacheTypes[9]);
        assertThat("default constructor => cacheType[10]''?", defaultCtor.mCacheTypes[10]);
        assertThat("default constructor => cacheType[11]'Munzee'", defaultCtor.mCacheTypes[11]);

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("default constructor => attributesFilter.length", AtributeLength == defaultCtor.mAttributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("default constructor => attributesFilter[" + i + "]", attributesFilter[i] == defaultCtor.mAttributes[i]);
        }

        assertThat("default constructor => GPXFilenameIds.size", 0 == defaultCtor.GPXFilenameIds.size);
        assertThat("default constructor => Categories.size", 0 == defaultCtor.Categories.size);
        assertThat("default constructor => filterName", "" == defaultCtor.filterName);
        assertThat("default constructor => filterGcCode", "" == defaultCtor.filterGcCode);
        assertThat("default constructor => filterOwner", "" == defaultCtor.filterOwner);


        assertEquals(
                "{\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                defaultCtor.toString(), "default constructor =>toString");

        assertFilteredReadedDB(defaultCtor);
    }

    @Test
    void chehkSqlWhere() {

        FilterProperties[] filters = new FilterProperties[]{FilterInstances.ACTIVE, FilterInstances.ALL,
                FilterInstances.BEGINNER, FilterInstances.DROPTB, FilterInstances.FAVORITES, FilterInstances.HIGHLIGHTS,
                FilterInstances.LISTINGCHANGED, FilterInstances.QUICK, FilterInstances.TOARCHIVE, FilterInstances.WITHTB};


        String[] SqlStringList = new String[]
                {
                        "BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and (not Owner='User') and Difficulty >= 2.0 and Difficulty <= 10.0 and Terrain >= 2.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                        "Difficulty >= 2.0 and Difficulty <= 10.0 and Terrain >= 2.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22)",
                        "BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and (not Owner='User') and Difficulty >= 2.0 and Difficulty <= 4.0 and Terrain >= 2.0 and Terrain <= 4.0 and Size >= 2.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                        "BooleanStore & 8= 8 and ~BooleanStore & 4= 4 and Difficulty >= 2.0 and Difficulty <= 6.0 and Terrain >= 2.0 and Terrain <= 6.0 and Size >= 2.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,5,6,7,9,10,21) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                        "BooleanStore & 16= 16 and Difficulty >= 2.0 and Difficulty <= 10.0 and Terrain >= 2.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                        "BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and Difficulty >= 2.0 and Difficulty <= 10.0 and Terrain >= 2.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 350.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                        "BooleanStore & 512= 512 and Difficulty >= 2.0 and Difficulty <= 10.0 and Terrain >= 2.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                        "BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and (not Owner='User') and Difficulty >= 2.0 and Difficulty <= 5.0 and Terrain >= 2.0 and Terrain <= 5.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,3,4) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                        "~BooleanStore & 788= 788 and (not Owner='User') and Difficulty >= 2.0 and Difficulty <= 10.0 and Terrain >= 2.0 and Terrain <= 10.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )",
                        "BooleanStore & 8= 8 and ~BooleanStore & 4= 4 and NumTravelbugs > 0 and Difficulty >= 2.0 and Difficulty <= 6.0 and Terrain >= 2.0 and Terrain <= 6.0 and Size >= 0.0 and Size <= 4.0 and Rating >= 0.0 and Rating <= 500.0 and Type in (0,1,2,5,6,7,9,10,21) and name like '%%' and GcCode like '%%' and ( PlacedBy like '%%' or Owner like '%%' )"
                };

        for (int i = 0; i < filters.length; i++) {
            assertEquals(SqlStringList[i], filters[i].getSqlWhere("User"), "presets[" + i + "] '=>getSqlWhere(\"User\")");
        }
    }


    private void assertFilteredReadedDB(FilterProperties properties) {
        CacheList list = new CacheList();
        CacheList3DAO dao = new CacheList3DAO();
        dao.readCacheList(testDb, list, properties.getSqlWhere("NAME"), false, false);

        int n = list.size;
        while (n-- > 0) {
            AbstractCache cache = list.get(n);
            assertThat("Filter has to pass", properties.passed(cache));
        }
    }


}
