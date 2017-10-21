package de.longri.cachebox3.sqlite.dao;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheTypes;
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
    void writeToDatabase() {
    }

    @Test
    void updateDatabase() {
    }

    @Test
    void resetStartWaypoint() {
    }

    @Test
    void delete() {
    }

}