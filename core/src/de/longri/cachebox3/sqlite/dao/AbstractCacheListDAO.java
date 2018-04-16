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

import com.badlogic.gdx.utils.LongArray;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.CacheList;

/**
 * Created by Longri on 20.10.2017.
 */
public abstract class AbstractCacheListDAO {
    public abstract void readCacheList(Database database, CacheList cacheList, String where, boolean fullDetails, boolean loadAllWaypoints);

    public abstract AbstractCache reloadCache(Database database, CacheList cacheList, AbstractCache selectedCache);

    public abstract int getFilteredCacheCount(Database database, String statement);

    public abstract void writeToDB(Database database, CacheList cacheList);

}
