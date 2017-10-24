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


import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.longri.cachebox3.sqlite.dao.SQL.*;

public class CacheDAO extends AbstractCacheDAO {
    final static Logger log = LoggerFactory.getLogger(CacheDAO.class);


    public static String GetShortDescription(AbstractCache abstractCache) {
        String description = "";
        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select ShortDescription from Caches where Id=?", new String[]{Long.toString(abstractCache.getId())});
        if (reader == null)
            return "";
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            if (reader.getString(0) != null)
                description = reader.getString(0);
            reader.moveToNext();
        }
        reader.close();
        return description;
    }

    public static String getDescription(AbstractCache abstractCache) {
        String description = "";
        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select Description from Caches where Id=?", new String[]{Long.toString(abstractCache.getId())});
        if (reader == null)
            return "";
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            if (reader.getString(0) != null)
                description = reader.getString(0);
            reader.moveToNext();
        }
        reader.close();
        return description;
    }


    Cache ReadFromCursor(SQLiteGdxDatabaseCursor reader, boolean fullDetails, boolean withDescription) {

        try {
            Cache cache = new Cache(reader.getDouble(2), reader.getDouble(3), fullDetails);

            cache.setId(reader.getLong(0));
            cache.setGcCode(reader.getString(1).trim());
            cache.setName(reader.getString(4).trim());
            cache.setSize(CacheSizes.parseInt(reader.getInt(5)));
            cache.setDifficulty(((float) reader.getShort(6)) / 2);
            cache.setTerrain(((float) reader.getShort(7)) / 2);
            cache.setArchived(reader.getInt(8) != 0);
            cache.setAvailable(reader.getInt(9) != 0);
            cache.setFound(reader.getInt(10) != 0);
            cache.setType(CacheTypes.values()[reader.getShort(11)]);
            cache.setOwner(reader.getString(12).trim());

            cache.setNumTravelbugs(reader.getInt(13));
            cache.setGcId(reader.getString(14));
            cache.setRating((reader.getShort(15)) / 100.0f);
            if (reader.getInt(16) > 0)
                cache.setFavorite(true);
            else
                cache.setFavorite(false);

            if (reader.getInt(17) > 0)
                cache.setHasUserData(true);
            else
                cache.setHasUserData(false);

            if (reader.getInt(18) > 0)
                cache.setListingChanged(true);
            else
                cache.setListingChanged(false);

            if (reader.getInt(19) > 0)
                cache.setCorrectedCoordinates(true);
            else
                cache.setCorrectedCoordinates(false);

            if (fullDetails) {
                readDetailFromCursor(reader, cache.getDetail(), fullDetails, withDescription);
            }

            return cache;
        } catch (Exception exc) {
            log.error("Read Cache", exc);
            return null;
        }
    }

    public boolean readDetail(AbstractCache abstractCache) {
        if (abstractCache.getDetail() != null)
            return true;
        abstractCache.setDetail(new CacheDetail());

        SQLiteGdxDatabaseCursor reader = null;

        try {
            reader = Database.Data.rawQuery(SQL_GET_DETAIL_FROM_ID, new String[]{String.valueOf(abstractCache.getId())});

            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                readDetailFromCursor(reader, abstractCache.getDetail(), false, false);

                reader.close();
                return true;
            } else {
                if (reader != null)
                    reader.close();
                return false;
            }
        } catch (Exception e) {
            if (reader != null)
                reader.close();
            return false;
        }
    }

    private boolean readDetailFromCursor(SQLiteGdxDatabaseCursor reader, CacheDetail detail, boolean withReaderOffset, boolean withDescription) {
        // Reader includes Compleate Cache or Details only
        int readerOffset = withReaderOffset ? 20 : 0;

        detail.PlacedBy = reader.getString(readerOffset + 0).trim();

        if (reader.isNull(readerOffset + 5))
            detail.apiState = Cache.NOTLIVE;
        else
            detail.apiState = (byte) reader.getInt(readerOffset + 5);

        String sDate = reader.getString(readerOffset + 1);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            detail.DateHidden = iso8601Format.parse(sDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        detail.Url = reader.getString(readerOffset + 2).trim();
        if (reader.getString(readerOffset + 3) != null)
            detail.TourName = reader.getString(readerOffset + 3).trim();
        else
            detail.TourName = "";
        if (reader.getString(readerOffset + 4) != "")
            detail.GPXFilename_ID = reader.getLong(readerOffset + 4);
        else
            detail.GPXFilename_ID = -1;
        detail.setAttributesPositive(new DLong(reader.getLong(readerOffset + 7), reader.getLong(readerOffset + 6)));
        detail.setAttributesNegative(new DLong(reader.getLong(readerOffset + 9), reader.getLong(readerOffset + 8)));

        if (reader.getString(readerOffset + 10) != null)
            detail.setHint(reader.getString(readerOffset + 10).trim());
        else
            detail.setHint("");

        if (withDescription) {
            detail.longDescription = reader.getString(readerOffset + 11);
            detail.tmpSolver = reader.getString(readerOffset + 12);
            detail.tmpNote = reader.getString(readerOffset + 13);
            detail.shortDescription = reader.getString(readerOffset + 14);
        }
        return true;
    }

    @Override
    public AbstractWaypointDAO getWaypointDAO() {
        return new WaypointDAO();
    }

    @Override
    public void writeToDatabase(Database database, AbstractCache abstractCache) {
        // int newCheckSum = createCheckSum(WP);
        // Replication.WaypointChanged(CacheId, checkSum, newCheckSum, GcCode);
        Database.Parameters args = new Database.Parameters();
        args.put("Id", abstractCache.getId());
        args.put("GcCode", abstractCache.getGcCode());
        args.put("Latitude", abstractCache.latitude);
        args.put("Longitude", abstractCache.longitude);
        args.put("name", abstractCache.getName());
        try {
            args.put("Size", abstractCache.getSize().ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        args.put("Difficulty", (int) (abstractCache.getDifficulty() * 2));
        args.put("Terrain", (int) (abstractCache.getTerrain() * 2));
        args.put("Archived", abstractCache.isArchived() ? 1 : 0);
        args.put("Available", abstractCache.isAvailable() ? 1 : 0);
        args.put("Found", abstractCache.isFound());
        args.put("Type", abstractCache.getType().ordinal());
        args.put("Owner", abstractCache.getOwner());
        args.put("Country", abstractCache.getCountry());
        args.put("State", abstractCache.getState());
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String firstimported = iso8601Format.format(new Date());
            args.put("FirstImported", firstimported);
        } catch (Exception e) {

            e.printStackTrace();
        }

        if ((abstractCache.getShortDescription(database) != null) && (abstractCache.getShortDescription(database).length() > 0)) {
            args.put("ShortDescription", abstractCache.getShortDescription(database));
        }

        if ((abstractCache.getLongDescription(database) != null) && (abstractCache.getLongDescription(database).length() > 0)) {
            args.put("Description", abstractCache.getLongDescription(database));
        }

        args.put("NumTravelbugs", abstractCache.getNumTravelbugs());
        args.put("Rating", (int) (abstractCache.getRating() * 100));
        // args.put("Vote", cache.);
        // args.put("VotePending", cache.);
        // args.put("Notes", );
        // args.put("Solver", cache.);
        // args.put("ListingCheckSum", cache.);
        args.put("CorrectedCoordinates", abstractCache.hasCorrectedCoordinates() ? 1 : 0);

        if (abstractCache.getDetail() != null) {
            // write detail information if existing
            args.put("GcId", abstractCache.getGcId());
            args.put("PlacedBy", abstractCache.getPlacedBy());
            args.put("ApiStatus", abstractCache.getApiState());
            try {
                String stimestamp = iso8601Format.format(abstractCache.getDateHidden());
                args.put("DateHidden", stimestamp);
            } catch (Exception e) {

                e.printStackTrace();
            }
            args.put("Url", abstractCache.getUrl());
            args.put("TourName", abstractCache.getTourName());
            args.put("GPXFilename_Id", abstractCache.getGPXFilename_ID());
            args.put("AttributesPositive", abstractCache.getAttributesPositive().getLow());
            args.put("AttributesPositiveHigh", abstractCache.getAttributesPositive().getHigh());
            args.put("AttributesNegative", abstractCache.getAttributesNegative().getLow());
            args.put("AttributesNegativeHigh", abstractCache.getAttributesNegative().getHigh());
            args.put("Hint", abstractCache.getHint(database));

        }
        try {
            Database.Data.insert("Caches", args);

        } catch (Exception exc) {
            log.error("Write Cache", exc);

        }
    }

    @Override
    public void writeToDatabaseFound(Database database, AbstractCache abstractCache) {
        Database.Parameters args = new Database.Parameters();
        args.put("found", abstractCache.isFound());
        try {
            Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(abstractCache.getId())});
            Replication.FoundChanged(abstractCache.getId(), abstractCache.isFound());
        } catch (Exception exc) {
            log.error("Write Cache Found", exc);
        }
    }

    @Override
    public boolean updateDatabase(Database database, AbstractCache abstractCache) {

        Database.Parameters args = new Database.Parameters();

        args.put("Id", abstractCache.getId());
        args.put("GcCode", abstractCache.getGcCode());
        args.put("GcId", abstractCache.getGcId());
        if (abstractCache.isValid() && !abstractCache.isZero()) {
            // Update Cache position only when new position is valid and not zero
            args.put("Latitude", abstractCache.latitude);
            args.put("Longitude", abstractCache.longitude);
        }
        args.put("name", abstractCache.getName());
        try {
            args.put("Size", abstractCache.getSize().ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        args.put("Difficulty", (int) (abstractCache.getDifficulty() * 2));
        args.put("Terrain", (int) (abstractCache.getTerrain() * 2));
        args.put("Archived", abstractCache.isArchived() ? 1 : 0);
        args.put("Available", abstractCache.isAvailable() ? 1 : 0);
        args.put("Found", abstractCache.isFound());
        args.put("Type", abstractCache.getType().ordinal());
        args.put("PlacedBy", abstractCache.getPlacedBy());
        args.put("Owner", abstractCache.getOwner());
        args.put("Country", abstractCache.getCountry());
        args.put("State", abstractCache.getState());
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String stimestamp = iso8601Format.format(abstractCache.getDateHidden());
            args.put("DateHidden", stimestamp);
        } catch (Exception e) {

            e.printStackTrace();
        }
        args.put("Hint", abstractCache.getHint(database));

        if ((abstractCache.getShortDescription(database) != null) && (abstractCache.getShortDescription(database).length() > 0)) {
            args.put("ShortDescription", abstractCache.getShortDescription(database));
        }

        if ((abstractCache.getLongDescription(database) != null) && (abstractCache.getLongDescription(database).length() > 0)) {
            args.put("Description", abstractCache.getLongDescription(database));
        }

        args.put("Url", abstractCache.getUrl());
        args.put("NumTravelbugs", abstractCache.getNumTravelbugs());
        args.put("Rating", (int) (abstractCache.getRating() * 100));
        // args.put("Vote", cache.);
        // args.put("VotePending", cache.);
        // args.put("Notes", );
        // args.put("Solver", cache.);
        args.put("AttributesPositive", abstractCache.getAttributesPositive().getLow());
        args.put("AttributesPositiveHigh", abstractCache.getAttributesPositive().getHigh());
        args.put("AttributesNegative", abstractCache.getAttributesNegative().getLow());
        args.put("AttributesNegativeHigh", abstractCache.getAttributesNegative().getHigh());
        // args.put("ListingCheckSum", cache.);
        args.put("GPXFilename_Id", abstractCache.getGPXFilename_ID());
        args.put("Favorit", abstractCache.isFavorite() ? 1 : 0);
        args.put("ApiStatus", abstractCache.getApiState());
        args.put("CorrectedCoordinates", abstractCache.hasCorrectedCoordinates() ? 1 : 0);
        args.put("TourName", abstractCache.getTourName());

        try {
            long ret = Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(abstractCache.getId())});
            return ret > 0;
        } catch (Exception exc) {
            log.error("Update Cache", exc);
            return false;

        }
    }

    @Override
    public AbstractCache getFromDbByCacheId(Database database, long CacheID, boolean withWaypoints) {
        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery(SQL_GET_CACHE + SQL_BY_ID, new String[]{String.valueOf(CacheID)});

        Cache ret;
        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                ret = ReadFromCursor(reader, false, false);

                reader.close();

            } else {
                if (reader != null)
                    reader.close();
                return null;
            }
        } catch (Exception e) {
            if (reader != null)
                reader.close();
            e.printStackTrace();
            return null;
        } finally {
            reader = null;
        }

        if (withWaypoints && ret != null) {
            ret.setWaypoints(getWaypointDAO().getWaypointsFromCacheID(Database.Data, ret.getId(), true));
        }
        return ret;
    }

//    public AbstractCache getFromDbByGcCode(String GcCode, boolean withDetail) // NO_UCD (test only)
//    {
//        String where = SQL_GET_CACHE + (withDetail ? ", " + SQL_DETAILS : "") + SQL_BY_GC_CODE;
//
//        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery(where, new String[]{GcCode});
//
//        try {
//            if (reader != null && reader.getCount() > 0) {
//                reader.moveToFirst();
//                AbstractCache ret = ReadFromCursor(reader, withDetail, false);
//
//                reader.close();
//                return ret;
//            } else {
//                if (reader != null)
//                    reader.close();
//                return null;
//            }
//        } catch (Exception e) {
//            if (reader != null)
//                reader.close();
//            e.printStackTrace();
//            return null;
//        }
//
//    }

//    public Boolean cacheExists(long CacheID) {
//
//        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery(SQL_EXIST_CACHE, new String[]{String.valueOf(CacheID)});
//
//        boolean exists = (reader.getCount() > 0);
//
//        reader.close();
//
//        return exists;
//
//    }

    @Override
    public boolean updateDatabaseCacheState(Database database, AbstractCache writeTmp) {

        // chk of changes
        boolean changed = false;
        AbstractCache fromDB = getFromDbByCacheId(database, writeTmp.getId(),false);

        if (fromDB == null)
            return false; // nichts zum Updaten gefunden

        if (fromDB.isArchived() != writeTmp.isArchived()) {
            changed = true;
            Replication.ArchivedChanged(writeTmp.getId(), writeTmp.isArchived());
        }
        if (fromDB.isAvailable() != writeTmp.isAvailable()) {
            changed = true;
            Replication.AvailableChanged(writeTmp.getId(), writeTmp.isAvailable());
        }

        if (fromDB.getNumTravelbugs() != writeTmp.getNumTravelbugs()) {
            changed = true;
            Replication.NumTravelbugsChanged(writeTmp.getId(), writeTmp.getNumTravelbugs());
        }

        if (changed) {

            Database.Parameters args = new Database.Parameters();

            args.put("Archived", writeTmp.isArchived() ? 1 : 0);
            args.put("Available", writeTmp.isAvailable() ? 1 : 0);
            args.put("NumTravelbugs", writeTmp.getNumTravelbugs());

            try {
                Database.Data.update("Caches", args, "Id = ?", new String[]{String.valueOf(writeTmp.getId())});
            } catch (Exception exc) {
                log.error("Update Cache", exc);

            }
        }

        return changed;
    }

//    public ArrayList<String> getGcCodesFromMustLoadImages() {
//
//        ArrayList<String> GcCodes = new ArrayList<String>();
//
//        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select GcCode from Caches where Type<>4 and (ImagesUpdated=0 or DescriptionImagesUpdated=0)", null);
//
//        if (reader.getCount() > 0) {
//            reader.moveToFirst();
//            while (!reader.isAfterLast()) {
//                String GcCode = reader.getString(0);
//                GcCodes.add(GcCode);
//                reader.moveToNext();
//            }
//        }
//        reader.close();
//        return GcCodes;
//    }
//
//    public Boolean loadBooleanValue(String gcCode, String key) {
//        SQLiteGdxDatabaseCursor reader = Database.Data.rawQuery("select " + key + " from Caches where GcCode = \"" + gcCode + "\"", null);
//        try {
//            reader.moveToFirst();
//            while (!reader.isAfterLast()) {
//                if (reader.getInt(0) != 0) { // gefunden. Suche abbrechen
//                    return true;
//                }
//                reader.moveToNext();
//            }
//        } catch (Exception ex) {
//            return false;
//        } finally {
//            reader.close();
//        }
//
//        return false;
//    }

}
