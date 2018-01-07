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

public class Replication {

    public static void FoundChanged(long CacheId, boolean found) {
        if (Database.Data.MasterDatabaseId > 0) {
            ChangeType changeType;
            if (found)
                changeType = ChangeType.Found;
            else
                changeType = ChangeType.NotFound;

            Database.Data.delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=6 or CacheId=" + String.valueOf(CacheId) + " and ChangeType=7");

            Database.Parameters val = new Database.Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            Database.Data.insert("Replication", val);
        }

    }

    public static void SolverChanged(long CacheId, int oldSolverCheckSum, int newSolverCheckSum) {
        if (Database.Data.MasterDatabaseId > 0) {
            if (oldSolverCheckSum != newSolverCheckSum) {
                if (oldSolverCheckSum != newSolverCheckSum) {
                    // When the Solver text is changed then this must be added
                    // into the Replication table
                    // When for this cache already a SolverChanged entry exists
                    // -> read the SolverCheckSum from this entry -> if the
                    // stored CheckSum == newSolverCheckSum
                    // -> the new Solver Text is the same than the original ->
                    // remove this entry
                    // When no SolverText entry for this Cache exists -> create
                    // a new entry and store the original CheckSum of the Solver
                    // Text
                    int dbCheckSum = -1;
                    GdxSqliteCursor c = Database.Data.rawQuery("select SolverCheckSum from Replication where CacheId=? and ChangeType=?", new String[]{String.valueOf(CacheId), String.valueOf(ChangeType.SolverText.ordinal())});
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        dbCheckSum = c.getInt(0);
                        break;
                    }
                    ;
                    if (dbCheckSum < 0) {
                        // a Change for the Solvertext for this Cache must be
                        // added to the Replication Table
                        Database.Parameters val = new Database.Parameters();
                        val.put("CacheId", CacheId);
                        val.put("ChangeType", ChangeType.SolverText.ordinal());
                        val.put("SolverCheckSum", oldSolverCheckSum);
                        Database.Data.insert("Replication", val);
                    } else {
                        // compare the stored CheckSum with the new
                        if (dbCheckSum == newSolverCheckSum) {
                            // the dbCheckSum is the same than the newCheckSum
                            // -> Solver text is the same than the original
                            // -> delete this entry!
                            Database.Data.delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=" + String.valueOf(ChangeType.SolverText.ordinal()));
                        }
                    }
                }
            }
        }
    }

    public static void NoteChanged(long CacheId, int oldNoteCheckSum, int newNoteCheckSum) {
        if (Database.Data.MasterDatabaseId > 0) {
            if (oldNoteCheckSum != newNoteCheckSum) {
                if (oldNoteCheckSum != newNoteCheckSum) {
                    // When the Note text is changed then this must be added
                    // into the Replication table
                    // When for this cache already a NoteChanged entry exists
                    // -> read the NoteCheckSum from this entry -> if the stored
                    // CheckSum == newNoteCheckSum
                    // -> the new Note Text is the same than the original ->
                    // remove this entry
                    // When no Note Text entry for this Cache exists -> create a
                    // new entry and store the original CheckSum of the Note
                    // Text
                    int dbCheckSum = -1;
                    GdxSqliteCursor c = Database.Data.rawQuery("select NotesCheckSum from Replication where CacheId=? and ChangeType=?", new String[]{String.valueOf(CacheId), String.valueOf(ChangeType.NotesText.ordinal())});
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        dbCheckSum = c.getInt(0);
                        break;
                    }
                    ;
                    if (dbCheckSum < 0) {
                        // a Change for the Notestext for this Cache must be
                        // added to the Replication Table
                        Database.Parameters val = new Database.Parameters();
                        val.put("CacheId", CacheId);
                        val.put("ChangeType", ChangeType.NotesText.ordinal());
                        val.put("SolverCheckSum", oldNoteCheckSum);
                        Database.Data.insert("Replication", val);
                    } else {
                        // compare the stored CheckSum with the new
                        if (dbCheckSum == newNoteCheckSum) {
                            // the dbCheckSum is the same than the newCheckSum
                            // -> Notes text is the same than the original
                            // -> delete this entry!
                            Database.Data.delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=" + String.valueOf(ChangeType.NotesText.ordinal()));
                        }
                    }
                }
            }
        }
    }

    public static void WaypointChanged(long CacheId, int oldCheckSum, int newCheckSum, String WpGcCode) {
        Changed(CacheId, oldCheckSum, newCheckSum, "WpCoordCheckSum", ChangeType.WaypointChanged, WpGcCode);
    }

    public static void WaypointNew(long CacheId, int oldCheckSum, int newCheckSum, String WpGcCode) {
        Changed(CacheId, oldCheckSum, newCheckSum, "WpCoordCheckSum", ChangeType.NewWaypoint, WpGcCode);
    }

    public static void WaypointDelete(long CacheId, int oldCheckSum, int newCheckSum, String WpGcCode) {
        Changed(CacheId, oldCheckSum, newCheckSum, "WpCoordCheckSum", ChangeType.DeleteWaypoint, WpGcCode);
    }

    private static void Changed(long CacheId, int oldCheckSum, int newCheckSum, String checkSumType, ChangeType changeType, String WpGcCode) {
        if (Database.Data.MasterDatabaseId > 0) {
            if (oldCheckSum != newCheckSum) {
                // When the item is changed then this must be added into the
                // Replication table
                // When for this cache already a Changed entry exists
                // -> read the CheckSum from this entry -> if the stored
                // CheckSum == newCheckSum
                // -> the new item is the same than the original -> remove this
                // entry
                // When no entry for this Cache exists -> create a new entry and
                // store the original CheckSum of the item
                int dbCheckSum = -1;
                GdxSqliteCursor c = Database.Data.rawQuery("select " + checkSumType + " from Replication where CacheId=? and ChangeType=? and WpGcCode=?", new String[]{String.valueOf(CacheId), String.valueOf(changeType.ordinal()), WpGcCode});
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    dbCheckSum = c.getInt(0);
                    break;
                }
                ;
                if (dbCheckSum < 0) {
                    // a Change for the WP for this Cache must be added to the
                    // Replication Table
                    Database.Parameters val = new Database.Parameters();
                    val.put("CacheId", CacheId);
                    val.put("ChangeType", changeType.ordinal());
                    val.put("WpCoordCheckSum", oldCheckSum);
                    val.put("WpGcCode", WpGcCode);
                    Database.Data.insert("Replication", val);
                } else {
                    // compare the stored CheckSum with the new
                    if (dbCheckSum == newCheckSum) {
                        // the dbCheckSum is the same than the newCheckSum ->
                        // value is the same than the original
                        // -> delete this entry!
                        Database.Data.delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=" + String.valueOf(changeType));
                    }
                }
            }
        }
    }

    public static void AvailableChanged(long CacheId, boolean available) {
        if (Database.Data.MasterDatabaseId > 0) {
            ChangeType changeType;
            if (available)
                changeType = ChangeType.Available;
            else
                changeType = ChangeType.NotAvailable;

            Database.Data.delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=10 or CacheId=" + String.valueOf(CacheId) + " and ChangeType=11");

            Database.Parameters val = new Database.Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            Database.Data.insert("Replication", val);
        }

    }

    public static void ArchivedChanged(long CacheId, boolean archived) {
        if (Database.Data.MasterDatabaseId > 0) {
            ChangeType changeType;
            if (archived)
                changeType = ChangeType.Archived;
            else
                changeType = ChangeType.NotArchived;

            Database.Data.delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=8 or CacheId=" + String.valueOf(CacheId) + " and ChangeType=9");

            Database.Parameters val = new Database.Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            Database.Data.insert("Replication", val);
        }

    }

    public static void NumTravelbugsChanged(long CacheId, int numTravelbugs) {
        if (Database.Data.MasterDatabaseId > 0) {
            ChangeType changeType;

            Database.Data.delete("Replication", "CacheId=" + String.valueOf(CacheId) + " and ChangeType=12");

            changeType = ChangeType.NumTravelbugs;
            Database.Parameters val = new Database.Parameters();
            val.put("CacheId", CacheId);
            val.put("ChangeType", changeType.ordinal());
            val.put("SolverCheckSum", numTravelbugs);
            Database.Data.insert("Replication", val);
        }

    }

}
