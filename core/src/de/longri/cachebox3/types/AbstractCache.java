/*
 * Copyright (C) 2017 - 2019 team-cachebox.de
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
import de.longri.gdx.sqlite.GdxSqliteCursor;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 16.10.2017.
 */
public abstract class AbstractCache extends Coordinate implements Comparable<AbstractCache> {
    public AbstractCache(double latitude, double longitude) {
        super(latitude, longitude);
    }

    public AtomicBoolean isChanged = new AtomicBoolean(false);

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

    /**
     * Last calculated distance as meter
     */
    private int cachedDistance;

    public final float distance(MathUtils.CalculationType type, boolean useFinal, Coordinate fromPos) {
        if (fromPos == null)
            return -1;

        AbstractWaypoint waypoint = null;
        if (useFinal)
            waypoint = this.GetFinalWaypoint();
        // If a mystery has a final waypoint, the distance will be calculated to
        // the final not the the cache coordinates
        Coordinate toPos = this;
        if (waypoint != null) {
            toPos = new Coordinate(waypoint.getLatitude(), waypoint.getLongitude());
            // nur sinnvolles Final, sonst vom Cache
            if (waypoint.getLatitude() == 0 && waypoint.getLongitude() == 0)
                toPos = this;
        }
        float[] dist = new float[4];
        MathUtils.computeDistanceAndBearing(type, fromPos.getLatitude(), fromPos.getLongitude(), toPos.getLatitude(), toPos.getLongitude(), dist);
        cachedDistance = Math.round(dist[0]);
        return cachedDistance;
    }

    public String toString() {
        return getGcCode().toString();
    }

    /**
     * Returns the last calculated distance
     *
     * @return
     */
    public final int getCachedDistance() {
        return cachedDistance;
    }


    @Override
    public int compareTo(AbstractCache o) {
        return this.cachedDistance - o.cachedDistance;
    }


    public abstract Array<Attributes> getAttributes();

    public abstract boolean ImTheOwner();

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

    protected abstract AbstractWaypoint findWaypointByGc(String gc);

    public abstract CharSequence getGcCode();

    public abstract void setGcCode(String gcCode);

    public abstract CharSequence getName();

    public abstract void setName(String name);

    public abstract CharSequence getOwner();

    public abstract void setOwner(String owner);

    public abstract CharSequence getGcId();

    public abstract void setGcId(String gcId);

    public abstract CharSequence getHint();

    public abstract void setHint(CharSequence hint);

    public abstract long getGPXFilename_ID();

    public abstract void setGPXFilename_ID(long gpxFilenameId);

    public abstract boolean hasHint();

    public abstract boolean hasCorrectedCoordinates();

    public abstract void setHasCorrectedCoordinates(boolean correctedCoordinates);

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

    public abstract CharSequence getState();

    public abstract Date getDateHidden();

    public abstract void setDateHidden(Date date);

    public abstract byte getApiState();

    public abstract void setApiState(byte value);

    public abstract int getNoteChecksum();

    public abstract void setNoteChecksum(int value);

    public abstract CharSequence getTmpNote();

    public abstract void setTmpNote(CharSequence value);

    public abstract int getSolverChecksum();

    public abstract void setSolverChecksum(int value);

    public abstract CharSequence getTmpSolver();

    public abstract void setTmpSolver(CharSequence value);

    public abstract CharSequence getUrl();

    public abstract void setUrl(CharSequence value);

    public abstract CharSequence getCountry();

    public abstract void setCountry(CharSequence value);

    public abstract void setState(CharSequence value);

    public abstract void addAttributeNegative(Attributes attribute);

    public abstract void addAttributePositive(Attributes attribute);

    public abstract DLong getAttributesPositive();

    public abstract DLong getAttributesNegative();

    public abstract void setAttributesPositive(DLong dLong);

    public abstract void setAttributesNegative(DLong dLong);

    public abstract void setLongDescription(CharSequence value);

    public abstract CharSequence getLongDescription();

    public abstract void setShortDescription(CharSequence value);

    public abstract CharSequence getShortDescription();

    public abstract void setTourName(CharSequence value);

    public abstract CharSequence getTourName();

    public abstract boolean isAttributePositiveSet(Attributes attribute);

    public abstract boolean isAttributeNegativeSet(Attributes attribute);


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

    public abstract void setRating(short rating);

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

    public abstract void setNumTravelbugs(short numTravelbugs);

    public abstract void dispose();

    public abstract void setAttributes(Array<Attributes> attributes);

    public abstract void setHasHint(boolean hasHint);

    public abstract short getBooleanStore();


    public abstract AbstractCache getCopy();

    public abstract void setInfo(GdxSqliteCursor cursor);

    public abstract void setText(GdxSqliteCursor cursor);

    public abstract void setAttributes(GdxSqliteCursor cursor);

    public abstract void updateBooleanStore(Database database);

    public abstract void setShowOriginalHtmlColor(boolean value);

    public abstract boolean getShowOriginalHtmlColor();
}
