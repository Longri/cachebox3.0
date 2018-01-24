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

import de.longri.cachebox3.types.Categories;
import de.longri.cachebox3.types.Category;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Longri on 16.10.2017.
 */
public class AlterCacheboxDB {

    private final static Logger log = LoggerFactory.getLogger(AlterCacheboxDB.class);

    public void alterCacheboxDB(Database database, int lastDatabaseSchemeVersion) {
        database.beginTransaction();
        try {
            if (lastDatabaseSchemeVersion <= 0) {
                // First Initialization of the Database
                database.execSQL("CREATE TABLE [Caches] ([Id] bigint NOT NULL primary key,[GcCode] nvarchar (12) NULL,[GcId] nvarchar (255) NULL,[Latitude] float NULL,[Longitude] float NULL,[name] nchar (255) NULL,[Size] int NULL,[Difficulty] smallint NULL,[Terrain] smallint NULL,[Archived] bit NULL,[Available] bit NULL,[Found] bit NULL,[Type] smallint NULL,[PlacedBy] nvarchar (255) NULL,[Owner] nvarchar (255) NULL,[DateHidden] datetime NULL,[Hint] ntext NULL,[Description] ntext NULL,[Url] nchar (255) NULL,[NumTravelbugs] smallint NULL,[Rating] smallint NULL,[Vote] smallint NULL,[VotePending] bit NULL,[Notes] ntext NULL,[Solver] ntext NULL,[Favorit] bit NULL,[AttributesPositive] bigint NULL,[AttributesNegative] bigint NULL,[TourName] nchar (255) NULL,[GPXFilename_Id] bigint NULL,[HasUserData] bit NULL,[ListingCheckSum] int NULL DEFAULT 0,[ListingChanged] bit NULL,[ImagesUpdated] bit NULL,[DescriptionImagesUpdated] bit NULL,[CorrectedCoordinates] bit NULL);");
                database.execSQL("CREATE INDEX [archived_idx] ON [Caches] ([Archived] ASC);");
                database.execSQL("CREATE INDEX [AttributesNegative_idx] ON [Caches] ([AttributesNegative] ASC);");
                database.execSQL("CREATE INDEX [AttributesPositive_idx] ON [Caches] ([AttributesPositive] ASC);");
                database.execSQL("CREATE INDEX [available_idx] ON [Caches] ([Available] ASC);");
                database.execSQL("CREATE INDEX [Difficulty_idx] ON [Caches] ([Difficulty] ASC);");
                database.execSQL("CREATE INDEX [Favorit_idx] ON [Caches] ([Favorit] ASC);");
                database.execSQL("CREATE INDEX [found_idx] ON [Caches] ([Found] ASC);");
                database.execSQL("CREATE INDEX [GPXFilename_Id_idx] ON [Caches] ([GPXFilename_Id] ASC);");
                database.execSQL("CREATE INDEX [HasUserData_idx] ON [Caches] ([HasUserData] ASC);");
                database.execSQL("CREATE INDEX [ListingChanged_idx] ON [Caches] ([ListingChanged] ASC);");
                database.execSQL("CREATE INDEX [NumTravelbugs_idx] ON [Caches] ([NumTravelbugs] ASC);");
                database.execSQL("CREATE INDEX [placedby_idx] ON [Caches] ([PlacedBy] ASC);");
                database.execSQL("CREATE INDEX [Rating_idx] ON [Caches] ([Rating] ASC);");
                database.execSQL("CREATE INDEX [Size_idx] ON [Caches] ([Size] ASC);");
                database.execSQL("CREATE INDEX [Terrain_idx] ON [Caches] ([Terrain] ASC);");
                database.execSQL("CREATE INDEX [Type_idx] ON [Caches] ([Type] ASC);");

                database.execSQL("CREATE TABLE [CelltowerLocation] ([CellId] nvarchar (20) NOT NULL primary key,[Latitude] float NULL,[Longitude] float NULL);");

                database.execSQL("CREATE TABLE [GPXFilenames] ([Id] integer not null primary key autoincrement,[GPXFilename] nvarchar (255) NULL,[Imported] datetime NULL, [name] nvarchar (255) NULL,[CacheCount] int NULL);");

                database.execSQL("CREATE TABLE [Logs] ([Id] bigint NOT NULL primary key, [CacheId] bigint NULL,[Timestamp] datetime NULL,[Finder] nvarchar (128) NULL,[Type] smallint NULL,[Comment] ntext NULL);");
                database.execSQL("CREATE INDEX [log_idx] ON [Logs] ([CacheId] ASC);");
                database.execSQL("CREATE INDEX [timestamp_idx] ON [Logs] ([Timestamp] ASC);");

                database.execSQL("CREATE TABLE [PocketQueries] ([Id] integer not null primary key autoincrement,[PQName] nvarchar (255) NULL,[CreationTimeOfPQ] datetime NULL);");

                database.execSQL("CREATE TABLE [Waypoint] ([GcCode] nvarchar (12) NOT NULL primary key,[CacheId] bigint NULL,[Latitude] float NULL,[Longitude] float NULL,[Description] ntext NULL,[Clue] ntext NULL,[Type] smallint NULL,[SyncExclude] bit NULL,[UserWaypoint] bit NULL,[Title] ntext NULL);");
                database.execSQL("CREATE INDEX [UserWaypoint_idx] ON [Waypoint] ([UserWaypoint] ASC);");

                database.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                database.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");

                database.execSQL("CREATE TABLE [Replication] ([Id] integer not null primary key autoincrement, [ChangeType] int NOT NULL, [CacheId] bigint NOT NULL, [WpGcCode] nvarchar (12) NULL, [SolverCheckSum] int NULL, [NotesCheckSum] int NULL, [WpCoordCheckSum] int NULL);");
                database.execSQL("CREATE INDEX [Replication_idx] ON [Replication] ([Id] ASC);");
                database.execSQL("CREATE INDEX [ReplicationCache_idx] ON [Replication] ([CacheId] ASC);");
            }

            if (lastDatabaseSchemeVersion < 1003) {
                database.execSQL("CREATE TABLE [Locations] ([Id] integer not null primary key autoincrement, [name] nvarchar (255) NULL, [Latitude] float NULL, [Longitude] float NULL);");
                database.execSQL("CREATE INDEX [Locatioins_idx] ON [Locations] ([Id] ASC);");

                database.execSQL("CREATE TABLE [SdfExport] ([Id]  integer not null primary key autoincrement, [Description] nvarchar(255) NULL, [ExportPath] nvarchar(255) NULL, [MaxDistance] float NULL, [LocationID] Bigint NULL, [Filter] ntext NULL, [Update] bit NULL, [ExportImages] bit NULL, [ExportSpoilers] bit NULL, [ExportMaps] bit NULL, [OwnRepository] bit NULL, [ExportMapPacks] bit NULL, [MaxLogs] int NULL);");
                database.execSQL("CREATE INDEX [SdfExport_idx] ON [SdfExport] ([Id] ASC);");

                database.execSQL("ALTER TABLE [CACHES] ADD [FirstImported] datetime NULL;");

                database.execSQL("CREATE TABLE [Category] ([Id]  integer not null primary key autoincrement, [GpxFilename] nvarchar(255) NULL, [Pinned] bit NULL default 0, [CacheCount] int NULL);");
                database.execSQL("CREATE INDEX [Category_idx] ON [Category] ([Id] ASC);");

                database.execSQL("ALTER TABLE [GpxFilenames] ADD [CategoryId] bigint NULL;");

                database.execSQL("ALTER TABLE [Caches] add [state] nvarchar(50) NULL;");
                database.execSQL("ALTER TABLE [Caches] add [country] nvarchar(50) NULL;");
            }
            if (lastDatabaseSchemeVersion < 1015) {
                // GpxFilenames mit Kategorien verknüpfen

                // alte Category Tabelle löschen
                database.delete("Category", "");
                HashMap<Long, String> gpxFilenames = new HashMap<Long, String>();
                HashMap<String, Long> categories = new HashMap<String, Long>();

                try {
                    GdxSqliteCursor reader = database.rawQuery("select ID, GPXFilename from GPXFilenames", (String[]) null);
                    reader.moveToFirst();
                    while (!reader.isAfterLast()) {
                        long id = reader.getLong(0);
                        String gpxFilename = reader.getString(1);
                        gpxFilenames.put(id, gpxFilename);
                        reader.moveToNext();
                    }
                    reader.close();
                } catch (Exception e) {
                    //no GPXFilenames stored
                }
                for (Map.Entry<Long, String> entry : gpxFilenames.entrySet()) {
                    if (!categories.containsKey(entry.getValue())) {
                        // add new Category
                        Categories cs = new Categories();
                        Category category = cs.createNewCategory(entry.getValue());
                        // and store
                        categories.put(entry.getValue(), category.Id);
                    }
                    if (categories.containsKey(entry.getValue())) {
                        // and store CategoryId in GPXFilenames
                        Database.Parameters args = new Database.Parameters();
                        args.put("CategoryId", categories.get(entry.getValue()));
                        try {
                            Database.Data.update("GpxFilenames", args, "Id=" + entry.getKey(), null);
                        } catch (Exception exc) {
                            log.error("Update_CategoryId", exc);
                        }
                    }
                }

            }
            if (lastDatabaseSchemeVersion < 1016) {
                database.execSQL("ALTER TABLE [CACHES] ADD [ApiStatus] smallint NULL default 0;");
            }
            if (lastDatabaseSchemeVersion < 1017) {
                database.execSQL("CREATE TABLE [Trackable] ([Id] integer not null primary key autoincrement, [Archived] bit NULL, [GcCode] nvarchar (12) NULL, [CacheId] bigint NULL, [CurrentGoal] ntext, [CurrentOwnerName] nvarchar (255) NULL, [DateCreated] datetime NULL, [Description] ntext, [IconUrl] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [path] nvarchar (255) NULL, [OwnerName] nvarchar (255), [Url] nvarchar (255) NULL);");
                database.execSQL("CREATE INDEX [cacheid_idx] ON [Trackable] ([CacheId] ASC);");
                database.execSQL("CREATE TABLE [TbLogs] ([Id] integer not null primary key autoincrement, [TrackableId] integer not NULL, [CacheID] bigint NULL, [GcCode] nvarchar (12) NULL, [LogIsEncoded] bit NULL DEFAULT 0, [LogText] ntext, [LogTypeId] bigint NULL, [LoggedByName] nvarchar (255) NULL, [Visited] datetime NULL);");
                database.execSQL("CREATE INDEX [trackableid_idx] ON [TbLogs] ([TrackableId] ASC);");
                database.execSQL("CREATE INDEX [trackablecacheid_idx] ON [TBLOGS] ([CacheId] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1018) {
                database.execSQL("ALTER TABLE [SdfExport] ADD [MapPacks] nvarchar(512) NULL;");

            }
            if (lastDatabaseSchemeVersion < 1019) {
                // neue Felder für die erweiterten Attribute einfügen
                database.execSQL("ALTER TABLE [CACHES] ADD [AttributesPositiveHigh] bigint NULL default 0");
                database.execSQL("ALTER TABLE [CACHES] ADD [AttributesNegativeHigh] bigint NULL default 0");

                // Die Nummerierung der Attribute stimmte nicht mit der von
                // Groundspeak überein. Bei 16 und 45 wurde jeweils eine
                // Nummber übersprungen
                try {
                    GdxSqliteCursor reader = database.rawQuery("select Id, AttributesPositive, AttributesNegative from Caches", new String[]{});
                    reader.moveToFirst();
                    while (!reader.isAfterLast()) {
                        long id = reader.getLong(0);
                        long attributesPositive = reader.getLong(1);
                        long attributesNegative = reader.getLong(2);

                        attributesPositive = convertAttribute(attributesPositive);
                        attributesNegative = convertAttribute(attributesNegative);

                        Database.Parameters val = new Database.Parameters();
                        val.put("AttributesPositive", attributesPositive);
                        val.put("AttributesNegative", attributesNegative);
                        String whereClause = "[Id]=" + id;
                        database.update("Caches", val, whereClause, null);
                        reader.moveToNext();
                    }
                    reader.close();
                } catch (Exception e) {
                    // no attributes stored
                }

            }
            if (lastDatabaseSchemeVersion < 1020) {
                // for long Settings
                database.execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");

            }
            if (lastDatabaseSchemeVersion < 1021) {
                // Image Table
                database.execSQL("CREATE TABLE [Images] ([Id] integer not null primary key autoincrement, [CacheId] bigint NULL, [GcCode] nvarchar (12) NULL, [Description] ntext, [name] nvarchar (255) NULL, [ImageUrl] nvarchar (255) NULL, [IsCacheImage] bit NULL);");
                database.execSQL("CREATE INDEX [images_cacheid_idx] ON [Images] ([CacheId] ASC);");
                database.execSQL("CREATE INDEX [images_gccode_idx] ON [Images] ([GcCode] ASC);");
                database.execSQL("CREATE INDEX [images_iscacheimage_idx] ON [Images] ([IsCacheImage] ASC);");
                database.execSQL("CREATE UNIQUE INDEX [images_imageurl_idx] ON [Images] ([ImageUrl] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1022) {
                database.execSQL("ALTER TABLE [Caches] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");

                database.execSQL("ALTER TABLE [Waypoint] DROP CONSTRAINT Waypoint_PK ");
                database.execSQL("ALTER TABLE [Waypoint] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                database.execSQL("ALTER TABLE [Waypoint] ADD CONSTRAINT  [Waypoint_PK] PRIMARY KEY ([GcCode]); ");

                database.execSQL("ALTER TABLE [Replication] ALTER COLUMN [WpGcCode] nvarchar(15) NOT NULL; ");
                database.execSQL("ALTER TABLE [Trackable] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                database.execSQL("ALTER TABLE [TbLogs] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
                database.execSQL("ALTER TABLE [Images] ALTER COLUMN [GcCode] nvarchar(15) NOT NULL; ");
            }
            if (lastDatabaseSchemeVersion < 1024) {
                database.execSQL("ALTER TABLE [Waypoint] ADD COLUMN [IsStart] BOOLEAN DEFAULT 'false' NULL");
            }
            if (lastDatabaseSchemeVersion < 1025) {
                // nicht mehr benötigt database.execSQL("ALTER TABLE [Waypoint] ADD COLUMN [UserNote] ntext NULL");
            }

            if (lastDatabaseSchemeVersion < 1026) {
                // add one column for short description
                // [ShortDescription] ntext NULL
                database.execSQL("ALTER TABLE [Caches] ADD [ShortDescription] ntext NULL;");
            }
            if (lastDatabaseSchemeVersion < 1027) {
                // Long Text Field for long Strings
                database.execSQL("ALTER TABLE [Config] ADD [desired] ntext NULL;");
            }
        } catch (Exception exc) {
            log.error("alterDatabase", exc);
        } finally {
            database.endTransaction();
        }
    }

    private long convertAttribute(long att) {
        // Die Nummerierung der Attribute stimmte nicht mit der von Groundspeak
        // überein. Bei 16 und 45 wurde jeweils eine Nummber übersprungen
        long result = 0;
        // Maske für die untersten 15 bit
        long mask = 0;
        for (int i = 0; i < 16; i++)
            mask += (long) 1 << i;
        // unterste 15 bit ohne Verschiebung kopieren
        result = att & mask;
        // Maske für die Bits 16-45
        mask = 0;
        for (int i = 16; i < 45; i++)
            mask += (long) 1 << i;
        long tmp = att & mask;
        // Bits 16-44 um eins verschieben
        tmp = tmp << 1;
        // und zum Result kopieren
        result += tmp;
        // Maske für die Bits 45-45
        mask = 0;
        for (int i = 45; i < 63; i++)
            mask += (long) 1 << i;
        tmp = att & mask;
        // Bits 45-63 um 2 verschieben
        tmp = tmp << 2;
        // und zum Result kopieren
        result += tmp;

        return result;
    }

}
