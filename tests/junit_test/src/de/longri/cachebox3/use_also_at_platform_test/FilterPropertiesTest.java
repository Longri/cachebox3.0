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
package de.longri.cachebox3.use_also_at_platform_test;


import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheList3DAO;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheList;
import de.longri.cachebox3.types.FilterInstances;
import de.longri.cachebox3.types.FilterProperties;
import de.longri.gdx.sqlite.SQLiteGdxException;
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
            FileHandle dbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/cacheboxFilterTestDB.db3", false);
            dbFileHandle.parent().mkdirs();
            testDb.startUp(dbFileHandle);
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
        assertThat("FilterInstances.ALL 'All Caches' => MinDifficulty", 1.0f == FilterInstances.ALL.MinDifficulty.get() / 2F);
        assertThat("FilterInstances.ALL 'All Caches' => MaxDifficulty", 5.0f == FilterInstances.ALL.MaxDifficulty.get() / 2F);
        assertThat("FilterInstances.ALL 'All Caches' => MinTerrain", 1.0f == FilterInstances.ALL.MinTerrain.get() / 2F);
        assertThat("FilterInstances.ALL 'All Caches' => MaxTerrain", 5.0f == FilterInstances.ALL.MaxTerrain.get() / 2F);
        assertThat("FilterInstances.ALL 'All Caches' => MinContainerSize", 0.0f == FilterInstances.ALL.MinContainerSize.get());
        assertThat("FilterInstances.ALL 'All Caches' => MaxContainerSize", 6.0f == FilterInstances.ALL.MaxContainerSize.get());
        assertThat("FilterInstances.ALL 'All Caches' => MinRating", 0.0f == FilterInstances.ALL.MinRating.get() / 2F);
        assertThat("FilterInstances.ALL 'All Caches' => MaxRating", 5.0f == FilterInstances.ALL.MaxRating.get() / 2F);
        assertThat("FilterInstances.ALL 'All Caches' => MinFavPoints", -1 == FilterInstances.ALL.MinFavPoints.get());
        assertThat("FilterInstances.ALL 'All Caches' => MinFavPoints", -1 == FilterInstances.ALL.MaxFavPoints.get());

        // CacheTypes
        assertThat("FilterInstances.ALL 'All Caches' => cacheType.length", 26 == FilterInstances.ALL.cacheTypes.length);
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[0]''?", FilterInstances.ALL.cacheTypes[0].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[1]''?", FilterInstances.ALL.cacheTypes[1].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[2]''?", FilterInstances.ALL.cacheTypes[2].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[3]''?", FilterInstances.ALL.cacheTypes[3].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[4]''?", FilterInstances.ALL.cacheTypes[4].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[5]''?", FilterInstances.ALL.cacheTypes[5].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[6]''?", FilterInstances.ALL.cacheTypes[6].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[7]''?", FilterInstances.ALL.cacheTypes[7].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[8]''?", FilterInstances.ALL.cacheTypes[8].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[9]''?", FilterInstances.ALL.cacheTypes[9].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[10]''?", FilterInstances.ALL.cacheTypes[10].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[12]'GIGA'", FilterInstances.ALL.cacheTypes[21].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("FilterInstances.ALL 'All Caches' => attributesFilter.length", AtributeLength ==
                FilterInstances.ALL.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("FilterInstances.ALL 'All Caches' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.ALL.attributes[i].get());
        }

        assertThat("FilterInstances.ALL 'All Caches' => GPXFilenameIds.size", 0 == FilterInstances.ALL.GPXFilenameIds.size);
        assertThat("FilterInstances.ALL 'All Caches' => Categories.size", 0 == FilterInstances.ALL.Categories.size);
        assertEquals("", FilterInstances.ALL.filterName, "FilterInstances.ALL 'All Caches' => filterName");
        assertEquals("", FilterInstances.ALL.filterGcCode, "FilterInstances.ALL 'All Caches' => filterGcCode");
        assertEquals("", FilterInstances.ALL.filterOwner, "FilterInstances.ALL 'All Caches' => filterOwner");


        assertEquals("{\"name\":\"ALL\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.ALL.getJsonString(),
                "FilterInstances.ALL 'All Caches' =>toString");

        assertEquals("SELECT * FROM CacheCoreInfo",
                FilterInstances.ALL.getSqlWhere("NAME"), "SqlWhere must Equals");


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
        assertThat("presets[1] 'All Caches to find' => MinDifficulty", 1.0f == FilterInstances.ACTIVE.MinDifficulty.get() / 2F);
        assertThat("presets[1] 'All Caches to find' => MaxDifficulty", 5.0f == FilterInstances.ACTIVE.MaxDifficulty.get() / 2F);
        assertThat("presets[1] 'All Caches to find' => MinTerrain", 1.0f == FilterInstances.ACTIVE.MinTerrain.get() / 2F);
        assertThat("presets[1] 'All Caches to find' => MaxTerrain", 5.0f == FilterInstances.ACTIVE.MaxTerrain.get() / 2F);
        assertThat("presets[1] 'All Caches to find' => MinContainerSize", 0.0f == FilterInstances.ACTIVE.MinContainerSize.get());
        assertThat("presets[1] 'All Caches to find' => MaxContainerSize", 6.0f == FilterInstances.ACTIVE.MaxContainerSize.get());
        assertThat("presets[1] 'All Caches to find' => MinRating", 0.0f == FilterInstances.ACTIVE.MinRating.get() / 2F);
        assertThat("presets[1] 'All Caches to find' => MaxRating", 5.0f == FilterInstances.ACTIVE.MaxRating.get() / 2F);
        assertThat("presets[1] 'All Caches to find' => MinFavPoints", -1 == FilterInstances.ACTIVE.MinFavPoints.get());
        assertThat("presets[1] 'All Caches to find' => MinFavPoints", -1 == FilterInstances.ACTIVE.MaxFavPoints.get());

        // CacheTypes
        assertThat("presets[1] 'All Caches to find' => cacheType.length", 26 == FilterInstances.ACTIVE.cacheTypes.length);
        assertThat("presets[1] 'All Caches to find' => cacheType[0]''?", FilterInstances.ACTIVE.cacheTypes[0].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[1]''?", FilterInstances.ACTIVE.cacheTypes[1].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[2]''?", FilterInstances.ACTIVE.cacheTypes[2].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[3]''?", FilterInstances.ACTIVE.cacheTypes[3].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[4]''?", FilterInstances.ACTIVE.cacheTypes[4].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[5]''?", FilterInstances.ACTIVE.cacheTypes[5].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[6]''?", FilterInstances.ACTIVE.cacheTypes[6].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[7]''?", FilterInstances.ACTIVE.cacheTypes[7].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[8]''?", FilterInstances.ACTIVE.cacheTypes[8].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[9]''?", FilterInstances.ACTIVE.cacheTypes[9].get());
        assertThat("presets[1] 'All Caches to find' => cacheType[10]''?", FilterInstances.ACTIVE.cacheTypes[10].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[12]'GIGA'", FilterInstances.ACTIVE.cacheTypes[21].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[1] 'All Caches to find' => attributesFilter.length", AtributeLength ==
                FilterInstances.ACTIVE.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[1] 'All Caches to find' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.ACTIVE.attributes[i].get());
        }

        assertThat("presets[1] 'All Caches to find' => GPXFilenameIds.size", 0 == FilterInstances.ACTIVE.GPXFilenameIds.size);
        assertThat("presets[1] 'All Caches to find' => Categories.size", 0 == FilterInstances.ACTIVE.Categories.size);
        assertEquals("", FilterInstances.ACTIVE.filterName, "presets[1] 'All Caches to find' => filterName");
        assertEquals("", FilterInstances.ACTIVE.filterGcCode, "presets[1] 'All Caches to find' => filterGcCode");
        assertEquals("", FilterInstances.ACTIVE.filterOwner, "presets[1] 'All Caches to find' => filterOwner");

        assertEquals("{\"name\":\"ACTIVE\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.ACTIVE.getJsonString(),
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
        assertThat("presets[2] 'Quick Cache' => MinDifficulty", 1.0f == FilterInstances.QUICK.MinDifficulty.get() / 2F);
        assertThat("presets[2] 'Quick Cache' => MaxDifficulty", 2.5f == FilterInstances.QUICK.MaxDifficulty.get() / 2F);
        assertThat("presets[2] 'Quick Cache' => MinTerrain", 1.0f == FilterInstances.QUICK.MinTerrain.get() / 2F);
        assertThat("presets[2] 'Quick Cache' => MaxTerrain", 2.5f == FilterInstances.QUICK.MaxTerrain.get() / 2F);
        assertThat("presets[2] 'Quick Cache' => MinContainerSize", 0.0f == FilterInstances.QUICK.MinContainerSize.get());
        assertThat("presets[2] 'Quick Cache' => MaxContainerSize", 6.0f == FilterInstances.QUICK.MaxContainerSize.get());
        assertThat("presets[2] 'Quick Cache' => MinRating", 0.0f == FilterInstances.QUICK.MinRating.get() / 2F);
        assertThat("presets[2] 'Quick Cache' => MaxRating", 5.0f == FilterInstances.QUICK.MaxRating.get() / 2F);
        assertThat("presets[2] 'Quick Cache' => MinFavPoints", -1 == FilterInstances.QUICK.MinFavPoints.get());
        assertThat("presets[2] 'Quick Cache' => MinFavPoints", -1 == FilterInstances.QUICK.MaxFavPoints.get());


        // CacheTypes
        assertThat("presets[2] 'Quick Cache' => cacheType.length", 26 == FilterInstances.QUICK.cacheTypes.length);
        assertThat("presets[2] 'Quick Cache' => cacheType[0]''?", FilterInstances.QUICK.cacheTypes[0].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[1]''?", !FilterInstances.QUICK.cacheTypes[1].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[2]''?", !FilterInstances.QUICK.cacheTypes[2].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[3]''?", FilterInstances.QUICK.cacheTypes[3].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[4]''?", FilterInstances.QUICK.cacheTypes[4].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[5]''?", !FilterInstances.QUICK.cacheTypes[5].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[6]''?", !FilterInstances.QUICK.cacheTypes[6].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[7]''?", !FilterInstances.QUICK.cacheTypes[7].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[8]''?", !FilterInstances.QUICK.cacheTypes[8].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[9]''?", !FilterInstances.QUICK.cacheTypes[9].get());
        assertThat("presets[2] 'Quick Cache' => cacheType[10]''?", !FilterInstances.QUICK.cacheTypes[10].get());
        assertThat("FilterInstances.ALL 'All Caches' => cacheType[12]'GIGA'", !FilterInstances.QUICK.cacheTypes[21].get());


        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[2] 'Quick Cache' => attributesFilter.length", AtributeLength ==
                FilterInstances.QUICK.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[2] 'Quick Cache' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.QUICK.attributes[i].get());
        }

        assertThat("presets[2] 'Quick Cache' => GPXFilenameIds.size", 0 == FilterInstances.QUICK.GPXFilenameIds.size);
        assertThat("presets[2] 'Quick Cache' => Categories.size", 0 == FilterInstances.QUICK.Categories.size);
        assertEquals("", FilterInstances.QUICK.filterName, "presets[2] 'Quick Cache' => filterName");
        assertEquals("", FilterInstances.QUICK.filterGcCode, "presets[2] 'Quick Cache' => filterGcCode");
        assertEquals("", FilterInstances.QUICK.filterOwner, "presets[2] 'Quick Cache' => filterOwner");


        assertEquals(
                "{\"name\":\"QUICK\",\"types\":\"true,false,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false\",\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,2.5,1.0,2.5,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.QUICK.getJsonString(),
                "presets[2] 'Quick Cache' =>toString");

        assertEquals("SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and (not Owner='NAME') and Difficulty <= 5 and Terrain <= 5 and Type in (0,3,4)",
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
        assertThat("presets[3] 'Fetch some Travelbugs' => MinDifficulty", 1.0f == FilterInstances.WITHTB.MinDifficulty.get() / 2F);
        assertThat("presets[3] 'Fetch some Travelbugs' => MaxDifficulty", 3.0f == FilterInstances.WITHTB.MaxDifficulty.get() / 2F);
        assertThat("presets[3] 'Fetch some Travelbugs' => MinTerrain", 1.0f == FilterInstances.WITHTB.MinTerrain.get() / 2F);
        assertThat("presets[3] 'Fetch some Travelbugs' => MaxTerrain", 3.0f == FilterInstances.WITHTB.MaxTerrain.get() / 2F);
        assertThat("presets[3] 'Fetch some Travelbugs' => MinContainerSize", 0.0f == FilterInstances.WITHTB.MinContainerSize.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => MaxContainerSize", 6.0f == FilterInstances.WITHTB.MaxContainerSize.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => MinRating", 0.0f == FilterInstances.WITHTB.MinRating.get() / 2F);
        assertThat("presets[3] 'Fetch some Travelbugs' => MaxRating", 5.0f == FilterInstances.WITHTB.MaxRating.get() / 2F);
        assertThat("presets[3] 'Fetch some Travelbugs' => MinFavPoints", -1 == FilterInstances.WITHTB.MinFavPoints.get());
        assertThat("presets[3] 'Fetch some Travelbugs' => MinFavPoints", -1 == FilterInstances.WITHTB.MaxFavPoints.get());


        // CacheTypes
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType.length", 26 == FilterInstances.WITHTB.cacheTypes.length);
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[0]''?", FilterInstances.WITHTB.cacheTypes[0].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[1]''?", FilterInstances.WITHTB.cacheTypes[1].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[2]''?", FilterInstances.WITHTB.cacheTypes[2].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[3]''?", !FilterInstances.WITHTB.cacheTypes[3].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[4]''?", !FilterInstances.WITHTB.cacheTypes[4].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[5]''?", FilterInstances.WITHTB.cacheTypes[5].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[6]''?", FilterInstances.WITHTB.cacheTypes[6].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[7]''?", FilterInstances.WITHTB.cacheTypes[7].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[8]''?", !FilterInstances.WITHTB.cacheTypes[8].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[9]''?", FilterInstances.WITHTB.cacheTypes[9].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[10]''?", FilterInstances.WITHTB.cacheTypes[10].get());
        assertThat("presets[3] 'Fetch some Travelbugs' => cacheType[22]'GIGA'", FilterInstances.WITHTB.cacheTypes[21].get());


        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[3] 'Fetch some Travelbugs' => attributesFilter.length", AtributeLength ==
                FilterInstances.QUICK.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[3] 'Fetch some Travelbugs' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.QUICK.attributes[i].get());
        }

        assertThat("presets[3] 'Fetch some Travelbugs' => GPXFilenameIds.size", 0 == FilterInstances.WITHTB.GPXFilenameIds.size);
        assertThat("presets[3] 'Fetch some Travelbugs' => Categories.size", 0 == FilterInstances.WITHTB.Categories.size);
        assertEquals("", FilterInstances.WITHTB.filterName, "presets[3] 'Fetch some Travelbugs' => filterName");
        assertEquals("", FilterInstances.WITHTB.filterGcCode, "presets[3] 'Fetch some Travelbugs' => filterGcCode");
        assertEquals("", FilterInstances.WITHTB.filterOwner, "presets[3] 'Fetch some Travelbugs' => filterOwner");


        assertEquals(
                "{\"name\":\"WITHTB\",\"types\":\"true,true,true,false,false,true,true,true,false,true,true,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false\",\"caches\":\"0,-1,-1,0,1,0,0,0,0,1.0,3.0,1.0,3.0,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.WITHTB.getJsonString(), "presets[3] 'Fetch some Travelbugs' =>toString");

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
        assertThat("presets[4] 'Drop off Travelbugs' => MinDifficulty", 1.0f == FilterInstances.DROPTB.MinDifficulty.get() / 2F);
        assertThat("presets[4] 'Drop off Travelbugs' => MaxDifficulty", 3.0f == FilterInstances.DROPTB.MaxDifficulty.get() / 2F);
        assertThat("presets[4] 'Drop off Travelbugs' => MinTerrain", 1.0f == FilterInstances.DROPTB.MinTerrain.get() / 2F);
        assertThat("presets[4] 'Drop off Travelbugs' => MaxTerrain", 3.0f == FilterInstances.DROPTB.MaxTerrain.get() / 2F);
        assertThat("presets[4] 'Drop off Travelbugs' => MinContainerSize", 1.0f == FilterInstances.DROPTB.MinContainerSize.get());
        assertThat("presets[4] 'Drop off Travelbugs' => MaxContainerSize", 6.0f == FilterInstances.DROPTB.MaxContainerSize.get());
        assertThat("presets[4] 'Drop off Travelbugs' => MinRating", 0.0f == FilterInstances.DROPTB.MinRating.get() / 2F);
        assertThat("presets[4] 'Drop off Travelbugs' => MaxRating", 5.0f == FilterInstances.DROPTB.MaxRating.get() / 2F);
        assertThat("presets[4] 'Drop off Travelbugs' => MinFavPoints", -1 == FilterInstances.DROPTB.MinFavPoints.get());
        assertThat("presets[4] 'Drop off Travelbugs' => MinFavPoints", -1 == FilterInstances.DROPTB.MaxFavPoints.get());

        // CacheTypes
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType.length", 26 == FilterInstances.DROPTB.cacheTypes.length);
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[0]''?", FilterInstances.DROPTB.cacheTypes[0].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[1]''?", FilterInstances.DROPTB.cacheTypes[1].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[2]''?", FilterInstances.DROPTB.cacheTypes[2].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[3]''?", !FilterInstances.DROPTB.cacheTypes[3].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[4]''?", !FilterInstances.DROPTB.cacheTypes[4].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[5]''?", FilterInstances.DROPTB.cacheTypes[5].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[6]''?", FilterInstances.DROPTB.cacheTypes[6].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[7]''?", FilterInstances.DROPTB.cacheTypes[7].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[8]''?", !FilterInstances.DROPTB.cacheTypes[8].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[9]''?", FilterInstances.DROPTB.cacheTypes[9].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[10]''?", FilterInstances.DROPTB.cacheTypes[10].get());
        assertThat("presets[4] 'Drop off Travelbugs' => cacheType[11]'Giga'", FilterInstances.DROPTB.cacheTypes[21].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[4] 'Drop off Travelbugs' => attributesFilter.length", AtributeLength ==
                FilterInstances.DROPTB.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[4] 'Drop off Travelbugs' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.DROPTB.attributes[i].get());
        }

        assertThat("presets[4] 'Drop off Travelbugs' => GPXFilenameIds.size", 0 == FilterInstances.DROPTB.GPXFilenameIds.size);
        assertThat("presets[4] 'Drop off Travelbugs' => Categories.size", 0 == FilterInstances.DROPTB.Categories.size);
        assertEquals("", FilterInstances.DROPTB.filterName, "presets[4] 'Drop off Travelbugs' => filterName");
        assertEquals("", FilterInstances.DROPTB.filterGcCode, "presets[4] 'Drop off Travelbugs' => filterGcCode");
        assertEquals("", FilterInstances.DROPTB.filterOwner, "presets[4] 'Drop off Travelbugs' => filterOwner");


        assertEquals("{\"name\":\"DROPTB\",\"types\":\"true,true,true,false,false,true,true,true,false,true,true,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false\",\"caches\":\"0,-1,-1,0,0,0,0,0,0,1.0,3.0,1.0,3.0,1.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.DROPTB.getJsonString(),
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
        assertThat("presets[5] 'Highlights' => MinDifficulty", 1.0f == FilterInstances.HIGHLIGHTS.MinDifficulty.get() / 2F);
        assertThat("presets[5] 'Highlights' => MaxDifficulty", 5.0f == FilterInstances.HIGHLIGHTS.MaxDifficulty.get() / 2F);
        assertThat("presets[5] 'Highlights' => MinTerrain", 1.0f == FilterInstances.HIGHLIGHTS.MinTerrain.get() / 2F);
        assertThat("presets[5] 'Highlights' => MaxTerrain", 5.0f == FilterInstances.HIGHLIGHTS.MaxTerrain.get() / 2F);
        assertThat("presets[5] 'Highlights' => MinContainerSize", 0.0f == FilterInstances.HIGHLIGHTS.MinContainerSize.get());
        assertThat("presets[5] 'Highlights' => MaxContainerSize", 6.0f == FilterInstances.HIGHLIGHTS.MaxContainerSize.get());
        assertThat("presets[5] 'Highlights' => MinRating", 3.5f == FilterInstances.HIGHLIGHTS.MinRating.get() / 2F);
        assertThat("presets[5] 'Highlights' => MaxRating", 5.0f == FilterInstances.HIGHLIGHTS.MaxRating.get() / 2F);
        assertThat("presets[5] 'Highlights' => MinFavPoints", 50 == FilterInstances.HIGHLIGHTS.MinFavPoints.get());
        assertThat("presets[5] 'Highlights' => MinFavPoints", -1 == FilterInstances.HIGHLIGHTS.MaxFavPoints.get());


        // CacheTypes
        assertThat("presets[5] 'Highlights' => cacheType.length", 26 == FilterInstances.HIGHLIGHTS.cacheTypes.length);
        assertThat("presets[5] 'Highlights' => cacheType[0]''?", FilterInstances.HIGHLIGHTS.cacheTypes[0].get());
        assertThat("presets[5] 'Highlights' => cacheType[1]''?", FilterInstances.HIGHLIGHTS.cacheTypes[1].get());
        assertThat("presets[5] 'Highlights' => cacheType[2]''?", FilterInstances.HIGHLIGHTS.cacheTypes[2].get());
        assertThat("presets[5] 'Highlights' => cacheType[3]''?", FilterInstances.HIGHLIGHTS.cacheTypes[3].get());
        assertThat("presets[5] 'Highlights' => cacheType[4]''?", FilterInstances.HIGHLIGHTS.cacheTypes[4].get());
        assertThat("presets[5] 'Highlights' => cacheType[5]''?", FilterInstances.HIGHLIGHTS.cacheTypes[5].get());
        assertThat("presets[5] 'Highlights' => cacheType[6]''?", FilterInstances.HIGHLIGHTS.cacheTypes[6].get());
        assertThat("presets[5] 'Highlights' => cacheType[7]''?", FilterInstances.HIGHLIGHTS.cacheTypes[7].get());
        assertThat("presets[5] 'Highlights' => cacheType[8]''?", FilterInstances.HIGHLIGHTS.cacheTypes[8].get());
        assertThat("presets[5] 'Highlights' => cacheType[9]''?", FilterInstances.HIGHLIGHTS.cacheTypes[9].get());
        assertThat("presets[5] 'Highlights' => cacheType[10]''?", FilterInstances.HIGHLIGHTS.cacheTypes[10].get());
        assertThat("presets[5] 'Highlights' => cacheType[11]'Munzee'", FilterInstances.HIGHLIGHTS.cacheTypes[11].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[5] 'Highlights' => attributesFilter.length", AtributeLength ==
                FilterInstances.HIGHLIGHTS.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[5] 'Highlights' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.HIGHLIGHTS.attributes[i].get());
        }

        assertThat("presets[5] 'Highlights' => GPXFilenameIds.size", 0 == FilterInstances.HIGHLIGHTS.GPXFilenameIds.size);
        assertThat("presets[5] 'Highlights' => Categories.size", 0 == FilterInstances.HIGHLIGHTS.Categories.size);
        assertEquals("", FilterInstances.HIGHLIGHTS.filterName, "presets[5] 'Highlights' => filterName");
        assertEquals("", FilterInstances.HIGHLIGHTS.filterGcCode, "presets[5] 'Highlights' => filterGcCode");
        assertEquals("", FilterInstances.HIGHLIGHTS.filterOwner, "presets[5] 'Highlights' => filterOwner");


        assertEquals(
                "{\"name\":\"HIGHLIGHTS\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"-1,-1,-1,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,6.0,3.5,5.0,50,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.HIGHLIGHTS.getJsonString(),
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
        assertThat("presets[6] 'Favoriten' => MinDifficulty", 1.0f == FilterInstances.FAVORITES.MinDifficulty.get() / 2F);
        assertThat("presets[6] 'Favoriten' => MaxDifficulty", 5.0f == FilterInstances.FAVORITES.MaxDifficulty.get() / 2F);
        assertThat("presets[6] 'Favoriten' => MinTerrain", 1.0f == FilterInstances.FAVORITES.MinTerrain.get() / 2F);
        assertThat("presets[6] 'Favoriten' => MaxTerrain", 5.0f == FilterInstances.FAVORITES.MaxTerrain.get() / 2F);
        assertThat("presets[6] 'Favoriten' => MinContainerSize", 0.0f == FilterInstances.FAVORITES.MinContainerSize.get());
        assertThat("presets[6] 'Favoriten' => MaxContainerSize", 6.0f == FilterInstances.FAVORITES.MaxContainerSize.get());
        assertThat("presets[6] 'Favoriten' => MinRating", 0.0f == FilterInstances.FAVORITES.MinRating.get() / 2F);
        assertThat("presets[6] 'Favoriten' => MaxRating", 5.0f == FilterInstances.FAVORITES.MaxRating.get() / 2F);
        assertThat("presets[6] 'Favoriten' => MinFavPoints", -1 == FilterInstances.FAVORITES.MinFavPoints.get());
        assertThat("presets[6] 'Favoriten' => MinFavPoints", -1 == FilterInstances.FAVORITES.MaxFavPoints.get());

        // CacheTypes
        assertThat("presets[6] 'Favoriten' => cacheType.length", 26 == FilterInstances.FAVORITES.cacheTypes.length);
        assertThat("presets[6] 'Favoriten' => cacheType[0]''?", FilterInstances.FAVORITES.cacheTypes[0].get());
        assertThat("presets[6] 'Favoriten' => cacheType[1]''?", FilterInstances.FAVORITES.cacheTypes[1].get());
        assertThat("presets[6] 'Favoriten' => cacheType[2]''?", FilterInstances.FAVORITES.cacheTypes[2].get());
        assertThat("presets[6] 'Favoriten' => cacheType[3]''?", FilterInstances.FAVORITES.cacheTypes[3].get());
        assertThat("presets[6] 'Favoriten' => cacheType[4]''?", FilterInstances.FAVORITES.cacheTypes[4].get());
        assertThat("presets[6] 'Favoriten' => cacheType[5]''?", FilterInstances.FAVORITES.cacheTypes[5].get());
        assertThat("presets[6] 'Favoriten' => cacheType[6]''?", FilterInstances.FAVORITES.cacheTypes[6].get());
        assertThat("presets[6] 'Favoriten' => cacheType[7]''?", FilterInstances.FAVORITES.cacheTypes[7].get());
        assertThat("presets[6] 'Favoriten' => cacheType[8]''?", FilterInstances.FAVORITES.cacheTypes[8].get());
        assertThat("presets[6] 'Favoriten' => cacheType[9]''?", FilterInstances.FAVORITES.cacheTypes[9].get());
        assertThat("presets[6] 'Favoriten' => cacheType[10]''?", FilterInstances.FAVORITES.cacheTypes[10].get());
        assertThat("presets[6] 'Favoriten' => cacheType[11]'Munzee'", FilterInstances.FAVORITES.cacheTypes[11].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[6] 'Favoriten' => attributesFilter.length", AtributeLength ==
                FilterInstances.FAVORITES.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[6] 'Favoriten' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.FAVORITES.attributes[i].get());
        }

        assertThat("presets[6] 'Favoriten' => GPXFilenameIds.size", 0 == FilterInstances.FAVORITES.GPXFilenameIds.size);
        assertThat("presets[6] 'Favoriten' => Categories.size", 0 == FilterInstances.FAVORITES.Categories.size);
        assertEquals("", FilterInstances.FAVORITES.filterName, "presets[6] 'Favoriten' => filterName");
        assertEquals("", FilterInstances.FAVORITES.filterGcCode, "presets[6] 'Favoriten' => filterGcCode");
        assertEquals("", FilterInstances.FAVORITES.filterOwner, "presets[6] 'Favoriten' => filterOwner");


        assertEquals(
                "{\"name\":\"FAVORITES\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,1,0,0,0,1.0,5.0,1.0,5.0,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.FAVORITES.getJsonString(),
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
        assertThat("presets[7] 'prepare to archive' => MinDifficulty", 1.0f == FilterInstances.TOARCHIVE.MinDifficulty.get() / 2F);
        assertThat("presets[7] 'prepare to archive' => MaxDifficulty", 5.0f == FilterInstances.TOARCHIVE.MaxDifficulty.get() / 2F);
        assertThat("presets[7] 'prepare to archive' => MinTerrain", 1.0f == FilterInstances.TOARCHIVE.MinTerrain.get() / 2F);
        assertThat("presets[7] 'prepare to archive' => MaxTerrain", 5.0f == FilterInstances.TOARCHIVE.MaxTerrain.get() / 2F);
        assertThat("presets[7] 'prepare to archive' => MinContainerSize", 0.0f == FilterInstances.TOARCHIVE.MinContainerSize.get());
        assertThat("presets[7] 'prepare to archive' => MaxContainerSize", 6.0f == FilterInstances.TOARCHIVE.MaxContainerSize.get());
        assertThat("presets[7] 'prepare to archive' => MinRating", 0.0f == FilterInstances.TOARCHIVE.MinRating.get() / 2F);
        assertThat("presets[7] 'prepare to archive' => MaxRating", 5.0f == FilterInstances.TOARCHIVE.MaxRating.get() / 2F);
        assertThat("presets[7] 'prepare to archive' => MinFavPoints", -1 == FilterInstances.TOARCHIVE.MinFavPoints.get());
        assertThat("presets[7] 'prepare to archive' => MinFavPoints", -1 == FilterInstances.TOARCHIVE.MaxFavPoints.get());

        // CacheTypes
        assertThat("presets[7] 'prepare to archive' => cacheType.length", 26 == FilterInstances.TOARCHIVE.cacheTypes.length);
        assertThat("presets[7] 'prepare to archive' => cacheType[0]''?", FilterInstances.TOARCHIVE.cacheTypes[0].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[1]''?", FilterInstances.TOARCHIVE.cacheTypes[1].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[2]''?", FilterInstances.TOARCHIVE.cacheTypes[2].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[3]''?", FilterInstances.TOARCHIVE.cacheTypes[3].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[4]''?", FilterInstances.TOARCHIVE.cacheTypes[4].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[5]''?", FilterInstances.TOARCHIVE.cacheTypes[5].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[6]''?", FilterInstances.TOARCHIVE.cacheTypes[6].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[7]''?", FilterInstances.TOARCHIVE.cacheTypes[7].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[8]''?", FilterInstances.TOARCHIVE.cacheTypes[8].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[9]''?", FilterInstances.TOARCHIVE.cacheTypes[9].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[10]''?", FilterInstances.TOARCHIVE.cacheTypes[10].get());
        assertThat("presets[7] 'prepare to archive' => cacheType[11]'Munzee'", FilterInstances.TOARCHIVE.cacheTypes[11].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[7] 'prepare to archive' => attributesFilter.length", AtributeLength ==
                FilterInstances.TOARCHIVE.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[7] 'prepare to archive' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.TOARCHIVE.attributes[i].get());
        }

        assertThat("presets[7] 'prepare to archive' => GPXFilenameIds.size", 0 == FilterInstances.TOARCHIVE.GPXFilenameIds.size);
        assertThat("presets[7] 'prepare to archive' => Categories.size", 0 == FilterInstances.TOARCHIVE.Categories.size);
        assertEquals("", FilterInstances.TOARCHIVE.filterName, "presets[7] 'prepare to archive' => filterName");
        assertEquals("", FilterInstances.TOARCHIVE.filterGcCode, "presets[7] 'prepare to archive' => filterGcCode");
        assertEquals("", FilterInstances.TOARCHIVE.filterOwner, "presets[7] 'prepare to archive' => filterOwner");


        assertEquals(
                "{\"name\":\"TOARCHIVE\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,-1,-1,0,-1,-1,-1,0,1.0,5.0,1.0,5.0,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.TOARCHIVE.getJsonString(), "presets[7] 'prepare to archive' =>toString");

        assertEquals("SELECT * FROM CacheCoreInfo core WHERE ~BooleanStore & 788= 788 and (not Owner='NAME')",
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
        assertThat("presets[8] 'Listing Changed' => MinDifficulty", 1.0f == FilterInstances.LISTINGCHANGED.MinDifficulty.get() / 2F);
        assertThat("presets[8] 'Listing Changed' => MaxDifficulty", 5.0f == FilterInstances.LISTINGCHANGED.MaxDifficulty.get() / 2F);
        assertThat("presets[8] 'Listing Changed' => MinTerrain", 1.0f == FilterInstances.LISTINGCHANGED.MinTerrain.get() / 2F);
        assertThat("presets[8] 'Listing Changed' => MaxTerrain", 5.0f == FilterInstances.LISTINGCHANGED.MaxTerrain.get() / 2F);
        assertThat("presets[8] 'Listing Changed' => MinContainerSize", 0.0f == FilterInstances.LISTINGCHANGED.MinContainerSize.get());
        assertThat("presets[8] 'Listing Changed' => MaxContainerSize", 6.0f == FilterInstances.LISTINGCHANGED.MaxContainerSize.get());
        assertThat("presets[8] 'Listing Changed' => MinRating", 0.0f == FilterInstances.LISTINGCHANGED.MinRating.get() / 2F);
        assertThat("presets[8] 'Listing Changed' => MaxRating", 5.0f == FilterInstances.LISTINGCHANGED.MaxRating.get() / 2F);
        assertThat("presets[8] 'Listing Changed' => MinFavPoints", -1 == FilterInstances.LISTINGCHANGED.MinFavPoints.get());
        assertThat("presets[8] 'Listing Changed' => MinFavPoints", -1 == FilterInstances.LISTINGCHANGED.MaxFavPoints.get());

        // CacheTypes
        assertThat("presets[8] 'Listing Changed' => cacheType.length", 26 == FilterInstances.LISTINGCHANGED.cacheTypes.length);
        assertThat("presets[8] 'Listing Changed' => cacheType[0]''?", FilterInstances.LISTINGCHANGED.cacheTypes[0].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[1]''?", FilterInstances.LISTINGCHANGED.cacheTypes[1].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[2]''?", FilterInstances.LISTINGCHANGED.cacheTypes[2].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[3]''?", FilterInstances.LISTINGCHANGED.cacheTypes[3].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[4]''?", FilterInstances.LISTINGCHANGED.cacheTypes[4].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[5]''?", FilterInstances.LISTINGCHANGED.cacheTypes[5].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[6]''?", FilterInstances.LISTINGCHANGED.cacheTypes[6].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[7]''?", FilterInstances.LISTINGCHANGED.cacheTypes[7].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[8]''?", FilterInstances.LISTINGCHANGED.cacheTypes[8].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[9]''?", FilterInstances.LISTINGCHANGED.cacheTypes[9].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[10]''?", FilterInstances.LISTINGCHANGED.cacheTypes[10].get());
        assertThat("presets[8] 'Listing Changed' => cacheType[11]'Munzee'", FilterInstances.LISTINGCHANGED.cacheTypes[11].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("presets[8] 'Listing Changed' => attributesFilter.length", AtributeLength ==
                FilterInstances.LISTINGCHANGED.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("presets[8] 'Listing Changed' => attributesFilter[" + i + "]", attributesFilter[i] ==
                    FilterInstances.LISTINGCHANGED.attributes[i].get());
        }

        assertThat("presets[8] 'Listing Changed' => GPXFilenameIds.size", 0 == FilterInstances.LISTINGCHANGED.GPXFilenameIds.size);
        assertThat("presets[8] 'Listing Changed' => Categories.size", 0 == FilterInstances.LISTINGCHANGED.Categories.size);
        assertEquals("", FilterInstances.LISTINGCHANGED.filterName, "presets[8] 'Listing Changed' => filterName");
        assertEquals("", FilterInstances.LISTINGCHANGED.filterGcCode, "presets[8] 'Listing Changed' => filterGcCode");
        assertEquals("", FilterInstances.LISTINGCHANGED.filterOwner, "presets[8] 'Listing Changed' => filterOwner");


        assertEquals(
                "{\"name\":\"LISTINGCHANGED\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,0,0,1,0,1.0,5.0,1.0,5.0,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                FilterInstances.LISTINGCHANGED.getJsonString(), "presets[8] 'Listing Changed' =>toString");

        assertFilteredReadedDB(FilterInstances.LISTINGCHANGED);

    }

    @Test
    void checkDefault() {
        FilterProperties defaultCtor = new FilterProperties("ALL");

        assertThat("default constructor => Finds", 0 == defaultCtor.Finds.get());
        assertThat("default constructor => NotAvailable", 0 == defaultCtor.NotAvailable.get());
        assertThat("default constructor => Archived", 0 == defaultCtor.Archived.get());
        assertThat("default constructor => Own", 0 == defaultCtor.Own.get());
        assertThat("default constructor => ContainsTravelbugs", 0 == defaultCtor.ContainsTravelbugs.get());
        assertThat("default constructor => Favorites", 0 == defaultCtor.Favorites.get());
        assertThat("default constructor => HasUserData", 0 == defaultCtor.HasUserData.get());
        assertThat("default constructor => ListingChanged", 0 == defaultCtor.ListingChanged.get());
        assertThat("default constructor => WithManualWaypoint", 0 == defaultCtor.WithManualWaypoint.get());
        assertThat("default constructor => MinDifficulty", 1.0f == defaultCtor.MinDifficulty.get() / 2F);
        assertThat("default constructor => MaxDifficulty", 5.0f == defaultCtor.MaxDifficulty.get() / 2F);
        assertThat("default constructor => MinTerrain", 1.0f == defaultCtor.MinTerrain.get() / 2F);
        assertThat("default constructor => MaxTerrain", 5.0f == defaultCtor.MaxTerrain.get() / 2F);
        assertThat("default constructor => MinContainerSize", 0.0f == defaultCtor.MinContainerSize.get());
        assertThat("default constructor => MaxContainerSize", 6.0f == defaultCtor.MaxContainerSize.get());
        assertThat("default constructor => MinRating", 0.0f == defaultCtor.MinRating.get() / 2F);
        assertThat("default constructor => MaxRating", 5.0f == defaultCtor.MaxRating.get() / 2F);
        assertThat("default constructor => MinFavPoints", -1 == defaultCtor.MinFavPoints.get());
        assertThat("default constructor => MinFavPoints", -1 == defaultCtor.MaxFavPoints.get());

        // CacheTypes
        assertThat("default constructor => cacheType.length", 26 == defaultCtor.cacheTypes.length);
        assertThat("default constructor => cacheType[0]''?", defaultCtor.cacheTypes[0].get());
        assertThat("default constructor => cacheType[1]''?", defaultCtor.cacheTypes[1].get());
        assertThat("default constructor => cacheType[2]''?", defaultCtor.cacheTypes[2].get());
        assertThat("default constructor => cacheType[3]''?", defaultCtor.cacheTypes[3].get());
        assertThat("default constructor => cacheType[4]''?", defaultCtor.cacheTypes[4].get());
        assertThat("default constructor => cacheType[5]''?", defaultCtor.cacheTypes[5].get());
        assertThat("default constructor => cacheType[6]''?", defaultCtor.cacheTypes[6].get());
        assertThat("default constructor => cacheType[7]''?", defaultCtor.cacheTypes[7].get());
        assertThat("default constructor => cacheType[8]''?", defaultCtor.cacheTypes[8].get());
        assertThat("default constructor => cacheType[9]''?", defaultCtor.cacheTypes[9].get());
        assertThat("default constructor => cacheType[10]''?", defaultCtor.cacheTypes[10].get());
        assertThat("default constructor => cacheType[11]'Munzee'", defaultCtor.cacheTypes[11].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("default constructor => attributesFilter.length", AtributeLength == defaultCtor.attributes.length);

        int[] attributesFilter = new int[]
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("default constructor => attributesFilter[" + i + "]", attributesFilter[i] == defaultCtor.attributes[i].get());
        }

        assertThat("default constructor => GPXFilenameIds.size", 0 == defaultCtor.GPXFilenameIds.size);
        assertThat("default constructor => Categories.size", 0 == defaultCtor.Categories.size);
        assertThat("default constructor => filterName", "" == defaultCtor.filterName);
        assertThat("default constructor => filterGcCode", "" == defaultCtor.filterGcCode);
        assertThat("default constructor => filterOwner", "" == defaultCtor.filterOwner);


        assertEquals(
                "{\"name\":\"ALL\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                defaultCtor.getJsonString(), "default constructor =>toString");

        assertFilteredReadedDB(defaultCtor);
    }

    @Test
    void checkAttributeFilter() {
        FilterProperties attributeFilter = new FilterProperties("ALL");

        attributeFilter.attributes[1].set(-1);
        attributeFilter.attributes[3].set(1);

        assertThat("default constructor => Finds", 0 == attributeFilter.Finds.get());
        assertThat("default constructor => NotAvailable", 0 == attributeFilter.NotAvailable.get());
        assertThat("default constructor => Archived", 0 == attributeFilter.Archived.get());
        assertThat("default constructor => Own", 0 == attributeFilter.Own.get());
        assertThat("default constructor => ContainsTravelbugs", 0 == attributeFilter.ContainsTravelbugs.get());
        assertThat("default constructor => Favorites", 0 == attributeFilter.Favorites.get());
        assertThat("default constructor => HasUserData", 0 == attributeFilter.HasUserData.get());
        assertThat("default constructor => ListingChanged", 0 == attributeFilter.ListingChanged.get());
        assertThat("default constructor => WithManualWaypoint", 0 == attributeFilter.WithManualWaypoint.get());
        assertThat("default constructor => MinDifficulty", 1.0f == attributeFilter.MinDifficulty.get() / 2F);
        assertThat("default constructor => MaxDifficulty", 5.0f == attributeFilter.MaxDifficulty.get() / 2F);
        assertThat("default constructor => MinTerrain", 1.0f == attributeFilter.MinTerrain.get() / 2F);
        assertThat("default constructor => MaxTerrain", 5.0f == attributeFilter.MaxTerrain.get() / 2F);
        assertThat("default constructor => MinContainerSize", 0.0f == attributeFilter.MinContainerSize.get());
        assertThat("default constructor => MaxContainerSize", 6.0f == attributeFilter.MaxContainerSize.get());
        assertThat("default constructor => MinRating", 0.0f == attributeFilter.MinRating.get() / 2F);
        assertThat("default constructor => MaxRating", 5.0f == attributeFilter.MaxRating.get() / 2F);
        assertThat("default constructor => MinFavPoints", -1 == attributeFilter.MinFavPoints.get());
        assertThat("default constructor => MinFavPoints", -1 == attributeFilter.MaxFavPoints.get());

        // CacheTypes
        assertThat("default constructor => cacheType.length", 26 == attributeFilter.cacheTypes.length);
        assertThat("default constructor => cacheType[0]''?", attributeFilter.cacheTypes[0].get());
        assertThat("default constructor => cacheType[1]''?", attributeFilter.cacheTypes[1].get());
        assertThat("default constructor => cacheType[2]''?", attributeFilter.cacheTypes[2].get());
        assertThat("default constructor => cacheType[3]''?", attributeFilter.cacheTypes[3].get());
        assertThat("default constructor => cacheType[4]''?", attributeFilter.cacheTypes[4].get());
        assertThat("default constructor => cacheType[5]''?", attributeFilter.cacheTypes[5].get());
        assertThat("default constructor => cacheType[6]''?", attributeFilter.cacheTypes[6].get());
        assertThat("default constructor => cacheType[7]''?", attributeFilter.cacheTypes[7].get());
        assertThat("default constructor => cacheType[8]''?", attributeFilter.cacheTypes[8].get());
        assertThat("default constructor => cacheType[9]''?", attributeFilter.cacheTypes[9].get());
        assertThat("default constructor => cacheType[10]''?", attributeFilter.cacheTypes[10].get());
        assertThat("default constructor => cacheType[11]'Munzee'", attributeFilter.cacheTypes[11].get());

        // AttributesFilter
        int AtributeLength = 68;
        assertThat("default constructor => attributesFilter.length", AtributeLength == attributeFilter.attributes.length);

        int[] attributesFilter = new int[]
                {0, -1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        for (int i = 0; i < AtributeLength; i++) {
            assertThat("default constructor => attributesFilter[" + i + "]", attributesFilter[i] == attributeFilter.attributes[i].get());
        }

        assertThat("default constructor => GPXFilenameIds.size", 0 == attributeFilter.GPXFilenameIds.size);
        assertThat("default constructor => Categories.size", 0 == attributeFilter.Categories.size);
        assertThat("default constructor => filterName", "" == attributeFilter.filterName);
        assertThat("default constructor => filterGcCode", "" == attributeFilter.filterGcCode);
        assertThat("default constructor => filterOwner", "" == attributeFilter.filterOwner);


        assertEquals(
                "{\"name\":\"ALL\",\"types\":\"true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true\",\"caches\":\"0,0,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,6.0,0.0,5.0,-1,-1,0\",\"filtergc\":\"\",\"gpxfilenameids\":\"\",\"attributes\":\"-1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\",\"filtername\":\"\",\"isHistory\":false,\"categories\":\"\",\"filterowner\":\"\"}",
                attributeFilter.getJsonString(), "default constructor =>toString");

        assertEquals("SELECT * FROM CacheCoreInfo core JOIN Attributes attr ON attr.Id = core.Id WHERE (attr.AttributesNegative &  2) > 0 and (attr.AttributesPositive & 8) > 0",
                attributeFilter.getSqlWhere("NAME"), "SqlWhere must Equals");


        assertFilteredReadedDB(attributeFilter);
    }

    @Test
    void chehkSqlWhere() {

        FilterProperties[] filters = new FilterProperties[]{FilterInstances.ACTIVE, FilterInstances.ALL,
                FilterInstances.BEGINNER, FilterInstances.DROPTB, FilterInstances.FAVORITES, FilterInstances.HIGHLIGHTS,
                FilterInstances.LISTINGCHANGED, FilterInstances.QUICK, FilterInstances.TOARCHIVE, FilterInstances.WITHTB};


        String[] SqlStringList = new String[]
                {
                        "SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and (not Owner='User')",
                        "SELECT * FROM CacheCoreInfo",
                        "SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and (not Owner='User') and Difficulty <= 4 and Terrain <= 4 and Size >= 2 and Type in (0)",
                        "SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 8= 8 and ~BooleanStore & 4= 4 and Difficulty <= 6 and Terrain <= 6 and Size >= 1 and Type in (0,1,2,5,6,7,9,10,21)",
                        "SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 16= 16",
                        "SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and Rating >= 350 and FavPoints >= 50",
                        "SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 512= 512",
                        "SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 8= 8 and ~BooleanStore & 36= 36 and (not Owner='User') and Difficulty <= 5 and Terrain <= 5 and Type in (0,3,4)",
                        "SELECT * FROM CacheCoreInfo core WHERE ~BooleanStore & 788= 788 and (not Owner='User')",
                        "SELECT * FROM CacheCoreInfo core WHERE BooleanStore & 8= 8 and ~BooleanStore & 4= 4 and NumTravelbugs > 0 and Difficulty <= 6 and Terrain <= 6 and Type in (0,1,2,5,6,7,9,10,21)"
                };

        for (int i = 0; i < filters.length; i++) {
            assertEquals(SqlStringList[i], filters[i].getSqlWhere("User"), "presets[" + i + "] '=>getSqlWhere(\"User\")");
        }
    }


    private void assertFilteredReadedDB(FilterProperties properties) {
        CacheList list = new CacheList();
        CacheList3DAO dao = new CacheList3DAO();
        dao.readCacheList(testDb, list, properties.getSqlWhere("NAME"), false, false);

        // Cachelist is Async loading, so wait a moment
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int n = list.size;
        while (n-- > 0) {
            AbstractCache cache = list.get(n);
            assertThat("Filter has to pass", properties.passed(cache));
        }
    }


}
