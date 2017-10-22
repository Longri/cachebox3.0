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

import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.UnitFormatter;

/**
 * Created by Longri on 20.10.2017.
 */
public class Waypoint3DAO extends AbstractWaypointDAO {

    private final String GET_ALL_WAYPOINTS = "SELECT * FROM Waypoints";
    private final String GET_ALL_WAYPOINTS_FROM_CACHE = "SELECT * FROM Waypoints WHERE CacheId=?";


    @Override
    public Array<AbstractWaypoint> getWaypointsFromCacheID(Database database, Long cacheID, boolean full) {

        String[] args = cacheID == null ? null : new String[]{String.valueOf(cacheID)};
        String where = cacheID == null ? GET_ALL_WAYPOINTS : GET_ALL_WAYPOINTS_FROM_CACHE;
        Array<AbstractWaypoint> waypoints = new Array<>();
        SQLiteGdxDatabaseCursor cursor = database.rawQuery(where, args);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Waypoint3 wp = new Waypoint3(cursor);
            waypoints.add(wp);
            cursor.moveToNext();
        }
        cursor.close();
        return waypoints;
    }


    @Override
    public void writeToDatabase(Database database, AbstractWaypoint wp) {

        //TODO  int newCheckSum = createCheckSum(database, wp);

        Database.Parameters args = new Database.Parameters();
        Database.Parameters args2 = new Database.Parameters();
        args.put("CacheId", wp.getCacheId());
        args.put("GcCode", wp.getGcCode());
        args.put("Latitude", wp.getLatitude());
        args.put("Longitude", wp.getLongitude());
        args.put("Type", wp.getType().ordinal());
        args.put("IsStart", wp.isStart());
        args.put("SyncExclude", wp.isSyncExcluded());
        args.put("UserWaypoint", wp.isUserWaypoint());
        args.put("Title", wp.getTitle());

        args2.put("GcCode", wp.getGcCode());
        args2.put("description", wp.getDescription(database));
        args2.put("clue", wp.getClue(database));

        long count = database.insert("Waypoints", args);
        if (count <= 0) {
            database.update("Waypoints", args, "GcCode=\"" + wp.getGcCode() + "\"", null);
            database.update("WaypointsText", args2, "GcCode=\"" + wp.getGcCode() + "\"", null);
        } else {
            database.insert("WaypointsText", args2);
        }

        if (wp.isUserWaypoint()) {

            String[] cacheIdString = new String[]{String.valueOf(wp.getCacheId())};

            //read booleanStore of Cache
            SQLiteGdxDatabaseCursor cursor = database.rawQuery("SELECT BooleanStore from CacheCoreInfo WHERE Id=?", cacheIdString);
            cursor.moveToFirst();
            short booleanStore = cursor.getShort(0);

            if (Cache3.getMaskValue(Cache3.MASK_HAS_USER_DATA, booleanStore)) {
                // HasUserData is set, return!
                return;
            }

            Cache3.setMaskValue(Cache3.MASK_HAS_USER_DATA, true, booleanStore);

            //Set 'HasUserData' on Cache table
            args = new Database.Parameters();
            args.put("BooleanStore", booleanStore);
            database.update("CacheCoreInfo", args, "Id = ?", cacheIdString);
        }


    }

    @Override
    public boolean updateDatabase(AbstractWaypoint WP) {
        return false;
    }

    @Override
    public void resetStartWaypoint(AbstractCache abstractCache, AbstractWaypoint except) {

    }

    @Override
    public void delete(Database database, AbstractWaypoint waypoint) {

    }

    private int createCheckSum(Database database, AbstractWaypoint WP) {
        // for Replication
        String sCheckSum = WP.getGcCode().toString();
        sCheckSum += UnitFormatter.formatLatitudeDM(WP.getLatitude());
        sCheckSum += UnitFormatter.formatLongitudeDM(WP.getLongitude());
        sCheckSum += WP.getDescription(database);
        sCheckSum += WP.getType().ordinal();
        sCheckSum += WP.getClue(database);
        sCheckSum += WP.getTitle();
        if (WP.isStart())
            sCheckSum += "1";
        return (int) Utils.sdbm(sCheckSum);
    }
}
