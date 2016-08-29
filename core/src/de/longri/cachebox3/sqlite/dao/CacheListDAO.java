/* 
 * Copyright (C) 2014 team-cachebox.de
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

import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import com.badlogic.gdx.files.FileHandle;
import de.cb.sqlite.CoreCursor;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author ging-buh
 * @author Longri
 */
public class CacheListDAO {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(CacheListDAO.class);

	/**
	 * !!! only exportBatch 
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

	// public CacheList ReadCacheList(CacheList cacheList, String join, String where, boolean fullDetails, boolean loadAllWaypoints)
	// {
	// return ReadCacheList(cacheList, join, where, false, fullDetails, loadAllWaypoints);
	// }

	public CacheList ReadCacheList(CacheList cacheList, String join, String where, boolean withDescription, boolean fullDetails, boolean loadAllWaypoints) {
		if (cacheList == null)
			return null;

		// Clear List before read
		cacheList.clear();

		Log.trace(log, "ReadCacheList 1.Waypoints");
		SortedMap<Long, CB_List<Waypoint>> waypoints;
		waypoints = new TreeMap<Long, CB_List<Waypoint>>();
		// zuerst alle Waypoints einlesen
		CB_List<Waypoint> wpList = new CB_List<Waypoint>();
		long aktCacheID = -1;

		String sql = fullDetails ? WaypointDAO.SQL_WP_FULL : WaypointDAO.SQL_WP;
		if (!((fullDetails || loadAllWaypoints))) {
			// when CacheList should be loaded without full details and without all Waypoints
			// do not load all waypoints from db!
			sql += " where IsStart=\"true\" or Type=" + CacheTypes.Final.ordinal(); // StartWaypoint or CacheTypes.Final
		}
		sql += " order by CacheId";
		CoreCursor reader = Database.Data.rawQuery(sql, null);
		if (reader == null)
		    return cacheList;
		
		reader.moveToFirst();
		while (!reader.isAfterLast()) {
			WaypointDAO waypointDAO = new WaypointDAO();
			Waypoint wp = waypointDAO.getWaypoint(reader, fullDetails);
			if (!(fullDetails || loadAllWaypoints)) {
				// wenn keine FullDetails geladen werden sollen dann sollen nur die Finals und Start-Waypoints geladen werden
				if (!(wp.IsStart || wp.Type == CacheTypes.Final)) {
					reader.moveToNext();
					continue;
				}
			}
			if (wp.CacheId != aktCacheID) {
				aktCacheID = wp.CacheId;
				wpList = new CB_List<Waypoint>();
				waypoints.put(aktCacheID, wpList);
			}
			wpList.add(wp);
			reader.moveToNext();

		}
		reader.close();

		Log.debug(log, "ReadCacheList 2.Caches");
		try {
			if (fullDetails) {
				sql = CacheDAO.SQL_GET_CACHE + ", " + CacheDAO.SQL_DETAILS;
				if (withDescription) {
					// load Cache with Description, Solver, Notes for Transfering Data from Server to ACB
					sql += "," + CacheDAO.SQL_GET_DETAIL_WITH_DESCRIPTION;
				}
			} else {
				sql = CacheDAO.SQL_GET_CACHE;

			}

			sql += " from Caches c " + join + " " + ((where.length() > 0) ? "where " + where : where);
			reader = Database.Data.rawQuery(sql, null);

		} catch (Exception e) {
			Log.err(log, "CacheList.LoadCaches()", "reader = Database.Data.myDB.rawQuery(....", e);
		}
		reader.moveToFirst();

		CacheDAO cacheDAO = new CacheDAO();

		while (!reader.isAfterLast()) {
			Cache cache = cacheDAO.ReadFromCursor(reader, fullDetails, withDescription);
			boolean doAdd = true;
			if (FilterInstances.hasCorrectedCoordinates != 0) {
				if (waypoints.containsKey(cache.Id)) {
					CB_List<Waypoint> tmpwaypoints = waypoints.get(cache.Id);
					for (int i = 0, n = tmpwaypoints.size(); i < n; i++) {
						cache.waypoints.add(tmpwaypoints.get(i));
					}
				}
				boolean hasCorrectedCoordinates = cache.CorrectedCoordiantesOrMysterySolved();
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
				cacheList.add(cache);
				cache.waypoints.clear();
				if (waypoints.containsKey(cache.Id)) {
					CB_List<Waypoint> tmpwaypoints = waypoints.get(cache.Id);

					for (int i = 0, n = tmpwaypoints.size(); i < n; i++) {
						cache.waypoints.add(tmpwaypoints.get(i));
					}

					waypoints.remove(cache.Id);
				}
			}
			// ++Global.CacheCount;
			reader.moveToNext();

		}
		reader.close();

		// clear other never used WP`s from Mem
		waypoints.clear();
		waypoints = null;

		// do it manual (or automated after fix), got hanging app on startup
		// Log.debug(log, "ReadCacheList 3.Sorting");
		try

		{
			// Collections.sort(cacheList);
		} catch (

		Exception e)

		{
			// Log.err(log, "CacheListDAO.ReadCacheList()", "Sort ERROR", e);
		}
		// Log.debug(log, "ReadCacheList 4. ready");
		return cacheList;

	}

	/**
	 * @param SpoilerFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @return
	 */
	public long deleteArchived(String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
		try {
			delCacheImages(getGcCodeList("Archived=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", "Archived=1", null);
			Database.Data.GPXFilenameUpdateCacheCount(); // CoreSettingsForward.Categories will be set
			return ret;
		} catch (Exception e) {
			Log.err(log, "CacheListDAO.DelArchiv()", "Archiv ERROR", e);
			return -1;
		}
	}

	/**
	 * @param SpoilerFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @return
	 */
	public long deleteFinds(String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
		try {
			delCacheImages(getGcCodeList("Found=1"), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", "Found=1", null);
			Database.Data.GPXFilenameUpdateCacheCount(); // CoreSettingsForward.Categories will be set
			return ret;
		} catch (Exception e) {
			Log.err(log, "CacheListDAO.DelFound()", "Found ERROR", e);
			return -1;
		}
	}

	/**
	 * @param Where
	 * @param SpoilerFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 * @return
	 */
	public long deleteFiltered(String Where, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
		try {
			delCacheImages(getGcCodeList(Where), SpoilerFolder, SpoilerFolderLocal, DescriptionImageFolder, DescriptionImageFolderLocal);
			long ret = Database.Data.delete("Caches", Where, null);
			Database.Data.GPXFilenameUpdateCacheCount(); // CoreSettingsForward.Categories will be set
			return ret;
		} catch (Exception e) {
			Log.err(log, "CacheListDAO.DelFilter()", "Filter ERROR", e);
			return -1;
		}
	}

	private ArrayList<String> getGcCodeList(String where) {
		CacheList list = new CacheList();
		ReadCacheList(list, where, false, false);
		ArrayList<String> StrList = new ArrayList<String>();

		for (int i = 0, n = list.size(); i < n; i++) {
			StrList.add(list.get(i).getGcCode());
		}
		list.dispose();
		list = null;
		return StrList;
	}

	/**
	 * Löscht alle Spoiler und Description Images der übergebenen Liste mit GC-Codes
	 * 
	 * @param list
	 * @param SpoilerFolder
	 *            Config.settings.SpoilerFolder.getValue()
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 */
	public void delCacheImages(ArrayList<String> list, String SpoilerFolder, String SpoilerFolderLocal, String DescriptionImageFolder, String DescriptionImageFolderLocal) {
		String spoilerpath = SpoilerFolder;
		if (SpoilerFolderLocal.length() > 0)
			spoilerpath = SpoilerFolderLocal;

		String imagespath = DescriptionImageFolder;
		if (DescriptionImageFolderLocal.length() > 0)
			imagespath = DescriptionImageFolderLocal;

		delCacheImagesByPath(spoilerpath, list);
		delCacheImagesByPath(imagespath, list);

		ImageDAO imageDAO = new ImageDAO();
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			final String GcCode = iterator.next();
			imageDAO.deleteImagesForCache(GcCode);
		}
		imageDAO = null;
	}

	public void delCacheImagesByPath(String path, ArrayList<String> list) {
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			final String GcCode = iterator.next().toLowerCase();
			String directory = path + "/" + GcCode.substring(0, 4);
			if (!FileIO.DirectoryExists(directory))
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
						Log.err(log, "Error deleting : " + filename);
				}
			}
		}
	}
}
