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
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.gui.actions.show_activities.Action_Show_SelectDB_Dialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.dialogs.OnMsgBoxClickListener;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheList;
import de.longri.cachebox3.types.ImmutableCache;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqlite;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import de.longri.gdx.sqlite.SQLiteGdxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Longri on 19.10.2017.
 */
public class CacheList3DAO extends AbstractCacheListDAO {

    private final Logger log = LoggerFactory.getLogger(CacheList3DAO.class);

    @Override
    public void readCacheList(final Database database, final CacheList cacheList, String statement, boolean fullDetails, boolean loadAllWaypoints) {

        final long startTime = System.currentTimeMillis();


        if (statement == null || statement.isEmpty()) {
            statement = "SELECT * from CacheCoreInfo";
        }

        if (cacheList == null) {
            throw new RuntimeException("CacheList can't be NULL");
        }

        final String msg = Translation.isInitial() ? Translation.get("LoadCacheList").toString() : "";
        final int count = getFilteredCacheCount(database, statement);
        if (count == 0) {
            cacheList.clear();
            EventHandler.fire(new IncrementProgressEvent(100, msg, 100));
            return;
        }

        cacheList.clear(count);

        final int progressEventcount = count / 200; // fire event every 2% changes

        try {
            database.rawQuery(statement, new GdxSqlite.RowCallback() {
                int progressFireCount = 0;
                int actCacheCount = 0;

                @Override
                public void newRow(String[] columnName, Object[] value, int[] types) {
                    cacheList.add(new ImmutableCache(value));
                    actCacheCount++;
                    progressFireCount++;
                    if (progressFireCount >= progressEventcount) {
                        EventHandler.fire(new IncrementProgressEvent(actCacheCount, msg, count));
                        progressFireCount = 0;
                    }
                }
            });
        } catch (SQLiteGdxException e) {
            if (e.getMessage().equals("database disk image is malformed")) {
                // if the DB malformed, we inform the User

                CB.scheduleOnGlThread(new NamedRunnable("CacheList3DAO") {
                    @Override
                    public void run() {
                        MessageBox.show(Translation.get("ErrDbStartup"), Translation.get("corruptDB"), MessageBoxButtons.OK, MessageBoxIcon.Error, new OnMsgBoxClickListener() {
                            @Override
                            public boolean onClick(int which, Object data) {
                                //show select DB Dialog
                                CB.postAsync(new NamedRunnable("CacheList3DAO:showSelectDbDialog") {
                                    @Override
                                    public void run() {
                                        new Action_Show_SelectDB_Dialog(Action_Show_SelectDB_Dialog.ViewMode.ASK).execute();
                                    }
                                });
                                return true;
                            }
                        });
                    }
                }, 500);
                return;
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventHandler.fire(new IncrementProgressEvent(count, msg, count));
        log.debug("CacheLoadReady after {} ms", System.currentTimeMillis() - startTime);

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

        GdxSqliteCursor cursor = database.rawQuery(statement, new String[]{Long.toString(cache.getId())});
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
        GdxSqliteCursor cursor = database.rawQuery(statement, (String[]) null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    @Override
    public void writeToDB(Database database, CacheList cacheList) {
        //create statements
        GdxSqlitePreparedStatement REPLACE_CORE_INFO = database.myDB.prepare("REPLACE INTO CacheCoreInfo VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ;");
        GdxSqlitePreparedStatement REPLACE_INFO = database.myDB.prepare("REPLACE INTO CacheInfo VALUES(?,?,?,?,?,?,?,?,?) ;");
        GdxSqlitePreparedStatement REPLACE_CACHE_TEXT = database.myDB.prepare("REPLACE INTO CacheText VALUES(?,?,?,?,?,?,?) ;");
        GdxSqlitePreparedStatement REPLACE_ATTRIBUTES = database.myDB.prepare("REPLACE INTO Attributes VALUES(?,?,?,?,?) ;");

        database.myDB.beginTransaction();
        try {
            for (AbstractCache ca : cacheList) {
                REPLACE_CORE_INFO.bind(
                        ca.getId(),
                        ca.getLatitude(),
                        ca.getLongitude(),
                        ca.getSize().ordinal(),
                        ((int) (ca.getDifficulty() * 2)),
                        ((int) (ca.getTerrain() * 2)),
                        ca.getType().ordinal(),
                        ((int) (ca.getRating() * 200)),
                        ca.getNumTravelbugs(),
                        ca.getGcCode(),
                        ca.getName(),
                        ca.getPlacedBy(),
                        ca.getOwner(),
                        ca.getGcId(),
                        ca.getBooleanStore(),
                        ca.getFavoritePoints(),
                        (int) (ca.getRating() * 2)
                ).commit().reset();

                REPLACE_ATTRIBUTES.bind(
                        ca.getId(),
                        ca.getAttributesPositive() == null ? 0 : ca.getAttributesPositive().getLow(),
                        ca.getAttributesNegative() == null ? 0 : ca.getAttributesNegative().getLow(),
                        ca.getAttributesPositive() == null ? 0 : ca.getAttributesPositive().getHigh(),
                        ca.getAttributesNegative() == null ? 0 : ca.getAttributesNegative().getHigh()
                ).commit().reset();

                REPLACE_INFO.bind(
                        ca.getId(),
                        Database.cbDbFormat.format(ca.getDateHidden(database) == null ? new Date() : ca.getDateHidden(database)),
                        Database.cbDbFormat.format(new Date()),
                        ca.getTourName(),
                        ca.getGPXFilename_ID(),
                        null, //todo handle listing checksum
                        ca.getState(database),
                        ca.getCountry(database),
                        ca.getApiState(database)
                ).commit().reset();


                REPLACE_CACHE_TEXT.bind(
                        ca.getId(),
                        ca.getUrl(database),
                        ca.getHint(database),
                        ca.getLongDescription(database),
                        ca.getTmpNote(database),
                        ca.getTmpSolver(),
                        ca.getShortDescription(database)
                ).commit().reset();


            }
        } finally {
            database.myDB.endTransaction();
        }


        //store Waypoints
        Array<AbstractWaypoint> allWaypoints = new Array<>();
        for (AbstractCache ca : cacheList) {
            //store Waypoints
            Array<AbstractWaypoint> waypoints = ca.getWaypoints();
            if (waypoints != null) {
                int n = waypoints.size;
                while (n-- > 0) {
                    AbstractWaypoint wp = waypoints.get(n);
                    wp.setCacheId(ca.getId());
                    allWaypoints.add(wp);
                }
            }
        }

        GdxSqlitePreparedStatement REPLACE_WAYPOINTS = database.myDB.prepare("REPLACE INTO Waypoints VALUES(?,?,?,?,?,?,?,?,?) ;");
        GdxSqlitePreparedStatement REPLACE_WAYPOINTS_TEXT = database.myDB.prepare("REPLACE INTO WaypointsText VALUES(?,?,?) ;");

        database.myDB.beginTransaction();
        try {
            for (AbstractWaypoint wp : allWaypoints) {
                REPLACE_WAYPOINTS.bind(
                        wp.getCacheId(),
                        wp.getGcCode(),
                        wp.getLatitude(),
                        wp.getLongitude(),
                        wp.getType().ordinal(),
                        wp.isStart(),
                        wp.isSyncExcluded(),
                        wp.isUserWaypoint(),
                        wp.getTitle()
                ).commit().reset();

                REPLACE_WAYPOINTS_TEXT.bind(
                        wp.getGcCode(),
                        wp.getDescription(database),
                        wp.getClue(database)
                ).commit().reset();
            }
        } finally {
            database.myDB.endTransaction();
        }

    }
}
