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


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Longri on 31.08.2017
 */
public class FieldNoteList extends Array<FieldNoteEntry> {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(FieldNoteList.class);

    private static final long serialVersionUID = 1L;

    public enum LoadingType {
        LOAD_ALL, LOAD_NEW, LOAD_MORE, LOAD_NEW_LAST_LENGTH
    }

    private boolean croppedList = false;
    private int actCroppedLength = -1;

    public FieldNoteList() {

    }

    public boolean isCropped() {
        return croppedList;
    }

    public void loadFieldNotes(String where, LoadingType loadingType) {
        synchronized (this) {
            loadFieldNotes(where, "", loadingType);
        }
    }

    public void loadFieldNotes(String where, String order, LoadingType loadingType) {
        synchronized (this) {
            // List clear?
            if (loadingType == LoadingType.LOAD_ALL || loadingType == LoadingType.LOAD_NEW || loadingType == LoadingType.LOAD_NEW_LAST_LENGTH) {
                this.clear();
            }

            String sql = "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog from FieldNotes";
            if (!where.equals("")) {
                sql += " where " + where;
            }
            if (order == "") {
                sql += " order by FoundNumber DESC, Timestamp DESC";
            } else {
                sql += " order by " + order;
            }

            // SQLite Limit ?
            boolean maybeCropped = !Config.FieldNotesLoadAll.getValue() && loadingType != LoadingType.LOAD_ALL;

            if (maybeCropped) {
                switch (loadingType) {
                    case LOAD_ALL:
                        // do nothing
                        break;
                    case LOAD_NEW:
                        actCroppedLength = Config.FieldNotesLoadLength.getValue();
                        sql += " LIMIT " + String.valueOf(actCroppedLength + 1);
                        break;
                    case LOAD_NEW_LAST_LENGTH:
                        if (actCroppedLength == -1)
                            actCroppedLength = Config.FieldNotesLoadLength.getValue();
                        sql += " LIMIT " + String.valueOf(actCroppedLength + 1);
                        break;
                    case LOAD_MORE:
                        int Offset = actCroppedLength;
                        actCroppedLength += Config.FieldNotesLoadLength.getValue();
                        sql += " LIMIT " + String.valueOf(Config.FieldNotesLoadLength.getValue() + 1);
                        sql += " OFFSET " + String.valueOf(Offset);
                }
            }

            SQLiteGdxDatabaseCursor reader = null;
            try {
                reader = Database.FieldNotes.rawQuery(sql, null);
            } catch (Exception exc) {
                log.error("loadFieldNotes", exc);
            }
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                FieldNoteEntry fne = new FieldNoteEntry(reader);
                if (!this.contains(fne)) {
                    this.add(fne);
                }

                reader.moveToNext();
            }
            reader.close();

            // check Cropped
            if (maybeCropped) {
                if (this.size > actCroppedLength) {
                    croppedList = true;
                    // remove last item
                    this.removeIndex(this.size - 1);
                } else {
                    croppedList = false;
                }
            }

            //sort by Date/time
            this.sort(new Comparator<FieldNoteEntry>() {
                @Override
                public int compare(FieldNoteEntry o1, FieldNoteEntry o2) {
                    return o2.timestamp.compareTo(o1.timestamp);
                }
            });
        }
    }

    /**
     * @param dirFileName Config.settings.FieldNotesGarminPath.getValue()
     */
    public static void createVisitsTxt(String dirFileName) {
        FieldNoteList lFieldNotes = new FieldNoteList();
        lFieldNotes.loadFieldNotes("", "Timestamp ASC", LoadingType.LOAD_ALL);

        FileHandle txtFile = Gdx.files.absolute(dirFileName);
        OutputStream writer;
        try {
            writer = txtFile.write(false);

            // write utf8 bom EF BB BF
            byte[] bom = {(byte) 239, (byte) 187, (byte) 191};
            writer.write(bom);

            for (FieldNoteEntry fieldNote : lFieldNotes) {
                String log = fieldNote.gcCode + "," + fieldNote.getDateTimeString() + "," + fieldNote.type.toString() + ",\"" + fieldNote.comment + "\"\n";
                writer.write((log + "\n").getBytes("UTF-8"));
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFieldNoteByCacheId(long cacheId, LogTypes type) {
        synchronized (this) {
            int foundNumber = 0;
            FieldNoteEntry fne = null;
            // deletes a possible existing FieldNote of type for the Cache with cacheId
            for (FieldNoteEntry fn : this) {
                if ((fn.CacheId == cacheId) && (fn.type == type)) {
                    fne = fn;
                }
            }
            if (fne != null) {
                if (fne.type == LogTypes.found)
                    foundNumber = fne.foundNumber;
                this.removeValue(fne, true);
                fne.deleteFromDatabase();
            }
            decreaseFoundNumber(foundNumber);
        }
    }

    public void deleteFieldNote(long fieldNoteID, LogTypes type) {
        synchronized (this) {
            int foundNumber = 0;
            FieldNoteEntry fne = null;
            // deletes a possible existing FieldNote of type for the Cache with fieldNoteID
            for (FieldNoteEntry fn : this) {
                if (fn.Id == fieldNoteID) {
                    fne = fn;
                }
            }
            if (fne != null) {
                if (fne.type == LogTypes.found)
                    foundNumber = fne.foundNumber;
                this.removeValue(fne, true);
                fne.deleteFromDatabase();
            }
            decreaseFoundNumber(foundNumber);
        }
    }

    public void decreaseFoundNumber(int deletedFoundNumber) {
        if (deletedFoundNumber > 0) {
            // Customize all FoundNumbers that are larger
            for (FieldNoteEntry fn : this) {
                if ((fn.type == LogTypes.found) && (fn.foundNumber > deletedFoundNumber)) {
                    int oldFoundNumber = fn.foundNumber;
                    fn.foundNumber--;
                    fn.comment = fn.comment.replaceAll("#" + oldFoundNumber, "#" + fn.foundNumber);
                    fn.fillType();
                    fn.updateDatabase();
                }
            }
        }
    }

    public boolean contains(FieldNoteEntry fne) {
        synchronized (this) {
            for (FieldNoteEntry item : this) {
                if (fne.equals(item))
                    return true;
            }
            return false;
        }
    }
}
