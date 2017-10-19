/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
package de.longri.cachebox3.sqlite;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.dao.CacheList3DAO;
import de.longri.cachebox3.sqlite.dao.CacheListDAO;
import de.longri.cachebox3.types.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mapsforge.core.util.LatLongUtils;
import org.oscim.core.Tile;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for convert ACB V2.x DB to CB3 DB
 * Created by Longri on 18.10.2017.
 */
@RunWith(JUnitPlatform.class)
class DatabaseConvert {

    static {
        TestUtils.initialGdx();
    }

    @Test
    void convert() throws SQLiteGdxException {

        // copy testDb
        FileHandle testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3");
        FileHandle copyDbFileHandle = testDbFileHandle.parent().child("testDb.db3");
        if (copyDbFileHandle.exists()) {
            // delete first
            assertThat("TestDB must be deleted for cleanup", copyDbFileHandle.delete());
        }
        testDbFileHandle.copyTo(copyDbFileHandle);
        assertThat("TestDB must exist", copyDbFileHandle.exists());


        // open DataBase
        Database.Data = new Database(Database.DatabaseType.CacheBox);
        Database.Data.startUp(testDbFileHandle);
        Database cb3Database = new Database(Database.DatabaseType.CacheBox3);
        cb3Database.startUp(copyDbFileHandle);

        //read Waypoint list and check
        Array<Waypoint> waypointList = new Array<>();
        Array<Waypoint3> waypoint3List = new Array<>();

        assertThat("All Waypoints must equals", waypointList.equals(waypoint3List));



        //read Cachelist and check
        CacheList tmpCacheList = new CacheList();
        CacheListDAO cacheListDAO = new CacheListDAO();
        cacheListDAO.ReadCacheList(tmpCacheList, "", true, Config.ShowAllWaypoints.getValue());
        Database.Data.Query = tmpCacheList;
        assertThat("TestDB must have 33 Caches but has:" + Database.Data.Query.size, Database.Data.Query.size == 33);


        //Load CacheList's and check equals
        CacheList3DAO dao3 = new CacheList3DAO();
        CacheList cacheList3 = dao3.readCacheList(cb3Database);

        assertThat("Cache3DB must have 33 Caches but has:" + cacheList3.size, cacheList3.size == 33);

        int n = cacheList3.size;
        int i = 0;
        while (n-- > 0) {
            Cache cache = (Cache) Database.Data.Query.get(i);
            Cache3 cache3 = (Cache3) cacheList3.get(i);
            assertThat("Cache Id must equals", cache.getId() == cache3.getId());
            assertThat("Cache Latitude must equals", roundDoubleCoordinate(cache.getLatitude()) == roundDoubleCoordinate(cache3.getLatitude()));
            assertThat("Cache Longitude must equals", roundDoubleCoordinate(cache.getLongitude()) == roundDoubleCoordinate(cache3.getLongitude()));
            assertThat("Cache Size must equals", cache.getSize() == cache3.getSize());
            assertThat("Cache Difficulty must equals", cache.getDifficulty() == cache3.getDifficulty());
            assertThat("Cache Terrain must equals", cache.getTerrain() == cache3.getTerrain());
            assertThat("Cache Type must equals", cache.getType() == cache3.getType());
            assertThat("Cache Rating must equals", cache.getRating() == cache3.getRating());
            assertThat("Cache NumTravelbugs must equals", cache.getNumTravelbugs() == cache3.getNumTravelbugs());
            assertThat("Cache GcCode must equals", cache3.getGcCode().equals(cache.getGcCode()));
            assertThat("Cache Name must equals", cache3.getName().equals(cache.getName()));
            assertThat("Cache PlacedBy must equals", cache3.getPlacedBy().equals(cache.getPlacedBy()));
            assertThat("Cache Owner must equals", cache3.getOwner().equals(cache.getOwner()));
            assertThat("Cache GcId must equals", cache3.getGcId().equals(cache.getGcId()));
            assertThat("Cache Rating must equals", cache.getRating() == cache3.getRating());
            assertThat("Cache Archived must equals", cache.isArchived() == cache3.isArchived());
            assertThat("Cache Available must equals", cache.isAvailable() == cache3.isAvailable());
            assertThat("Cache Found must equals", cache.isFound() == cache3.isFound());
            assertThat("Cache Favorit must equals", cache.isFavorite() == cache3.isFavorite());
            assertThat("Cache HasUserData must equals", cache.isHasUserData() == cache3.isHasUserData());
            assertThat("Cache ListingChanged must equals", cache.isListingChanged() == cache3.isListingChanged());
            assertThat("Cache CorrectedCoordinates must equals", cache.hasCorrectedCoordinates() == cache3.hasCorrectedCoordinates());
            assertThat("Cache FavePoints must be 0", cache3.getFaviritPoints() == 0);
            assertThat("Cache HasHint must equals", cache3.hasHint() == cache.hasHint());


            Array<Waypoint> cacheWayPoints = cache.getWaypoints();
            Array<Waypoint> cache3WayPoints = cache3.getWaypoints();
            assertThat("Cache Waypoints must equals", cacheWayPoints.equals(cache3WayPoints));

            //check properties that not stored on class (direct DB Access)
            Array<Attributes> cacheAttributes = cache.getAttributes(Database.Data);
            Array<Attributes> cache3Attributes = cache3.getAttributes(cb3Database);
            assertThat("Cache Attributes must equals", cacheAttributes.equals(cache3Attributes));


            i++;
        }

        // cleanup
//        assertThat("TestDB must be deleted after cleanup", copyDbFileHandle.delete());
    }


    private double roundDoubleCoordinate(double value) {
        value = Math.round(LatLongUtils.degreesToMicrodegrees(value));
        value = LatLongUtils.microdegreesToDegrees((int) value);
        return value;
    }

}