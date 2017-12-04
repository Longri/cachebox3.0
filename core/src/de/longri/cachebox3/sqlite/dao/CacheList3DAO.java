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
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
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
    private final int LIMIT = 500;

    @Override
    public void readCacheList(final Database database, final CacheList cacheList, String statement, boolean fullDetails, final boolean loadAllWaypoints) {

        if (cacheList == null) throw new RuntimeException("CacheList can't be NULL");

        if (statement == null || statement.isEmpty()) {
            statement = "SELECT * from CacheCoreInfo";
        }

        int count = DaoFactory.CACHE_LIST_DAO.getFilteredCacheCount(database, statement);
        EventHandler.fire(new IncrementProgressEvent(0, "", count));

        cacheList.clear();

        final String finalStatement = statement;
        int limitOffset = 0;
        int debugCount = 0;
        while (limitOffset < count) {
            debugCount++;
            final String offset = Integer.toString(limitOffset);
            CB.postAsync(new Runnable() {
                @Override
                public void run() {
                    String query = finalStatement + " LIMIT "
                            + Integer.toString(LIMIT) + " OFFSET "
                            + offset;
                    SQLiteGdxDatabaseCursor cursor = database.rawQuery(query, null);
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        final double latitude = cursor.getDouble(1);
                        final double longitude = cursor.getDouble(2);
                        final long id = cursor.getLong(0);
                        final short sizeOrigin = cursor.getShort(3);
                        final short difficulty = cursor.getShort(4);
                        final short terrain = cursor.getShort(5);
                        final short typeOrigin = cursor.getShort(6);
                        final short rating = cursor.getShort(7);
                        final short numTravelbugs = cursor.getShort(8);
                        final String gcCode = cursor.getString(9);
                        final String name = cursor.getString(10);
                        final String placedBy = cursor.getString(11);
                        final String owner = cursor.getString(12);
                        final String gcId = cursor.getString(13);
                        final short booleanStore = cursor.getShort(14);
                        final int favPoints = cursor.getInt(15);

                        CB.postAsync(new Runnable() {
                            @Override
                            public void run() {
                                cacheList.add(new ImmutableCache(loadAllWaypoints ? database : null,
                                        latitude, longitude, id, sizeOrigin, difficulty, terrain, typeOrigin,
                                        rating, numTravelbugs, gcCode, name, placedBy, owner, gcId, booleanStore, favPoints));
                            }
                        });

                        cursor.moveToNext();
                    }
                    cursor.close();

                }
            });
            limitOffset += LIMIT;
        }

        log.debug("finish post all {} async loading posts", debugCount);

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

    @Override
    public int getFilteredCacheCount(Database database, String statement) {
        if (statement == null || statement.isEmpty()) {
            statement = "SELECT COUNT(*) FROM CacheCoreInfo";
        } else {
            statement = statement.replace("SELECT *", "SELECT COUNT(*)");
        }
        SQLiteGdxDatabaseCursor cursor = database.rawQuery(statement, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
