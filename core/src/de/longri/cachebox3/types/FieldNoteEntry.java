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


import com.badlogic.gdx.sql.SQLiteGdxDatabaseCursor;
import de.longri.cachebox3.sqlite.Database;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Longri on 31.08.2017
 */
public class FieldNoteEntry {


    public long Id;
    public long CacheId;
    public String gcCode = "";
    public Date timestamp;
    public String typeString = "";
    public LogTypes type;
    public CacheTypes cacheType;
    public String comment = "";
    public int foundNumber;
    public String CacheName = "";
    public String CacheUrl = "";
    public int typeIcon;
    public boolean uploaded;
    public int gc_Vote;
    public boolean isTbFieldNote = false;
    public String TbName = "";
    public String TbIconUrl = "";
    public String TravelBugCode = "";
    public String TrackingNumber = "";
    public boolean isDirectLog = false;

    private FieldNoteEntry(FieldNoteEntry fne) {
        this.Id = fne.Id;
        this.CacheId = fne.CacheId;
        this.gcCode = fne.gcCode;
        this.timestamp = fne.timestamp;
        this.typeString = fne.typeString;
        this.type = fne.type;
        this.cacheType = fne.cacheType;
        this.comment = fne.comment;
        this.foundNumber = fne.foundNumber;
        this.CacheName = fne.CacheName;
        this.CacheUrl = fne.CacheUrl;
        this.typeIcon = fne.typeIcon;
        this.uploaded = fne.uploaded;
        this.gc_Vote = fne.gc_Vote;
        this.isTbFieldNote = fne.isTbFieldNote;
        this.TbName = fne.TbName;
        this.TbIconUrl = fne.TbIconUrl;
        this.TravelBugCode = fne.TravelBugCode;
        this.TrackingNumber = fne.TrackingNumber;
        this.isDirectLog = fne.isDirectLog;
    }

    public FieldNoteEntry(LogTypes Type) {
        Id = -1;
        this.type = Type;
        fillType();
    }

    FieldNoteEntry(SQLiteGdxDatabaseCursor reader) {
        CacheId = reader.getLong(0);
        gcCode = reader.getString(1).trim();
        CacheName = reader.getString(2);
        cacheType = CacheTypes.get(reader.getInt(3));
        String sDate = reader.getString(4);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            timestamp = iso8601Format.parse(sDate);
        } catch (ParseException e) {
        }
        if (timestamp == null)
            timestamp = new Date();
        type = LogTypes.GC2CB_LogType(reader.getInt(5));
        foundNumber = reader.getInt(6);
        comment = reader.getString(7);
        Id = reader.getLong(8);
        CacheUrl = reader.getString(9);
        uploaded = reader.getInt(10) != 0;
        gc_Vote = reader.getInt(11);
        isTbFieldNote = reader.getInt(12) != 0;
        TbName = reader.getString(13);
        TbIconUrl = reader.getString(14);
        TravelBugCode = reader.getString(15);
        TrackingNumber = reader.getString(16);
        isDirectLog = reader.getInt(17) != 0;
        fillType();

    }

    public void fillType() {
        typeIcon = type.getIconID();

        if (type == LogTypes.found || type == LogTypes.attended || type == LogTypes.webcam_photo_taken) {
            typeString = "#" + foundNumber + " - Found it!";
            if (cacheType == CacheTypes.Event
                    || cacheType == CacheTypes.MegaEvent
                    || cacheType == CacheTypes.Giga
                    || cacheType == CacheTypes.CITO)
                typeString = "Attended";
            if (cacheType == CacheTypes.Camera)
                typeString = "Webcam Photo Taken";
        }

        if (type == LogTypes.didnt_find) {
            typeString = "Did not find!";
        }

        if (type == LogTypes.needs_maintenance) {
            typeString = "Needs Maintenance";
        }

        if (type == LogTypes.note) {
            typeString = "Write Note";
        }
    }

    public String getDateTimeString() {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd");
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sDate = datFormat.format(timestamp) + "T";

        datFormat = new SimpleDateFormat("HH:mm:ss");
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        sDate += datFormat.format(timestamp) + "Z";

        return sDate;
    }

    public void writeToDatabase() {
        Database.Parameters args = new Database.Parameters();
        if (Id >= 0)
            args.put("id", Id); // with Update!!!
        args.put("cacheid", CacheId);
        args.put("gccode", gcCode);
        args.put("name", CacheName);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stimestamp = iso8601Format.format(timestamp);
        args.put("timestamp", stimestamp);
        args.put("type", type.getGcLogTypeId());
        args.put("foundnumber", foundNumber);
        args.put("comment", comment);
        args.put("cachetype", cacheType);
        args.put("url", CacheUrl);
        args.put("Uploaded", uploaded);
        args.put("gc_Vote", gc_Vote);
        args.put("TbFieldNote", isTbFieldNote);
        args.put("TbName", TbName);
        args.put("TbIconUrl", TbIconUrl);
        args.put("TravelBugCode", TravelBugCode);
        args.put("TrackingNumber", TrackingNumber);
        args.put("directLog", isDirectLog);
        try {
            Database.FieldNotes.insertWithConflictReplace("Fieldnotes", args);
        } catch (Exception exc) {
            return;
        }
        // search FieldNote Id
        SQLiteGdxDatabaseCursor reader = Database.FieldNotes
                .rawQuery("select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog from FieldNotes where GcCode='" + gcCode
                        + "' and type=" + type.getGcLogTypeId(), null);
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            FieldNoteEntry fne = new FieldNoteEntry(reader);
            this.Id = fne.Id;
            reader.moveToNext();
        }
        reader.close();
    }

    public void updateDatabase() {
        if (timestamp == null)
            timestamp = new Date();
        Database.Parameters args = new Database.Parameters();
        args.put("cacheid", CacheId);
        args.put("gccode", gcCode);
        args.put("name", CacheName);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stimestamp = iso8601Format.format(timestamp);
        args.put("timestamp", stimestamp);
        args.put("type", type.getGcLogTypeId());
        args.put("foundnumber", foundNumber);
        args.put("comment", comment);
        args.put("cachetype", cacheType);
        args.put("url", CacheUrl);
        args.put("Uploaded", uploaded);
        args.put("gc_Vote", gc_Vote);
        args.put("TbFieldNote", isTbFieldNote);
        args.put("TbName", TbName);
        args.put("TbIconUrl", TbIconUrl);
        args.put("TravelBugCode", TravelBugCode);
        args.put("TrackingNumber", TrackingNumber);
        args.put("directLog", isDirectLog);
        try {
            long count = Database.FieldNotes.update("FieldNotes", args, "id=" + Id, null);
            if (count > 0)
                return;
        } catch (Exception exc) {
            return;
        }
    }

    public void deleteFromDatabase() {
        try {
            Database.FieldNotes.delete("FieldNotes", "id=" + Id, null);
        } catch (Exception exc) {
            return;
        }
    }

    public boolean equals(FieldNoteEntry fne) {
        if (this.Id != fne.Id)
            return false;
        if (this.CacheId != fne.CacheId)
            return false;
        if (this.gcCode != fne.gcCode)
            return false;
        if (this.timestamp != fne.timestamp)
            return false;
        if (this.typeString != fne.typeString)
            return false;
        if (this.type != fne.type)
            return false;
        if (this.cacheType != fne.cacheType)
            return false;
        if (this.comment != fne.comment)
            return false;
        if (this.foundNumber != fne.foundNumber)
            return false;
        if (this.CacheName != fne.CacheName)
            return false;
        if (this.CacheUrl != fne.CacheUrl)
            return false;
        if (this.typeIcon != fne.typeIcon)
            return false;
        if (this.uploaded != fne.uploaded)
            return false;
        if (this.gc_Vote != fne.gc_Vote)
            return false;
        if (this.isTbFieldNote != fne.isTbFieldNote)
            return false;
        if (this.TravelBugCode != fne.TravelBugCode)
            return false;
        if (this.TrackingNumber != fne.TrackingNumber)
            return false;
        if (this.isDirectLog != fne.isDirectLog)
            return false;

        return true;
    }

    public FieldNoteEntry copy() {
        return new FieldNoteEntry(this);
    }

}