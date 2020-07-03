/*
 * Copyright (C) 2020 team-cachebox.de
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


import de.longri.cachebox3.Utils;
import de.longri.cachebox3.sqlite.Database;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Longri on 31.08.2017
 */
public class Draft {

    private final static Logger log = LoggerFactory.getLogger(Draft.class);


    public long Id;
    public long CacheId;
    public String gcCode = "";
    public String GcId = ""; // (mis)used for LogId (or ReferenceCode)
    public Date timestamp;
    public LogType type;
    public CacheTypes cacheType;
    public String comment = "";
    public int foundNumber;
    public CharSequence CacheName = "";
    public String CacheUrl = "";
    public boolean uploaded;
    public int gc_Vote;
    public boolean isTbDraft = false;
    public String TbName = "";
    public String TbIconUrl = "";
    public String TravelBugCode = "";
    public String TrackingNumber = "";
    public boolean isDirectLog = false; // is obsolete (in CB2)

    private Draft(Draft fne) {
        this.Id = fne.Id;
        this.CacheId = fne.CacheId;
        this.gcCode = fne.gcCode;
        GcId=fne.GcId;
        this.timestamp = fne.timestamp;
        this.type = fne.type;
        this.cacheType = fne.cacheType;
        this.comment = fne.comment;
        this.foundNumber = fne.foundNumber;
        this.CacheName = fne.CacheName;
        this.CacheUrl = fne.CacheUrl;
        this.uploaded = fne.uploaded;
        this.gc_Vote = fne.gc_Vote;
        this.isTbDraft = fne.isTbDraft;
        this.TbName = fne.TbName;
        this.TbIconUrl = fne.TbIconUrl;
        this.TravelBugCode = fne.TravelBugCode;
        this.TrackingNumber = fne.TrackingNumber;
        this.isDirectLog = fne.isDirectLog;
    }

    public Draft(LogType logType) {
        Id = -1;
        this.type = logType;
    }

    Draft(GdxSqliteCursor reader) {
        CacheId = reader.getLong(0);
        gcCode = reader.getString(1).trim();
        CacheName = reader.getString(2);
        cacheType = CacheTypes.get(reader.getInt(3));
        String sDate = reader.getString(4);
        try {
            timestamp = Database.dateFormat.parse(sDate);
        } catch (ParseException ignored) {
        }
        if (timestamp == null)
            timestamp = new Date();
        type = LogType.GC2CB_LogType(reader.getInt(5));
        foundNumber = reader.getInt(6);
        comment = reader.getString(7);
        Id = reader.getLong(8);
        CacheUrl = reader.getString(9);
        uploaded = reader.getInt(10) != 0;
        gc_Vote = reader.getInt(11);
        isTbDraft = reader.getInt(12) != 0;
        TbName = reader.getString(13);
        TbIconUrl = reader.getString(14);
        TravelBugCode = reader.getString(15);
        TrackingNumber = reader.getString(16);
        isDirectLog = reader.getInt(17) != 0;
        GcId = reader.getString("GcId");
        if (GcId == null) GcId = "";
    }

    public String getTypeString() {
        switch (type) {
            case found:
                return "#" + foundNumber + " - Found it!";
            case attended:
                return "Attended";
            case webcam_photo_taken:
                return "Webcam Photo Taken";
            case didnt_find:
                return "Did not find!";
            case needs_maintenance:
                return "Needs Maintenance";
            case note:
                return "Write Note";
        }
        return "";
    }

    public String getDateTimeString() {
        SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        datFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String sDate = datFormat.format(timestamp) + "T";

        datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
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
        args.put("GcId", GcId);
        args.put("name", CacheName);
        String stimestamp = Database.dateFormat.format(timestamp);
        args.put("timestamp", stimestamp);
        args.put("type", type.getGcLogTypeId());
        args.put("foundnumber", foundNumber);
        args.put("comment", comment);
        args.put("cachetype", cacheType.ordinal());
        args.put("url", CacheUrl);
        args.put("Uploaded", uploaded);
        args.put("gc_Vote", gc_Vote);
        args.put("TbFieldNote", isTbDraft);
        args.put("TbName", TbName);
        args.put("TbIconUrl", TbIconUrl);
        args.put("TravelBugCode", TravelBugCode);
        args.put("TrackingNumber", TrackingNumber);
        args.put("directLog", isDirectLog);
        try {
            Database.Drafts.insertWithConflictReplace("Fieldnotes", args);
        } catch (Exception exc) {
            exc.printStackTrace();
            return;
        }
        // search Draft Id
        GdxSqliteCursor reader = Database.Drafts
                .rawQuery("select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog, GcId from FieldNotes where GcCode='" + gcCode
                        + "' and Type=" + type.getGcLogTypeId(), (String[]) null);

        if (reader == null) {
            log.error("Can't find updated Draft on DB");
            return;
        }

        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Draft fne = new Draft(reader);
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
        args.put("GcId", GcId);
        args.put("name", CacheName);
        String stimestamp = Database.dateFormat.format(timestamp);
        args.put("timestamp", stimestamp);
        args.put("type", type.getGcLogTypeId());
        args.put("foundnumber", foundNumber);
        args.put("comment", comment);
        args.put("cachetype", cacheType.ordinal());
        args.put("url", CacheUrl);
        args.put("Uploaded", uploaded);
        args.put("gc_Vote", gc_Vote);
        args.put("TbFieldNote", isTbDraft);
        args.put("TbName", TbName);
        args.put("TbIconUrl", TbIconUrl);
        args.put("TravelBugCode", TravelBugCode);
        args.put("TrackingNumber", TrackingNumber);
        args.put("directLog", isDirectLog);
        try {
            Database.Drafts.update("FieldNotes", args, "id=" + Id, null);
            Database.Drafts.endTransaction();
        } catch (Exception ignored) {
        }
    }

    public void deleteFromDatabase() {
        try {
            Database.Drafts.delete("FieldNotes", "Id=" + Id);
            Database.Drafts.endTransaction();
        } catch (Exception ignored) {
        }
    }

    public boolean equals(Draft fne) {
        if (!GcId.equals(fne.GcId))
            return false;
        if (this.Id != fne.Id)
            return false;
        if (this.CacheId != fne.CacheId)
            return false;
        if (!this.gcCode.equals(fne.gcCode))
            return false;
        if (!Utils.equalsDate(this.timestamp, fne.timestamp))
            return false;
        if (this.type != fne.type)
            return false;
        if (this.cacheType != fne.cacheType)
            return false;
        if (!this.comment.equals(fne.comment))
            return false;
        if (this.foundNumber != fne.foundNumber)
            return false;
        if (!this.CacheName.equals(fne.CacheName))
            return false;
        if (!this.CacheUrl.equals(fne.CacheUrl))
            return false;
        if (this.uploaded != fne.uploaded)
            return false;
        if (this.gc_Vote != fne.gc_Vote)
            return false;
        if (this.isTbDraft != fne.isTbDraft)
            return false;
        if (!this.TravelBugCode.equals(fne.TravelBugCode))
            return false;
        return this.TrackingNumber.equals(fne.TrackingNumber);
    }

    public Draft copy() {
        return new Draft(this);
    }

}
