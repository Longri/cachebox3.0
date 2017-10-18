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

import de.longri.cachebox3.gui.utils.CharSequenceArray;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Longri on 17.10.2017.
 */
public class Cache3 extends AbstractCache {

    // ########################################################
    // Boolean Handling
    // one Boolean use up to 4 Bytes
    // Boolean data type represents one bit of information, but its "size" isn't something that's precisely defined. (Oracle Docs)
    //
    // so we use one Short for Store all Boolean and Use a BitMask
    // ########################################################

    // Masks
    public final static short MASK_HAS_HINT = 1 << 0;
    public final static short MASK_CORECTED_COORDS = 1 << 1;
    public final static short MASK_ARCHIVED = 1 << 2;
    public final static short MASK_AVAILABLE = 1 << 3;
    public final static short MASK_FAVORITE = 1 << 4;
    public final static short MASK_FOUND = 1 << 5;
    public final static short MASK_IS_LIVE = 1 << 6;
    public final static short MASK_SOLVER1CHANGED = 1 << 7;
    public final static short MASK_HAS_USER_DATA = 1 << 8;
    public final static short MASK_LISTING_CHANGED = 1 << 9;

    public static boolean getMaskValue(short mask, short bitFlags) {
        return (bitFlags & mask) == mask;
    }

    public static short setMaskValue(short mask, boolean value, short bitFlags) {
        if (getMaskValue(mask, bitFlags) == value){
            return bitFlags;
        }

        if (value) {
            bitFlags |= mask;
        } else {
            bitFlags &= ~mask;
        }
        return bitFlags;
    }


    private final static Logger log = LoggerFactory.getLogger(Cache3.class);

    private CharSequence name;

    public Cache3(double latitude, double longitude) {
        super(latitude, longitude);
    }


    //################################################################################
    //# properties retained at the class
    ///###############################################################################

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = new CharSequenceArray(name);
    }

    //################################################################################
    //# properties that not retained at the class but read/write directly from/to DB
    ///###############################################################################

    @Override
    public boolean ImTheOwner() {
        return false;
    }

    @Override
    public boolean CorrectedCoordiantesOrMysterySolved() {
        return false;
    }

    @Override
    public boolean HasFinalWaypoint() {
        return false;
    }

    @Override
    public Waypoint GetFinalWaypoint() {
        return null;
    }

    @Override
    public boolean HasStartWaypoint() {
        return false;
    }

    @Override
    public Waypoint GetStartWaypoint() {
        return null;
    }

    @Override
    public boolean hasSpoiler() {
        return false;
    }

    @Override
    public float Distance(MathUtils.CalculationType type, boolean useFinal) {
        return 0;
    }

    @Override
    float Distance(MathUtils.CalculationType type, boolean useFinal, Coordinate fromPos) {
        return 0;
    }

    @Override
    public int compareTo(AbstractCache c2) {
        return 0;
    }

    @Override
    protected Waypoint findWaypointByGc(String gc) {
        return null;
    }

    @Override
    public String getGcCode() {
        return null;
    }

    @Override
    public void setGcCode(String gcCode) {

    }


    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public void setOwner(String owner) {

    }

    @Override
    public String getGcId() {
        return null;
    }

    @Override
    public void setGcId(String gcId) {

    }

    @Override
    public String getHint() {
        return null;
    }

    @Override
    public void setHint(String hint) {

    }

    @Override
    public long getGPXFilename_ID() {
        return 0;
    }

    @Override
    public void setGPXFilename_ID(long gpxFilenameId) {

    }

    @Override
    public boolean hasHint() {
        return false;
    }

    @Override
    public boolean hasCorrectedCoordinates() {
        return false;
    }

    @Override
    public void setCorrectedCoordinates(boolean correctedCoordinates) {

    }

    @Override
    public boolean isArchived() {
        return false;
    }

    @Override
    public void setArchived(boolean archived) {

    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void setAvailable(boolean available) {

    }

    @Override
    public boolean isFavorite() {
        return false;
    }

    @Override
    public void setFavorite(boolean favorite) {

    }

    @Override
    public float getDifficulty() {
        return 0;
    }

    @Override
    public void setDifficulty(float difficulty) {

    }

    @Override
    public float getTerrain() {
        return 0;
    }

    @Override
    public void setTerrain(float terrain) {

    }

    @Override
    public boolean isFound() {
        return false;
    }

    @Override
    public void setFound(boolean found) {

    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public void setLive(boolean isLive) {

    }

    @Override
    public boolean isHasUserData() {
        return false;
    }

    @Override
    public void setHasUserData(boolean hasUserData) {

    }

    @Override
    public boolean isListingChanged() {
        return false;
    }

    @Override
    public void setListingChanged(boolean listingChanged) {

    }

    @Override
    public String getPlacedBy() {
        return null;
    }

    @Override
    public void setPlacedBy(String value) {

    }

    @Override
    public Date getDateHidden() {
        return null;
    }

    @Override
    public void setDateHidden(Date date) {

    }

    @Override
    public byte getApiState() {
        return 0;
    }

    @Override
    public void setApiState(byte value) {

    }

    @Override
    public int getNoteChecksum() {
        return 0;
    }

    @Override
    public void setNoteChecksum(int value) {

    }

    @Override
    public String getTmpNote() {
        return null;
    }

    @Override
    public void setTmpNote(String value) {

    }

    @Override
    public int getSolverChecksum() {
        return 0;
    }

    @Override
    public void setSolverChecksum(int value) {

    }

    @Override
    public String getTmpSolver() {
        return null;
    }

    @Override
    public void setTmpSolver(String value) {

    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public void setUrl(String value) {

    }

    @Override
    public String getCountry() {
        return null;
    }

    @Override
    public void setCountry(String value) {

    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public void setState(String value) {

    }

    @Override
    public ArrayList<Attributes> getAttributes() {
        return null;
    }

    @Override
    public void addAttributeNegative(Attributes attribute) {

    }

    @Override
    public void addAttributePositive(Attributes attribute) {

    }

    @Override
    public DLong getAttributesPositive() {
        return null;
    }

    @Override
    public DLong getAttributesNegative() {
        return null;
    }

    @Override
    public void setAttributesPositive(DLong dLong) {

    }

    @Override
    public void setAttributesNegative(DLong dLong) {

    }

    @Override
    public void setLongDescription(String value) {

    }

    @Override
    public String getLongDescription() {
        return null;
    }

    @Override
    public void setShortDescription(String value) {

    }

    @Override
    public String getShortDescription() {
        return null;
    }

    @Override
    public void setTourName(String value) {

    }

    @Override
    public String getTourName() {
        return null;
    }

    @Override
    public boolean isAttributePositiveSet(Attributes attribute) {
        return false;
    }

    @Override
    public boolean isAttributeNegativeSet(Attributes attribute) {
        return false;
    }

    @Override
    public void setFavoritePoints(int value) {

    }

    @Override
    public int getFaviritPoints() {
        return 0;
    }

    @Override
    public int getFavoritePoints() {
        return 0;
    }

    @Override
    public CB_List<Waypoint> getWaypoints() {
        return null;
    }

    @Override
    public void setWaypoints(CB_List<Waypoint> waypoints) {

    }

    @Override
    public CacheDetail getDetail() {
        return null;
    }

    @Override
    public void setDetail(CacheDetail detail) {

    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long id) {

    }

    @Override
    public float getRating() {
        return 0;
    }

    @Override
    public void setRating(float rating) {

    }

    @Override
    public CacheSizes getSize() {
        return null;
    }

    @Override
    public void setSize(CacheSizes size) {

    }

    @Override
    public CacheTypes getType() {
        return null;
    }

    @Override
    public void setType(CacheTypes type) {

    }

    @Override
    public int getNumTravelbugs() {
        return 0;
    }

    @Override
    public void setNumTravelbugs(int numTravelbugs) {

    }

    @Override
    public float getCachedDistance() {
        return 0;
    }

    @Override
    public void setCachedDistance(float cachedDistance) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void loadDetail() {

    }

    @Override
    public void deleteDetail(Boolean value) {

    }

    @Override
    public boolean isDetailLoaded() {
        return false;
    }
}
