/*
 * Copyright (C) 2014-2017 team-cachebox.de
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


import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.sqlite.Database;

import java.io.Serializable;
import java.nio.charset.Charset;

public class Waypoint extends AbstractWaypoint implements Serializable {
    private static final long serialVersionUID = 67610567646416L;
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String EMPTY_STRING = "";

    private long CacheId;

    private String GcCode;


    private String Title;

    private CacheTypes Type;

    private boolean IsUserWaypoint;

    private boolean IsSyncExcluded;

    private boolean IsStart = false;

    // Detail Information of Waypoint which are not always loaded
    private WaypointDetail detail = null;

    public Waypoint(double lat, double lon, boolean withDetails) {
        super(lat, lon);
        setCacheId(-1);
        setGcCode("");
        setDescription("");
        setStart(false);
        if (withDetails) {
            setDetail(new WaypointDetail());
        }
    }

    public Waypoint(String gcCode, CacheTypes type, String description, double latitude, double longitude, long cacheId, String clue, String title) {
        super(latitude, longitude);
        setGcCode(gcCode);
        setCacheId(cacheId);
        setDescription(description);
        setType(type);
        setSyncExcluded(true);
        setUserWaypoint(true);
        setClue(clue);
        setTitle(title);
        setStart(false);
        setDetail(new WaypointDetail());
    }

    /**
     * Copy constructor
     *
     * @param latitude
     * @param longitude
     * @param other
     */
    public Waypoint(double latitude, double longitude, Waypoint other) {
        super(latitude, longitude);
        this.setCacheId(other.getCacheId());
        this.GcCode = other.getGcCode().toString();
        this.Title = other.getTitle().toString();
        this.setType(other.getType());
        this.setUserWaypoint(other.isUserWaypoint());
        this.setSyncExcluded(other.isSyncExcluded());
        this.setStart(other.isStart());
        this.setDetail(other.getDetail());
    }


    @Override
    public float distance() {
        Coordinate fromPos = EventHandler.getMyPosition();
        if (fromPos == null) return -1;
        float[] dist = new float[4];
        MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, fromPos.getLatitude(), fromPos.getLongitude(), latitude, longitude, dist);
        return dist[0];
    }


    /**
     * @param strText
     */
    public void parseTypeString(String strText) {
        // Log.d(TAG, "Parsing type string: " + strText);

		/*
         * Geocaching.com cache types are in the form Geocache|Multi-cache Waypoint|Question to Answer Waypoint|Stages of a Multicache Other
		 * pages / bcaching.com results do not contain the | separator, so make sure that the parsing functionality does work with both
		 * variants
		 */

        String[] arrSplitted = strText.split("\\|");
        if (arrSplitted[0].toLowerCase().equals("geocache")) {
            this.setType(CacheTypes.Cache);
        } else {
            String strCacheType;
            if (arrSplitted.length > 1)
                strCacheType = arrSplitted[1];
            else
                strCacheType = arrSplitted[0];

            String[] strFirstWord = strCacheType.split(" ");

            for (String word : strFirstWord) {
                this.setType(CacheTypes.parseString(word));
                if (this.getType() != CacheTypes.Undefined)
                    break;
            }

        }
    }

    @Override
    public String toString() {
        return "WP:" + getGcCode() + " " + super.toString();
    }

    public void dispose() {
        setDescription(null);
        setType(null);
        setClue(null);
    }

    /**
     * Waypoint Code
     */
    @Override
    public CharSequence getGcCode() {
        return GcCode;
    }

    @Override
    public void setGcCode(String gcCode) {
        GcCode = gcCode;
    }

    /**
     * Titel des Wegpunktes
     */
    @Override
    public CharSequence getTitle() {
        return Title;
    }

    @Override
    public void setTitle(String title) {
        Title = title;
    }

    @Override
    public CharSequence getDescription(Database database) {
        if (getDetail() == null) {
            return EMPTY_STRING;
        } else {
            return getDetail().getDescription();
        }
    }

    @Override
    public void setDescription(String description) {
        if (getDetail() != null) {
            getDetail().setDescription(description);
        }
    }

    @Override
    public CharSequence getClue(Database cb3Database) {
        if (getDetail() == null) {
            return EMPTY_STRING;
        } else {
            return getDetail().getClue();
        }
    }

    @Override
    public void setClue(String clue) {
        if (getDetail() != null) {
            getDetail().setClue(clue);
        }
    }

    @Override
    public void setCheckSum(int i) {
        if (getDetail() != null) {
            getDetail().setCheckSum(i);
        }
    }

    @Override
    public int getCheckSum() {
        if (getDetail() == null) {
            return 0;
        } else {
            return getDetail().checkSum;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object instanceof AbstractWaypoint) {
            AbstractWaypoint other = (AbstractWaypoint) object;
            return this.getGcCode().equals(other.getGcCode());
        }
        return false;
    }

    /**
     * Id des dazugehörigen Caches in der Datenbank von geocaching.com
     */
    @Override
    public long getCacheId() {
        return CacheId;
    }

    @Override
    public void setCacheId(long cacheId) {
        CacheId = cacheId;
    }

    /**
     * Art des Wegpunkts
     */
    @Override
    public CacheTypes getType() {
        return Type;
    }

    @Override
    public void setType(CacheTypes type) {
        Type = type;
    }

    /**
     * true, falls der Wegpunkt vom Benutzer erstellt wurde
     */
    @Override
    public boolean isUserWaypoint() {
        return IsUserWaypoint;
    }

    @Override
    public void setUserWaypoint(boolean userWaypoint) {
        IsUserWaypoint = userWaypoint;
    }

    /**
     * true, falls der Wegpunkt von der Synchronisation ausgeschlossen wird
     */
    @Override
    public boolean isSyncExcluded() {
        return IsSyncExcluded;
    }

    @Override
    public void setSyncExcluded(boolean syncExcluded) {
        IsSyncExcluded = syncExcluded;
    }

    /**
     * True wenn dies der Startpunkt für den nächsten Besuch ist.<br>
     * Das CacheIcon wird dann auf diesen Waypoint verschoben und dieser Waypoint wird standardmäßig aktiviert<br>
     * Es muss aber sichergestellt sein dass immer nur 1 Waypoint eines Caches ein Startpunkt ist!<br>
     */
    @Override
    public boolean isStart() {
        return IsStart;
    }

    @Override
    public void setStart(boolean start) {
        IsStart = start;
    }

    public WaypointDetail getDetail() {
        return detail;
    }

    public void setDetail(WaypointDetail detail) {
        this.detail = detail;
    }
}