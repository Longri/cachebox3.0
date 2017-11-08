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
package de.longri.cachebox3.types;

import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;

/**
 * Created by Longri on 19.10.2017.
 */
public abstract class AbstractWaypoint extends Coordinate {
    public AbstractWaypoint(double latitude, double longitude) {
        super(latitude, longitude);
    }

    public abstract float distance();

    public abstract CharSequence getGcCode();

    public abstract void setGcCode(String gcCode);

    public abstract CharSequence getTitle();

    public abstract void setTitle(String title);

    public abstract CharSequence getDescription(Database database);

    public abstract void setDescription(String description);

    public abstract CharSequence getClue(Database cb3Database);

    public abstract void setClue(String clue);

    public abstract void setCheckSum(int i);

    public abstract int getCheckSum();

    public abstract long getCacheId();

    public abstract void setCacheId(long cacheId);

    public abstract CacheTypes getType();

    public abstract void setType(CacheTypes type);

    public abstract boolean isUserWaypoint();

    public abstract void setUserWaypoint(boolean userWaypoint);

    public abstract boolean isSyncExcluded();

    public abstract void setSyncExcluded(boolean syncExcluded);

    public abstract boolean isStart();

    public abstract void setStart(boolean start);

    public abstract boolean isMutable();

    public abstract AbstractWaypoint getMutable(Database database);

    public abstract void setLatLon(double latitude, double longitude);


    public abstract void setLatitude(double latitude);

    public abstract void setLongitude(double longitude);

    public abstract void dispose();
}