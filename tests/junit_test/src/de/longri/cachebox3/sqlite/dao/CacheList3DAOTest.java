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
package de.longri.cachebox3.sqlite.dao;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 23.10.2017.
 */
class CacheList3DAOTest {

    static FileHandle testDbFileHandle;
    static FileHandle copyDbFileHandle;
    static Database cb3Database;
    static Database writeDatabase;
    private static FileHandle writeDbFileHandle;

    private final double should_latitude = 53.123;
    private final double should_longitude = 13.456;
    private final Array<Attributes> should_attributes = new Array<>(new Attributes[]{Attributes.Abandoned_mines, Attributes.Boat.setNegative()});
    private final String should_name = "Cache Name";
    private final String should_gcCode = "GCXXXX";
    private final String should_placedBy = "Myself";
    private final String should_owner = "I";
    private final String should_gcId = "1111111";
    private final short should_rating = 7;
    private final short should_numTravelbugs = 3;
    private final int should_favPoints = 237;
    private final long should_id = 123456789L;
    private final CacheTypes should_type = CacheTypes.Traditional;
    private final CacheSizes should_size = CacheSizes.regular;
    private final float should_difficulty = 2.5f;
    private final float should_terrain = 4f;

    private final boolean should_hint = true;
    private final boolean should_correctedCoordinates = true;
    private final boolean should_archived = true;
    private final boolean should_available = true;
    private final boolean should_favorite = true;
    private final boolean should_found = true;
    private final boolean should_userData = true;
    private final boolean should_listingChanged = true;
    private final Array<AbstractWaypoint> should_waypoints = new Array<>(new AbstractWaypoint[]{new MutableWaypoint(53.456, 13.789, should_id)});

    private final String should_LongDescription = "Cache Long Description";
    private final String should_ShortDescription = "Cache Short Description";
    private final String should_Hint = "Cache Hint";
    private final Date should_DateHidden = new Date();

    @BeforeAll
    public static void beforeAll() throws SQLiteGdxException {

        TestUtils.initialGdx();

        // copy testDb
        testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3", true);
        copyDbFileHandle = testDbFileHandle.parent().child("testCacheListDAO.db3");
        if (copyDbFileHandle.exists()) {
            // delete first
            assertThat("TestDB must be deleted for cleanup", copyDbFileHandle.delete());
        }
        testDbFileHandle.copyTo(copyDbFileHandle);
        assertThat("TestDB must exist", copyDbFileHandle.exists());

        // open DataBase
        cb3Database = new Database(Database.DatabaseType.CacheBox3);
        cb3Database.startUp(copyDbFileHandle);


        writeDbFileHandle = testDbFileHandle.parent().child("writeTestCacheList.db3");

        writeDatabase = new Database(Database.DatabaseType.CacheBox3);
        writeDatabase.startUp(writeDbFileHandle);


    }

    @AfterAll
    public static void cleanUpRecources() {
        cb3Database.close();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void readCacheList() {

        AbstractCacheListDAO DAO = new CacheList3DAO();
        CacheList caches = new CacheList();

        DAO.readCacheList(cb3Database, caches, "", true, true);

        // Cachelist is Async loading, so wait a moment
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("CacheList must have 731 Caches but has:" + caches.size, caches.size == 731);

        AbstractCache cache = caches.getCacheById(14731588679189319L);
        assertThat("Cache must have one Waypoint but has:" + cache.getWaypoints().size, cache.getWaypoints().size == 1);


        {// check conversion of CacheSizes
            AbstractCache cache1 = caches.GetCacheByGcCode("GC1Z18H");
            assertThat("CacheSize must be 'Other', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.other);

            cache1 = caches.GetCacheByGcCode("GC2DCGK");
            assertThat("CacheSize must be 'Micro', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.micro);

            cache1 = caches.GetCacheByGcCode("GC5Q51E");
            assertThat("CacheSize must be 'Small', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.small);

            cache1 = caches.GetCacheByGcCode("GC3CTWZ");
            assertThat("CacheSize must be 'Regular', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.regular);

            cache1 = caches.GetCacheByGcCode("GC58TMQ");
            assertThat("CacheSize must be 'Large', but was: " + cache1.getSize().toString(), cache1.getSize() == CacheSizes.large);
        }


        // read Caches by Type
        String statement = "SELECT * FROM CacheCoreInfo core WHERE Type=" + CacheTypes.Multi.ordinal();
        DAO.readCacheList(cb3Database, caches, statement, true, true);

        // Cachelist is Async loading, so wait a moment
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("CacheList must have 117 Caches but has:" + caches.size, caches.size == 117);


    }

    @Test
    void writeToDatabase() {
        AbstractCache cache = new MutableCache(should_latitude, should_longitude);

        cache.setLatLon(should_latitude, should_longitude);
        cache.setDateHidden(should_DateHidden);
        cache.setAttributes(should_attributes);
        cache.setName(should_name);
        cache.setGcCode(should_gcCode);
        cache.setPlacedBy(should_placedBy);
        cache.setOwner(should_owner);
        cache.setGcId(should_gcId);
        cache.setRating(should_rating);
        cache.setNumTravelbugs(should_numTravelbugs);
        cache.setFavoritePoints(should_favPoints);
        cache.setId(should_id);
        cache.setType(should_type);
        cache.setSize(should_size);
        cache.setDifficulty(should_difficulty);
        cache.setTerrain(should_terrain);
        cache.setHasHint(should_hint);
        cache.setHasCorrectedCoordinates(should_correctedCoordinates);
        cache.setArchived(should_archived);
        cache.setAvailable(should_available);
        cache.setFavorite(should_favorite);
        cache.setFound(should_found);
        cache.setHasUserData(should_userData);
        cache.setListingChanged(should_listingChanged);
        cache.setWaypoints(should_waypoints);
        cache.setLongDescription(should_LongDescription);
        cache.setShortDescription(should_ShortDescription);
        cache.setHint(should_Hint);

        assertCache("MutableCache", cache);


        CacheList cacheList = new CacheList();
        cacheList.add(cache);

        AbstractCacheListDAO dao = new CacheList3DAO();
        dao.writeToDB(writeDatabase, cacheList);

        AbstractCache storedCache = new Cache3DAO().getFromDbByCacheId(writeDatabase, should_id, true, true);
        assertCache("StoredCache", storedCache);

        //clean up
        writeDbFileHandle.delete();
    }


    private void assertCache(String msg, AbstractCache cache) {
        assertThat(msg + " Latitude must charSequenceEquals", TestUtils.roundDoubleCoordinate(cache.getLatitude()) == should_latitude);
        assertThat(msg + " Longitude must charSequenceEquals", TestUtils.roundDoubleCoordinate(cache.getLongitude()) == should_longitude);
        assertThat(msg + " Name must charSequenceEquals", cache.getName().equals(should_name));
        assertThat(msg + " Attributes must charSequenceEquals", cache.getAttributes().equals(should_attributes));
        assertThat(msg + " GcCode must charSequenceEquals", cache.getGcCode().equals(should_gcCode));
        assertThat(msg + " PlacedBy must charSequenceEquals", cache.getPlacedBy().equals(should_placedBy));
        assertThat(msg + " Owner must charSequenceEquals", cache.getOwner().equals(should_owner));
        assertThat(msg + " GcID must charSequenceEquals", cache.getGcId().equals(should_gcId));
        assertThat(msg + " Rating must charSequenceEquals", cache.getRating() == should_rating);
        assertThat(msg + " NumTravelbugs must charSequenceEquals", cache.getNumTravelbugs() == should_numTravelbugs);
        assertThat(msg + " FavPoints must charSequenceEquals", cache.getFavoritePoints() == should_favPoints);
        assertThat(msg + " Id must charSequenceEquals", cache.getId() == should_id);
        assertThat(msg + " Type must charSequenceEquals", cache.getType() == should_type);
        assertThat(msg + " Size must charSequenceEquals", cache.getSize() == should_size);
        assertThat(msg + " Difficulty must charSequenceEquals", cache.getDifficulty() == should_difficulty);
        assertThat(msg + " Terrain must charSequenceEquals", cache.getTerrain() == should_terrain);
        assertThat(msg + " HasHint must charSequenceEquals", cache.hasHint() == should_hint);
        assertThat(msg + " HasCorrectedCoordinates must charSequenceEquals", cache.hasCorrectedCoordinates() == should_correctedCoordinates);
        assertThat(msg + " Archived must charSequenceEquals", cache.isArchived() == should_archived);
        assertThat(msg + " Available must charSequenceEquals", cache.isAvailable() == should_available);
        assertThat(msg + " Favorite must charSequenceEquals", cache.isFavorite() == should_favorite);
        assertThat(msg + " IsFound must charSequenceEquals", cache.isFound() == should_found);
        assertThat(msg + " HasUserData must charSequenceEquals", cache.isHasUserData() == should_userData);
        assertThat(msg + " ListingChanged must charSequenceEquals", cache.isListingChanged() == should_listingChanged);
        assertThat(msg + " Waypoints must charSequenceEquals", cache.getWaypoints().equals(should_waypoints));
        assertThat(msg + " LongDescription must charSequenceEquals", cache.getLongDescription().equals(should_LongDescription));
        assertThat(msg + " ShortDescription must charSequenceEquals", cache.getShortDescription().equals(should_ShortDescription));
        assertThat(msg + " Hint must charSequenceEquals", cache.getHint().equals(should_Hint));
    }

}