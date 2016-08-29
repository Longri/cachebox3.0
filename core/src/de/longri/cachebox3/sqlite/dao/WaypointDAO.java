/*
 * Copyright (C) 2014-2016 team-cachebox.de
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


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.Database.Parameters;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.Replication;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.utils.UnitFormatter;
import de.longri.cachebox3.utils.lists.CB_List;

public class WaypointDAO {

    public static final String SQL_WP = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart from Waypoint";
    public static final String SQL_WP_FULL = "select GcCode, CacheId, Latitude, Longitude, Type, SyncExclude, UserWaypoint, Title, isStart, Description, Clue from Waypoint";

    public void WriteToDatabase(Waypoint WP) {
        WriteToDatabase(WP, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public void WriteToDatabase(Waypoint WP, boolean useReplication) {
        int newCheckSum = createCheckSum(WP);
        if (useReplication) {
            Replication.WaypointNew(WP.CacheId, WP.getCheckSum(), newCheckSum, WP.getGcCode());
        }
        Parameters args = new Parameters();
        args.put("gccode", WP.getGcCode());
        args.put("cacheid", WP.CacheId);
        args.put("latitude", WP.Pos.getLatitude());
        args.put("longitude", WP.Pos.getLongitude());
        args.put("description", WP.getDescription());
        args.put("type", WP.Type.ordinal());
        args.put("syncexclude", WP.IsSyncExcluded);
        args.put("userwaypoint", WP.IsUserWaypoint);
        if (WP.getClue() == null)
            WP.setClue("");
        args.put("clue", WP.getClue());
        args.put("title", WP.getTitle());
        args.put("isStart", WP.IsStart);

        try {
            long count = Database.Data.insert("Waypoint", args);
            if (count <= 0) {
                Database.Data.update("Waypoint", args, "gccode=\"" + WP.getGcCode() + "\"", null);
            }
            if (WP.IsUserWaypoint) {
                // HasUserData nicht updaten wenn der Waypoint kein UserWaypoint ist!!!
                args = new Parameters();
                args.put("hasUserData", true);
                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(WP.CacheId)});
            }
        } catch (Exception exc) {
            return;

        }
    }

    public boolean UpdateDatabase(Waypoint WP) {
        return UpdateDatabase(WP, true);
    }

    // sometimes Replication for synchronization with CBServer should not be used (when importing caches from gc api)
    public boolean UpdateDatabase(Waypoint WP, boolean useReplication) {
        boolean result = false;
        int newCheckSum = createCheckSum(WP);
        if (useReplication) {
            Replication.WaypointChanged(WP.CacheId, WP.getCheckSum(), newCheckSum, WP.getGcCode());
        }
        if (newCheckSum != WP.getCheckSum()) {
            Parameters args = new Parameters();
            args.put("gccode", WP.getGcCode());
            args.put("cacheid", WP.CacheId);
            args.put("latitude", WP.Pos.getLatitude());
            args.put("longitude", WP.Pos.getLongitude());
            args.put("description", WP.getDescription());
            args.put("type", WP.Type.ordinal());
            args.put("syncexclude", WP.IsSyncExcluded);
            args.put("userwaypoint", WP.IsUserWaypoint);
            args.put("clue", WP.getClue());
            args.put("title", WP.getTitle());
            args.put("isStart", WP.IsStart);
            try {
                long count = Database.Data.update("Waypoint", args, "CacheId=" + WP.CacheId + " and GcCode=\"" + WP.getGcCode() + "\"", null);
                if (count > 0)
                    result = true;
            } catch (Exception exc) {
                result = false;

            }

            if (WP.IsUserWaypoint) {
                // HasUserData nicht updaten wenn der Waypoint kein UserWaypoint ist (z.B. über API)
                args = new Parameters();
                args.put("hasUserData", true);
                try {
                    Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(WP.CacheId)});
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

        WP = new Waypoint(full);
        
        Gdx.app.log("DEBUG", "Laeuft");
        WP.setGcCode(reader.getString(0));

        WP.CacheId = reader.getLong(1);
        double latitude = reader.getDouble(2);
        double longitude = reader.getDouble(3);
        WP.Pos = new Coordinate(latitude, longitude);
        WP.Type = CacheTypes.values()[reader.getShort(4)];
        WP.IsSyncExcluded = reader.getInt(5) == 1;
        WP.IsUserWaypoint = reader.getInt(6) == 1;
        WP.setTitle(reader.getString(7).trim());
        WP.IsStart = reader.getInt(8) == 1;

        if (full) {
            WP.setClue(reader.getString(10));
            WP.setDescription(reader.getString(9));
            WP.setCheckSum(createCheckSum(WP));
        }
        return WP;
    }

    private int createCheckSum(Waypoint WP) {
        // for Replication
        String sCheckSum = WP.getGcCode();
        sCheckSum += UnitFormatter.FormatLatitudeDM(WP.Pos.getLatitude());
        sCheckSum += UnitFormatter.FormatLongitudeDM(WP.Pos.getLongitude());
        sCheckSum += WP.getDescription();
        sCheckSum += WP.Type.ordinal();
        sCheckSum += WP.getClue();
        sCheckSum += WP.getTitle();
        if (WP.IsStart)
            sCheckSum += "1";
        return (int) Utils.sdbm(sCheckSum);
    }

    public void WriteImportToDatabase(Waypoint WP) {
        Parameters args = new Parameters();
        args.put("gccode", WP.getGcCode());
        args.put("cacheid", WP.CacheId);
        args.put("latitude", WP.Pos.getLatitude());
        args.put("longitude", WP.Pos.getLongitude());
        args.put("description", WP.getDescription());
        args.put("type", WP.Type.ordinal());
        args.put("syncexclude", WP.IsSyncExcluded);
        args.put("userwaypoint", WP.IsUserWaypoint);
        args.put("clue", WP.getClue());
        args.put("title", WP.getTitle());
        args.put("isStart", WP.IsStart);

        try {
            Database.Data.insertWithConflictReplace("Waypoint", args);

            args = new Parameters();
            args.put("hasUserData", true);
            Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(WP.CacheId)});
        } catch (Exception exc) {
            return;

        }
    }

    // Hier wird überprüft, ob für diesen Cache ein Start-Waypoint existiert und dieser in diesem Fall zurückgesetzt
    // Damit kann bei der Definition eines neuen Start-Waypoints vorher der alte entfernt werden damit sichergestellt ist dass ein Cache nur
    // 1 Start-Waypoint hat
    public void ResetStartWaypoint(Cache cache, Waypoint except) {
        for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
            Waypoint wp = cache.waypoints.get(i);
            if (except == wp)
                continue;
            if (wp.IsStart) {
                wp.IsStart = false;
                Parameters args = new Parameters();
                args.put("isStart", false);
                try {
                    long count = Database.Data.update("Waypoint", args, "CacheId=" + wp.CacheId + " and GcCode=\"" + wp.getGcCode() + "\"", null);

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

    /**
     * Returns a WaypointList from reading DB!
     *
     * @param CacheID ID of Cache
     * @param Full    Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return
     */
    public CB_List<Waypoint> getWaypointsFromCacheID(Long CacheID, boolean Full) {
        CB_List<Waypoint> wpList = new CB_List<Waypoint>();
        long aktCacheID = -1;

        StringBuilder sqlState = new StringBuilder(Full ? SQL_WP_FULL : SQL_WP);
        sqlState.append("  where CacheId = ?");

        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery(sqlState.toString(), new String[]{String.valueOf(CacheID)});
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Waypoint wp = getWaypoint(reader, Full);
            if (wp.CacheId != aktCacheID) {
                aktCacheID = wp.CacheId;
                wpList = new CB_List<Waypoint>();

            }
            wpList.add(wp);
            reader.moveToNext();

        }
        reader.close();

        return wpList;
    }

}
