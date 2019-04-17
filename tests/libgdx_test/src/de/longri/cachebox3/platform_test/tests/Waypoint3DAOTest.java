

//  Don't modify this file, it's created by tool 'extract_libgdx_test

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
package de.longri.cachebox3.platform_test.tests;

import de.longri.cachebox3.sqlite.dao.*;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableWaypoint;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.SQLiteGdxException;
import de.longri.cachebox3.platform_test.AfterAll;
import de.longri.cachebox3.platform_test.BeforeAll;
import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.platform_test.Assert.assertThat;

/**
 * Created by Longri on 21.10.2017.
 */
public class Waypoint3DAOTest {

    static FileHandle testDbFileHandle;
    static FileHandle copyDbFileHandle;
    static Database cb3Database;

    @BeforeAll
    public static void beforeAll() throws SQLiteGdxException, PlatformAssertionError {

        TestUtils.initialGdx();

        // copy testDb
        testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3", true);
        copyDbFileHandle = testDbFileHandle.parent().child("testWaypointDAO.db3");
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
    public static void cleanUpRecources() {
        cb3Database.close();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void getWaypointsFromCacheID() throws PlatformAssertionError {

        Waypoint3DAO dao = new Waypoint3DAO();
        Array<AbstractWaypoint> waypoints = dao.getWaypointsFromCacheID(cb3Database, null, true);
        assertThat("TestDB must have 282 Waypoints but has:" + waypoints.size, waypoints.size == 282);

        waypoints = dao.getWaypointsFromCacheID(cb3Database, 20919627218633543L, true);
        assertThat("TestDB must have 6 Waypoints but has:" + waypoints.size, waypoints.size == 6);

        AbstractWaypoint wp = waypoints.get(1);
        assertThat("Waypoint.GcCode must be 'S23EJRJ' but was: '" + wp.getGcCode() + "'", wp.getGcCode().equals("S23EJRJ"));
        assertThat("Waypoint.Latitude must be '52.507768' but was: '" + TestUtils.roundDoubleCoordinate(wp.getLatitude()) + "'", TestUtils.roundDoubleCoordinate(wp.getLatitude()) == 52.507768);
        assertThat("Waypoint.Longitude must be '13.465333' but was: '" + TestUtils.roundDoubleCoordinate(wp.getLongitude()) + "'", TestUtils.roundDoubleCoordinate(wp.getLongitude()) == 13.465333);
        assertThat("Waypoint.Type must be 'Question to Answer' but was: '" + wp.getType() + "'", wp.getType() == CacheTypes.MultiQuestion);
        assertThat("Waypoint.Start must be 'false' but was: '" + wp.isStart() + "'", !wp.isStart());
        assertThat("Waypoint.SyncExcluded must be 'false' but was: '" + wp.isSyncExcluded() + "'", !wp.isSyncExcluded());
        assertThat("Waypoint.UserWaypoint must be 'false' but was: '" + wp.isUserWaypoint() + "'", !wp.isUserWaypoint());
        assertThat("Waypoint.Title must be 'Stage 2' but was: '" + wp.getTitle() + "'", wp.getTitle().equals("Stage 2"));

        assertThat("Waypoint.Description must be 'Wohin geht der Mann im Hauseingang Nr. 9? Der erste Buchstabe wird gesucht' but was: '" + wp.getDescription() + "'", wp.getDescription().equals("Wohin geht der Mann im Hauseingang Nr. 9? Der erste Buchstabe wird gesucht"));
        assertThat("Waypoint.Clue must be '' but was: '" + wp.getClue() + "'", wp.getClue().equals(""));

    }

    private final double should_Latitude = 53.123;
    private final double should_Longitude = 13.456;
    private final long should_cacheId = 1234567890L;
    private final String should_GcCode = "GCCCCCX";
    private final String should_Title = "Waypoint-Title";
    private final CacheTypes should_Type = CacheTypes.MultiQuestion;
    private final boolean should_isStart = true;
    private final boolean should_syncExclude = true;
    private final boolean should_userWaypoint = true;
    private final String should_Description = " Waypoint description";
    private final String should_Clue = " Waypoint clue";

    private final double should2_Latitude = 53.456;
    private final double should2_Longitude = 13.789;
    private final String should2_Title = "Waypoint-Title Updated";
    private final CacheTypes should2_Type = CacheTypes.CITO;
    private final boolean should2_isStart = false;
    private final boolean should2_syncExclude = false;
    private final boolean should2_userWaypoint = false;
    private final String should2_Description = " Waypoint updated description";
    private final String should2_Clue = " Waypoint updated clue";


    @Test
    public void writeToDatabase() throws PlatformAssertionError {
        //1. write new wp to DB
        //2. update wp
        //3. delete wp

//1. write new wp to DB -------------------------------------------------------------------        
        AbstractWaypoint wp = new MutableWaypoint(0, 0, should_cacheId);

        wp.setLatLon(should_Latitude, should_Longitude);
        wp.setGcCode(should_GcCode);
        wp.setTitle(should_Title);
        wp.setType(should_Type);
        wp.setStart(should_isStart);
        wp.setSyncExcluded(should_syncExclude);
        wp.setUserWaypoint(should_userWaypoint);
        wp.setDescription(should_Description);
        wp.setClue(should_Clue);
        assertWp("MutableWaypoint", wp);

        Waypoint3DAO DAO = new Waypoint3DAO();
        DAO.writeToDatabase(cb3Database, wp, false);

        Array<AbstractWaypoint> waypoints = DAO.getWaypointsFromCacheID(cb3Database, should_cacheId, true);
        AbstractWaypoint wp2 = waypoints.get(0);
        assertWp("StoredWaypoint", wp2);


//2. update wp -----------------------------------------------------------------------------  

        wp2.setLatLon(should2_Latitude, should2_Longitude);
        wp2.setTitle(should2_Title);
        wp2.setType(should2_Type);
        wp2.setStart(should2_isStart);
        wp2.setSyncExcluded(should2_syncExclude);
        wp2.setUserWaypoint(should2_userWaypoint);
        wp2.setDescription(should2_Description);
        wp2.setClue(should2_Clue);
        assertWp2("ChangedWaypoint", wp2);

        DAO.updateDatabase(cb3Database, wp2, false);

        Array<AbstractWaypoint> waypoints2 = DAO.getWaypointsFromCacheID(cb3Database, should_cacheId, true);
        AbstractWaypoint wp3 = waypoints2.get(0);
        assertWp2("updatedWaypoint", wp3);

//3. delete wp -----------------------------------------------------------------------------

        DAO.delete(cb3Database, wp3, false);
        Array<AbstractWaypoint> waypoints3 = DAO.getWaypointsFromCacheID(cb3Database, should_cacheId, true);
        assertThat("Waypoint list must be empty", waypoints3.size == 0);

        //check is also deleted from WaypointsText table
        GdxSqliteCursor cursor = cb3Database.rawQuery("SELECT * FROM WaypointsText WHERE GcCode='GCCCCCX'", (String[]) null);
        if (cursor != null) {
            cursor.moveToFirst();
            assertThat("Waypoint must also deleted from WaypointsText table", cursor.isAfterLast());
        }
    }

    private void assertWp(String msg, AbstractWaypoint wp) throws PlatformAssertionError {
        assertThat(msg + " Id must charSequenceEquals", wp.getCacheId() == should_cacheId);
        assertThat(msg + " Latitude must charSequenceEquals", TestUtils.roundDoubleCoordinate(wp.getLatitude()) == should_Latitude);
        assertThat(msg + " Longitude must charSequenceEquals", TestUtils.roundDoubleCoordinate(wp.getLongitude()) == should_Longitude);
        assertThat(msg + " GcCode must charSequenceEquals", wp.getGcCode().equals(should_GcCode));
        assertThat(msg + " Type must charSequenceEquals", wp.getType() == should_Type);
        assertThat(msg + " IsStart must charSequenceEquals", wp.isStart() == should_isStart);
        assertThat(msg + " SyncExclude must charSequenceEquals", wp.isSyncExcluded() == should_syncExclude);
        assertThat(msg + " IsUserWaypoint must charSequenceEquals", wp.isUserWaypoint() == should_userWaypoint);
        assertThat(msg + " Title must charSequenceEquals", wp.getTitle().equals(should_Title));
        assertThat(msg + " Description must charSequenceEquals", wp.getDescription().equals(should_Description));
        assertThat(msg + " Clue must charSequenceEquals", wp.getClue().equals(should_Clue));
    }

    private void assertWp2(String msg, AbstractWaypoint wp) throws PlatformAssertionError {
        assertThat(msg + " Id must charSequenceEquals", wp.getCacheId() == should_cacheId);
        assertThat(msg + " Latitude must charSequenceEquals", TestUtils.roundDoubleCoordinate(wp.getLatitude()) == should2_Latitude);
        assertThat(msg + " Longitude must charSequenceEquals", TestUtils.roundDoubleCoordinate(wp.getLongitude()) == should2_Longitude);
        assertThat(msg + " GcCode must charSequenceEquals", wp.getGcCode().equals(should_GcCode));
        assertThat(msg + " Type must charSequenceEquals", wp.getType() == should2_Type);
        assertThat(msg + " IsStart must charSequenceEquals", wp.isStart() == should2_isStart);
        assertThat(msg + " SyncExclude must charSequenceEquals", wp.isSyncExcluded() == should2_syncExclude);
        assertThat(msg + " IsUserWaypoint must charSequenceEquals", wp.isUserWaypoint() == should2_userWaypoint);
        assertThat(msg + " Title must charSequenceEquals", wp.getTitle().equals(should2_Title));
        assertThat(msg + " Description must charSequenceEquals", wp.getDescription().equals(should2_Description));
        assertThat(msg + " Clue must charSequenceEquals", wp.getClue().equals(should2_Clue));
    }


    @Test
    public void resetStartWaypoint() {
    }


}
