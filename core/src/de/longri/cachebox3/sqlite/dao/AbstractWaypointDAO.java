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

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;

/**
 * Created by Longri on 20.10.2017.
 */
public abstract class AbstractWaypointDAO {

    public abstract boolean updateDatabase(Database database, AbstractWaypoint WP, boolean fireChangedEvent);

    // Hier wird überprüft, ob für diesen Cache ein Start-Waypoint existiert und dieser in diesem Fall zurückgesetzt
    // Damit kann bei der Definition eines neuen Start-Waypoints vorher der alte entfernt werden damit sichergestellt ist dass ein Cache nur
    // 1 Start-Waypoint hat
    public abstract void resetStartWaypoint(AbstractCache abstractCache, AbstractWaypoint except);

    /**
     * Returns a WaypointList from reading DB!
     *
     * @param CacheID ID of Cache
     * @param Full    Waypoints as FullWaypoints (true) or Waypoint (false)
     * @return
     */
    public abstract Array<AbstractWaypoint> getWaypointsFromCacheID(Database database, Long CacheID, boolean Full);

    public abstract void delete(Database database, AbstractWaypoint waypoint, boolean fireChangedEvent);

    public abstract void writeToDatabase(Database database, AbstractWaypoint wp, boolean fireChangedEvent);
}
