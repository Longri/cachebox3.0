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

import com.badlogic.gdx.utils.StringBuilder;
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

            String sql = schemaStrings.getEmptyNewDB();

            //EXECUTE combined SQL
            database.execSQL(sql);

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
                //add column desired on Config Table
                boolean isExist = false;
                GdxSqliteCursor cursor = database.rawQuery("PRAGMA table_info(Config)");
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    if (cursor.getString(1).equals("desired")) {
                        isExist = true;
                        break;
                    }
                    cursor.next();
                }

                DatabaseSchema schemaStrings = new DatabaseSchema();
                StringBuilder sb = new StringBuilder();

                sb.append("BEGIN TRANSACTION;").append("\n");

                if (!isExist)
                    sb.append("ALTER TABLE Config ADD desired ntext;").append("\n");

                //create new Tables

                sb.append(schemaStrings.CACHE_CORE_INFO).append("\n");
                sb.append(schemaStrings.ATTRIBUTES).append("\n");
                sb.append(schemaStrings.TEXT).append("\n");
                sb.append(schemaStrings.CACHE_INFO).append("\n");
                sb.append(schemaStrings.WAYPOINTS).append("\n");
                sb.append(schemaStrings.WAYPOINTS_TEXT).append("\n");

                sb.append(schemaStrings.CACHE_CORE_INFO_IX_ID).append("\n");

                //drop alt Tables
                sb.append("DROP TABLE CelltowerLocation;").append("\n");
                sb.append("DROP TABLE Locations;").append("\n");
                sb.append("DROP TABLE SdfExport;").append("\n");

                //copy values
                sb.append(schemaStrings.COPY_DATA_FROM_V2_TO_V3).append("\n");
                sb.append(schemaStrings.COPY_ATTRIBUTES_FROM_V2_TO_V3).append("\n");
                sb.append(schemaStrings.COPY_CACHEINFO_FROM_V2_TO_V3).append("\n");
                sb.append(schemaStrings.COPY_TEXT_FROM_V2_TO_V3).append("\n");
                sb.append(schemaStrings.COPY_WAYPOINTS_FROM_V2_TO_V3).append("\n");
                sb.append(schemaStrings.COPY_WAYPOINTS_TEXT_FROM_V2_TO_V3).append("\n");

                {// Convert CacheSizes
                    sb.append("UPDATE CacheCoreInfo SET Size = Size - 1;").append("\n");
                    sb.append("UPDATE CacheCoreInfo SET Size = 4 WHERE Size<0;").append("\n");
                }

                //Delete Data from Caches
                sb.append("DELETE FROM Caches;").append("\n");

                //Delete Data from Waypont
                sb.append("DELETE FROM Waypoint;").append("\n");

                sb.append("DROP TABLE Caches;").append("\n");
                sb.append("DROP TABLE Waypoint;").append("\n");
                sb.append("END TRANSACTION;").append("\n");
                sb.append("VACUUM").append("\n");


                //EXECUTE combined SQL
                database.execSQL(sb.toString());
                log.debug("FINISH Convert Database from ACB to CB3");
            }
        } catch (Exception exc) {
            log.error("alterDatabase", exc);
        }

        try {
            if (lastDatabaseSchemeVersion < 1029) {
                // add primary key on [Key]
                String CREATE = "CREATE TABLE ConfigCopy (\n" +
                        "    [Key]      NVARCHAR (30)  NOT NULL\n" +
                        "                              PRIMARY KEY\n" +
                        "                              UNIQUE,\n" +
                        "    Value      NVARCHAR (255),\n" +
                        "    LongString NTEXT,\n" +
                        "    desired    NTEXT\n" +
                        ");";
                String COPY = "INSERT INTO ConfigCopy SELECT * FROM Config;";
                String DROP = "DROP TABLE Config;";
                String RENAME = "ALTER TABLE ConfigCopy RENAME TO Config;";

                String SQL = CREATE + COPY + DROP + RENAME;
                database.execSQL(SQL);
            }
        } catch (Exception exc) {
            log.error("alterDatabase", exc);
        }

        try {
            if (lastDatabaseSchemeVersion < 1030) {
                //Extend Config with Blob
                database.execSQL("ALTER TABLE [Config] ADD [blob] BLOB;");
            }
        } catch (Exception exc) {
            log.error("alterDatabase", exc);
        }
    }


}
