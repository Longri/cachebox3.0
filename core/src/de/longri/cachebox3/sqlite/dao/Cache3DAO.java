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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Longri on 20.10.2017.
 */
public class Cache3DAO extends AbstractCacheDAO {

    private final static Logger log = LoggerFactory.getLogger(Cache3DAO.class);
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public AbstractWaypointDAO getWaypointDAO() {
        return new Waypoint3DAO();
    }

    @Override
    public void writeToDatabase(Database database, AbstractCache abstractCache) {

        //Write to CacheCoreInfo Table

        Database.Parameters args = new Database.Parameters();
        args.put("Id", abstractCache.getId());
        args.put("Latitude", abstractCache.latitude);
        args.put("Longitude", abstractCache.longitude);
        args.put("Size", abstractCache.getSize().ordinal());
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

        if (database.insert("CacheCoreInfo", args) <= 0) {
            //Cache not inserted, can't write other information's!
            log.error("Cache {} not inserted on CacheCoreInfo table", abstractCache.toString());
            return;
        }

        //Write to CacheInfo table
        args.clear();
        args.put("Id", abstractCache.getId());
        args.put("DateHidden", iso8601Format.format(abstractCache.getDateHidden() == null ? new Date() : abstractCache.getDateHidden()));
        args.put("FirstImported", iso8601Format.format(new Date()));
        args.put("TourName", abstractCache.getTourName());
        args.put("GPXFilename_Id", abstractCache.getGPXFilename_ID());
        args.put("state", abstractCache.getState());
        args.put("country", abstractCache.getCountry());
        args.put("ApiStatus", abstractCache.getApiState());
        if (database.insert("CacheInfo", args) <= 0) {
            //CacheInfo not inserted, can't write other information's!
            log.error("Cache {} not inserted on CacheInfo table", abstractCache.toString());
        }

        //Write to CacheText table
        args.clear();
        args.put("Id", abstractCache.getId());
        args.put("Url", abstractCache.getUrl(database));
        args.put("Hint", abstractCache.getHint(database));
        args.put("Description", abstractCache.getLongDescription(database));
        args.put("Notes", abstractCache.getTmpNote());
        args.put("Solver", abstractCache.getTmpSolver());
        args.put("ShortDescription", abstractCache.getShortDescription(database));
        if (database.insert("CacheText", args) <= 0) {
            //CacheInfo not inserted, can't write other information's!
            log.error("Cache {} not inserted on CacheText table", abstractCache.toString());
        }

        //Write to Attributes table
        args.clear();
        args.put("Id", abstractCache.getId());
        if (abstractCache.getAttributesPositive() != null) {
            args.put("AttributesPositive", abstractCache.getAttributesPositive().getLow());
            args.put("AttributesPositiveHigh", abstractCache.getAttributesPositive().getHigh());
        }
        if (abstractCache.getAttributesNegative() != null) {
            args.put("AttributesNegative", abstractCache.getAttributesNegative().getLow());
            args.put("AttributesNegativeHigh", abstractCache.getAttributesNegative().getHigh());
        }

        if (database.insert("Attributes", args) <= 0) {
            //CacheInfo not inserted, can't write other information's!
            log.error("Cache {} not inserted on Attributes table", abstractCache.toString());
        }
        args.clear();

        //store Waypoints
        Array<AbstractWaypoint> waypoints = abstractCache.getWaypoints();
        if (waypoints != null) {
            AbstractWaypointDAO WDAO = getWaypointDAO();

            int n = waypoints.size;
            while (n-- > 0) {
                AbstractWaypoint wp = waypoints.get(n);
                WDAO.writeToDatabase(database, wp);
            }
        }
    }

    @Override
    public void writeToDatabaseFound(Database database, AbstractCache abstractCache) {

    }

    @Override
    public boolean updateDatabase(Database database, AbstractCache abstractCache) {
        return false;
    }

    @Override
    public AbstractCache getFromDbByCacheId(Database database, long cacheID, boolean withWaypoints) {
        String statement = "SELECT * from CacheCoreInfo WHERE Id=?";
        SQLiteGdxDatabaseCursor cursor = database.rawQuery(statement, new String[]{String.valueOf(cacheID)});
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            AbstractCache cache = new ImmutableCache(cursor);
            if (withWaypoints) {
                cache.setWaypoints(getWaypointDAO().getWaypointsFromCacheID(database, cacheID, true));
            }
            return cache;
        }
        return null;
    }

    @Override
    public boolean updateDatabaseCacheState(Database database, AbstractCache writeTmp) {
        return false;
    }
}