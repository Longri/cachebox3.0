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
package de.longri.cachebox3.sqlite;

import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.types.ImmutableCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 18.10.2017.
 */
public class AlterCachebox3DB {

    private final static Logger log = LoggerFactory.getLogger(AlterCachebox3DB.class);

    public void alterCachebox3DB(Database database, int lastDatabaseSchemeVersion) {

        if (lastDatabaseSchemeVersion < DatabaseVersions.LatestDatabaseChange) {
            // update to latest ACB2 Database Version
            new AlterCacheboxDB().alterCacheboxDB(database, DatabaseVersions.LatestDatabaseChange);
        }

        database.beginTransaction();
        try {
            if (lastDatabaseSchemeVersion <= 1028) {
                // Convert DB from version ACB2 to CB3
                //create new Tables
                DatabaseSchema schemaStrings = new DatabaseSchema();
                database.execSQL(schemaStrings.CACHE_CORE_INFO);
                database.execSQL(schemaStrings.ATTRIBUTES);
                database.execSQL(schemaStrings.TEXT);
                database.execSQL(schemaStrings.CACHE_INFO);
                database.execSQL(schemaStrings.WAYPOINTS);
                database.execSQL(schemaStrings.WAYPOINTS_TEXT);

                //drop alt Tables
                database.execSQL("DROP TABLE CelltowerLocation;");
                database.execSQL("DROP TABLE Locations;");
                database.execSQL("DROP TABLE SdfExport;");

                //copy values
                database.execSQL(schemaStrings.COPY_DATA_FROM_V2_TO_V3);
                database.execSQL(schemaStrings.COPY_ATTRIBUTES_FROM_V2_TO_V3);
                database.execSQL(schemaStrings.COPY_CACHEINFO_FROM_V2_TO_V3);
                database.execSQL(schemaStrings.COPY_TEXT_FROM_V2_TO_V3);
                database.execSQL(schemaStrings.COPY_WAYPOINTS_FROM_V2_TO_V3);
                database.execSQL(schemaStrings.COPY_WAYPOINTS_TEXT_FROM_V2_TO_V3);

                //convert Boolean values to Short
                {
                    // get list of Id's
                    Array<Long> allIds = new Array<>();
                    SQLiteGdxDatabaseCursor cursor = database.rawQuery("SELECT id from CacheCoreInfo", null);
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        long id = cursor.getLong(0);
                        allIds.add(id);
                        cursor.moveToNext();
                    }

                    //read all boolean for id
                    int i = 0;
                    int n = allIds.size;
                    while (n-- > 0) {
                        cursor = database.rawQuery("SELECT Archived, Available, Found, VotePending, Favorit, " +
                                "HasUserData, ListingChanged, ImagesUpdated, DescriptionImagesUpdated, " +
                                "CorrectedCoordinates, Hint" +
                                " from Caches WHERE Id=?", new String[]{String.valueOf(allIds.get(i))});
                        cursor.moveToFirst();
                        boolean Archived = cursor.getInt(0) != 0;
                        boolean Available = cursor.getInt(1) != 0;
                        boolean Found = cursor.getInt(2) != 0;
                        boolean VotePending = cursor.getInt(3) != 0;
                        boolean Favorit = cursor.getInt(4) != 0;
                        boolean HasUserData = cursor.getInt(5) != 0;
                        boolean ListingChanged = cursor.getInt(6) != 0;
                        boolean ImagesUpdated = cursor.getInt(7) != 0;
                        boolean DescriptionImagesUpdated = cursor.getInt(8) != 0;
                        boolean CorrectedCoordinates = cursor.getInt(9) != 0;
                        String hint = cursor.getString(10);
                        boolean hasHint = hint != null && hint.length() > 0;

                        short bitFlags = 0;
                        bitFlags = ImmutableCache.setMaskValue(ImmutableCache.MASK_ARCHIVED, Archived, bitFlags);
                        bitFlags = ImmutableCache.setMaskValue(ImmutableCache.MASK_AVAILABLE, Available, bitFlags);
                        bitFlags = ImmutableCache.setMaskValue(ImmutableCache.MASK_FOUND, Found, bitFlags);
                        bitFlags = ImmutableCache.setMaskValue(ImmutableCache.MASK_FAVORITE, Favorit, bitFlags);
                        bitFlags = ImmutableCache.setMaskValue(ImmutableCache.MASK_HAS_USER_DATA, HasUserData, bitFlags);
                        bitFlags = ImmutableCache.setMaskValue(ImmutableCache.MASK_LISTING_CHANGED, ListingChanged, bitFlags);
                        bitFlags = ImmutableCache.setMaskValue(ImmutableCache.MASK_CORECTED_COORDS, CorrectedCoordinates, bitFlags);
                        bitFlags = ImmutableCache.setMaskValue(ImmutableCache.MASK_HAS_HINT, hasHint, bitFlags);

                        //Store bitFlags
                        Database.Parameters args = new Database.Parameters();
                        args.put("BooleanStore", bitFlags);
                        database.update("CacheCoreInfo", args, "id=" + String.valueOf(allIds.get(i)), null);

                        i++;
                        cursor.close();
                    }

                }


                //Delete Data from Caches
                database.execSQL("DELETE FROM Caches;");

                //Delete Data from Waypont
                database.execSQL("DELETE FROM Waypoint;");

                database.setTransactionSuccessful();
                database.endTransaction();

                //drop alt Table Caches, Waypoint (Close and reopen connection)
                database.close();
                database.open();
                database.disableAutoCommit();
                database.execSQL("DROP TABLE Caches;");
                database.execSQL("DROP TABLE Waypoint;");


                //execute VACUUM
                database.setTransactionSuccessful();
                database.endTransaction();
                database.close();
                database.open();
                database.disableAutoCommit();
                database.execSQL("end transaction");
                database.execSQL("VACUUM");
            }


        } catch (Exception exc) {
            log.error("alterDatabase", exc);
        } finally {

        }
    }


}
