/*
 * Copyright (C) 2018 team-cachebox.de
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
import de.longri.cachebox3.types.Trackable;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;

import java.util.Date;

/**
 * Created by Longri on 22.01.2018.
 */
public class TrackableDao {

    public void writeToDB(Database database, Array<Trackable> tbList) {
        //create statements
        GdxSqlitePreparedStatement REPLACE_LOGS = database.myDB.prepare("REPLACE INTO Trackable VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) ;");

        database.myDB.beginTransaction();
        try {
            for (Trackable entry : tbList) {
                REPLACE_LOGS.bind(
                        entry.getId(),
                        entry.getArchived(),
                        entry.getTBCode(),
                        entry.CacheId(),
                        entry.getCurrentGoal(),
                        entry.getCurrentOwnerName(),
                        Database.cbDbFormat.format(entry.getDateCreated() == null ? new Date() : entry.getDateCreated()),
                        entry.getDescription(),
                        entry.getIconUrl(),
                        entry.getImageUrl(),
                        entry.getName(),
                        entry.getOwnerName(),
                        entry.getUrl()
                ).commit().reset();
            }
        } finally {
            database.myDB.endTransaction();
        }
    }

    public Array<Trackable> getTBs(Database database, Integer cacheId) {
        Array<Trackable> tbList = new Array<>();

        String sql;
        if (cacheId == null) {
            sql = "SELECT * FROM Trackable";
        } else {
            sql = "SELECT * FROM Trackable WHERE CacheId=" + Integer.toString(cacheId) + "'";
        }

        GdxSqliteCursor cursor = database.myDB.rawQuery(sql);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            tbList.add(new Trackable(cursor));
            cursor.moveToNext();
        }
        return tbList;
    }
}
