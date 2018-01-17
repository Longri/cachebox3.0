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

import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 18.10.2017.
 */
public class AlterCachebox3DB {

    private final static Logger log = LoggerFactory.getLogger(AlterCachebox3DB.class);

    public void alterCachebox3DB(Database database, int lastDatabaseSchemeVersion) {

        if (lastDatabaseSchemeVersion == -1) {
            //create new Database
            DatabaseSchema schemaStrings = new DatabaseSchema();

            database.execSQL(schemaStrings.CONFIG_TABLE);
            database.execSQL(schemaStrings.CATEGORY_TABLE);
            database.execSQL(schemaStrings.GPX_FILE_NAMES);
            database.execSQL(schemaStrings.IMAGES);
            database.execSQL(schemaStrings.LOGS);
            database.execSQL(schemaStrings.POCKET_QUERIES);
            database.execSQL(schemaStrings.REPLICATION);
            database.execSQL(schemaStrings.TB_LOGS);
            database.execSQL(schemaStrings.TRACKABLE);
            database.execSQL(schemaStrings.CACHE_CORE_INFO);
            database.execSQL(schemaStrings.ATTRIBUTES);
            database.execSQL(schemaStrings.TEXT);
            database.execSQL(schemaStrings.CACHE_INFO);
            database.execSQL(schemaStrings.WAYPOINTS);
            database.execSQL(schemaStrings.WAYPOINTS_TEXT);

            return;
        }

        if (lastDatabaseSchemeVersion < DatabaseVersions.LatestDatabaseChange) {
            // update to latest ACB2 Database Version
            new AlterCacheboxDB().alterCacheboxDB(database, DatabaseVersions.LatestDatabaseChange);
        }


        try {
            if (lastDatabaseSchemeVersion < 1028) {

                log.debug("Convert Database from ACB to CB3");

                // Convert DB from version ACB2 to CB3
//                database.beginTransaction();
                //add column desired on Config Table

                boolean isExist = false;
                GdxSqliteCursor cursor = database.rawQuery("PRAGMA table_info(Config)");
                cursor.moveToFirst();
                while (cursor.next()){
                    if(cursor.getString(1).equals("desired")){
                        isExist = true;
                        break;
                    }
                }

                if(!isExist)database.execSQL("ALTER TABLE Config ADD desired ntext;");

                //create new Tables
                DatabaseSchema schemaStrings = new DatabaseSchema();
                database.execSQL(schemaStrings.CACHE_CORE_INFO);
                database.execSQL(schemaStrings.ATTRIBUTES);
                database.execSQL(schemaStrings.TEXT);
                database.execSQL(schemaStrings.CACHE_INFO);
                database.execSQL(schemaStrings.WAYPOINTS);
                database.execSQL(schemaStrings.WAYPOINTS_TEXT);

                database.execSQL(schemaStrings.CACHE_CORE_INFO_IX_ID);

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

                {// Convert CacheSizes
                    database.execSQL("UPDATE CacheCoreInfo SET Size = Size - 1");
                    database.execSQL("UPDATE CacheCoreInfo SET Size = 4 WHERE Size<0");
                }

                //Delete Data from Caches
                database.execSQL("DELETE FROM Caches;");

                //Delete Data from Waypont
                database.execSQL("DELETE FROM Waypoint;");

                //drop alt Table Caches, Waypoint (Close and reopen connection)
                database.close();
                database.open();
                database.execSQL("DROP TABLE Caches;");
                database.execSQL("DROP TABLE Waypoint;");


                //execute VACUUM
                database.close();
                database.open();
                database.execSQL("VACUUM");
                log.debug("FINISH Convert Database from ACB to CB3");
            }


        } catch (Exception exc) {
            log.error("alterDatabase", exc);
        } finally {

        }
    }


}
