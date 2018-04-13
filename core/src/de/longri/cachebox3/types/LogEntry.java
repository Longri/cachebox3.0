/*
 * Copyright (C) 2014-2016 team-cachebox.de
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


import de.longri.cachebox3.sqlite.Database;
import de.longri.gdx.sqlite.GdxSqliteCursor;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEntry {

    /**
     * Benutzername des Loggers
     */
    public String Finder = "";

    /**
     * Logtyp, z.B. "Found it!"
     */
    public LogTypes Type = LogTypes.unknown;


    /**
     * Geschriebener Text
     */
    public String Comment = "";

    /**
     * Zeitpunkt
     */
    public Date Timestamp = new Date(0);

    /**
     * Id des Caches
     */
    public long CacheId = -1;

    /**
     * Id des Logs
     */
    public long Id = -1;

    public LogEntry(GdxSqliteCursor cursor) {
        this.Id = cursor.getLong(0);
        this.CacheId = cursor.getLong(1);

        try {
            this.Timestamp = Database.cbDbFormat.parse(cursor.getString(2));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.Finder = cursor.getString(3);
        this.Type = LogTypes.values()[cursor.getInt(4)];
        this.Comment = cursor.getString(5);
    }

    public LogEntry() {
    }

    public void clear() {
        Finder = "";
        Type = null;
        Comment = "";
        Timestamp = new Date(0);
        CacheId = -1;
        Id = -1;
    }

    public void dispose() {
        Finder = null;
        Type = null;
        Comment = null;
        Timestamp = null;
    }

    @Override
    public String toString() {
        return "ID:" + Id;
    }


    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        // return true, if the id are equals
        return other instanceof LogEntry && ((LogEntry) other).Id == this.Id;
    }

    public LogEntry copy() {
        LogEntry ret = new LogEntry();
        ret.Finder = Finder;
        ret.Type = Type;
        ret.Comment = Comment;
        ret.Timestamp = Timestamp;
        ret.CacheId = CacheId;
        ret.Id = Id;
        return ret;
    }

    public static String filterBBCode(String string) {
        if (string == null) return null;
        int lIndex;
        while ((lIndex = string.indexOf('[')) >= 0) {
            int rIndex = string.indexOf(']', lIndex);
            if (rIndex == -1)
                break;
            string = string.substring(0, lIndex) + string.substring(rIndex + 1);
        }
        return string;
    }
}
