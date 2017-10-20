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
package de.longri.cachebox3.sqlite.dao;

import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Waypoint;
import de.longri.cachebox3.types.Waypoint3;

/**
 * Created by Longri on 20.10.2017.
 */
public class Waypoint3DAO {

    private final String GET_ALL_WAYPOINTS = "SELECT * FROM Waypoints";

    public Array<Waypoint3> getAllWayPoints(Database database) {
        Array<Waypoint3> waypoints = new Array<>();
        SQLiteGdxDatabaseCursor cursor = database.rawQuery(GET_ALL_WAYPOINTS, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Waypoint3 wp = new Waypoint3(cursor);
            waypoints.add(wp);
            cursor.moveToNext();
        }
        cursor.close();
        return waypoints;
    }

}
