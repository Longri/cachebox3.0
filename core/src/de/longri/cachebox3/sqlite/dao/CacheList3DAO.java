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
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.ImmutableCache;
import de.longri.cachebox3.types.CacheList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Longri on 19.10.2017.
 */
public class CacheList3DAO extends AbstractCacheListDAO {

    private final Logger log = LoggerFactory.getLogger(CacheList3DAO.class);

    @Override
    public CacheList readCacheList(Database database, CacheList cacheList, String where, boolean fullDetails, boolean loadAllWaypoints) {

        String statement = "SELECT * from CacheCoreInfo" + (where == null || where.isEmpty() ? "" : " WHERE " + where);

        if (cacheList == null) {
            cacheList = new CacheList();
        } else {
            cacheList.clear();
        }

        SQLiteGdxDatabaseCursor cursor = database.rawQuery(statement, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            cacheList.add(new ImmutableCache(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        if (!loadAllWaypoints) return cacheList;

        //read waypoints
        Array<AbstractWaypoint> waypoints = new Waypoint3DAO().getWaypointsFromCacheID(database, null, true);

        int n = cacheList.size - 1;
        int i = 0;
        while (n-- >= 0) {
            ImmutableCache cache = (ImmutableCache) cacheList.get(i++);
            Array<AbstractWaypoint> cachewaypoints = new Array<>();
            int m = waypoints.size - 1;
            int j = 0;
            while (m-- >= 0) {
                AbstractWaypoint waypoint = waypoints.get(j++);
                if (waypoint.getCacheId() == cache.getId()) {
                    cachewaypoints.add(waypoint);
                }
            }
            cache.setWaypoints(cachewaypoints);
        }
        return cacheList;
    }

    @Override
    public AbstractCache reloadCache(Database database, CacheList cacheList, AbstractCache cache) {
        String statement = "SELECT * from CacheCoreInfo WHERE id=?";

        SQLiteGdxDatabaseCursor cursor = database.rawQuery(statement, new String[]{Long.toString(cache.getId())});
        cursor.moveToFirst();
        ImmutableCache newCache = null;
        while (!cursor.isAfterLast()) {
            newCache = new ImmutableCache(cursor);
            cursor.moveToNext();
        }
        cursor.close();

        if (newCache == null) {
            log.warn("Can't reload Cache! Can't find on DB");
            return null;
        }

        //read waypoints
        Array<AbstractWaypoint> waypoints = new Waypoint3DAO().getWaypointsFromCacheID(database, cache.getId(), true);
        newCache.setWaypoints(waypoints);


        //remove cache by id
        AbstractCache remove = cacheList.getCacheById(cache.getId());
        cacheList.removeValue(remove, true);
        cacheList.add(newCache);
        return newCache;
    }
}
