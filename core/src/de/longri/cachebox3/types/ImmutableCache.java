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
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.gui.utils.CharSequenceArray;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by Longri on 17.10.2017.
 */
public class ImmutableCache extends AbstractCache {

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
    private Array<AbstractWaypoint> waypoints;

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


    private final static Logger log = LoggerFactory.getLogger(ImmutableCache.class);

    private final CharSequence name, gcCode, placedBy, owner, gcId;
    private final short rating, numTravelbugs, booleanStore;
    private final int favPoints;
    private final long id;
    private final CacheTypes type;
    private final CacheSizes size;
    private final float difficulty, terrain;


    public ImmutableCache(SQLiteGdxDatabaseCursor cursor) {
        super(cursor.getDouble(1), cursor.getDouble(2));
        this.id = cursor.getLong(0);
        short sizeOrigin = cursor.getShort(3);
        this.size = CacheSizes.parseInt(sizeOrigin);
        this.difficulty = (float) cursor.getShort(4) / 2.0f;
        this.terrain = (float) cursor.getShort(5) / 2.0f;
        short typeOrigin = cursor.getShort(6);
        this.type = CacheTypes.get(typeOrigin);
        this.rating = (short) (cursor.getShort(7) / 100);
        this.numTravelbugs = cursor.getShort(8);
        this.gcCode = new CharSequenceArray(cursor.getString(9));
        this.name = new CharSequenceArray(cursor.getString(10).trim());
        this.placedBy = new CharSequenceArray(cursor.getString(11));
        this.owner = new CharSequenceArray(cursor.getString(12));
        this.gcId = new CharSequenceArray(cursor.getString(13));
        this.booleanStore = cursor.getShort(14);
        this.favPoints = cursor.getInt(15);
    }

    //################################################################################
    //# properties retained at the class
    ///###############################################################################

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CacheSizes getSize() {
        return size;
    }

    @Override
    public float getDifficulty() {
        return difficulty;
    }

    @Override
    public float getTerrain() {
        return terrain;
    }

    @Override
    public CacheTypes getType() {
        return type;
    }

    @Override
    public CharSequence getGcCode() {
        return gcCode;
    }

    @Override
    public CharSequence getPlacedBy() {
        return placedBy;
    }

    @Override
    public CharSequence getOwner() {
        return owner;
    }

    @Override
    public CharSequence getGcId() {
        return gcId;
    }

    @Override
    public boolean isAvailable() {
        return this.getMaskValue(MASK_AVAILABLE);
    }

    @Override
    public boolean isFound() {
        return this.getMaskValue(MASK_FOUND);
    }

    @Override
    public boolean isHasUserData() {
        return this.getMaskValue(MASK_HAS_USER_DATA);
    }

    @Override
    public int getNumTravelbugs() {
        return numTravelbugs;
    }

    @Override
    public boolean isArchived() {
        return this.getMaskValue(MASK_ARCHIVED);
    }

    @Override
    public boolean hasCorrectedCoordinates() {
        return this.getMaskValue(MASK_CORECTED_COORDS);
    }

    @Override
    public boolean isFavorite() {
        return this.getMaskValue(MASK_FAVORITE);
    }

    @Override
    public boolean hasHint() {
        return this.getMaskValue(MASK_HAS_HINT);
    }

    @Override
    public boolean isListingChanged() {
        return this.getMaskValue(MASK_LISTING_CHANGED);
    }


    private boolean getMaskValue(short mask) {
        return getMaskValue(mask, booleanStore);
    }

    @Override
    public Array<AbstractWaypoint> getWaypoints() {
        return this.waypoints;
    }

    @Override
    public void setWaypoints(Array<AbstractWaypoint> waypoints) {
        this.waypoints = waypoints;
    }

    //################################################################################
    //# method's that throws exceptions
    ///###############################################################################

    private void throwNotChangeable(String propertyName) {
        throw new RuntimeException("'" + propertyName + "' is not changeable! Use MutableCache.class instead of ImmutableCache.class");
    }

    @Override
    public void setId(long id) {
        throwNotChangeable("Id");
    }

    @Override
    public void setName(String name) {
        throwNotChangeable("Name");
    }

    @Override
    public void setSize(CacheSizes size) {
        throwNotChangeable("Size");
    }

    @Override
    public void setDifficulty(float difficulty) {
        throwNotChangeable("Difficulty");
    }

    @Override
    public void setTerrain(float terrain) {
        throwNotChangeable("Terrain");
    }

    @Override
    public void setType(CacheTypes type) {
        throwNotChangeable("Type");
    }

    @Override
    public void setGcCode(String gcCode) {
        throwNotChangeable("GcCode");
    }

    @Override
    public void setPlacedBy(String value) {
        throwNotChangeable("PlacedBy");
    }

    @Override
    public void setOwner(String owner) {
        throwNotChangeable("Owner");
    }

    @Override
    public void setGcId(String gcId) {
        throwNotChangeable("GcId");
    }

    @Override
    public void setAvailable(boolean available) {
        throwNotChangeable("Available");
    }

    @Override
    public void setHasUserData(boolean hasUserData) {
        throwNotChangeable("HasUserData");
    }

    @Override
    public void setNumTravelbugs(int numTravelbugs) {
        throwNotChangeable("NumTravelbugs");
    }

    @Override
    public void setArchived(boolean archived) {
        throwNotChangeable("Archived");
    }

    @Override
    public void setCorrectedCoordinates(boolean correctedCoordinates) {
        throwNotChangeable("hasCorrectedCoordinates");
    }

    @Override
    public void setFavorite(boolean favorite) {
        throwNotChangeable("Favorite");
    }

    @Override
    public void setLatLon(double latitude, double longitude) {
        throwNotChangeable("LatLon");
    }

    @Override
    public void setAttributesPositive(DLong dLong) {
        throwNotChangeable("AttributesPositive");
    }

    @Override
    public void setAttributesNegative(DLong dLong) {
        throwNotChangeable("AttributesNegative");
    }

    @Override
    public void setAttributes(Array<Attributes> attributes) {
        throwNotChangeable("Attributes");
    }

    @Override
    public void setDateHidden(Date date) {
        throwNotChangeable("DateHidden");
    }

    @Override
    public void setRating(float rating) {
        throwNotChangeable("Rating");
    }

    @Override
    public void setFavoritePoints(int value) {
        throwNotChangeable("FavoritePoints");
    }

    @Override
    public void setHasHint(boolean hasHint) {
        throwNotChangeable("HasHint");
    }


    @Override
    public void setShortDescription(Database database, String value) {
        throwNotChangeable("ShortDescription");
    }

    @Override
    public void setLongDescription(Database database, String value) {
        throwNotChangeable("LongDescription");
    }

    @Override
    public void setHint(Database database, String hint) {
        throwNotChangeable("Hint");
    }

    @Override
    public void setListingChanged(boolean listingChanged) {
        throwNotChangeable("ListingChanged");
    }

    @Override
    public void setLatitude(double latitude) {
        throwNotChangeable("Latitude");
    }

    @Override
    public void setLongitude(double longitude) {
        throwNotChangeable("Longitude");
    }


    @Override
    public void setUrl(String value) {
        throwNotChangeable("Url");
    }

    //################################################################################
    //# properties that not retained at the class but read/write directly from/to DB
    ///###############################################################################


    @Override
    public Array<Attributes> getAttributes(Database database) {
        SQLiteGdxDatabaseCursor cursor = database.rawQuery("SELECT * FROM Attributes WHERE Id=?", new String[]{String.valueOf(this.id)});
        DLong attributesPositive;
        DLong attributesNegative;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isNull(1)) {
                attributesPositive = new DLong(cursor.getLong(3), cursor.getLong(1));
                attributesNegative = new DLong(cursor.getLong(4), cursor.getLong(2));
            } else {
                attributesPositive = new DLong(0, 0);
                attributesNegative = new DLong(0, 0);
            }
            cursor.close();
        } else {
            attributesPositive = new DLong(0, 0);
            attributesNegative = new DLong(0, 0);
        }
        return Attributes.getAttributes(attributesPositive, attributesNegative);
    }

    @Override
    public String getLongDescription(Database database) {
        return getStringFromDB(database, "SELECT Description FROM CacheText WHERE Id=?");
    }


    @Override
    public String getShortDescription(Database database) {
        return getStringFromDB(database, "SELECT ShortDescription FROM CacheText WHERE Id=?");
    }

    @Override
    public String getHint(Database database) {
        return getStringFromDB(database, "SELECT Hint FROM CacheText WHERE Id=?");
    }

    @Override
    public String getUrl(Database database) {
        return getStringFromDB(database, "SELECT Url FROM CacheText WHERE Id=?");
    }

    private String getStringFromDB(Database database, String statement) {
        SQLiteGdxDatabaseCursor cursor = database.rawQuery(statement, new String[]{String.valueOf(this.id)});
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    return cursor.getString(0);
                }
            }
            return "";
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @Override
    public void setFound(boolean found) {
        //write direct to DB
        int newBooleanStore = ImmutableCache.setMaskValue(ImmutableCache.MASK_FOUND, found, booleanStore);
        DaoFactory.CACHE_DAO.writeCacheBooleanStore(Database.Data, newBooleanStore, getId());
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
    public Date getDateHidden() {
        return null;
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
    public void setTourName(String value) {

    }

    @Override
    public String getTourName() {
        return null;
    }

    @Override
    public boolean isAttributePositiveSet(Attributes attribute) {
        throw new RuntimeException("not Stored on ImmutableCache.class");
    }

    @Override
    public boolean isAttributeNegativeSet(Attributes attribute) {
        throw new RuntimeException("not Stored on ImmutableCache.class");
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
    public boolean isMutable() {
        return false;
    }

    @Override
    public MutableCache getMutable(Database database) {
        return new MutableCache(database, this);
    }


    @Override
    public short getBooleanStore() {
        return this.booleanStore;
    }


}
