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
public class Cache3DAO extends AbstractCacheDAO {
    @Override
    public void writeToDatabase(AbstractCache abstractCache) {

    }

    @Override
    public void writeToDatabaseFound(AbstractCache abstractCache) {

    }

    @Override
    public boolean updateDatabase(AbstractCache abstractCache) {
        return false;
    }

    @Override
    public AbstractCache getFromDbByCacheId(long CacheID) {
        return null;
    }

    @Override
    public boolean updateDatabaseCacheState(AbstractCache writeTmp) {
        return false;
    }
}
