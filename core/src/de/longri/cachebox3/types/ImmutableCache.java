/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
import com.badlogic.gdx.utils.Pool;
import de.longri.cachebox3.gui.utils.CharSequenceArray;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Longri on 17.10.2017.
 */
public class ImmutableCache extends AbstractCache implements Pool.Poolable {

    private final static Logger log = LoggerFactory.getLogger(ImmutableCache.class);


    // ########################################################
    // Boolean Handling
    // one Boolean use up to 4 Bytes
    // Boolean data type represents one bit of information, but its "size" isn't something that's precisely defined. (Oracle Docs)
    //
    // so we use one Short for Store all Boolean and Use a BitMask
    // ########################################################

    // Masks
    public final static short MASK_HAS_HINT = 1 << 0;
    public final static short MASK_CORECTED_COORDS = 1 << 1; //   2
    public final static short MASK_ARCHIVED = 1 << 2;        //   4
    public final static short MASK_AVAILABLE = 1 << 3;       //   8
    public final static short MASK_FAVORITE = 1 << 4;        //  16
    public final static short MASK_FOUND = 1 << 5;           //  32
    public final static short MASK_IS_LIVE = 1 << 6;         //  64
    public final static short MASK_SOLVER1CHANGED = 1 << 7;  // 128
    public final static short MASK_HAS_USER_DATA = 1 << 8;   // 256
    public final static short MASK_LISTING_CHANGED = 1 << 9; // 512


    public static boolean getMaskValue(short mask, short bitFlags) {
        return (bitFlags & mask) == mask;
    }

    public static short setMaskValue(short mask, boolean value, short bitFlags) {
        if (getMaskValue(mask, bitFlags) == value) {
            return bitFlags;
        }

        if (value) {
            bitFlags |= mask;
        } else {
            bitFlags &= ~mask;
        }
        return bitFlags;
    }


    protected CharSequence name, gcCode, placedBy, owner, gcId, country, state, tmpNote, tmpSolver, url,
            shortDescription, longDescription, hint, tourName;
    protected short rating, numTravelbugs, booleanStore;
    protected int favPoints;
    protected long id;
    protected CacheTypes type;
    protected CacheSizes size;
    protected float difficulty, terrain;
    protected Array<AbstractWaypoint> waypoints;
    protected Array<Attributes> attributes;
    protected Date dateHidden;
    protected byte apiState;
    protected DLong attributesNegative;
    protected DLong attributesPositive;


    public ImmutableCache(double latitude, double longitude) {
        super(latitude, longitude);
        this.id = 0;
        short sizeOrigin = 0;
        this.size = CacheSizes.parseInt(sizeOrigin);
        this.difficulty = 0f;
        this.terrain = 0f;
        short typeOrigin = 0;
        this.type = CacheTypes.get(typeOrigin);
        this.rating = 0;
        this.numTravelbugs = 0;
        this.gcCode = "";
        this.name = "";
        this.placedBy = "";
        this.owner = "";
        this.gcId = "";
        this.booleanStore = 0;
        this.favPoints = 0;
    }

    public ImmutableCache(AbstractCache cache) {
        super(cache.getLatitude(), cache.getLongitude());
        this.name = cache.getName() == null ? null : cache.getName().toString();
        this.gcCode = cache.getGcCode() == null ? null : cache.getGcCode().toString();
        this.placedBy = cache.getPlacedBy() == null ? null : cache.getPlacedBy().toString();
        this.owner = cache.getOwner() == null ? null : cache.getOwner().toString();
        this.gcId = cache.getGcId() == null ? null : cache.getGcId().toString();
        this.rating = (short) (cache.getRating() * 2);
        this.favPoints = cache.getFavoritePoints();
        this.id = cache.getId();
        this.type = cache.getType();
        this.size = cache.getSize();
        this.difficulty = cache.getDifficulty();
        this.terrain = cache.getTerrain();
        this.waypoints = cache.getWaypoints();
        this.numTravelbugs = (short) cache.getNumTravelbugs();

        short bitStore = 0;
        bitStore = setMaskValue(MASK_HAS_HINT, cache.hasHint(), bitStore);
        bitStore = setMaskValue(MASK_ARCHIVED, cache.isArchived(), bitStore);
        bitStore = setMaskValue(MASK_AVAILABLE, cache.isAvailable(), bitStore);
        bitStore = setMaskValue(MASK_FAVORITE, cache.isFavorite(), bitStore);
        bitStore = setMaskValue(MASK_FOUND, cache.isFound(), bitStore);
        bitStore = setMaskValue(MASK_HAS_USER_DATA, cache.isHasUserData(), bitStore);
        bitStore = setMaskValue(MASK_LISTING_CHANGED, cache.isListingChanged(), bitStore);
        bitStore = setMaskValue(MASK_CORECTED_COORDS, cache.hasCorrectedCoordinates(), bitStore);
        this.booleanStore = bitStore;
    }

    public ImmutableCache(GdxSqliteCursor cursor) {
        super(cursor.getDouble(1), cursor.getDouble(2));
        this.id = cursor.getLong(0);
        this.size = CacheSizes.parseInt(cursor.getShort(3));
        this.difficulty = (float) cursor.getShort(4) / 2.0f;
        this.terrain = (float) cursor.getShort(5) / 2.0f;
        this.type = CacheTypes.get(cursor.getShort(6));
        this.rating = (short) (cursor.getShort(7) / 100);
        this.numTravelbugs = cursor.getShort(8);

        this.gcCode = new CharSequenceArray(cursor.getString(9));

        String nameString = cursor.getString(10);
        if (nameString != null) this.name = new CharSequenceArray(nameString.trim());
        else this.name = null;

        String placedByString = cursor.getString(11);
        if (placedByString != null) this.placedBy = new CharSequenceArray(placedByString);
        else this.placedBy = null;

        String ownerString = cursor.getString(12);
        if (ownerString != null) this.owner = new CharSequenceArray(ownerString);
        else this.owner = null;

        String gcIdString = cursor.getString(13);
        if (gcIdString != null) this.gcId = new CharSequenceArray(gcIdString);
        else this.gcId = null;

        this.booleanStore = cursor.getShort(14);
        this.favPoints = cursor.getInt(15);
    }

    public ImmutableCache(Object[] values) {
        super((double) values[1], (double) values[2]);
        this.id = (long) values[0];
        this.size = CacheSizes.parseInt(((Long) values[3]).intValue());
        this.difficulty = (float) ((Long) values[4]) / 2.0f;
        this.terrain = (float) ((Long) values[5]) / 2.0f;
        this.type = CacheTypes.get(((Long) values[6]).intValue());
        this.rating = (short) (((Long) values[7]).shortValue() / 100);
        this.numTravelbugs = ((Long) values[8]).shortValue();

        this.gcCode = new CharSequenceArray((String) values[9]);
        this.name = new CharSequenceArray((String) values[10]);
        this.placedBy = new CharSequenceArray((String) values[11]);
        this.owner = new CharSequenceArray((String) values[12]);
        this.gcId = new CharSequenceArray((String) values[13]);

        this.booleanStore = ((Long) values[14]).shortValue();
        this.favPoints = values[15] == null ? 0 : ((Long) values[15]).intValue();

    }

    //################################################################################
    //# properties retained at the class
    ///###############################################################################

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }


    @Override
    public CharSequence getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }


    @Override
    public CacheSizes getSize() {
        return this.size;
    }

    @Override
    public void setSize(CacheSizes size) {
        this.size = size;
    }


    @Override
    public float getDifficulty() {
        return this.difficulty;
    }

    @Override
    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }


    @Override
    public float getTerrain() {
        return this.terrain;
    }

    @Override
    public void setTerrain(float terrain) {
        this.terrain = terrain;
    }


    @Override
    public CacheTypes getType() {
        return this.type;
    }

    @Override
    public void setType(CacheTypes type) {
        this.type = type;
    }


    @Override
    public CharSequence getGcCode() {
        return this.gcCode;
    }

    @Override
    public void setGcCode(String gcCode) {
        this.gcCode = gcCode;
    }


    @Override
    public CharSequence getPlacedBy() {
        return this.placedBy;
    }

    @Override
    public void setPlacedBy(String value) {
        this.placedBy = placedBy;
    }


    @Override
    public CharSequence getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }


    @Override
    public CharSequence getGcId() {
        return this.gcId;
    }

    @Override
    public void setGcId(String gcId) {
        this.gcId = gcId;
    }


    @Override
    public boolean isAvailable() {
        return this.getMaskValue(MASK_AVAILABLE);
    }

    @Override
    public void setAvailable(boolean available) {
        this.setMaskValue(MASK_AVAILABLE, available);
    }


    @Override
    public boolean isFound() {
        return this.getMaskValue(MASK_FOUND);
    }

    @Override
    public void setFound(boolean isFound) {
        this.setMaskValue(MASK_FOUND, isFound);
    }


    @Override
    public boolean isHasUserData() {
        return this.getMaskValue(MASK_HAS_USER_DATA);
    }

    @Override
    public void setHasUserData(boolean hasUserData) {
        this.setMaskValue(MASK_HAS_USER_DATA, hasUserData);
    }


    @Override
    public int getNumTravelbugs() {
        return this.numTravelbugs;
    }

    @Override
    public void setNumTravelbugs(short numTravelbugs) {
        this.numTravelbugs = numTravelbugs;
    }


    @Override
    public boolean isArchived() {
        return this.getMaskValue(MASK_ARCHIVED);
    }

    @Override
    public void setArchived(boolean archived) {
        this.setMaskValue(MASK_ARCHIVED, archived);
    }


    @Override
    public boolean hasCorrectedCoordinates() {
        return this.getMaskValue(MASK_CORECTED_COORDS);
    }

    @Override
    public void setHasCorrectedCoordinates(boolean correctedCoordinates) {
        this.setMaskValue(MASK_CORECTED_COORDS, correctedCoordinates);
    }


    @Override
    public boolean isFavorite() {
        return this.getMaskValue(MASK_FAVORITE);
    }

    @Override
    public void setFavorite(boolean favorite) {
        setMaskValue(MASK_FAVORITE, favorite);
    }


    @Override
    public boolean hasHint() {
        return this.getMaskValue(MASK_HAS_HINT);
    }

    @Override
    public void setHasHint(boolean hasHint) {
        setMaskValue(MASK_HAS_HINT, hasHint);
    }

    @Override
    public boolean isListingChanged() {
        return this.getMaskValue(MASK_LISTING_CHANGED);
    }

    @Override
    public void setListingChanged(boolean listingChanged) {
        setMaskValue(MASK_LISTING_CHANGED, listingChanged);
    }


    @Override
    public Array<AbstractWaypoint> getWaypoints() {
        return this.waypoints;
    }

    @Override
    public void setWaypoints(Array<AbstractWaypoint> waypoints) {
        this.waypoints = waypoints;
    }


    @Override
    public CharSequence getCountry() {
        return this.country;
    }

    @Override
    public CharSequence getState() {
        return this.state;
    }


    @Override
    public Date getDateHidden() {
        return this.dateHidden;
    }

    @Override
    public void setDateHidden(Date date) {
        this.dateHidden = date;
    }


    @Override
    public CharSequence getTmpNote() {
        return this.tmpNote;
    }

    @Override
    public CharSequence getTmpSolver() {
        return this.tmpSolver;
    }


    @Override
    public void setAttributesPositive(DLong dLong) {

    }

    @Override
    public void setAttributesNegative(DLong dLong) {

    }

    @Override
    public void setAttributes(Array<Attributes> attributes) {
        this.attributes = attributes;
    }


    @Override
    public void setRating(short rating) {
        this.rating = rating;
    }

    @Override
    public void setFavoritePoints(int value) {
        this.favPoints = value;
    }


    @Override
    public void setShortDescription(CharSequence value) {
        this.shortDescription = value;
    }

    @Override
    public void setLongDescription(CharSequence value) {
        this.longDescription = value;
    }

    @Override
    public void setHint(CharSequence hint) {
        this.hint = hint;
    }


    @Override
    public void setUrl(CharSequence value) {
        this.url = value;
    }

    @Override
    public void setState(CharSequence value) {
        this.state = value;
    }

    @Override
    public void setCountry(CharSequence value) {
        this.country = value;
    }


    @Override
    public void setTmpNote(CharSequence value) {
        this.tmpNote = value;
    }

    @Override
    public void setTmpSolver(CharSequence value) {
        this.tmpSolver = value;
    }

    //################################################################################
    //# properties that not retained at the class but read/write directly from/to DB
    ///###############################################################################


    @Override
    public Array<Attributes> getAttributes() {
        return this.attributes;
    }

    @Override
    public CharSequence getLongDescription() {
        return this.longDescription;
    }


    @Override
    public CharSequence getShortDescription() {
        return this.shortDescription;
    }

    @Override
    public CharSequence getHint() {
        return this.hint;
    }

    @Override
    public CharSequence getUrl() {
        return this.url;
    }


    //################################################################################
    //# not tested now
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
    public AbstractWaypoint GetFinalWaypoint() {
        return null;
    }

    @Override
    public boolean HasStartWaypoint() {
        return false;
    }

    @Override
    public AbstractWaypoint GetStartWaypoint() {
        return null;
    }

    @Override
    public boolean hasSpoiler() {
        return false;
    }

    @Override
    public int compareTo(AbstractCache c2) {
        return 0;
    }

    @Override
    protected AbstractWaypoint findWaypointByGc(String gc) {
        return null;
    }


    @Override
    public long getGPXFilename_ID() {
        return 0;
    }

    @Override
    public void setGPXFilename_ID(long gpxFilenameId) {

    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public void setLive(boolean isLive) {

    }

    @Override
    public byte getApiState() {
        return this.apiState;
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
    public int getSolverChecksum() {
        return 0;
    }

    @Override
    public void setSolverChecksum(int value) {

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
    public void setTourName(CharSequence value) {
        this.tourName = value;
    }

    @Override
    public CharSequence getTourName() {
        return this.tourName;
    }

    @Override
    public boolean isAttributePositiveSet(Attributes attribute) {
        if (attributesPositive == null) {
            if (this.attributes == null) return false;
            for (Attributes at : this.attributes) {
                if (at.isNegative()) this.addAttributeNegative(at);
                else this.addAttributePositive(at);
            }
        }
        if (attributesPositive == null) return false;
        return attributesPositive.BitAndBiggerNull(Attributes.GetAttributeDlong(attribute));
    }

    @Override
    public boolean isAttributeNegativeSet(Attributes attribute) {
        if (attributesNegative == null) {
            if (this.attributes == null) return false;
            for (Attributes at : this.attributes) {
                if (at.isNegative()) this.addAttributeNegative(at);
                else this.addAttributePositive(at);
            }
        }
        if (attributesNegative == null) return false;
        return attributesNegative.BitAndBiggerNull(Attributes.GetAttributeDlong(attribute));
    }

    @Override
    public int getFavoritePoints() {
        return this.favPoints;
    }


    @Override
    public float getRating() {
        return this.rating / 2f;
    }


    @Override
    public void dispose() {

    }

    @Override
    public AbstractCache getCopy() {
        return new ImmutableCache(this);
    }

    private boolean getMaskValue(short mask) {
        return getMaskValue(mask, booleanStore);
    }

    private void setMaskValue(short mask, boolean value) {
        booleanStore = setMaskValue(mask, value, booleanStore);
    }

    @Override
    public short getBooleanStore() {
        return this.booleanStore;
    }

    @Override //Pool.Poolable
    public void reset() {
        name = null;
        gcCode = null;
        placedBy = null;
        owner = null;
        gcId = null;
        rating = 0;
        numTravelbugs = 0;
        booleanStore = 0;
        favPoints = 0;
        id = 0;
        type = null;
        size = null;
        difficulty = 0;
        terrain = 0;
        waypoints = null; //TODO free waypoints if called Pool.free(Cache)
    }
}
