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

import de.longri.cachebox3.types.AbstractCache;

/**
 * Created by Longri on 20.10.2017.
 */
public abstract class AbstractCacheDAO {
    public abstract void writeToDatabase(AbstractCache abstractCache);

    public abstract void writeToDatabaseFound(AbstractCache abstractCache);

    public abstract boolean updateDatabase(AbstractCache abstractCache);

    public abstract AbstractCache getFromDbByCacheId(long CacheID);

    /**
     * hier wird nur die Status Abfrage zurück geschrieben und gegebenen Falls die Replication Informationen geschrieben.
     *
     * @param writeTmp
     */
    public abstract boolean updateDatabaseCacheState(AbstractCache writeTmp);
}
