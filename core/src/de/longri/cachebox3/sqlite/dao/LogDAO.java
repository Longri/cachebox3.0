/*
 * Copyright (C) 2014 team-cachebox.de
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
import de.longri.cachebox3.types.LogEntry;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Iterator;

public class LogDAO {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(ImageDAO.class);


    public synchronized void WriteToDatabase(LogEntry logEntry) {
        Database.Parameters args = new Database.Parameters();
        args.put("Id", logEntry.Id);
        args.put("Finder", logEntry.Finder);
        args.put("Type", logEntry.Type.ordinal());
        args.put("Comment", logEntry.Comment);
        String stimestamp = Database.cbDbFormat.format(logEntry.Timestamp);
        args.put("Timestamp", stimestamp);
        args.put("CacheId", logEntry.CacheId);
        try {
            Database.Data.insertWithConflictReplace("Logs", args);
        } catch (Exception exc) {
            log.error("Write Log", exc);
        }

    }

    public synchronized void WriteImports(Iterator<LogEntry> logIterator) {
        while (logIterator.hasNext()) {
            LogEntry log = logIterator.next();
            try {
                WriteToDatabase(log);
            } catch (Exception e) {

                // Statt hier den Fehler abzufangen, sollte die LogTabelle
                // Indexiert werden
                // und nur die noch nicht vorhandenen Logs geschrieben werden.

                e.printStackTrace();
            }

        }

    }

    /**
     * Delete all Logs without exist Cache
     */
    public synchronized void ClearOrphanedLogs() {
        String SQL = "DELETE  FROM  Logs WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Logs.CacheId = c.Id)";
        Database.Data.execSQL(SQL);
    }

    /**
     * Delete all Logs for Cache
     */
    public synchronized void deleteLogs(long cacheId) {
        String SQL = "DELETE  FROM  Logs WHERE Logs.CacheId = " + cacheId;
        Database.Data.execSQL(SQL);
    }

    public void writeToDB(Database database, Array<LogEntry> logList) {
        //create statements
        GdxSqlitePreparedStatement REPLACE_LOGS = database.myDB.prepare("INSERT OR REPLACE INTO Logs VALUES(?,?,?,?,?,?) ;");
        for (LogEntry entry : logList) {
            try {
                REPLACE_LOGS.bind(
                        entry.Id,
                        entry.CacheId,
                        Database.cbDbFormat.format(entry.Timestamp == null ? new Date() : entry.Timestamp),
                        entry.Finder,
                        entry.Type,
                        entry.Comment
                ).commit();
            } catch (Exception e) {
                log.error("Can't write Log-Entry with values: \n" +
                        "ID:{}\n" +
                        "CacheID:{}\n" +
                        "Date:{}\n" +
                        "Finder:{}\n" +
                        "Type:{}\n" +
                        "Comment:{}\n\n\n",
                        entry.Id,entry.CacheId,
                        Database.cbDbFormat.format(entry.Timestamp == null ? new Date() : entry.Timestamp),
                        entry.Finder,
                        entry.Type,
                        entry.Comment
                        );
            } finally {
                REPLACE_LOGS.reset();
            }
        }
    }

    public Array<LogEntry> getLogs(Database database, Integer cacheId) {
        Array<LogEntry> logList = new Array<>();

        String sql;
        if (cacheId == null) {
            sql = "SELECT * FROM Logs";
        } else {
            sql = "SELECT * FROM Logs WHERE CacheId=" + Integer.toString(cacheId) + "'";
        }

        GdxSqliteCursor cursor = database.myDB.rawQuery(sql);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            logList.add(new LogEntry(cursor));
            cursor.moveToNext();
        }
        return logList;
    }
}
