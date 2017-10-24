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
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.dao.*;
import de.longri.cachebox3.types.*;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;

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
        Database.Data = new Database(Database.DatabaseType.CacheBox3);
        Database.Data.startUp(testDbFileHandle);
        Database cb3Database = new Database(Database.DatabaseType.CacheBox3);
        cb3Database.startUp(copyDbFileHandle);

        //read Waypoint list and check
        Array<AbstractWaypoint> waypointList = new Array<>();

        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("", null);
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            AbstractWaypoint wp = new ImmutableWaypoint(reader);
            waypointList.add(wp);
            reader.moveToNext();
        }
        reader.close();

        Array<AbstractWaypoint> waypoint3list = new Waypoint3DAO().getWaypointsFromCacheID(cb3Database, null, true);

        int n = waypointList.size;
        int i = 0;
        while (n-- > 0) {
            AbstractWaypoint waypoint = waypointList.get(i);
            AbstractWaypoint waypoint3 = waypoint3list.get(i);
            assertThat("Waypoint Id must equals", waypoint.getCacheId() == waypoint3.getCacheId());
            assertThat("Waypoint Latitude must equals", TestUtils.roundDoubleCoordinate(waypoint.getLatitude()) == TestUtils.roundDoubleCoordinate(waypoint3.getLatitude()));
            assertThat("Waypoint Longitude must equals", TestUtils.roundDoubleCoordinate(waypoint.getLongitude()) == TestUtils.roundDoubleCoordinate(waypoint3.getLongitude()));
            assertThat("Waypoint GcCode must equals", waypoint3.getGcCode().equals(waypoint.getGcCode()));
            assertThat("Waypoint Type must equals", waypoint.getType() == waypoint3.getType());
            assertThat("Waypoint IsStart must equals", waypoint.isStart() == waypoint3.isStart());
            assertThat("Waypoint SyncExclude must equals", waypoint.isSyncExcluded() == waypoint3.isSyncExcluded());
            assertThat("Waypoint IsUserWaypoint must equals", waypoint.isUserWaypoint() == waypoint3.isUserWaypoint());
            assertThat("Waypoint Title must equals", waypoint3.getTitle().equals(waypoint.getTitle()));

            assertThat("Waypoint Description must equals", waypoint3.getDescription(cb3Database).equals(waypoint.getDescription(Database.Data)));
            assertThat("Waypoint Clue must equals", waypoint3.getClue(cb3Database).equals(waypoint.getClue(Database.Data)));
            i++;
        }

        assertThat("All Waypoints must equals", waypoint3list.equals(waypointList));


        //read Cachelist and check
        CacheList tmpCacheList = new CacheList();
        DaoFactory.CACHE_LIST_DAO.readCacheList(Database.Data, tmpCacheList, "", true, Config.ShowAllWaypoints.getValue());
        Database.Data.Query = tmpCacheList;
        assertThat("TestDB must have 33 Caches but has:" + Database.Data.Query.size, Database.Data.Query.size == 33);


        //Load CacheList's and check equals
        CacheList3DAO dao3 = new CacheList3DAO();
        CacheList cacheList3 = dao3.readCacheList(cb3Database, null, null, true, true);

        assertThat("Cache3DB must have 33 Caches but has:" + cacheList3.size, cacheList3.size == 33);

        n = cacheList3.size;
        i = 0;
        while (n-- > 0) {
            AbstractCache cache = Database.Data.Query.get(i);
            ImmutableCache immutableCache = (ImmutableCache) cacheList3.get(i);
            assertThat("Cache Id must equals", cache.getId() == immutableCache.getId());
            assertThat("Cache Latitude must equals", TestUtils.roundDoubleCoordinate(cache.getLatitude()) == TestUtils.roundDoubleCoordinate(immutableCache.getLatitude()));
            assertThat("Cache Longitude must equals", TestUtils.roundDoubleCoordinate(cache.getLongitude()) == TestUtils.roundDoubleCoordinate(immutableCache.getLongitude()));
            assertThat("Cache Size must equals", cache.getSize() == immutableCache.getSize());
            assertThat("Cache Difficulty must equals", cache.getDifficulty() == immutableCache.getDifficulty());
            assertThat("Cache Terrain must equals", cache.getTerrain() == immutableCache.getTerrain());
            assertThat("Cache Type must equals", cache.getType() == immutableCache.getType());
            assertThat("Cache Rating must equals", cache.getRating() == immutableCache.getRating());
            assertThat("Cache NumTravelbugs must equals", cache.getNumTravelbugs() == immutableCache.getNumTravelbugs());
            assertThat("Cache GcCode must equals", immutableCache.getGcCode().equals(cache.getGcCode()));
            assertThat("Cache Name must equals", immutableCache.getName().equals(cache.getName()));
            assertThat("Cache PlacedBy must equals", immutableCache.getPlacedBy().equals(cache.getPlacedBy()));
            assertThat("Cache Owner must equals", immutableCache.getOwner().equals(cache.getOwner()));
            assertThat("Cache GcId must equals", immutableCache.getGcId().equals(cache.getGcId()));
            assertThat("Cache Rating must equals", cache.getRating() == immutableCache.getRating());
            assertThat("Cache Archived must equals", cache.isArchived() == immutableCache.isArchived());
            assertThat("Cache Available must equals", cache.isAvailable() == immutableCache.isAvailable());
            assertThat("Cache Found must equals", cache.isFound() == immutableCache.isFound());
            assertThat("Cache Favorit must equals", cache.isFavorite() == immutableCache.isFavorite());
            assertThat("Cache HasUserData must equals", cache.isHasUserData() == immutableCache.isHasUserData());
            assertThat("Cache ListingChanged must equals", cache.isListingChanged() == immutableCache.isListingChanged());
            assertThat("Cache CorrectedCoordinates must equals", cache.hasCorrectedCoordinates() == immutableCache.hasCorrectedCoordinates());
            assertThat("Cache FavePoints must be 0", immutableCache.getFavoritePoints() == 0);
            assertThat("Cache HasHint must equals", immutableCache.hasHint() == cache.hasHint());


            Array<AbstractWaypoint> cacheWayPoints = cache.getWaypoints();
            Array<AbstractWaypoint> cache3WayPoints = immutableCache.getWaypoints();
            assertThat("Cache Waypoints must equals", cache3WayPoints.equals(cacheWayPoints));

            //check properties that not stored on class (direct DB Access)
            Array<Attributes> cacheAttributes = cache.getAttributes(Database.Data);
            Array<Attributes> cache3Attributes = immutableCache.getAttributes(cb3Database);
            assertThat("Cache Attributes must equals", cacheAttributes.equals(cache3Attributes));

            i++;
        }

        // cleanup
//        assertThat("TestDB must be deleted after cleanup", copyDbFileHandle.delete());
    }


}