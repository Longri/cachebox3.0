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

import java.text.ParseException;
import java.util.Date;

public class LogEntry {

    /**
     * Benutzername des Loggers
     */
    public String finder = "";

    /**
     * Logtyp, z.B. "Found it!"
     */
    public LogTypes geoCacheLogType = LogTypes.unknown;


    /**
     * Geschriebener Text
     */
    public String logText = "";

    /**
     * Zeitpunkt
     */
    public Date logDate = new Date(0);

    /**
     * Id des Caches
     */
    public long cacheId = -1;

    /**
     * Id des Logs
     */
    public long logId = -1;

    public LogEntry(GdxSqliteCursor cursor) {
        this.logId = cursor.getLong(0);
        this.cacheId = cursor.getLong(1);

        try {
            this.logDate = Database.cbDbFormat.parse(cursor.getString(2));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.finder = cursor.getString(3);
        this.geoCacheLogType = LogTypes.values()[cursor.getInt(4)];
        this.logText = cursor.getString(5);
    }

    public LogEntry() {
    }

    public void clear() {
        finder = "";
        geoCacheLogType = null;
        logText = "";
        logDate = new Date(0);
        cacheId = -1;
        logId = -1;
    }

    public void dispose() {
        finder = null;
        geoCacheLogType = null;
        logText = null;
        logDate = null;
    }

    @Override
    public String toString() {
        return "ID:" + logId;
    }


    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        // return true, if the id are equals
        return other instanceof LogEntry && ((LogEntry) other).logId == this.logId;
    }

    public LogEntry copy() {
        LogEntry ret = new LogEntry();
        ret.finder = finder;
        ret.geoCacheLogType = geoCacheLogType;
        ret.logText = logText;
        ret.logDate = logDate;
        ret.cacheId = cacheId;
        ret.logId = logId;
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
