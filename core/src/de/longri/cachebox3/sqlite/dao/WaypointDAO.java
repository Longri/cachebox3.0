/*
 * Copyright (C) 2014-2017 team-cachebox.de
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
import de.longri.cachebox3.sqlite.Database.Parameters;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.UnitFormatter;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaypointDAO extends AbstractWaypointDAO {
    private static final Logger log = LoggerFactory.getLogger(WaypointDAO.class);

    public static final String SQL_WP = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart from Waypoint";
    public static final String SQL_WP_FULL = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart, Description, Clue from Waypoint";

    @Override
    public void writeToDatabase(AbstractWaypoint WP) {
        writeToDatabase(WP, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public void writeToDatabase(AbstractWaypoint WP, boolean useReplication) {
        int newCheckSum = createCheckSum(WP);
        if (useReplication) {
            Replication.WaypointNew(WP.getCacheId(), WP.getCheckSum(), newCheckSum, WP.getGcCode().toString());
        }
        Parameters args = new Parameters();
        args.put("gccode", WP.getGcCode());
        args.put("cacheid", WP.getCacheId());
        args.put("latitude", WP.latitude);
        args.put("longitude", WP.longitude);
        args.put("description", WP.getDescription(Database.Data));
        args.put("type", WP.getType().ordinal());
        args.put("syncexclude", WP.isSyncExcluded());
        args.put("userwaypoint", WP.isUserWaypoint());
        if (WP.getClue(Database.Data) == null)
            WP.setClue("");
        args.put("clue", WP.getClue(Database.Data));
        args.put("title", WP.getTitle());
        args.put("isStart", WP.isStart());

        try {
            long count = Database.Data.insert("Waypoint", args);
            if (count <= 0) {
                Database.Data.update("Waypoint", args, "gccode=\"" + WP.getGcCode() + "\"", null);
            }
            if (WP.isUserWaypoint()) {
                // HasUserData nicht updaten wenn der Waypoint kein UserWaypoint ist!!!
                args = new Parameters();
                args.put("hasUserData", true);
                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(WP.getCacheId())});
            }
        } catch (Exception exc) {
            return;

        }
    }

    @Override
    public boolean updateDatabase(AbstractWaypoint WP) {
        return updateDatabase(WP, false);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public boolean updateDatabase(AbstractWaypoint WP, boolean useReplication) {
        boolean result = false;
        int newCheckSum = createCheckSum(WP);
        if (useReplication) {
            Replication.WaypointChanged(WP.getCacheId(), WP.getCheckSum(), newCheckSum, WP.getGcCode().toString());
        }
        if (newCheckSum != WP.getCheckSum()) {
            Parameters args = new Parameters();
            args.put("gccode", WP.getGcCode());
            args.put("cacheid", WP.getCacheId());
            args.put("latitude", WP.latitude);
            args.put("longitude", WP.longitude);
            args.put("description", WP.getDescription(Database.Data));
            args.put("type", WP.getType().ordinal());
            args.put("syncexclude", WP.isSyncExcluded());
            args.put("userwaypoint", WP.isUserWaypoint());
            args.put("clue", WP.getClue(Database.Data));
            args.put("title", WP.getTitle());
            args.put("isStart", WP.isStart());
            try {
                long count = Database.Data.update("Waypoint", args, "CacheId=" + WP.getCacheId() + " and GcCode=\"" + WP.getGcCode() + "\"", null);
                if (count > 0)
                    result = true;
            } catch (Exception exc) {
                result = false;

            }

            if (WP.isUserWaypoint()) {
                // HasUserData nicht updaten wenn der Waypoint kein UserWaypoint ist (z.B. über API)
                args = new Parameters();
                args.put("hasUserData", true);
                try {
                    Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(WP.getCacheId())});
                } catch (Exception exc) {
                    return result;
                }
            }
            WP.setCheckSum(newCheckSum);
        }
        return result;
    }

    /**
     * Create Waypoint Object from Reader.
     *
     * @param reader
     * @param full   Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return
     */
    public Waypoint getWaypoint(SQLiteGdxDatabaseCursor reader, boolean full) {
        Waypoint WP = null;

        WP = new Waypoint(reader.getDouble(2), reader.getDouble(3), full);

        WP.setGcCode(reader.getString(0));
        WP.setCacheId(reader.getLong(1));
        WP.setType(CacheTypes.values()[reader.getShort(4)]);
        WP.setSyncExcluded(reader.getInt(5) == 1);
        WP.setUserWaypoint(reader.getInt(6) == 1);
        WP.setTitle(reader.getString(7).trim());
        WP.setStart(reader.getInt(8) == 1);

        if (full) {
            WP.setClue(reader.getString(10));
            WP.setDescription(reader.getString(9));
            WP.setCheckSum(createCheckSum(WP));
        }
        return WP;
    }

    private int createCheckSum(AbstractWaypoint WP) {
        // for Replication
        String sCheckSum = WP.getGcCode().toString();
        sCheckSum += UnitFormatter.formatLatitudeDM(WP.latitude);
        sCheckSum += UnitFormatter.formatLongitudeDM(WP.longitude);
        sCheckSum += WP.getDescription(Database.Data);
        sCheckSum += WP.getType().ordinal();
        sCheckSum += WP.getClue(Database.Data);
        sCheckSum += WP.getTitle();
        if (WP.isStart())
            sCheckSum += "1";
        return (int) Utils.sdbm(sCheckSum);
    }

    public void WriteImportToDatabase(Waypoint WP) {
        Parameters args = new Parameters();
        args.put("gccode", WP.getGcCode());
        args.put("cacheid", WP.getCacheId());
        args.put("latitude", WP.latitude);
        args.put("longitude", WP.longitude);
        args.put("description", WP.getDescription(Database.Data));
        args.put("type", WP.getType().ordinal());
        args.put("syncexclude", WP.isSyncExcluded());
        args.put("userwaypoint", WP.isUserWaypoint());
        args.put("clue", WP.getClue(Database.Data));
        args.put("title", WP.getTitle());
        args.put("isStart", WP.isStart());

        try {
            Database.Data.insertWithConflictReplace("Waypoint", args);

            args = new Parameters();
            args.put("hasUserData", true);
            Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(WP.getCacheId())});
        } catch (Exception exc) {
            return;

        }
    }

    // Hier wird überprüft, ob für diesen Cache ein Start-Waypoint existiert und dieser in diesem Fall zurückgesetzt
    // Damit kann bei der Definition eines neuen Start-Waypoints vorher der alte entfernt werden damit sichergestellt ist dass ein Cache nur
    // 1 Start-Waypoint hat
    @Override
    public void resetStartWaypoint(AbstractCache abstractCache, AbstractWaypoint except) {
        for (int i = 0, n = abstractCache.getWaypoints().size; i < n; i++) {
            Waypoint wp = (Waypoint) abstractCache.getWaypoints().get(i);
            if (except == wp)
                continue;
            if (wp.isStart()) {
                wp.setStart(false);
                Parameters args = new Parameters();
                args.put("isStart", false);
                try {
                    long count = Database.Data.update("Waypoint", args, "CacheId=" + wp.getCacheId() + " and GcCode=\"" + wp.getGcCode() + "\"", null);

                } catch (Exception exc) {

                }
            }
        }
    }

    /**
     * Delete all Logs without exist Cache
     */
    public void ClearOrphanedWaypoints() {
        String SQL = "DELETE  FROM  Waypoint WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Waypoint.CacheId = c.Id)";
        Database.Data.execSQL(SQL);
    }

    @Override
    public Array<AbstractWaypoint> getWaypointsFromCacheID(Long CacheID, boolean Full) {
        Array<AbstractWaypoint> wpList = new CB_List<>();
        long aktCacheID = -1;

        StringBuilder sqlState = new StringBuilder(Full ? SQL_WP_FULL : SQL_WP);
        sqlState.append("  where CacheId = ?");

        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery(sqlState.toString(), new String[]{String.valueOf(CacheID)});
        if (reader != null) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                Waypoint wp = getWaypoint(reader, Full);
                if (wp.getCacheId() != aktCacheID) {
                    aktCacheID = wp.getCacheId();
                    wpList = new Array<AbstractWaypoint>();

                }
                wpList.add(wp);
                reader.moveToNext();

            }
            reader.close();
        }

        return wpList;
    }

    @Override
    public void delete(AbstractWaypoint waypoint) {
        try {
            Database.Data.delete("Waypoint", "GcCode='" + waypoint.getGcCode() + "'", null);
        } catch (Exception exc) {
            log.error("delete from dataBase", exc);
        }
    }
}
