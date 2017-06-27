/*
 * Copyright (C) 2016 team-cachebox.de
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

/**
 * Static Access to all used SQL-Command strings<br>
 * <br>
 * <p>
 * For iOS it is important that all table row names enclosed with `` !
 * <p>
 * <p>
 * Created by Longri on 30.08.16.
 */
public class SQL {

    static final String SQL_BY_ID = "FROM `Caches` `c` WHERE id = ?";
    static final String SQL_BY_GC_CODE = "FROM `Caches` `c` WHERE GCCode = ?";
    static final String SQL_DETAILS = "`PlacedBy`, `DateHidden`, `Url`, `TourName`, `GpxFilename_ID`, `ApiStatus`, `AttributesPositive`, `AttributesPositiveHigh`, `AttributesNegative`, `AttributesNegativeHigh`, `Hint` ";
    static final String SQL_GET_DETAIL_WITH_DESCRIPTION = "Description, Solver, Notes, ShortDescription ";
    static final String SQL_GET_DETAIL_FROM_ID = "SELECT " + SQL_DETAILS + SQL_BY_ID;
    static final String SQL_EXIST_CACHE = "SELECT `Id` FROM `Caches` WHERE Id = ?";
    static final String SQL_GET_CACHE = "SELECT `Id`, `GcCode`, `Latitude`, `Longitude`, `name`, `Size`, `Difficulty`, `Terrain`, `Archived`, `Available`, `Found`, `Type`, `Owner`, `NumTravelbugs`, `GcId`, `Rating`, `Favorit`, `HasUserData`, `ListingChanged`, `CorrectedCoordinates` ";
    static final String SQL_ALL_CACHE_IDS = "SELECT `Id` FROM `Caches`";

}
