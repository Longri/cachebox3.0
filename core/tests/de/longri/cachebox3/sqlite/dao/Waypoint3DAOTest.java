package de.longri.cachebox3.sqlite.dao;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.gui.utils.CharSequenceArray;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 21.10.2017.
 */
class Waypoint3DAOTest {

    static FileHandle testDbFileHandle;
    static FileHandle copyDbFileHandle;
    static Database cb3Database;

    @BeforeAll
    static void convertCB3_DB() throws SQLiteGdxException {

        TestUtils.initialGdx();

        // copy testDb
        testDbFileHandle = TestUtils.getResourceFileHandle("testsResources/Database/testACB2.db3");
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
    static void cleanUpRecources() {
        assertThat("TestDB must be deleted after cleanup", copyDbFileHandle.delete());
    }


    @Test
    void getWaypointsFromCacheID() {

        Waypoint3DAO dao = new Waypoint3DAO();
        Array<AbstractWaypoint> waypoints = dao.getWaypointsFromCacheID(cb3Database, null, true);
        assertThat("TestDB must have 16 Waypoints but has:" + waypoints.size, waypoints.size == 16);

        waypoints = dao.getWaypointsFromCacheID(cb3Database, 0L, true);
        assertThat("TestDB must have 11 Waypoints but has:" + waypoints.size, waypoints.size == 11);

        AbstractWaypoint wp = waypoints.get(1);
        assertThat("Waypoint.GcCode must be 'P079QV7' but was: '" + wp.getGcCode() + "'", wp.getGcCode().equals("P079QV7"));
        assertThat("Waypoint.Latitude must be '54.411916' but was: '" + TestUtils.roundDoubleCoordinate(wp.getLatitude()) + "'", TestUtils.roundDoubleCoordinate(wp.getLatitude()) == 54.411916);
        assertThat("Waypoint.Longitude must be '11.101833' but was: '" + TestUtils.roundDoubleCoordinate(wp.getLongitude()) + "'", TestUtils.roundDoubleCoordinate(wp.getLongitude()) == 11.101833);
        assertThat("Waypoint.Type must be 'ParkingArea' but was: '" + wp.getType() + "'", wp.getType() == CacheTypes.ParkingArea);
        assertThat("Waypoint.Start must be 'true' but was: '" + wp.isStart() + "'", wp.isStart());
        assertThat("Waypoint.SyncExcluded must be 'true' but was: '" + wp.isSyncExcluded() + "'", wp.isSyncExcluded());
        assertThat("Waypoint.UserWaypoint must be 'true' but was: '" + wp.isUserWaypoint() + "'", wp.isUserWaypoint());
        assertThat("Waypoint.Title must be 'öffentlicher Parkplatz' but was: '" + wp.getTitle() + "'", wp.getTitle().equals("öffentlicher Parkplatz"));

        assertThat("Waypoint.Description must be 'WP Desc' but was: '" + wp.getDescription(cb3Database) + "'", wp.getDescription(cb3Database).equals("WP Desc"));
        assertThat("Waypoint.Clue must be 'WP Clue' but was: '" + wp.getClue(cb3Database) + "'", wp.getClue(cb3Database).equals("WP Clue"));

    }


    @Test
    void exceptionThrowing() {
        //Waypoint3 class must throw a Exception with set properties

        AbstractWaypoint wp = new Waypoint3(should_Latitude, should_Longitude);
        boolean hasThrowed = false;
        try {
            wp.setCacheId(should_cacheId);
        } catch (Exception e) {
            hasThrowed = true;
        } finally {
            assertThat("Set CacheID must throw a RuntimeException", hasThrowed);
        }

        hasThrowed = false;
        try {
            wp.setGcCode(should_GcCode);
        } catch (Exception e) {
            hasThrowed = true;
        } finally {
            assertThat("Set GcCode must throw a RuntimeException", hasThrowed);
        }

        hasThrowed = false;
        try {
            wp.setTitle(should_Title);
        } catch (Exception e) {
            hasThrowed = true;
        } finally {
            assertThat("Set Title must throw a RuntimeException", hasThrowed);
        }

        hasThrowed = false;
        try {
            wp.setType(should_Type);
        } catch (Exception e) {
            hasThrowed = true;
        } finally {
            assertThat("Set Type must throw a RuntimeException", hasThrowed);
        }

        hasThrowed = false;
        try {
            wp.setStart(should_isStart);
        } catch (Exception e) {
            hasThrowed = true;
        } finally {
            assertThat("Set Start must throw a RuntimeException", hasThrowed);
        }

        hasThrowed = false;
        try {
            wp.setSyncExcluded(should_syncExclude);
        } catch (Exception e) {
            hasThrowed = true;
        } finally {
            assertThat("Set SyncExclude must throw a RuntimeException", hasThrowed);
        }

        hasThrowed = false;
        try {
            wp.setUserWaypoint(should_userWaypoint);
        } catch (Exception e) {
            hasThrowed = true;
        } finally {
            assertThat("Set UserWaypoint must throw a RuntimeException", hasThrowed);
        }

        hasThrowed = false;
        try {
            wp.setUserWaypoint(should_userWaypoint);
        } catch (Exception e) {
            hasThrowed = true;
        } finally {
            assertThat("Set UserWaypoint must throw a RuntimeException", hasThrowed);
        }
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


    @Test
    void writeToDatabase() {
        //1. write new wp to DB
        //2. update wp
        //3. delete wp

        AbstractWaypoint wp = new WaypointImport(should_Latitude, should_Longitude);
        wp.setCacheId(should_cacheId);
        wp.setGcCode(should_GcCode);
        wp.setTitle(should_Title);
        wp.setType(should_Type);
        wp.setStart(should_isStart);
        wp.setSyncExcluded(should_syncExclude);
        wp.setUserWaypoint(should_userWaypoint);
        wp.setDescription(should_Description);
        wp.setClue(should_Clue);
        assertWp("WaypointImport", wp);

        Waypoint3DAO DAO=new Waypoint3DAO();
        DAO.writeToDatabase(cb3Database,wp);

        Array<AbstractWaypoint> waypoints = DAO.getWaypointsFromCacheID(cb3Database, should_cacheId, true);
        AbstractWaypoint wp2= waypoints.get(0);
        assertWp("StoredWaypoint", wp2);

    }

    private void assertWp(String msg, AbstractWaypoint wp) {
        assertThat(msg + " Id must equals", wp.getCacheId() == should_cacheId);
        assertThat(msg + " Latitude must equals", TestUtils.roundDoubleCoordinate(wp.getLatitude()) == should_Latitude);
        assertThat(msg + " Longitude must equals", TestUtils.roundDoubleCoordinate(wp.getLongitude()) == should_Longitude);
        assertThat(msg + " GcCode must equals", wp.getGcCode().equals(should_GcCode));
        assertThat(msg + " Type must equals", wp.getType() == should_Type);
        assertThat(msg + " IsStart must equals", wp.isStart() == should_isStart);
        assertThat(msg + " SyncExclude must equals", wp.isSyncExcluded() == should_syncExclude);
        assertThat(msg + " IsUserWaypoint must equals", wp.isUserWaypoint() == should_userWaypoint);
        assertThat(msg + " Title must equals", wp.getTitle().equals(should_Title));

        assertThat(msg + " Description must equals", wp.getDescription(cb3Database).equals(should_Description));
        assertThat(msg + " Clue must equals", wp.getClue(cb3Database).equals(should_Clue));

    }


    @Test
    void resetStartWaypoint() {
    }


}