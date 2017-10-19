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


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author ging-buh
 * @author Longri
 */
public class CacheListDAO {
    final static Logger log = LoggerFactory.getLogger(CacheListDAO.class);

    /**
     * !!! only exportBatch
     *
     * @param cacheList
     * @param GC_Codes
     * @param withDescription
     * @param fullDetails
     * @param loadAllWaypoints
     * @return
     */
    public CacheList ReadCacheList(CacheList cacheList, ArrayList<String> GC_Codes, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints) {
        ArrayList<String> orParts = new ArrayList<String>();

        for (String gcCode : GC_Codes) {
            orParts.add("GcCode like '%" + gcCode + "%'");
        }
        String where = FilterProperties.join(" or ", orParts);
        return ReadCacheList(cacheList, "", where, withDescription, fullDetails, loadAllWaypoints);
    }

    public CacheList ReadCacheList(CacheList cacheList, String where, boolean fullDetails, boolean loadAllWaypoints) {
        return ReadCacheList(cacheList, "", where, false, fullDetails, loadAllWaypoints);
    }

    public CacheList ReadCacheList(CacheList cacheList, String join, String where, boolean fullDetails, boolean loadAllWaypoints) {
        return ReadCacheList(cacheList, join, where, false, fullDetails, loadAllWaypoints);
    }

    public CacheList ReadCacheList(CacheList cacheList, String join, String where, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints) {
        if (cacheList == null)
            return null;

        // Clear List before read
        cacheList.clear();
        boolean error = false;

        log.trace("ReadCacheList 1.Waypoints");
        LongMap<CB_List<Waypoint>> waypoints = new LongMap<CB_List<Waypoint>>();

        // zuerst alle Waypoints einlesen
        CB_List<Waypoint> wpList = new CB_List<Waypoint>();
        long aktCacheID = -1;

        String sql = fullDetails ? WaypointDAO.SQL_WP_FULL : WaypointDAO.SQL_WP;
        if (!((fullDetails || loadAllWaypoints))) {
            // when CacheList should be loaded without full details and without all Waypoints
            // do not load all waypoints from db!
            sql += " WHERE IsStart=\"true\" or Type=" + CacheTypes.Final.ordinal(); // StartWaypoint or CacheTypes.Final
        }
        sql += " ORDER BY 'CacheId'";
        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery(sql, null);
        if (reader == null)
            return cacheList;

        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            WaypointDAO waypointDAO = new WaypointDAO();
            Waypoint wp = waypointDAO.getWaypoint(reader, fullDetails);
            if (!(fullDetails || loadAllWaypoints)) {
                // wenn keine FullDetails geladen werden sollen dann sollen nur die Finals und Start-Waypoints geladen werden
                if (!(wp.isStart() || wp.getType() == CacheTypes.Final)) {
                    reader.moveToNext();
                    continue;
                }
            }
            if (wp.getCacheId() != aktCacheID) {
                aktCacheID = wp.getCacheId();
                wpList = new CB_List<Waypoint>();
                waypoints.put(aktCacheID, wpList);
            }
            wpList.add(wp);
            reader.moveToNext();

        }
        reader.close();
        log.debug(wpList.size + " Waypoints readed!");
        log.debug("ReadCacheList 2.Caches");
        try {
            if (fullDetails) {
                sql = SQL.SQL_GET_CACHE + ", " + SQL.SQL_DETAILS;
                if (withDescription) {
                    // load Cache with Description, Solver, Notes for Transfering Data from Server to ACB
                    sql += "," + SQL.SQL_GET_DETAIL_WITH_DESCRIPTION;
                }
            } else {
                sql = SQL.SQL_GET_CACHE;

            }

            sql += " FROM `Caches` AS `c` " + join + " " + ((where.length() > 0) ? "WHERE " + where : where);
            reader = Database.Data.rawQuery(sql, null);

        } catch (Exception e) {
            log.error("CacheList.LoadCaches() sql+ \n" + sql, e);
            error = true;
        }

        if (!error) {
            reader.moveToFirst();

            CacheDAO cacheDAO = new CacheDAO();

            while (!reader.isAfterLast()) {
                AbstractCache abstractCache = cacheDAO.ReadFromCursor(reader, fullDetails, withDescription);
                boolean doAdd = true;
                if (FilterInstances.hasCorrectedCoordinates != 0) {
                    if (waypoints.containsKey(abstractCache.getId())) {
                        CB_List<Waypoint> tmpwaypoints = waypoints.get(abstractCache.getId());
                        for (int i = 0, n = tmpwaypoints.size; i < n; i++) {
                            abstractCache.getWaypoints().add(tmpwaypoints.get(i));
                        }
                    }
                    boolean hasCorrectedCoordinates = abstractCache.CorrectedCoordiantesOrMysterySolved();
                    if (FilterInstances.hasCorrectedCoordinates < 0) {
                        // show only those without corrected ones
                        if (hasCorrectedCoordinates)
                            doAdd = false;
                    } else if (FilterInstances.hasCorrectedCoordinates > 0) {
                        // only those with corrected ones
                        if (!hasCorrectedCoordinates)
                            doAdd = false;
                    }
                }
                if (doAdd) {
                    cacheList.add(abstractCache);
                    abstractCache.getWaypoints().clear();
                    if (waypoints.containsKey(abstractCache.getId())) {
                        CB_List<Waypoint> tmpwaypoints = waypoints.get(abstractCache.getId());

                        for (int i = 0, n = tmpwaypoints.size; i < n; i++) {
                            abstractCache.getWaypoints().add(tmpwaypoints.get(i));
                        }

                        waypoints.remove(abstractCache.getId());
                    }
                }
                // ++Global.CacheCount;
                reader.moveToNext();

            }
            reader.close();
        } else {
            log.error("Corrupt database try cache by cache");

            // get all id's
            reader = Database.Data.rawQuery(SQL.SQL_ALL_CACHE_IDS, null);
            reader.moveToFirst();
            ArrayList<Long> idList = new ArrayList<Long>(reader.getCount());

            while (!reader.isAfterLast()) {
                idList.add(reader.getLong(0));
                reader.moveToNext();
            }

            CacheDAO cacheDAO = new CacheDAO();

            for (Long id : idList) {
                AbstractCache abstractCache = null;
                try {
                    abstractCache = cacheDAO.getFromDbByCacheId(id);
                } catch (Exception e) {
                    log.error("Can't read Cache (id:" + id + ") from database.");
                    try {
                        Database.Data.delete("Caches", "id=" + id, null);
                    } catch (Exception e1) {
                        log.error("Can't delete this Cache. Skip it!");
                    }
                    continue;
                }

                boolean doAdd = true;
                if (FilterInstances.hasCorrectedCoordinates != 0) {
                    if (waypoints.containsKey(abstractCache.getId())) {
                        CB_List<Waypoint> tmpwaypoints = waypoints.get(abstractCache.getId());
                        for (int i = 0, n = tmpwaypoints.size; i < n; i++) {
                            abstractCache.getWaypoints().add(tmpwaypoints.get(i));
                        }
                    }
                    boolean hasCorrectedCoordinates = abstractCache.CorrectedCoordiantesOrMysterySolved();
                    if (FilterInstances.hasCorrectedCoordinates < 0) {
                        // show only those without corrected ones
                        if (hasCorrectedCoordinates)
                            doAdd = false;
                    } else if (FilterInstances.hasCorrectedCoordinates > 0) {
                        // only those with corrected ones
                        if (!hasCorrectedCoordinates)
                            doAdd = false;
                    }
                }
                if (doAdd) {
                    cacheList.add(abstractCache);
                    abstractCache.getWaypoints().clear();
                    if (waypoints.containsKey(abstractCache.getId())) {
                        CB_List<Waypoint> tmpwaypoints = waypoints.get(abstractCache.getId());

                        for (int i = 0, n = tmpwaypoints.size; i < n; i++) {
                            abstractCache.getWaypoints().add(tmpwaypoints.get(i));
                        }

                        waypoints.remove(abstractCache.getId());
                    }
                }
            }
        }
        // clear other never used WP`s from Mem
        waypoints.clear();
        waypoints = null;

        // do it manual (or automated after fix), got hanging app on startup
        // log.debug( "ReadCacheList 3.Sorting");
        try

        {
            // Collections.sort(cacheList);
        } catch (

                Exception e)

        {
            // log.error( "CacheListDAO.ReadCacheList()", "Sort ERROR", e);
        }
        // log.debug( "ReadCacheList 4. ready");
        return cacheList;

    }


    /**
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     * @return
     */

    public long deleteArchived(String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
        try {
            delCacheImages(getGcCodeList("Archived=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
            long ret = Database.Data.delete("Caches", "Archived=1", null);
            Database.Data.gpxFilenameUpdateCacheCount();
            return ret;
        } catch (Exception e) {
            log.error("CacheListDAO.DelArchiv()", e);
            return -1;
        }
    }

    /**
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     * @return
     */
    public long deleteFinds(String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
        try {
            delCacheImages(getGcCodeList("Found=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
            long ret = Database.Data.delete("Caches", "Found=1", null);
            Database.Data.gpxFilenameUpdateCacheCount(); // CoreSettingsForward.Categories will be set
            return ret;
        } catch (Exception e) {
            log.error("CacheListDAO.DelFound()", e);
            return -1;
        }
    }

    /**
     * @param Where
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     * @return
     */
    public long deleteFiltered(String Where, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
        try {
            delCacheImages(getGcCodeList(Where), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
            long ret = Database.Data.delete("Caches", Where, null);
            Database.Data.gpxFilenameUpdateCacheCount(); // CoreSettingsForward.Categories will be set
            return ret;
        } catch (Exception e) {
            log.error("CacheListDAO.DelFilter()", e);
            return -1;
        }
    }

    private Array<String> getGcCodeList(String where) {
        CacheList list = new CacheList();
        ReadCacheList(list, where, false, false);
        return list.getGcCodes();
    }

    /**
     * Löscht alle Spoiler und Description Images der übergebenen Liste mit GC-Codes
     *
     * @param list
     * @param SpoilerFolder               Config.settings.SpoilerFolder.getValue()
     * @param SpoilerFolderLocal          Config.settings.SpoilerFolderLocal.getValue()
     * @param DescriptionImageFolder      Config.settings.DescriptionImageFolder.getValue()
     * @param DescriptionImageFolderLocal Config.settings.DescriptionImageFolderLocal.getValue()
     */
    public void delCacheImages(Array<String> list, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
        String spoilerpath = SpoilerFolder;
        if (SpoilerFolderLocal.length() > 0)
            spoilerpath = SpoilerFolderLocal;

        String imagespath = DescriptionImageFolder;
        if (DescriptionImageFolderLocal.length() > 0)
            imagespath = DescriptionImageFolderLocal;

        delCacheImagesByPath(spoilerpath, list);
        delCacheImagesByPath(imagespath, list);

        ImageDAO imageDAO = new ImageDAO();
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
            final String GcCode = iterator.next();
            imageDAO.deleteImagesForCache(GcCode);
        }
        imageDAO = null;
    }

    public void delCacheImagesByPath(String path, Array<String> list) {
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
            final String GcCode = iterator.next().toLowerCase();
            String directory = path + "/" + GcCode.substring(0, 4);
            if (!Utils.directoryExists(directory))
                continue;

            FileHandle dir = new FileHandle(directory);
            FileHandle[] files = dir.list();

            for (int i = 0; i < files.length; i++) {

                // simplyfied for startswith gccode, thumbs_gccode + ooverwiewthumbs_gccode
                if (!files[i].name().toLowerCase().contains(GcCode))
                    continue;

                String filename = directory + "/" + files[i].name();
                FileHandle file = new FileHandle(filename);
                if (file.exists()) {
                    if (!file.delete())
                        log.error("Error deleting : " + filename);
                }
            }
        }
    }
}
