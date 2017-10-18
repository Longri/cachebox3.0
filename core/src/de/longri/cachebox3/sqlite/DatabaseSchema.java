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

/**
 * Holds the SQL create strings for create a new Database with actual schema
 * <p>
 * Created by Longri on 18.10.2017.
 */
public class DatabaseSchema {
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
            " FavPoints        INT\n" +
            " \n" +
            " );       ";

    public final String ATTRIBUTES = "CREATE TABLE Attributes (\n" +
            "   Id                         BIGINT,\n" +
            "  AttributesPositive        BIGINT,\n" +
            "  AttributesNegative       BIGINT,\n" +
            "  AttributesPositiveHigh    BIGINT         DEFAULT 0,\n" +
            "  AttributesNegativeHigh    BIGINT         DEFAULT 0\n" +
            " );";

    public final String TEXT = "CREATE TABLE Text (\n" +
            "   Id                         BIGINT,\n" +
            "   Url                        NVARCHAR (255),\n" +
            "   Hint                       NTEXT,\n" +
            "   Description                NTEXT,\n" +
            "   Notes                      NTEXT,\n" +
            "   Solver                     NTEXT,\n" +
            "   ShortDescription          NTEXT\n" +
            "  );";

    public final String CACHE_INFO = "CREATE TABLE CacheInfo (\n" +
            "    Id                         BIGINT,\n" +
            "    DateHidden                DATETIME,\n" +
            "    FirstImported              DATETIME,\n" +
            "    Vote                       SMALLINT,\n" +
            "    TourName                   NCHAR (255),\n" +
            "    GPXFilename_Id            BIGINT,\n" +
            "    ListingCheckSum           INT            DEFAULT 0,\n" +
            "    state                      NVARCHAR (50),\n" +
            "    country                   NVARCHAR (50),\n" +
            "    ApiStatus                 SMALLINT       DEFAULT 0\n" +
            "    \n" +
            "   );";

    public final String COPY_DATA_FROM_V2_TO_V3 = "INSERT INTO CacheCoreInfo (" +
            "Id, Latitude, Longitude, Size, Difficulty, Terrain, Type, Rating, NumTravelbugs, GcCode, Name, PlacedBy, Owner, GcId)\n" +
            "SELECT " +
            "Id, Latitude, Longitude, Size, Difficulty, Terrain, Type, Rating, NumTravelbugs, GcCode, Name, PlacedBy, Owner, GcId\n" +
            "FROM Caches;";

    public final String COPY_ATTRIBUTES_FROM_V2_TO_V3 = "INSERT INTO Attributes (" +
            "Id, AttributesPositive, AttributesNegative, AttributesPositiveHigh, AttributesNegativeHigh)" +
            "SELECT " +
            "Id, AttributesPositive, AttributesNegative, AttributesPositiveHigh, AttributesNegativeHigh\n" +
            "FROM Caches;" ;

    public final String COPY_TEXT_FROM_V2_TO_V3 = "INSERT INTO Text (" +
            "Id, Url, Hint, Description, Notes, Solver, ShortDescription)" +
            "SELECT " +
            "Id, Url, Hint, Description, Notes, Solver, ShortDescription\n" +
            "FROM Caches;" ;

    public final String COPY_CACHEINFO_FROM_V2_TO_V3 = "INSERT INTO CacheInfo (" +
            "Id, DateHidden, FirstImported, Vote, TourName, GPXFilename_Id, ListingCheckSum, state, country, ApiStatus)" +
            "SELECT " +
            "Id, DateHidden, FirstImported, Vote, TourName, GPXFilename_Id, ListingCheckSum, state, country, ApiStatus\n" +
            "FROM Caches;" ;
}
