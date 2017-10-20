package de.longri.cachebox3.sqlite.dao;

import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.types.Waypoint3;

/**
 * Created by Longri on 20.10.2017.
 */
public class Waypoint3DAO {

    private final String GET_ALL_WAYPOINTS = "SELECT * FROM Waypoints";

    public Array<Waypoint3> getAllWayPoints(Database database) {
        Array<Waypoint3> waypoints = new Array<>();
        SQLiteGdxDatabaseCursor cursor = database.rawQuery(GET_ALL_WAYPOINTS, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Waypoint3 wp = new Waypoint3(cursor);
            waypoints.add(wp);
            cursor.moveToNext();
        }
        cursor.close();
        return waypoints;
    }

}
