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


import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.events.newT.EventHandler;
import de.longri.cachebox3.utils.MathUtils;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Waypoint extends Coordinate implements Serializable {
    private static final long serialVersionUID = 67610567646416L;
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String EMPTY_STRING = "";

    /**
     * Id des dazugehörigen Caches in der Datenbank von geocaching.com
     */
    public long CacheId;

    /**
     * Waypoint Code
     */
    private byte[] GcCode;


    /**
     * Titel des Wegpunktes
     */
    private byte[] Title;

    /**
     * Art des Wegpunkts
     */
    public CacheTypes Type;

    /**
     * true, falls der Wegpunkt vom Benutzer erstellt wurde
     */
    public boolean IsUserWaypoint;

    /**
     * true, falls der Wegpunkt von der Synchronisation ausgeschlossen wird
     */
    public boolean IsSyncExcluded;

    /**
     * True wenn dies der Startpunkt für den nächsten Besuch ist.<br>
     * Das CacheIcon wird dann auf diesen Waypoint verschoben und dieser Waypoint wird standardmäßig aktiviert<br>
     * Es muss aber sichergestellt sein dass immer nur 1 Waypoint eines Caches ein Startpunkt ist!<br>
     */
    public boolean IsStart = false;

    // Detail Information of Waypoint which are not always loaded
    public WaypointDetail detail = null;

    public Waypoint(double lat, double lon, boolean withDetails) {
        super(lat, lon);
        CacheId = -1;
        setGcCode("");
        setDescription("");
        IsStart = false;
        if (withDetails) {
            detail = new WaypointDetail();
        }
    }

    public Waypoint(String gcCode, CacheTypes type, String description, double latitude, double longitude, long cacheId, String clue, String title) {
        super(latitude, longitude);
        setGcCode(gcCode);
        CacheId = cacheId;
        setDescription(description);
        Type = type;
        IsSyncExcluded = true;
        IsUserWaypoint = true;
        setClue(clue);
        setTitle(title);
        IsStart = false;
        detail = new WaypointDetail();
    }


    public float distance() {
        Coordinate fromPos = EventHandler.getMyPosition();
        if (fromPos == null) return -1;
        float[] dist = new float[4];
        MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST, fromPos.getLatitude(), fromPos.getLongitude(), latitude, longitude, dist);
        return dist[0];
    }

//	public void setCoordinate(Coordinate result) {
//		Pos = result;
//	}

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
            this.Type = CacheTypes.Cache;
        } else {
            String strCacheType;
            if (arrSplitted.length > 1)
                strCacheType = arrSplitted[1];
            else
                strCacheType = arrSplitted[0];

            String[] strFirstWord = strCacheType.split(" ");

            for (String word : strFirstWord) {
                this.Type = CacheTypes.parseString(word);
                if (this.Type != CacheTypes.Undefined)
                    break;
            }

        }
    }

//	public void clear() {
//		CacheId = -1;
//		setGcCode("");
//		Pos = new Coordinate(0, 0);
//		setTitle("");
//		setDescription("");
//		Type = null;
//		IsUserWaypoint = false;
//		IsSyncExcluded = false;
//		setClue("");
//		setCheckSum(0);
//	}

    @Override
    public String toString() {
        return "WP:" + getGcCode() + " " + super.toString();
    }

    public void dispose() {
        setGcCode(null);
        setTitle(null);
        setDescription(null);
        Type = null;
        setClue(null);
    }

    public String getGcCode() {
        if (GcCode == null)
            return EMPTY_STRING;
        return new String(GcCode, US_ASCII);
    }

    public void setGcCode(String gcCode) {
        if (gcCode == null) {
            GcCode = null;
            return;
        }
        GcCode = gcCode.getBytes(US_ASCII);
    }

    public String getTitle() {
        if (Title == null)
            return EMPTY_STRING;
        return new String(Title, UTF_8);
    }

    public void setTitle(String title) {
        if (title == null) {
            Title = null;
            return;
        }
        Title = title.getBytes(UTF_8);
    }

    public String getDescription() {
        if (detail == null) {
            return EMPTY_STRING;
        } else {
            return detail.getDescription();
        }
    }

    public void setDescription(String description) {
        if (detail != null) {
            detail.setDescription(description);
        }
    }

    public String getClue() {
        if (detail == null) {
            return EMPTY_STRING;
        } else {
            return detail.getClue();
        }
    }

    public void setClue(String clue) {
        if (detail != null) {
            detail.setClue(clue);
        }
    }

    public void setCheckSum(int i) {
        if (detail != null) {
            detail.setCheckSum(i);
        }
    }

    public int getCheckSum() {
        if (detail == null) {
            return 0;
        } else {
            return detail.checkSum;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Waypoint) {

            Waypoint wp = (Waypoint) obj;
            if (wp.GcCode == null)
                return false;
            return this.GcCode != null && Arrays.equals(wp.GcCode, this.GcCode);
        }
        return false;
    }

}