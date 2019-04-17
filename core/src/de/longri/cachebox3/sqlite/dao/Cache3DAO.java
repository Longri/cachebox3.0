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
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.MutableCache;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by Longri on 20.10.2017.
 */
public class Cache3DAO extends AbstractCacheDAO {

    private final static Logger log = LoggerFactory.getLogger(Cache3DAO.class);


    @Override
    public AbstractWaypointDAO getWaypointDAO() {
        return new Waypoint3DAO();
    }

    @Override
    public boolean updateDatabase(Database database, AbstractCache abstractCache, boolean fireChangedEvent) {
        return writeOrUpdate(true, database, abstractCache, fireChangedEvent);
    }

    @Override
    public void writeToDatabase(Database database, AbstractCache abstractCache, boolean fireChangedEvent) {

        if (database == null || abstractCache == null) return;

        //check for update
        GdxSqliteCursor cousor = database.rawQuery("SELECT id FROM CacheCoreInfo WHERE id=" + abstractCache.getId());
        boolean update = false;
        if (cousor != null) {
            update = true;
            cousor.close();
        }
        writeOrUpdate(update, database, abstractCache, fireChangedEvent);
    }

    private boolean writeOrUpdate(boolean update, Database database, AbstractCache abstractCache, boolean fireChangedEvent) {

        if (database == null || abstractCache == null) return false;

        boolean noError = true;

        //Write to CacheCoreInfo Table
        Database.Parameters args = new Database.Parameters();
        if (!update) args.put("Id", abstractCache.getId());
        args.put("Latitude", abstractCache.getLatitude());
        args.put("Longitude", abstractCache.getLongitude());
        args.put("Size", abstractCache.getSize() != null ? abstractCache.getSize().ordinal() : 0);
        args.put("Difficulty", (int) (abstractCache.getDifficulty() * 2));
        args.put("Terrain", (int) (abstractCache.getTerrain() * 2));
        args.put("Type", abstractCache.getType().ordinal());
        args.put("Rating", (int) (abstractCache.getRating() * 200));
        args.put("NumTravelbugs", abstractCache.getNumTravelbugs());
        args.put("GcCode", abstractCache.getGcCode());
        args.put("Name", abstractCache.getName());
        args.put("PlacedBy", abstractCache.getPlacedBy());
        args.put("Owner", abstractCache.getOwner());
        args.put("GcId", abstractCache.getGcId());
        args.put("BooleanStore", abstractCache.getBooleanStore());
        args.put("FavPoints", abstractCache.getFavoritePoints());
        args.put("Vote", (int) (abstractCache.getRating() * 2));

        if (args.size() > 0) {
            if (update) {
                if (database.update("CacheCoreInfo", args, "WHERE id=?", new String[]{Long.toString(abstractCache.getId())}) <= 0) {
                    return false;
                }
            } else {
                if (database.insert("CacheCoreInfo", args) <= 0) {
                    //Cache not inserted, can't write other information's!
                    log.error("Cache {} not inserted on CacheCoreInfo table", abstractCache.toString());
                    return false;
                }
            }
        }


        //Write to CacheInfo table
        args.clear();
        if (!update) args.put("Id", abstractCache.getId());

        Date dateHidden = abstractCache.getDateHidden();
        if (dateHidden == null) dateHidden = new Date();
        String dateString = Database.cbDbFormat.format(dateHidden);

        args.put("DateHidden", dateString);
        args.put("FirstImported", Database.cbDbFormat.format(new Date()));
        args.put("TourName", abstractCache.getTourName());
        args.put("GPXFilename_Id", abstractCache.getGPXFilename_ID());
        args.put("state", abstractCache.getState());
        args.put("country", abstractCache.getCountry());
        args.put("ApiStatus", abstractCache.getApiState());

        if (args.size() > 0) {
            if (update) {
                if (database.update("CacheInfo", args, "WHERE id=?", new String[]{Long.toString(abstractCache.getId())}) <= 0) {
                    //try to insert
                    args.put("Id", abstractCache.getId());
                    if (database.insert("CacheInfo", args) <= 0) {
                        noError = false;
                    }
                }
            } else {
                if (database.insert("CacheInfo", args) <= 0) {
                    //CacheInfo not inserted, can't write other information's!
                    log.error("Cache {} not inserted on CacheInfo table", abstractCache.toString());
                    noError = false;
                }
            }
        }


        //Write to CacheText table
        args.clear();
        if (!update) args.put("Id", abstractCache.getId());
        args.put("Url", abstractCache.getUrl());
        args.put("Hint", abstractCache.getHint());
        args.put("Description", abstractCache.getLongDescription());
        args.put("Notes", abstractCache.getTmpNote());
        args.put("Solver", abstractCache.getTmpSolver());
        args.put("ShortDescription", abstractCache.getShortDescription());

        if (args.size() > 0) {
            if (update) {
                if (database.update("CacheText", args, "WHERE id=?", new String[]{Long.toString(abstractCache.getId())}) <= 0) {
                    //try to insert
                    args.put("Id", abstractCache.getId());
                    if (database.insert("CacheText", args) <= 0) {
                        noError = false;
                    }
                }
            } else {
                if (database.insert("CacheText", args) <= 0) {
                    //CacheInfo not inserted, can't write other information's!
                    log.error("Cache {} not inserted on CacheText table", abstractCache.toString());
                    noError = false;
                }
            }
        }


        //Write to Attributes table
        args.clear();
        if (!update) args.put("Id", abstractCache.getId());
        if (abstractCache.getAttributesPositive() != null) {
            args.put("AttributesPositive", abstractCache.getAttributesPositive().getLow());
            args.put("AttributesPositiveHigh", abstractCache.getAttributesPositive().getHigh());
        }
        if (abstractCache.getAttributesNegative() != null) {
            args.put("AttributesNegative", abstractCache.getAttributesNegative().getLow());
            args.put("AttributesNegativeHigh", abstractCache.getAttributesNegative().getHigh());
        }


        if (args.size() > 0) {
            if (update) {
                if (database.update("Attributes", args, "WHERE id=?", new String[]{Long.toString(abstractCache.getId())}) <= 0) {
                    //try to insert
                    args.put("Id", abstractCache.getId());
                    if (database.insert("Attributes", args) <= 0) {
                        noError = false;
                    }
                }
            } else {
                if (database.insert("Attributes", args) <= 0) {
                    //CacheInfo not inserted, can't write other information's!
                    log.error("Cache {} not inserted on Attributes table", abstractCache.toString());
                    noError = false;
                }
            }
        }

        args.clear();

        //store Waypoints
        Array<AbstractWaypoint> waypoints = abstractCache.getWaypoints();
        if (waypoints != null) {
            AbstractWaypointDAO WDAO = getWaypointDAO();

            int n = waypoints.size;
            while (n-- > 0) {
                AbstractWaypoint wp = waypoints.get(n);
                if (update) {
                    if (!WDAO.updateDatabase(database, wp, fireChangedEvent)) {
                        WDAO.writeToDatabase(database, wp, fireChangedEvent);
                    }
                } else {
                    WDAO.writeToDatabase(database, wp, fireChangedEvent);
                }

            }
        }
        return noError;
    }

    @Override
    public AbstractCache getFromDbByCacheId(Database database, long cacheID, boolean withWaypoints, boolean fullData) {
        String statement = "SELECT * from CacheCoreInfo WHERE Id=?";
        GdxSqliteCursor cursor = database.rawQuery(statement, new String[]{String.valueOf(cacheID)});
        if (cursor == null) return null;
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            AbstractCache cache = new MutableCache(cursor);
            if (withWaypoints) {
                cache.setWaypoints(getWaypointDAO().getWaypointsFromCacheID(database, cacheID, fullData));
            }
            cursor.close();
            return getFullData(database, cache);
        }
        cursor.close();
        return null;
    }

    @Override
    public void writeCacheBooleanStore(Database database, int newBooleanStore, long id) {
        Database.Parameters args = new Database.Parameters();
        args.put("BooleanStore", newBooleanStore);
        if (database.update("CacheCoreInfo", args, " Id=?", new String[]{Long.toString(id)}) <= 0) {
            log.error("Can't update booleanStore");
        }
    }

    @Override
    public AbstractCache getFromDbByGcCode(Database database, String gcCode, boolean withWaypoints, boolean fullData) {
        String statement = "SELECT * from CacheCoreInfo WHERE GcCode=?";
        GdxSqliteCursor cursor = database.rawQuery(statement, new String[]{String.valueOf(gcCode)});
        if (cursor == null) return null;
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            AbstractCache cache = new MutableCache(cursor);
            if (withWaypoints) {
                cache.setWaypoints(getWaypointDAO().getWaypointsFromCacheID(database, cache.getId(), fullData));
            }
            cursor.close();
            return getFullData(database, cache);
        }
        cursor.close();
        return null;
    }

    private AbstractCache getFullData(Database database, AbstractCache cache) {
        String statement = "SELECT * from CacheInfo WHERE Id=?";
        GdxSqliteCursor cursor = database.rawQuery(statement, new String[]{String.valueOf(cache.getId())});
        if (cursor != null) {
            cursor.moveToFirst();
            cache.setInfo(cursor);
        }
        cursor.close();

        statement = "SELECT * from CacheText WHERE Id=?";
        cursor = database.rawQuery(statement, new String[]{String.valueOf(cache.getId())});
        if (cursor != null) {
            cursor.moveToFirst();
            cache.setText(cursor);
        }
        cursor.close();

        statement = "SELECT * from Attributes WHERE Id=?";
        cursor = database.rawQuery(statement, new String[]{String.valueOf(cache.getId())});
        if (cursor != null) {
            cursor.moveToFirst();
            cache.setAttributes(cursor);
        }
        cursor.close();
        return cache;
    }

}
