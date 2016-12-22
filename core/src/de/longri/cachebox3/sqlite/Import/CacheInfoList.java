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
package de.longri.cachebox3.sqlite.Import;


import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.logging.Logger;
import de.longri.cachebox3.logging.LoggerFactory;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheDAO;
import de.longri.cachebox3.types.Cache;

import java.io.IOException;
import java.util.HashMap;

public class CacheInfoList {
    final static Logger log = LoggerFactory.getLogger(CacheInfoList.class);

    /**
     * Die Liste der Cache Infos, welche mit IndexDB() gefüllt und mit dispose() gelöscht wird.
     */
    private static HashMap<String, CacheInfo> mCacheInfoList = null;

    /**
     * Mit dieser Methode wird die DB indexiert und die Klasse enthält dann eine Statiche Liste mit den Cache Informationen. Wenn die Liste
     * nicht mehr benötigt wird, sollte sie mit dispose() gelöscht werden.
     */
    public static void IndexDB() {
        mCacheInfoList = new HashMap<String, CacheInfo>();

        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select GcCode, Id, ListingCheckSum, ImagesUpdated, DescriptionImagesUpdated, ListingChanged, Found, CorrectedCoordinates, Latitude, Longitude, GpxFilename_Id, Favorit from Caches", null);

        reader.moveToFirst();

        while (!reader.isAfterLast()) {
            CacheInfo cacheInfo = new CacheInfo();

            cacheInfo.id = reader.getLong(1);

            if (reader.isNull(2)) {
                cacheInfo.ListingCheckSum = 0;
            } else {
                cacheInfo.ListingCheckSum = reader.getInt(2);
            }

            if (reader.isNull(3)) {
                cacheInfo.ImagesUpdated = false;
            } else {
                cacheInfo.ImagesUpdated = reader.getInt(3) != 0;
            }

            if (reader.isNull(4)) {
                cacheInfo.DescriptionImagesUpdated = false;
            } else {
                cacheInfo.DescriptionImagesUpdated = reader.getInt(4) != 0;
            }

            if (reader.isNull(5)) {
                cacheInfo.ListingChanged = false;
            } else {
                cacheInfo.ListingChanged = reader.getInt(5) != 0;
            }

            if (reader.isNull(6)) {
                cacheInfo.Found = false;
            } else {
                cacheInfo.Found = reader.getInt(6) != 0;
            }

            if (reader.isNull(7)) {
                cacheInfo.CorrectedCoordinates = false;
            } else {
                cacheInfo.CorrectedCoordinates = reader.getInt(7) != 0;
            }

            if (reader.isNull(8)) {
                cacheInfo.Latitude = 361;
            } else {
                cacheInfo.Latitude = reader.getDouble(8);
            }
            if (reader.isNull(9)) {
                cacheInfo.Longitude = 361;
            } else {
                cacheInfo.Longitude = reader.getDouble(9);
            }

            cacheInfo.GpxFilename_Id = reader.getInt(10);

            if (reader.isNull(11)) {
                cacheInfo.favorite = false;
            } else {
                cacheInfo.favorite = reader.getInt(11) != 0;
            }

            mCacheInfoList.put(reader.getString(0), cacheInfo);
            reader.moveToNext();
        }
        reader.close();

    }

    /**
     * Die statische Liste der Cache Informationen wird mit diesem Aufruf gelöscht und der Speicher wieder frei gegeben.
     */
    public static void dispose() {
        if (mCacheInfoList == null)
            return;
        mCacheInfoList.clear();
        mCacheInfoList = null;
    }

    /**
     * True wenn der Cache in der Liste Existiert
     *
     * @param GcCode
     * @return
     */
    public static boolean ExistCache(String GcCode) {
        if (mCacheInfoList == null)
            return false;
        return mCacheInfoList.containsKey(GcCode);
    }

    public static boolean CacheIsFavoriteInDB(String GcCode) {
        if (mCacheInfoList == null)
            return false;
        if (mCacheInfoList.containsKey(GcCode)) {
            CacheInfo ci = mCacheInfoList.get(GcCode);
            return ci.favorite;
        } else
            return false;
    }

    public static boolean CacheIsFoundInDB(String GcCode) {
        if (mCacheInfoList == null)
            return false;

        if (mCacheInfoList.containsKey(GcCode)) {
            CacheInfo ci = mCacheInfoList.get(GcCode);
            return ci.Found;
        } else
            return false;
    }

    /**
     * Fügt die CacheInfo in der Liste mit dem Infos des übergebenen Caches zusammen und ändert gegebenenfalls die Changed Attribute neu!
     *
     * @param cache
     * @throws IOException
     */
    public static void mergeCacheInfo(Cache cache) throws IOException {
        String gcCode = cache.getGcCode();
        CacheInfo cacheInfo = mCacheInfoList.get(gcCode);
        if (cacheInfo != null) {
//            // if already exists and if the category of the cache is pinned:
//            // do not use the new GpxFilename_Id (in cache),
//            // but use existing category and add the gpxfilename to the category
//            long newGpxFilename_Id = cache.getGPXFilename_ID(); // overwrites the actual, if not pinned
//            Category newCategory = CoreSettingsForward.Categories.getCategoryByGpxFilenameId(newGpxFilename_Id); // set at beginning of gpx-import
//            String newGpxFilename = newCategory.getGpxFilename(newGpxFilename_Id); // the gpx-file, that is currently processed
//            Category existingCategory = CoreSettingsForward.Categories.getCategoryByGpxFilenameId(cacheInfo.GpxFilename_Id);
//            if ((existingCategory != null) && (existingCategory != newCategory) && (existingCategory.pinned)) {
//                // changing cache to the existingGpxFilename_Id
//                cache.setGPXFilename_ID(cacheInfo.GpxFilename_Id);
//                // perhaps the newGpxFilename must be added to the existingCategory
//                if (!existingCategory.containsGpxFilename(newGpxFilename))
//                    existingCategory.addGpxFilename(newGpxFilename);
//                // cacheInfo.GpxFilename_Id needs no change
//            } else {
//                cacheInfo.GpxFilename_Id = cache.getGPXFilename_ID();
//            }

            // handling logs
            String stringForListingCheckSum = CacheDAO.GetDescription(cache);
            String recentOwnerLogString = "";

//            CB_List<LogEntry> cleanLogs = new CB_List<LogEntry>();
//            cleanLogs = Database.Logs(cache);// cache.Logs();
//
//            if (cleanLogs.size() > 0) {
//                for (int i = 0, n = cleanLogs.size(); i < n; i++) {
//                    LogEntry entry = cleanLogs.get(i);
//                    String Comment = entry.Comment;
//                    String Finder = entry.Finder;
//
//                    if (Finder.equalsIgnoreCase(cache.getOwner())) {
//                        recentOwnerLogString += Comment;
//                        break;
//                    }
//                }
//            }

            int ListingCheckSum = (int) (Utils.sdbm(stringForListingCheckSum) + Utils.sdbm(recentOwnerLogString));

            boolean ListingChanged = cacheInfo.ListingChanged;
            boolean ImagesUpdated = cacheInfo.ImagesUpdated;
            boolean DescriptionImagesUpdated = cacheInfo.DescriptionImagesUpdated;

            if (cacheInfo.ListingCheckSum == 0) {
                ImagesUpdated = false;
                DescriptionImagesUpdated = false;
            } else if (ListingCheckSum != cacheInfo.ListingCheckSum) {
                int oldStyleListingCheckSum = stringForListingCheckSum.hashCode() + recentOwnerLogString.hashCode();

                if (oldStyleListingCheckSum != cacheInfo.ListingCheckSum) {
                    ListingChanged = true;
                    ImagesUpdated = false;
                    DescriptionImagesUpdated = false;

                    // 9.4.2016 arbor95: no , DescriptionImageFolder must always be the default ...\repository\images
                    // if (CB_Core_Settings.DescriptionImageFolderLocal.getValue().length() > 0)
                    // CB_Core_Settings.DescriptionImageFolder.setValue(CB_Core_Settings.DescriptionImageFolderLocal.getValue());

                    // 2014-06-21 - Ging-Buh - .changed files are no longer used. Only information in DB (ImagesUpdated and
                    // DescriptionImagesUpdated) are used
                    // CreateChangedListingFile(CB_Core_Settings.DescriptionImageFolder.getValue() + "/" + GcCode.substring(0, 4) + "/"
                    // + GcCode + ".changed");

                } else {
                    // old Style Hash codes must also be converted to sdbm, so force update Description Images but without creating changed
                    // files.
                    DescriptionImagesUpdated = false;
                    ImagesUpdated = false;
                }
            }

            if (!cacheInfo.Found) {
                // nur wenn der Cache nicht als gefunden markiert ist, wird der Wert aus dem GPX Import übernommen!
                cacheInfo.Found = cache.isFound();
            }

            // Schreibe info neu in die List(lösche den Eintrag vorher)

            mCacheInfoList.remove(gcCode);
            // Wenn das Flag schon gesetzt ist, dann nicht ausversehen wieder zurücksetzen!
            if (!cacheInfo.ListingChanged)
                cacheInfo.ListingChanged = ListingChanged;

            cacheInfo.ImagesUpdated = ImagesUpdated;
            cacheInfo.DescriptionImagesUpdated = DescriptionImagesUpdated;
            cacheInfo.ListingCheckSum = ListingCheckSum;
            mCacheInfoList.put(gcCode, cacheInfo);
        }

    }

    /**
     * Schreibt die Liste der CacheInfos zurück in die DB
     */
    public static void writeListToDB() {
        for (CacheInfo info : mCacheInfoList.values()) {
            Database.Parameters args = new Database.Parameters();

            // bei einem Update müssen nicht alle infos überschrieben werden

            args.put("ListingCheckSum", info.ListingCheckSum);
            args.put("ListingChanged", info.ListingChanged ? 1 : 0);
            args.put("ImagesUpdated", info.ImagesUpdated ? 1 : 0);
            args.put("DescriptionImagesUpdated", info.DescriptionImagesUpdated ? 1 : 0);
            args.put("ListingCheckSum", info.ListingCheckSum);
            args.put("Found", info.Found ? 1 : 0);

            try {
                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(info.id)});
            } catch (Exception exc) {
                log.error("CacheInfoList.writeListToDB()", exc);

            }
        }
    }

//    private static void CreateChangedListingFile(String changedFileString) throws IOException {
//        File file = FileFactory.createFile(changedFileString);
//
//        if (!file.exists()) {
//            String changedFileDir = changedFileString.substring(0, changedFileString.lastIndexOf("/"));
//            File Directory = FileFactory.createFile(changedFileDir);
//
//            if (!Directory.exists()) {
//                Directory.mkdirs();
//            }
//
//            PrintWriter writer = new PrintWriter(file.getFileWriter());
//
//            writer.write("Listing Changed!");
//            writer.close();
//        }
//    }

    /**
     * Packt eine neue CacheInfo des übergebenen Caches in die Liste
     *
     * @param cache
     */
    public static void putNewInfo(Cache cache) {
        CacheInfo info = new CacheInfo(cache.Id, cache.getGPXFilename_ID());
        String stringForListingCheckSum = CacheDAO.GetDescription(cache);
        String recentOwnerLogString = "";

//        CB_List<LogEntry> cleanLogs = new CB_List<LogEntry>();
//        cleanLogs = Database.Logs(cache);// cache.Logs();
//
//        if (cleanLogs.size() > 0) {
//            for (int i = 0, n = cleanLogs.size(); i < n; i++) {
//                LogEntry entry = cleanLogs.get(i);
//                String Comment = entry.Comment;
//                String Finder = entry.Finder;
//
//                if (Finder.equalsIgnoreCase(cache.getOwner())) {
//                    recentOwnerLogString += Comment;
//                    break;
//                }
//            }
//        }

        int ListingCheckSum = (int) (Utils.sdbm(stringForListingCheckSum) + Utils.sdbm(recentOwnerLogString));
        info.ListingCheckSum = ListingCheckSum;
        info.Latitude = cache.Latitude();
        info.Longitude = cache.Longitude();
        info.Found = cache.isFound();
        info.favorite = cache.isFavorite();
        info.CorrectedCoordinates = cache.CorrectedCoordiantesOrMysterySolved();

        if (mCacheInfoList == null)
            mCacheInfoList = new HashMap<String, CacheInfo>();

        mCacheInfoList.put(cache.getGcCode(), info);

    }

    public static long getIDfromGcCode(String gccode) {
        CacheInfo info = mCacheInfoList.get(gccode);
        if (info != null)
            return info.id;
        return 0;
    }

    public static void setImageUpdated(String GcCode) {
        CacheInfo info = mCacheInfoList.get(GcCode);
        mCacheInfoList.remove(GcCode);

        info.ImagesUpdated = true;
        info.DescriptionImagesUpdated = true;

        mCacheInfoList.put(GcCode, info);
    }
}
