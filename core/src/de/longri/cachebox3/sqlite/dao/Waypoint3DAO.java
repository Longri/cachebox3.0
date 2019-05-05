/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.MutableCache;
import de.longri.cachebox3.types.MutableWaypoint;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.UnitFormatter;
import de.longri.gdx.sqlite.GdxSqliteCursor;

/**
 * Created by Longri on 20.10.2017.
 */
public class Waypoint3DAO extends AbstractWaypointDAO {

    private final String GET_ALL_WAYPOINTS = "SELECT * FROM Waypoints";
    private final String GET_ALL_WAYPOINTS_FROM_CACHE = "SELECT * FROM Waypoints WHERE CacheId=?";
    private final String GET_WAYPOINT_TEXT = "SELECT * FROM WaypointsText WHERE GcCode=?";

    @Override
    public Array<AbstractWaypoint> getWaypointsFromCacheID(Database database, Long cacheID, boolean full) {

        String[] args = cacheID == null ? null : new String[]{String.valueOf(cacheID)};
        String where = cacheID == null ? GET_ALL_WAYPOINTS : GET_ALL_WAYPOINTS_FROM_CACHE;
        Array<AbstractWaypoint> waypoints = new Array<>();
        GdxSqliteCursor cursor = database.rawQuery(where, args);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                MutableWaypoint wp = new MutableWaypoint(cursor);
                if (full) {
                    GdxSqliteCursor wpTextCursor = database.rawQuery(GET_WAYPOINT_TEXT, new String[]{String.valueOf(wp.getGcCode())});
                    if (wpTextCursor != null) {
                        wpTextCursor.moveToFirst();
                        wp.setText(wpTextCursor);
                        wpTextCursor.close();
                    }
                }
                waypoints.add(wp);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return waypoints;
    }


    @Override
    public void writeToDatabase(Database database, AbstractWaypoint wp, boolean fireChangedEvent) {
        writeToDatabase(database, wp, false, fireChangedEvent);
    }

    @Override
    public boolean updateDatabase(Database database, AbstractWaypoint wp, boolean fireChangedEvent) {
        return writeToDatabase(database, wp, true, fireChangedEvent);
    }

    private boolean writeToDatabase(Database database, AbstractWaypoint wp, boolean update, boolean fireChangedEvent) {
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
        args2.put("Description", wp.getDescription());
        args2.put("Clue", wp.getClue());

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
        waypointListChanged(database, wp, false, fireChangedEvent);
        return updated;
    }


    @Override
    public void resetStartWaypoint(AbstractCache abstractCache, AbstractWaypoint except) {

    }

    @Override
    public void delete(Database database, AbstractWaypoint waypoint, boolean fireChangedEvent) {

        //delete from Waypoints table
        database.delete("Waypoints", "GcCode='" + waypoint.getGcCode() + "'");

        //delete from WaypointsText table
        database.delete("WaypointsText", "GcCode='" + waypoint.getGcCode() + "'");

        waypointListChanged(database, waypoint, true, fireChangedEvent);
    }

    private int createCheckSum(Database database, AbstractWaypoint WP) {
        // for Replication
        String sCheckSum = WP.getGcCode().toString();
        sCheckSum += UnitFormatter.formatLatitudeDM(WP.getLatitude());
        sCheckSum += UnitFormatter.formatLongitudeDM(WP.getLongitude());
        sCheckSum += WP.getDescription();
        sCheckSum += WP.getType().ordinal();
        sCheckSum += WP.getClue();
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
            if (MutableCache.getMaskValue(MutableCache.MASK_HAS_USER_DATA, booleanStore)) {
                // HasUserData is set, return!
                return;
            }

            MutableCache.setMaskValue(MutableCache.MASK_HAS_USER_DATA, true, booleanStore);

            //Set 'HasUserData' on Cache table
            Database.Parameters args = new Database.Parameters();
            args.put("BooleanStore", booleanStore);
            database.update("CacheCoreInfo", args, "Id = ?", cacheIdString);
        }
    }

    private void waypointListChanged(Database database, AbstractWaypoint wp, boolean delete, boolean fireChangedEvent) {
        AbstractCache cache = database.cacheList.getCacheById(wp.getCacheId());
        if (cache != null && cache.getWaypoints() != null) {
            if (delete) {
                cache.getWaypoints().removeValue(wp, false);
            } else {
                cache.getWaypoints().add(wp);
            }
            if (fireChangedEvent) {
                CB.postAsyncDelayd(100, new NamedRunnable("Call CacheListChanged Event") {
                    @Override
                    public void run() {
                        EventHandler.fire(new CacheListChangedEvent());
                    }
                });
            }
        }
    }
}
