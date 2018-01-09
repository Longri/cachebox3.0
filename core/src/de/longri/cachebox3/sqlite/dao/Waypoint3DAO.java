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

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.UnitFormatter;
import de.longri.gdx.sqlite.GdxSqliteCursor;

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
        GdxSqliteCursor cursor = database.rawQuery(where, args);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ImmutableWaypoint wp = new ImmutableWaypoint(cursor);
            waypoints.add(wp);
            cursor.moveToNext();
        }
        cursor.close();
        return waypoints;
    }


    @Override
    public void writeToDatabase(Database database, AbstractWaypoint wp) {
        writeToDatabase(database, wp, false);
    }

    @Override
    public boolean updateDatabase(Database database, AbstractWaypoint wp) {
        return writeToDatabase(database, wp, true);
    }

    private boolean writeToDatabase(Database database, AbstractWaypoint wp, boolean update) {
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

        if (!update) args2.put("GcCode", wp.getGcCode());
        args2.put("Description", wp.getDescription(database));
        args2.put("Clue", wp.getClue(database));

        boolean updated = false;
        if (update || database.insert("Waypoints", args) <= 0) {

            //remove GcCode from args2
            args2.remove("GcCode");

            updated = 0 < database.update("Waypoints", args, "GcCode=\"" + wp.getGcCode() + "\"", null);
            boolean textUpdated = 0 < database.update("WaypointsText", args2, "GcCode=\"" + wp.getGcCode() + "\"", null);
            updated = updated || textUpdated;
        } else {
            database.insert("WaypointsText", args2);
        }

        checkUserWaypointFlag(database, wp);

        return updated;
    }


    @Override
    public void resetStartWaypoint(AbstractCache abstractCache, AbstractWaypoint except) {

    }

    @Override
    public void delete(Database database, AbstractWaypoint waypoint) {

        //delete from Waypoints table
        database.delete("Waypoints", "GcCode='" + waypoint.getGcCode() + "'");

        //delete from WaypointsText table
        database.delete("WaypointsText", "GcCode='" + waypoint.getGcCode() + "'");
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

    private void checkUserWaypointFlag(Database database, AbstractWaypoint wp) {

        if (wp.isUserWaypoint()) {
            String[] cacheIdString = new String[]{String.valueOf(wp.getCacheId())};

            //read booleanStore of Cache
            GdxSqliteCursor cursor = database.rawQuery("SELECT BooleanStore from CacheCoreInfo WHERE Id=?", cacheIdString);
            if (cursor == null) return; // Cache not exists
            cursor.moveToFirst();
            short booleanStore = cursor.getShort(0);
            cursor.close();
            if (ImmutableCache.getMaskValue(ImmutableCache.MASK_HAS_USER_DATA, booleanStore)) {
                // HasUserData is set, return!
                return;
            }

            ImmutableCache.setMaskValue(ImmutableCache.MASK_HAS_USER_DATA, true, booleanStore);

            //Set 'HasUserData' on Cache table
            Database.Parameters args = new Database.Parameters();
            args.put("BooleanStore", booleanStore);
            database.update("CacheCoreInfo", args, "Id = ?", cacheIdString);
        }
    }
}
