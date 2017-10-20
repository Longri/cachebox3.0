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
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache3;
import de.longri.cachebox3.types.CacheList;

/**
 * Created by Longri on 19.10.2017.
 */
public class CacheList3DAO {

    private final String READ_ALL = "SELECT * from CacheCoreInfo";

    public CacheList readCacheList(Database database) {
        CacheList caches = new CacheList();
        SQLiteGdxDatabaseCursor cursor = database.rawQuery(READ_ALL, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            caches.add(new Cache3(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        //read waypoints


        return caches;
    }


}
