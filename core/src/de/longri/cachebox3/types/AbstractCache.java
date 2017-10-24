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

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.utils.MathUtils;

import java.util.Date;

/**
 * Created by Longri on 16.10.2017.
 */
public abstract class AbstractCache extends Coordinate {
    public AbstractCache(double latitude, double longitude) {
        super(latitude, longitude);
    }

    public static long GenerateCacheId(String GcCode) {
        long result = 0;
        char[] dummy = GcCode.toCharArray();
        byte[] byteDummy = new byte[8];
        for (int i = 0; i < 8; i++) {
            if (i < GcCode.length())
                byteDummy[i] = (byte) dummy[i];
            else
                byteDummy[i] = 0;
        }
        for (int i = 7; i >= 0; i--) {
            result *= 256;
            result += byteDummy[i];
        }
        return result;
    }

    public abstract Array<Attributes> getAttributes(Database database);

    public abstract boolean ImTheOwner();

    /**
     * -- korrigierte Koordinaten (kommt nur aus GSAK? bzw CacheWolf-Import) -- oder Mystery mit gueltigem Final
     */
    public abstract boolean CorrectedCoordiantesOrMysterySolved();

    /**
     * true, if a this mystery cache has a final waypoint
     */
    public abstract boolean HasFinalWaypoint();

    /**
     * search the final waypoint for a mystery cache
     */
    public abstract AbstractWaypoint GetFinalWaypoint();

    /**
     * true if this is a mystery of multi with a Stage Waypoint defined as StartPoint
     *
     * @return
     */
    public abstract boolean HasStartWaypoint();

    /**
     * search the start Waypoint for a multi or mystery
     *
     * @return
     */
    public abstract AbstractWaypoint GetStartWaypoint();

    /**
     * Returns true has the Cache Spoilers else returns false
     *
     * @return Boolean
     */
    public abstract boolean hasSpoiler();

    /**
     * Gibt die Entfernung zur uebergebenen User Position als Float zurueck.
     *
     * @return Entfernung zur uebergebenen User Position als Float
     */
    public abstract float Distance(MathUtils.CalculationType type, boolean useFinal);

    abstract float Distance(MathUtils.CalculationType type, boolean useFinal, Coordinate fromPos);

    public abstract int compareTo(AbstractCache c2);

    protected abstract AbstractWaypoint findWaypointByGc(String gc);

    public abstract CharSequence getGcCode();

    public abstract void setGcCode(String gcCode);

    public abstract CharSequence getName();

    public abstract void setName(String name);

    public abstract CharSequence getOwner();

    public abstract void setOwner(String owner);

    public abstract CharSequence getGcId();

    public abstract void setGcId(String gcId);

    public abstract String getHint(Database database);

    public abstract void setHint(Database database, String hint);

    public abstract long getGPXFilename_ID();

    public abstract void setGPXFilename_ID(long gpxFilenameId);

    public abstract boolean hasHint();

    public abstract boolean hasCorrectedCoordinates();

    public abstract void setCorrectedCoordinates(boolean correctedCoordinates);

    public abstract boolean isArchived();

    public abstract void setArchived(boolean archived);

    public abstract boolean isAvailable();

    public abstract void setAvailable(boolean available);

    public abstract boolean isFavorite();

    public abstract void setFavorite(boolean favorite);

    public abstract float getDifficulty();

    public abstract void setDifficulty(float difficulty);

    public abstract float getTerrain();

    public abstract void setTerrain(float terrain);

    public abstract boolean isFound();

    public abstract void setFound(boolean found);

    public abstract boolean isLive();

    public abstract void setLive(boolean isLive);

    public abstract boolean isHasUserData();

    public abstract void setHasUserData(boolean hasUserData);

    public abstract boolean isListingChanged();

    public abstract void setListingChanged(boolean listingChanged);

    public abstract CharSequence getPlacedBy();

    public abstract void setPlacedBy(String value);

    public abstract Date getDateHidden();

    public abstract void setDateHidden(Date date);

    public abstract byte getApiState();

    public abstract void setApiState(byte value);

    public abstract int getNoteChecksum();

    public abstract void setNoteChecksum(int value);

    public abstract String getTmpNote();

    public abstract void setTmpNote(String value);

    public abstract int getSolverChecksum();

    public abstract void setSolverChecksum(int value);

    public abstract String getTmpSolver();

    public abstract void setTmpSolver(String value);

    public abstract String getUrl();

    public abstract void setUrl(String value);

    public abstract String getCountry();

    public abstract void setCountry(String value);

    public abstract String getState();

    public abstract void setState(String value);

    public abstract void addAttributeNegative(Attributes attribute);

    public abstract void addAttributePositive(Attributes attribute);

    public abstract DLong getAttributesPositive();

    public abstract DLong getAttributesNegative();

    public abstract void setAttributesPositive(DLong dLong);

    public abstract void setAttributesNegative(DLong dLong);

    public abstract void setLongDescription(Database database, String value);

    public abstract String getLongDescription(Database database);

    public abstract void setShortDescription(Database database, String value);

    public abstract String getShortDescription(Database database);

    public abstract void setTourName(String value);

    public abstract String getTourName();

    public abstract boolean isAttributePositiveSet(Attributes attribute);

    public abstract boolean isAttributeNegativeSet(Attributes attribute);

    public abstract void setLatitude(double latitude);

    public abstract void setLongitude(double longitude);

    /**
     * Returns true if the Cache a event like Giga, Cito, Event or Mega
     *
     * @return
     */
    public boolean isEvent() {
        if (this.getType() == CacheTypes.Giga)
            return true;
        if (this.getType() == CacheTypes.CITO)
            return true;
        if (this.getType() == CacheTypes.Event)
            return true;
        if (this.getType() == CacheTypes.MegaEvent)
            return true;
        return false;
    }

    public abstract void setFavoritePoints(int value);


    public abstract int getFavoritePoints();

    /**
     * Liste der zusaetzlichen Wegpunkte des Caches
     */
    public abstract Array<AbstractWaypoint> getWaypoints();

    public abstract void setWaypoints(Array<AbstractWaypoint> waypoints);


    /**
     * Id des Caches in der Datenbank von geocaching.com
     */
    public abstract long getId();

    public abstract void setId(long id);

    /**
     * Durchschnittliche Bewertung des Caches von GcVote
     */
    public abstract float getRating();

    public abstract void setRating(float rating);

    /**
     * Groesse des Caches. Bei Wikipediaeintraegen enthaelt dieses Feld den Radius in m
     */
    public abstract CacheSizes getSize();

    public abstract void setSize(CacheSizes size);

    /**
     * Art des Caches
     */
    public abstract CacheTypes getType();

    public abstract void setType(CacheTypes type);

    /**
     * Anzahl der Travelbugs und Coins, die sich in diesem Cache befinden
     */
    public abstract int getNumTravelbugs();

    public abstract void setNumTravelbugs(int numTravelbugs);

    /**
     * Falls keine erneute Distanzberechnung noetig ist nehmen wir diese Distanz
     */
    public abstract float getCachedDistance();

    public abstract void setCachedDistance(float cachedDistance);

    public abstract void dispose();

    public abstract boolean isMutable();

    public abstract AbstractCache getMutable(Database database);

    public abstract void setAttributes(Array<Attributes> attributes);

    public abstract void setHasHint(boolean hasHint);

    public abstract void setLatLon(double latitude, double longitude);

    public abstract short getBooleanStore();
}
