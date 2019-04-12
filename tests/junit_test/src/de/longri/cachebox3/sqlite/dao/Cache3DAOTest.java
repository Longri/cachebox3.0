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
class Cache3DAOTest {

    static FileHandle testDbFileHandle;
    static FileHandle copyDbFileHandle;
    static Database cb3Database;

    @BeforeAll
    static void setUp() throws SQLiteGdxException {
        TestUtils.initialGdx();

        // copy testDb
        testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3", true);
        copyDbFileHandle = testDbFileHandle.parent().child("testCacheDAO.db3");
        if (copyDbFileHandle.exists()) {
            // delete first
            assertThat("TestDB must be deleted for cleanup", copyDbFileHandle.delete());
        }
        testDbFileHandle.copyTo(copyDbFileHandle);
        assertThat("TestDB must exist", copyDbFileHandle.exists());

        // open DataBase
        cb3Database = new Database(Database.DatabaseType.CacheBox3);
        cb3Database.startUp(copyDbFileHandle);

    }

    @AfterAll
    static void tearDown() {
        cb3Database.close();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

        AbstractCacheDAO DAO = new Cache3DAO();
        DAO.writeToDatabase(cb3Database, cache, false);

        AbstractCache storedCache = DAO.getFromDbByCacheId(cb3Database, should_id, true, true);
        assertCache("StoredCache", storedCache);
    }

    private void assertCache(String msg, AbstractCache cache) {
        assertThat(msg + " Latitude must equals", TestUtils.roundDoubleCoordinate(cache.getLatitude()) == should_latitude);
        assertThat(msg + " Longitude must equals", TestUtils.roundDoubleCoordinate(cache.getLongitude()) == should_longitude);
        assertThat(msg + " Attributes must equals", cache.getAttributes().equals(should_attributes));
        assertThat(msg + " Name must equals", cache.getName().equals(should_name));
        assertThat(msg + " GcCode must equals", cache.getGcCode().equals(should_gcCode));
        assertThat(msg + " PlacedBy must equals", cache.getPlacedBy().equals(should_placedBy));
        assertThat(msg + " Owner must equals", cache.getOwner().equals(should_owner));
        assertThat(msg + " GcID must equals", cache.getGcId().equals(should_gcId));
        assertThat(msg + " Rating must equals", cache.getRating() == should_rating);
        assertThat(msg + " NumTravelbugs must equals", cache.getNumTravelbugs() == should_numTravelbugs);
        assertThat(msg + " FavPoints must equals", cache.getFavoritePoints() == should_favPoints);
        assertThat(msg + " Id must equals", cache.getId() == should_id);
        assertThat(msg + " Type must equals", cache.getType() == should_type);
        assertThat(msg + " Size must equals", cache.getSize() == should_size);
        assertThat(msg + " Difficulty must equals", cache.getDifficulty() == should_difficulty);
        assertThat(msg + " Terrain must equals", cache.getTerrain() == should_terrain);
        assertThat(msg + " HasHint must equals", cache.hasHint() == should_hint);
        assertThat(msg + " HasCorrectedCoordinates must equals", cache.hasCorrectedCoordinates() == should_correctedCoordinates);
        assertThat(msg + " Archived must equals", cache.isArchived() == should_archived);
        assertThat(msg + " Available must equals", cache.isAvailable() == should_available);
        assertThat(msg + " Favorite must equals", cache.isFavorite() == should_favorite);
        assertThat(msg + " IsFound must equals", cache.isFound() == should_found);
        assertThat(msg + " HasUserData must equals", cache.isHasUserData() == should_userData);
        assertThat(msg + " ListingChanged must equals", cache.isListingChanged() == should_listingChanged);
        assertThat(msg + " Waypoints must equals", cache.getWaypoints().equals(should_waypoints));
        assertThat(msg + " LongDescription must equals", cache.getLongDescription().equals(should_LongDescription));
        assertThat(msg + " ShortDescription must equals", cache.getShortDescription().equals(should_ShortDescription));
        assertThat(msg + " Hint must equals", cache.getHint().equals(should_Hint));
        assertThat(msg + " Attributes must equals", cache.getAttributes().equals(should_attributes));
    }

    @Test
    void writeToDatabaseFound() {
    }

    @Test
    void updateDatabase() {
    }

    @Test
    void getFromDbByCacheId() {
    }

    @Test
    void updateDatabaseCacheState() {
    }

}