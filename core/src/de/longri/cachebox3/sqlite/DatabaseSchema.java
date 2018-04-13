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

/**
 * Holds the SQL create strings for create a new Database with actual schema
 * <p>
 * Created by Longri on 18.10.2017.
 */
public class DatabaseSchema {
    public static final String CACHE_CORE_INFO_IX_ID = "CREATE INDEX idx_id ON CacheCoreInfo (\n" +
            "    Id COLLATE BINARY ASC\n" +
            ");";
    public final String CACHE_CORE_INFO = "CREATE TABLE CacheCoreInfo (\n" +
            " Id     BIGINT         NOT NULL\n" +
            "      PRIMARY KEY,\n" +
            " Latitude    FLOAT,\n" +
            " Longitude    FLOAT,\n" +
            " Size     SMALLINT,\n" +
            " Difficulty    SMALLINT,\n" +
            " Terrain    SMALLINT,\n" +
            " Type     SMALLINT,\n" +
            " Rating    SMALLINT,\n" +
            " NumTravelbugs   SMALLINT,\n" +
            " GcCode    NCHAR (12),\n" +
            " Name    NVARCHAR (255),\n" +
            " PlacedBy    NVARCHAR (255),\n" +
            " Owner    NVARCHAR (255),\n" +
            " GcId     NVARCHAR (255),\n" +
            " BooleanStore        SMALLINT,\n" +
            " FavPoints        INT,\n" +
            " Vote            SMALLINT\n" +
            " );       ";

    public final String ATTRIBUTES = "CREATE TABLE Attributes (\n" +
            "   Id                         BIGINT\n" +
            "                              PRIMARY KEY\n" +
            "                              UNIQUE,\n" +
            "  AttributesPositive        BIGINT,\n" +
            "  AttributesNegative       BIGINT,\n" +
            "  AttributesPositiveHigh    BIGINT         DEFAULT 0,\n" +
            "  AttributesNegativeHigh    BIGINT         DEFAULT 0\n" +
            " );";

    public final String TEXT = "CREATE TABLE CacheText (\n" +
            "   Id                         BIGINT\n" +
            "                              PRIMARY KEY\n" +
            "                              UNIQUE,\n" +
            "   Url                        NVARCHAR (255),\n" +
            "   Hint                       NTEXT,\n" +
            "   Description                NTEXT,\n" +
            "   Notes                      NTEXT,\n" +
            "   Solver                     NTEXT,\n" +
            "   ShortDescription          NTEXT\n" +
            "  );";

    public final String CACHE_INFO = "CREATE TABLE CacheInfo (\n" +
            "    Id                         BIGINT\n" +
            "                              PRIMARY KEY\n" +
            "                              UNIQUE,\n" +
            "    DateHidden                DATETIME,\n" +
            "    FirstImported              DATETIME,\n" +
            "    TourName                   NCHAR (255),\n" +
            "    GPXFilename_Id            BIGINT,\n" +
            "    ListingCheckSum           INT            DEFAULT 0,\n" +
            "    state                      NVARCHAR (50),\n" +
            "    country                   NVARCHAR (50),\n" +
            "    ApiStatus                 SMALLINT       DEFAULT 0\n" +
            "    \n" +
            "   );";

    public final String COPY_DATA_FROM_V2_TO_V3 = "INSERT OR IGNORE INTO CacheCoreInfo (Id, Latitude, Longitude, Size, Difficulty, Terrain, Type, Rating, NumTravelbugs, GcCode, Name, PlacedBy, Owner, GcId, Vote, BooleanStore)\n" +
            "SELECT Id, Latitude, Longitude, Size, Difficulty, Terrain, Type, Rating, NumTravelbugs, GcCode, Name, PlacedBy, Owner, GcId, Vote, \n" +
            "(IfNull(ListingChanged,0)*512 + IfNull(HasUserData,0)*256 + IfNull(Found,0)*32 + IfNull(Favorit,0)*16 + IfNull(Available,0)*8 + IfNull(Archived,0)*4 + IfNull(CorrectedCoordinates,0)*2 + CASE WHEN LENGTH(TRIM(IfNull(Hint,'  '))) > 1 THEN 1 ELSE 0 END)\n" +
            "FROM Caches;";

    public final String COPY_ATTRIBUTES_FROM_V2_TO_V3 = "INSERT OR IGNORE INTO Attributes (" +
            "Id, AttributesPositive, AttributesNegative, AttributesPositiveHigh, AttributesNegativeHigh)" +
            "SELECT " +
            "Id, AttributesPositive, AttributesNegative, AttributesPositiveHigh, AttributesNegativeHigh\n" +
            "FROM Caches;";

    public final String COPY_TEXT_FROM_V2_TO_V3 = "INSERT OR IGNORE INTO CacheText (" +
            "Id, Url, Hint, Description, Notes, Solver, ShortDescription)" +
            "SELECT " +
            "Id, Url, Hint, Description, Notes, Solver, ShortDescription\n" +
            "FROM Caches;";

    public final String COPY_CACHEINFO_FROM_V2_TO_V3 = "INSERT OR IGNORE INTO CacheInfo (" +
            "Id, DateHidden, FirstImported, TourName, GPXFilename_Id, ListingCheckSum, state, country, ApiStatus)" +
            "SELECT " +
            "Id, DateHidden, FirstImported, TourName, GPXFilename_Id, ListingCheckSum, state, country, ApiStatus\n" +
            "FROM Caches;";

    public final String WAYPOINTS = "CREATE TABLE Waypoints (\n" +
            "    CacheId      BIGINT,\n" +
            "    GcCode       NVARCHAR (12) \n" +
            "                              PRIMARY KEY\n" +
            "                              UNIQUE,\n" +
            "    Latitude     FLOAT,\n" +
            "    Longitude    FLOAT,\n" +
            "    Type         SMALLINT,\n" +
            "    IsStart      BOOLEAN       DEFAULT 'false',\n" +
            "    SyncExclude  BIT,\n" +
            "    UserWaypoint BIT,\n" +
            "    Title        NTEXT\n" +
            ");";

    public final String WAYPOINTS_TEXT = "CREATE TABLE WaypointsText (\n" +
            "    GcCode       NVARCHAR (12) \n" +
            "                              PRIMARY KEY\n" +
            "                              UNIQUE,\n" +
            "    Description  NTEXT,\n" +
            "    Clue         NTEXT\n" +
            ");";


    public final String CONFIG_TABLE = "CREATE TABLE Config (\n" +
            "    [Key]      NVARCHAR (30)  NOT NULL\n" +
            "                              PRIMARY KEY\n" +
            "                              UNIQUE,\n" +
            "    Value      NVARCHAR (255),\n" +
            "    LongString NTEXT,\n" +
            "    desired    NTEXT,\n" +
            "    blob    BLOB\n" +
            ");";

    public final String CATEGORY_TABLE = "CREATE TABLE Category (\n" +
            "    Id          INTEGER        NOT NULL\n" +
            "                               PRIMARY KEY AUTOINCREMENT,\n" +
            "    GpxFilename NVARCHAR (255),\n" +
            "    Pinned      BIT            DEFAULT 0,\n" +
            "    CacheCount  INT\n" +
            ");";

    public final String GPX_FILE_NAMES = "CREATE TABLE GPXFilenames (\n" +
            "    Id          INTEGER        NOT NULL\n" +
            "                               PRIMARY KEY AUTOINCREMENT,\n" +
            "    GPXFilename NVARCHAR (255),\n" +
            "    Imported    DATETIME,\n" +
            "    name        NVARCHAR (255),\n" +
            "    CacheCount  INT,\n" +
            "    CategoryId  BIGINT\n" +
            ");";

    public final String IMAGES = "CREATE TABLE Images (\n" +
            "    Id           INTEGER        NOT NULL\n" +
            "                                PRIMARY KEY AUTOINCREMENT,\n" +
            "    CacheId      BIGINT,\n" +
            "    GcCode       NVARCHAR (12),\n" +
            "    Description  NTEXT,\n" +
            "    name         NVARCHAR (255),\n" +
            "    ImageUrl     NVARCHAR (255),\n" +
            "    IsCacheImage BIT\n" +
            ");";

    public final String LOGS = "CREATE TABLE Logs (\n" +
            "    Id        BIGINT         NOT NULL\n" +
            "                             PRIMARY KEY,\n" +
            "    CacheId   BIGINT,\n" +
            "    Timestamp DATETIME,\n" +
            "    Finder    NVARCHAR (128),\n" +
            "    Type      SMALLINT,\n" +
            "    Comment   NTEXT\n" +
            ");";

    public final String POCKET_QUERIES = "CREATE TABLE PocketQueries (\n" +
            "    Id               INTEGER        NOT NULL\n" +
            "                                    PRIMARY KEY AUTOINCREMENT,\n" +
            "    PQName           NVARCHAR (255),\n" +
            "    CreationTimeOfPQ DATETIME\n" +
            ");";

    public final String REPLICATION = "CREATE TABLE Replication (\n" +
            "    Id              INTEGER       NOT NULL\n" +
            "                                  PRIMARY KEY AUTOINCREMENT,\n" +
            "    ChangeType      INT           NOT NULL,\n" +
            "    CacheId         BIGINT        NOT NULL,\n" +
            "    WpGcCode        NVARCHAR (12),\n" +
            "    SolverCheckSum  INT,\n" +
            "    NotesCheckSum   INT,\n" +
            "    WpCoordCheckSum INT\n" +
            ");\n";

    public final String TB_LOGS = "CREATE TABLE TbLogs (\n" +
            "    Id           INTEGER        NOT NULL\n" +
            "                                PRIMARY KEY AUTOINCREMENT,\n" +
            "    TrackableId  INTEGER        NOT NULL,\n" +
            "    CacheID      BIGINT,\n" +
            "    GcCode       NVARCHAR (12),\n" +
            "    LogIsEncoded BIT            DEFAULT 0,\n" +
            "    LogText      NTEXT,\n" +
            "    LogTypeId    BIGINT,\n" +
            "    LoggedByName NVARCHAR (255),\n" +
            "    Visited      DATETIME\n" +
            ");";

    public final String TRACKABLE = "CREATE TABLE Trackable (\n" +
            "    Id               INTEGER        NOT NULL\n" +
            "                                    PRIMARY KEY AUTOINCREMENT,\n" +
            "    Archived         BIT,\n" +
            "    GcCode           NVARCHAR (12),\n" +
            "    CacheId          BIGINT,\n" +
            "    CurrentGoal      NTEXT,\n" +
            "    CurrentOwnerName NVARCHAR (255),\n" +
            "    DateCreated      DATETIME,\n" +
            "    Description      NTEXT,\n" +
            "    IconUrl          NVARCHAR (255),\n" +
            "    ImageUrl         NVARCHAR (255),\n" +
            "    path             NVARCHAR (255),\n" +
            "    OwnerName        NVARCHAR (255),\n" +
            "    Url              NVARCHAR (255) \n" +
            ");";


    public final String COPY_WAYPOINTS_FROM_V2_TO_V3 = "INSERT OR IGNORE INTO Waypoints (" +
            "CacheId, GcCode, Latitude, Longitude, Type, IsStart, SyncExclude, UserWaypoint, Title)" +
            "SELECT " +
            "CacheId, GcCode, Latitude, Longitude, Type, IsStart, SyncExclude, UserWaypoint, Title\n" +
            "FROM Waypoint;";

    public final String COPY_WAYPOINTS_TEXT_FROM_V2_TO_V3 = "INSERT OR IGNORE INTO WaypointsText (" +
            "GcCode, Description, Clue)" +
            "SELECT " +
            "GcCode, Description, Clue\n" +
            "FROM Waypoint;";

    public String getEmptyNewDB() {
        StringBuilder sb = new StringBuilder();

        sb.append("BEGIN TRANSACTION;").append("\n");

        sb.append(CONFIG_TABLE).append("\n");
        sb.append(CATEGORY_TABLE).append("\n");
        sb.append(GPX_FILE_NAMES).append("\n");
        sb.append(IMAGES).append("\n");
        sb.append(LOGS).append("\n");
        sb.append(POCKET_QUERIES).append("\n");
        sb.append(REPLICATION).append("\n");
        sb.append(TB_LOGS).append("\n");
        sb.append(TRACKABLE).append("\n");
        sb.append(CACHE_CORE_INFO).append("\n");
        sb.append(ATTRIBUTES).append("\n");
        sb.append(TEXT).append("\n");
        sb.append(CACHE_INFO).append("\n");
        sb.append(WAYPOINTS).append("\n");
        sb.append(WAYPOINTS_TEXT).append("\n");

        sb.append("END TRANSACTION;").append("\n");

        return sb.toString();
    }
}
