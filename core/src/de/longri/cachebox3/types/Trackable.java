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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.UnitFormatter;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 02.09.2017.
 */
public class Trackable implements Comparable<Trackable> {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Trackable.class);
    private int Id = -1;
    private boolean Archived;
    private String TBCode = "";
    private long CacheId;
    private String CurrentGoal = "";
    private String CurrentGeocacheCode = "";
    private String CurrentOwnerName = "";
    private Date DateCreated;
    private String Description;
    private String IconUrl = "";
    private String ImageUrl = "";
    private String Name = "";
    private String OwnerName = "";
    private String Url = "";
    private String TypeName = "";
    private String TrackingCode;

    // TODO must load info (the GS_API gives no info about this)
    private Date lastVisit;
    private String Home = "";
    private int TravelDistance;

    /**
     * <img src="doc-files/1.png"/>
     */
    public Trackable() {
    }

    /**
     * <img src="doc-files/1.png"/>
     *
     * @param Name
     * @param IconUrl
     * @param desc
     */
    public Trackable(String Name, String IconUrl, String desc) {
        this.Name = Name;
        this.IconUrl = IconUrl;
        this.Description = desc;
    }

    /**
     * DAO Constructor <br>
     * Der Constructor, der ein Trackable über eine DB Abfrage erstellt! <img src="doc-files/1.png"/>
     *
     * @param reader
     */
    public Trackable(GdxSqliteCursor reader) {
        try {
            Id = reader.getInt(0);
            Archived = reader.getInt(1) != 0;
            TBCode = reader.getString(2).trim();
            try {
                CacheId = reader.getLong(3);
            } catch (Exception e1) {

                e1.printStackTrace();
            }
            try {
                CurrentGoal = reader.getString(4).trim();
            } catch (Exception e1) {

                e1.printStackTrace();
            }
            try {
                CurrentOwnerName = reader.getString(5).trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String sDate = reader.getString(6);
            if (sDate != null && !sDate.isEmpty()) {
                try {
                    DateCreated = Database.cbDbFormat.parse(sDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            try {
                Description = reader.getString(7);
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                IconUrl = reader.getString(8);
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                ImageUrl = reader.getString(9);
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                Name = reader.getString(10);
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                OwnerName = reader.getString(11);
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                Url = reader.getString(12);
            } catch (Exception e) {

                e.printStackTrace();
            }
//            try {
//                TypeName = reader.getString(13);
//            } catch (Exception e1) {
//
//                e1.printStackTrace();
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    /**
//     * <img src="doc-files/1.png"/>
//     */
//    public Trackable(JSONObject JObj) {
//
//        try {
//            Archived = JObj.getBoolean("Archived");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            TBCode = JObj.getString("Code");
//        } catch (JSONException e) {
//
//            e.printStackTrace();
//        }
//        try {
//            CurrentGeocacheCode = JObj.getString("CurrentGeocacheCode");
//        } catch (JSONException e) {
//            // e.printStackTrace();
//        }
//        try {
//            CurrentGoal = JObj.getString("CurrentGoal");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        JSONObject jOwner;
//        try {
//            jOwner = JObj.getJSONObject("CurrentOwner");
//            CurrentOwnerName = jOwner.getString("UserName");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            String dateCreated = JObj.getString("DateCreated");
//            int date1 = dateCreated.indexOf("/Date(");
//            int date2 = dateCreated.indexOf("-");
//            String date = (String) dateCreated.subSequence(date1 + 6, date2);
//            DateCreated = new Date(Long.valueOf(date));
//        } catch (Exception exc) {
//            Log.err(log, "Constructor Trackable", "", exc);
//        }
//        try {
//            Description = JObj.getString("Description");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            IconUrl = JObj.getString("IconUrl");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        JSONArray jArray;
//        try {
//            jArray = JObj.getJSONArray("Images");
//
//            if (jArray.length() > 0) {
//                ImageUrl = jArray.getJSONObject(0).getString("Url");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            Name = JObj.getString("Name");
//        } catch (JSONException e) {
//
//            e.printStackTrace();
//        }
//        try {
//            jOwner = JObj.getJSONObject("OriginalOwner");
//            OwnerName = jOwner.getString("UserName");
//        } catch (JSONException e) {
//
//            e.printStackTrace();
//        }
//        try {
//            Url = JObj.getString("Url");
//        } catch (JSONException e) {
//
//            e.printStackTrace();
//        }
//        try {
//            TypeName = JObj.getString("TBTypeName");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            TypeName = JObj.getString("TBTypeName");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    /*
     * Getter
     */

    final SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy");

    public String getTravelDistance() {
        return UnitFormatter.distanceString(TravelDistance, false);
    }

    public String getBirth() {
        if (DateCreated == null)
            return "";
        return postFormater.format(DateCreated);
    }

    public String getCurrentGeocacheCode() {
        return CurrentGeocacheCode;
    }

    public String getHome() {
        return Home;
    }

    public String getLastVisit() {
        if (lastVisit == null)
            return "";
        return postFormater.format(lastVisit);
    }

    public String getTypeName() {
        return TypeName;
    }

    public String getOwnerName() {
        return OwnerName;
    }

    public String getIconUrl() {
        return IconUrl;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public long getId() {
        return Id;
    }

    public boolean getArchived() {
        return Archived;
    }

    public String getTBCode() {
        return TBCode;
    }

    public long CacheId() {
        return CacheId;
    }

//    public String getCurrentGoal() {
//        return Jsoup.parse(CurrentGoal).text();
//    }

    public String getCurrentOwnerName() {
        return CurrentOwnerName;
    }

    public Date getDateCreated() {
        return DateCreated;
    }

//    public String getDescription() {
//        return Jsoup.parse(Description).text();
//    }

    public String getName() {
        return Name;
    }

    public String getUrl() {
        return Url;
    }

    public String getTrackingCode() {
        return this.TrackingCode;
    }

    /*
     * Setter
     */

    public void setTrackingCode(String trackingCode) {
        this.TrackingCode = trackingCode;
    }

    /*
     * Overrides
     */

    @Override
    public int compareTo(Trackable T2) {
        return Name.compareToIgnoreCase(T2.Name);
    }

    /**
     * Returns True if a LogType possible <br>
     * <br>
     * Possible LogTypes for TB in Cache: <br>
     * 4 - Post Note <br>
     * 13 - Retrieve It from a Cache <br>
     * 14 - Place in a cache <br>
     * 16 - Mark as missing <br>
     * 48 - Discover <br>
     * <br>
     * Possible LogTypes for TB at other Person: <br>
     * 4 - Post Note <br>
     * 16 - Mark as missing <br>
     * 19 - Grab <br>
     * 48 - Discover <br>
     * 69 - Move to collection <br>
     * 70 - Move to inventory <br>
     * <br>
     * Possible LogTypes for TB at my inventory: <br>
     * 4 - Post Note <br>
     * 14 - Place in a cache <br>
     * 16 - Mark as missing<br>
     * 69 - Move to collection <br>
     * 70 - Move to inventory <br>
     * 75 - Visit<br>
     *
     * @param type
     * @param userName Config.settings.GcLogin.getValue()
     * @return
     */
    public boolean isLogTypePosible(LogTypes type, String userName) {
        int ID = type.getGcLogTypeId();

        if (ID == 4)
            return true; // Note

        if (CurrentGeocacheCode != null && CurrentGeocacheCode.length() > 0 && !CurrentGeocacheCode.equalsIgnoreCase("null")) {
            // TB in Cache
            if (ID == 16)
                return true;

            // the next LogTypes only possible if User has entered the Trackingnumber
            if (!(TrackingCode != null && TrackingCode.length() > 0))
                return false;
            if (ID == 13 || /* ID == 14 || */ID == 48)
                return true; // TODO ist es Sinnvoll einen TB aus einem Cache in einen Cache zu
            // Packen?? ID 14 ist Laut GS erlaubt!
            return false;
        }

        if (CurrentOwnerName.equalsIgnoreCase(userName)) {
            // TB in Inventory
            if (ID == 14 || ID == 16 || ID == 69 || ID == 70 || ID == 75)
                return true;
            return false;
        }

        // TB at other Person

        // User entered TB-Code and not TrackingCode: he can not Grab or Discover
        if (TrackingCode != null && TrackingCode.length() > 0) {
            if (ID == 19 || ID == 48)
                return true;
        }
        if (ID == 16 || ID == 69 || ID == 70)
            return true;

        return false;
    }

    public void setCacheId(long id) {
        this.CacheId = id;
    }

    public void setArchived(boolean value) {
        this.Archived = value;
    }

    public void setId(Long value) {
        this.Id = value.intValue();
    }

    public void setTBCode(String value) {
        this.TBCode = value;
    }

    public String getCurrentGoal() {
        return this.CurrentGoal;
    }


    public void setCurrentGoal(String value) {
        this.CurrentGoal = value;
    }

    public void setCurrentGeocacheCode(String value) {
        this.CurrentGeocacheCode = value;
    }

    public void setDateCreated(Date date) {
        this.DateCreated = date;
    }

    public void setDescription(String value) {
        this.Description = value;
    }

    public void setIconUrl(String value) {
        this.IconUrl = value;
    }

    public void setName(String value) {
        this.Name = value;
    }

    public void setUrl(String value) {
        this.Url = value;
    }

    public void setTypeName(String value) {
        this.TypeName = value;
    }

    public String getDescription() {
        return this.Description;
    }

    public void setImageUrl(String value) {
        this.ImageUrl = value;
    }

    public void setCurrentOwnerName(String stringValue) {
        CurrentOwnerName = stringValue;
    }

    public void setOwnerName(String stringValue) {
        OwnerName = stringValue;
    }
}
