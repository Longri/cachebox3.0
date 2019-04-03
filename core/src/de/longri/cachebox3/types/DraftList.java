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
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Longri on 31.08.2017
 */
public class DraftList extends Array<DraftEntry> {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(DraftList.class);

    private static final long serialVersionUID = 1L;

    public enum LoadingType {
        LOAD_ALL, LOAD_NEW, LOAD_MORE, LOAD_NEW_LAST_LENGTH
    }

    private boolean croppedList = false;
    private int actCroppedLength = -1;

    public DraftList() {

    }

    public boolean isCropped() {
        return croppedList;
    }

    public void loadDrafts(String where, LoadingType loadingType) {
        synchronized (this) {
            loadDrafts(where, "", loadingType);
        }
    }

    public void loadDrafts(String where, String order, LoadingType loadingType) {
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
            boolean maybeCropped = !Config.DraftsLoadAll.getValue() && loadingType != LoadingType.LOAD_ALL;

            if (maybeCropped) {
                switch (loadingType) {
                    case LOAD_ALL:
                        // do nothing
                        break;
                    case LOAD_NEW:
                        actCroppedLength = Config.DraftsLoadLength.getValue();
                        sql += " LIMIT " + String.valueOf(actCroppedLength + 1);
                        break;
                    case LOAD_NEW_LAST_LENGTH:
                        if (actCroppedLength == -1)
                            actCroppedLength = Config.DraftsLoadLength.getValue();
                        sql += " LIMIT " + String.valueOf(actCroppedLength + 1);
                        break;
                    case LOAD_MORE:
                        int Offset = actCroppedLength;
                        actCroppedLength += Config.DraftsLoadLength.getValue();
                        sql += " LIMIT " + String.valueOf(Config.DraftsLoadLength.getValue() + 1);
                        sql += " OFFSET " + String.valueOf(Offset);
                }
            }
            if (Database.Drafts == null) return;
            GdxSqliteCursor reader = null;
            try {
                reader = Database.Drafts.rawQuery(sql, (String[]) null);
            } catch (Exception exc) {
                log.error("loadDrafts", exc);
            }

            if (reader == null) return;

            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                DraftEntry fne = new DraftEntry(reader);
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
            this.sort(new Comparator<DraftEntry>() {
                @Override
                public int compare(DraftEntry o1, DraftEntry o2) {
                    return o2.timestamp.compareTo(o1.timestamp);
                }
            });
        }
    }

    /**
     * @param dirFileName Config.settings.DraftsGarminPath.getValue()
     */
    public static void createVisitsTxt(String dirFileName) {
        DraftList lDrafts = new DraftList();
        lDrafts.loadDrafts("", "Timestamp ASC", LoadingType.LOAD_ALL);

        FileHandle txtFile = Gdx.files.absolute(dirFileName);
        OutputStream writer;
        try {
            writer = txtFile.write(false);

            // write utf8 bom EF BB BF
            byte[] bom = {(byte) 239, (byte) 187, (byte) 191};
            writer.write(bom);

            for (DraftEntry fieldNote : lDrafts) {
                String log = fieldNote.gcCode + "," + fieldNote.getDateTimeString() + "," + fieldNote.type.toString() + ",\"" + fieldNote.comment + "\"\n";
                writer.write((log + "\n").getBytes("UTF-8"));
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            log.error("can't create visit.txt", e);
            e.printStackTrace();
        }
    }

    public void deleteDraftByCacheId(long cacheId, LogTypes type) {
        synchronized (this) {
            int foundNumber = 0;
            DraftEntry fne = null;
            // deletes a possible existing Draft of type for the Cache with cacheId
            for (DraftEntry fn : this) {
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

    public void deleteDraft(long fieldNoteID, LogTypes type) {
        synchronized (this) {
            int foundNumber = 0;
            DraftEntry fne = null;
            // deletes a possible existing Draft of type for the Cache with fieldNoteID
            for (DraftEntry fn : this) {
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
            for (DraftEntry fn : this) {
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

    public boolean contains(DraftEntry fne) {
        synchronized (this) {
            for (DraftEntry item : this) {
                if (fne.equals(item))
                    return true;
            }
            return false;
        }
    }
}
