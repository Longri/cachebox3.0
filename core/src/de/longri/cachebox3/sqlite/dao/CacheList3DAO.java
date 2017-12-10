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
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheList;
import de.longri.cachebox3.types.ImmutableCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Longri on 19.10.2017.
 */
public class CacheList3DAO extends AbstractCacheListDAO {

    private final Logger log = LoggerFactory.getLogger(CacheList3DAO.class);
    private final int LIMIT = 100;

//    @Override
//    public void readCacheList(final Database database, final CacheList cacheList, String statement, boolean fullDetails, final boolean loadAllWaypoints) {
//
//        if (cacheList == null) throw new RuntimeException("CacheList can't be NULL");
//
//        if (statement == null || statement.isEmpty()) {
//            statement = "SELECT * from CacheCoreInfo";
//        }
//
//        final String msg = Translation.get("LoadCacheList").toString();
//
//        final int count = DaoFactory.CACHE_LIST_DAO.getFilteredCacheCount(database, statement);
//        EventHandler.fire(new IncrementProgressEvent(0, msg, count));
//
//        cacheList.clear();
//
//        final String finalStatement = statement;
//        int limitOffset = 0;
//        int debugCount = 0;
//
//        final AtomicInteger postCount = new AtomicInteger(0);
//        final AtomicInteger readyCount = new AtomicInteger(0);
//        final AtomicInteger cacheCount = new AtomicInteger(0);
//
//        final FileHandle dbFileHandle = database.getFileHandle();
//
//
//        while (limitOffset < count) {
//            debugCount++;
//            postCount.incrementAndGet();
//            final String offset = Integer.toString(limitOffset);
//
//           final Database asyncDb = new Database(Database.DatabaseType.CacheBox3);
//            try {
//                asyncDb.startUp(dbFileHandle);
//            } catch (SQLiteGdxException e) {
//                e.printStackTrace();
//            }
//
//            CB.postAsync(new Runnable() {
//                @Override
//                public void run() {
//                    String query = finalStatement + " LIMIT "
//                            + Integer.toString(LIMIT) + " OFFSET "
//                            + offset;
//                    SQLiteGdxDatabaseCursor cursor = asyncDb.rawQuery(query, null);
//                    cursor.moveToFirst();
//                    while (!cursor.isAfterLast()) {
//                        postCount.incrementAndGet();
//                        final double latitude = cursor.getDouble(1);
//                        final double longitude = cursor.getDouble(2);
//                        final long id = cursor.getLong(0);
//                        final short sizeOrigin = cursor.getShort(3);
//                        final short difficulty = cursor.getShort(4);
//                        final short terrain = cursor.getShort(5);
//                        final short typeOrigin = cursor.getShort(6);
//                        final short rating = cursor.getShort(7);
//                        final short numTravelbugs = cursor.getShort(8);
//                        final String gcCode = cursor.getString(9);
//                        final String name = cursor.getString(10);
//                        final String placedBy = cursor.getString(11);
//                        final String owner = cursor.getString(12);
//                        final String gcId = cursor.getString(13);
//                        final short booleanStore = cursor.getShort(14);
//                        final int favPoints = cursor.getInt(15);
//
////                        CB.postAsync(new Runnable() {
////                            @Override
////                            public void run() {
////                                cacheList.add(new ImmutableCache(loadAllWaypoints ? database : null,
////                                        latitude, longitude, id, sizeOrigin, difficulty, terrain, typeOrigin,
////                                        rating, numTravelbugs, gcCode, name, placedBy, owner, gcId,
////                                        booleanStore, favPoints));
////                                readyCount.incrementAndGet();
////                                EventHandler.fire(new IncrementProgressEvent(cacheCount.incrementAndGet(), msg, count));
////                            }
////                        });
//
//
//                        cacheList.add(new ImmutableCache(loadAllWaypoints ? asyncDb : null,
//                                latitude, longitude, id, sizeOrigin, difficulty, terrain, typeOrigin,
//                                rating, numTravelbugs, gcCode, name, placedBy, owner, gcId,
//                                booleanStore, favPoints));
//                        readyCount.incrementAndGet();
//                        EventHandler.fire(new IncrementProgressEvent(cacheCount.incrementAndGet(), msg, count));
//
//                        cursor.moveToNext();
//
//                    }
//                    cursor.close();
//                }
//            });
//            limitOffset += LIMIT;
//            readyCount.incrementAndGet();
//        }
//
//        log.debug("finish post all {} async loading posts", debugCount);
//        while (postCount.get() > readyCount.get()) {
//            try {
//                Thread.sleep(20);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        log.debug("finish post all {} async loading posts", debugCount);
//    }

    @Override
    public void readCacheList(final Database database, final CacheList cacheList, String statement, boolean fullDetails, boolean loadAllWaypoints) {

        final long startTime = System.currentTimeMillis();


        if (statement == null || statement.isEmpty()) {
            statement = "SELECT * from CacheCoreInfo";
        }

        if (cacheList == null) {
            throw new RuntimeException("CacheList can't be NULL");
        }
        cacheList.clear();


        final AtomicBoolean finishStackFill = new AtomicBoolean(false);
        final String msg = Translation.isInitial() ? Translation.get("LoadCacheList").toString() : "";
        final int count = getFilteredCacheCount(database, statement);
        if (count == 0) return;

        cacheList.ensureCapacity(count);

        final AtomicInteger cacheCount = new AtomicInteger(0);
        final ImmutableCache.CursorData[] cursorDataStack = new ImmutableCache.CursorData[count];
        final AtomicInteger writeCount = new AtomicInteger(-1);
        final AtomicInteger readCount = new AtomicInteger(-1);
        final AtomicBoolean asyncCacheLoadReady = new AtomicBoolean(false);


        final int progressEventcount = count / 200; // fire event every 2% changes

        CB.postAsync(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int tryCount = 0;
                int progressFireCount = 0;

                while (true) {

                    if (readCount.get() < writeCount.get()) {
                        int idx = readCount.incrementAndGet();
                        if (cursorDataStack[idx] == null) {
                            if (tryCount > 1000) {
                                // ignore this Cache
                                log.warn("Cache index: {} are ignored", idx);
                                continue;
                            }
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            readCount.decrementAndGet();
                            tryCount++;
                            continue;
                        }
                        cacheList.add(new ImmutableCache(cursorDataStack[idx]));

                        int actCacheCount = cacheCount.incrementAndGet();
                        progressFireCount++;
                        if (progressFireCount >= progressEventcount) {
                            EventHandler.fire(new IncrementProgressEvent(actCacheCount, msg, count));
                            progressFireCount = 0;
                        }
                    } else {
                        if (finishStackFill.get()) {
                            break;
                        }
//                        log.debug("WAIT FOR CACHE DATA");
                    }
                }
                asyncCacheLoadReady.set(true);
                EventHandler.fire(new IncrementProgressEvent(cacheCount.get(), msg, count));
                log.debug("asyncCacheLoadReady after {} ms", System.currentTimeMillis() - startTime);
            }
        });

        EventHandler.fire(new IncrementProgressEvent(0, msg, count));

        int offset = 0;
        final String finalStatement = statement;
        final AtomicInteger runningAsyncTasks = new AtomicInteger(0);
        while (offset < count) {
            final int finalOffset = offset;
            Runnable runnable = new Runnable() {
                public void run() {
                    String query = finalStatement + " LIMIT "
                            + Integer.toString(LIMIT) + " OFFSET "
                            + finalOffset;
                    SQLiteGdxDatabaseCursor cursor = database.rawQuery(query, null);
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        ImmutableCache.CursorData data = new ImmutableCache.CursorData(cursor);
                        cursorDataStack[writeCount.incrementAndGet()] = data;
                        cursor.moveToNext();
                    }
                    cursor.close();
                    if (runningAsyncTasks.decrementAndGet() == 0) {
                        finishStackFill.set(true);
                        log.debug("finishStackFill after {} ms", System.currentTimeMillis() - startTime);
                    }
                }
            };

            CB.postAsync(runnable);
            runningAsyncTasks.incrementAndGet();
            offset += LIMIT;
        }


        //wait for Async ready
        while (!asyncCacheLoadReady.get()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if (!loadAllWaypoints) {
            float loadingTime = System.currentTimeMillis() - startTime;
            log.debug("Load {} Caches in {}ms ! Without Waypoints", cacheList.size, loadingTime);
            return;
        }

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

        float loadingTime = System.currentTimeMillis() - startTime;
        log.debug("Load {} Caches in {}ms ! With Waypoints", cacheList.size, loadingTime);
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
