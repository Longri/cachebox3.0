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
import de.longri.cachebox3.gui.utils.CharSequenceArray;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.gdx.sqlite.GdxSqliteCursor;

import java.util.Date;

/**
 * Created by Longri on 23.10.2017.
 */
public class MutableCache extends AbstractCache {

    // ########################################################
    // Boolean Handling
    // one Boolean use up to 4 Bytes
    // Boolean data type represents one bit of information, but its "size" isn't something that's precisely defined. (Oracle Docs)
    //
    // so we use one Short for Store all Boolean and Use a BitMask
    // ########################################################

    // Masks
    private final static short MASK_HAS_HINT = 1 << 0;
    private final static short MASK_CORECTED_COORDS = 1 << 1; //   2
    public final static short MASK_ARCHIVED = 1 << 2;        //   4
    public final static short MASK_AVAILABLE = 1 << 3;       //   8
    public final static short MASK_FAVORITE = 1 << 4;        //  16
    public final static short MASK_FOUND = 1 << 5;           //  32
    private final static short MASK_IS_LIVE = 1 << 6;         //  64
    private final static short MASK_SOLVER1CHANGED = 1 << 7;  // 128
    public final static short MASK_HAS_USER_DATA = 1 << 8;   // 256
    public final static short MASK_LISTING_CHANGED = 1 << 9; // 512
    public final static short MASK_SHOW_ORIGINAL_HTML_COLOR = 1 << 10; // 1024

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

    public final static byte NOT_LIVE = 0;
    public final static byte IS_LITE = 1;
    public final static byte IS_FULL = 2;

    private short booleanStore;

    private Array<Attributes> attributes;
    private CharSequence name, gcCode, placedBy, owner, gcId;
    private short rating, numTravelbugs;
    private int favPoints;
    private long id;
    private CacheTypes type;
    private CacheSizes size;
    private float difficulty, terrain;


    private Array<AbstractWaypoint> waypoints = new Array<>();
    private CharSequence longDescription;
    private CharSequence shortDescription;
    private CharSequence hint;
    private Date dateHidden;
    private DLong attributesNegative;
    private DLong attributesPositive;
    private CharSequence country;
    private CharSequence url;
    private byte apiState;
    private CharSequence state;
    private CharSequence note;
    private CharSequence solver;
    private CharSequence tourName;
    private long gpxFilenameId;


    public MutableCache(double latitude, double longitude) {
        super(latitude, longitude);
        type = CacheTypes.Undefined;
    }

    public MutableCache(AbstractCache cache) {
        super(cache.getLatitude(), cache.getLongitude());
        this.latitude = cache.getLatitude();
        this.longitude = cache.getLongitude();
        this.attributes = null;
        this.attributesPositive = cache.getAttributesPositive();
        this.attributesNegative = cache.getAttributesNegative();
        if (cache.getName() != null) this.name = cache.getName().toString();
        if (cache.getGcCode() != null) this.gcCode = cache.getGcCode().toString();
        if (cache.getPlacedBy() != null) this.placedBy = cache.getPlacedBy().toString();
        if (cache.getOwner() != null) this.owner = cache.getOwner().toString();
        if (cache.getGcId() != null) this.gcId = cache.getGcId().toString();
        this.rating = (short) (cache.getRating() * 2);
        this.favPoints = cache.getFavoritePoints();
        this.id = cache.getId();
        this.type = cache.getType();
        this.size = cache.getSize();
        this.difficulty = cache.getDifficulty();
        this.terrain = cache.getTerrain();
        this.setMaskValue(MASK_HAS_HINT, cache.hasHint());
        this.setMaskValue(MASK_ARCHIVED, cache.isArchived());
        this.setMaskValue(MASK_AVAILABLE, cache.isAvailable());
        this.setMaskValue(MASK_FAVORITE, cache.isFavorite());
        this.setMaskValue(MASK_FOUND, cache.isFound());
        this.setMaskValue(MASK_HAS_USER_DATA, cache.isHasUserData());
        this.setMaskValue(MASK_LISTING_CHANGED, cache.isListingChanged());
        this.waypoints = cache.getWaypoints();
        this.setMaskValue(MASK_CORECTED_COORDS, cache.hasCorrectedCoordinates());

        this.longDescription = cache.getLongDescription();
        this.shortDescription = cache.getShortDescription();
        this.hint = cache.getHint();
        this.url = cache.getUrl();
        this.dateHidden = cache.getDateHidden();
        this.state = cache.getState();
        this.country = cache.getCountry();
        this.apiState = cache.getApiState();
        this.note = cache.getTmpNote();
        this.solver = cache.getTmpSolver();
    }

    public MutableCache(double latitude, double longitude, String name, CacheTypes type, String gcCode) {
        super(latitude, longitude);
        this.id = 0;
        this.size = CacheSizes.regular;
        this.difficulty = 0.0f;
        this.terrain = 0.0f;
        this.type = type;
        this.rating = 0;
        this.numTravelbugs = 0;
        this.gcCode = gcCode;
        this.name = name;
        this.placedBy = "";
        this.owner = "";
        this.gcId = "";
        this.favPoints = 0;
    }

    public MutableCache(GdxSqliteCursor cursor) {
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

    public MutableCache(Object[] values) {
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

    @Override
    public void setInfo(GdxSqliteCursor cursor) {
        // cursor include

//        Id              BIGINT        PRIMARY KEY
//        DateHidden      DATETIME,
//        FirstImported   DATETIME,
//        TourName        NCHAR (255),
//        GPXFilename_Id  BIGINT,
//        ListingCheckSum INT           DEFAULT 0,
//        state           NVARCHAR (50),
//        country         NVARCHAR (50),
//        ApiStatus       SMALLINT      DEFAULT 0

        this.dateHidden = Database.getDateFromDataBaseString(cursor.getString(1));
        this.tourName = new CharSequenceArray(cursor.getString(3));
        this.gpxFilenameId = cursor.getLong(4);
        this.state = new CharSequenceArray(cursor.getString(6));
        this.country = new CharSequenceArray(cursor.getString(7));
        this.apiState = cursor.getByte(8);

    }

    @Override
    public void setText(GdxSqliteCursor cursor) {
        // cursor include
//        Id               BIGINT         PRIMARY KEY
//        Url              NVARCHAR (255),
//        Hint             NTEXT,
//        Description      NTEXT,
//        Notes            NTEXT,
//        Solver           NTEXT,
//        ShortDescription NTEXT

        this.url = new CharSequenceArray(cursor.getString(1));
        this.hint = new CharSequenceArray(cursor.getString(2));
        this.longDescription = new CharSequenceArray(cursor.getString(3));
        this.note = new CharSequenceArray(cursor.getString(4));
        this.solver = new CharSequenceArray(cursor.getString(5));
        this.shortDescription = new CharSequenceArray(cursor.getString(6));
    }

    @Override
    public void setAttributes(GdxSqliteCursor cursor) {
        // cursor include
//        Id                     BIGINT PRIMARY KEY
//        AttributesPositive     BIGINT,
//        AttributesNegative     BIGINT,
//        AttributesPositiveHigh BIGINT DEFAULT 0,
//        AttributesNegativeHigh BIGINT DEFAULT 0
        this.setAttributesPositive(new DLong(cursor.getLong(3), cursor.getLong(1)));
        this.setAttributesNegative(new DLong(cursor.getLong(4), cursor.getLong(2)));
    }

    @Override
    public void updateBooleanStore(Database database) {
        DaoFactory.CACHE_DAO.writeCacheBooleanStore(database, booleanStore, getId());
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public Array<Attributes> getAttributes() {
        if (this.attributes == null) {
            if (attributesPositive == null || attributesNegative == null) {
                GdxSqliteCursor cursor = Database.Data.rawQuery("SELECT * from Attributes WHERE Id=?", new String[]{String.valueOf(this.getId())});
                if (cursor != null) {
                    cursor.moveToFirst();
                    setAttributes(cursor);
                }
                cursor.close();
            }
            this.attributes = Attributes.getAttributes(attributesPositive, attributesNegative);
        }
        return this.attributes;
    }

    @Override
    public boolean ImTheOwner() {
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
    public int compareTo(AbstractCache c2) {
        return 0;
    }

    @Override
    protected AbstractWaypoint findWaypointByGc(String gc) {
        return null;
    }

    @Override
    public CharSequence getGcCode() {
        return gcCode;
    }

    @Override
    public void setGcCode(String gcCode) {
        this.gcCode = gcCode;
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public CharSequence getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public CharSequence getGcId() {
        return gcId;
    }

    @Override
    public void setGcId(String gcId) {
        this.gcId = gcId;
    }

    @Override
    public CharSequence getHint() {
        return this.hint;
    }

    @Override
    public void setHint(CharSequence hint) {
        this.hint = hint;
    }

    @Override
    public long getGPXFilename_ID() {
        return this.gpxFilenameId;
    }

    @Override
    public void setGPXFilename_ID(long gpxFilenameId) {
        this.gpxFilenameId = gpxFilenameId;
    }

    @Override
    public boolean hasHint() {
        return this.getMaskValue(MASK_HAS_HINT);
    }

    @Override
    public void setHasHint(boolean hasHint) {
        this.setMaskValue(MASK_HAS_HINT, hasHint);
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
    public boolean isArchived() {
        return this.getMaskValue(MASK_ARCHIVED);
    }

    @Override
    public void setArchived(boolean archived) {
        this.setMaskValue(MASK_ARCHIVED, archived);
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
    public boolean isFavorite() {
        return this.getMaskValue(MASK_FAVORITE);
    }

    @Override
    public void setFavorite(boolean favorite) {
        setMaskValue(MASK_FAVORITE, favorite);
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
        return terrain;
    }

    @Override
    public void setTerrain(float terrain) {
        this.terrain = terrain;
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
    public boolean isLive() {
        return false;
    }

    @Override
    public void setLive(boolean isLive) {

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
    public boolean isListingChanged() {
        return this.getMaskValue(MASK_LISTING_CHANGED);
    }

    @Override
    public void setListingChanged(boolean listingChanged) {
        setMaskValue(MASK_LISTING_CHANGED, listingChanged);
    }


    @Override
    public CharSequence getPlacedBy() {
        return placedBy;
    }

    @Override
    public void setPlacedBy(String placedBy) {
        this.placedBy = placedBy;
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
    public byte getApiState() {
        return this.apiState;
    }

    @Override
    public void setApiState(byte value) {
        this.apiState = value;
    }

    @Override
    public int getNoteChecksum() {
        return 0;
    }

    @Override
    public void setNoteChecksum(int value) {

    }

    @Override
    public CharSequence getTmpNote() {
        return this.note;
    }

    @Override
    public void setTmpNote(CharSequence value) {
        this.note = value;
    }

    @Override
    public int getSolverChecksum() {
        return 0;
    }

    @Override
    public void setSolverChecksum(int value) {

    }

    @Override
    public CharSequence getTmpSolver() {
        return this.solver;
    }

    @Override
    public void setTmpSolver(CharSequence value) {
        this.solver = value;
    }

    @Override
    public CharSequence getUrl() {
        return this.url;
    }

    @Override
    public void setUrl(CharSequence value) {
        this.url = value;
    }

    @Override
    public CharSequence getCountry() {
        return this.country;
    }

    @Override
    public void setCountry(CharSequence value) {
        this.country = value;
    }

    @Override
    public CharSequence getState() {
        return this.state;
    }

    @Override
    public void setState(CharSequence value) {
        this.state = value;
    }

    public void addAttributeNegative(Attributes attribute) {
        if (attributesNegative == null)
            attributesNegative = new DLong(0, 0);
        attributesNegative.BitOr(Attributes.GetAttributeDlong(attribute));
    }

    public void addAttributePositive(Attributes attribute) {
        if (attributesPositive == null)
            attributesPositive = new DLong(0, 0);
        attributesPositive.BitOr(Attributes.GetAttributeDlong(attribute));
    }

    @Override
    public DLong getAttributesPositive() {
        return attributesPositive;
    }

    @Override
    public DLong getAttributesNegative() {
        return attributesNegative;
    }

    @Override
    public void setAttributesPositive(DLong dLong) {
        attributesPositive = dLong;
    }

    @Override
    public void setAttributesNegative(DLong dLong) {
        attributesNegative = dLong;
    }

    @Override
    public void setLongDescription(CharSequence value) {
        this.longDescription = value;
    }

    @Override
    public CharSequence getLongDescription() {
        return this.longDescription;
    }

    @Override
    public void setShortDescription(CharSequence value) {
        this.shortDescription = value;
    }

    @Override
    public CharSequence getShortDescription() {
        return this.shortDescription;
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
    public void setFavoritePoints(int value) {
        if (this.favPoints != value)
            isChanged.set(true);
        this.favPoints = value;
    }


    @Override
    public int getFavoritePoints() {
        return this.favPoints;
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
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public float getRating() {
        return rating / 2.0f;
    }

    @Override
    public void setRating(short rating) {
        this.rating = (short) (rating * 2);
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
    public CacheTypes getType() {
        return this.type;
    }

    @Override
    public void setType(CacheTypes type) {
        this.type = type;
    }

    @Override
    public int getNumTravelbugs() {
        return numTravelbugs;
    }

    @Override
    public void setNumTravelbugs(short numTravelbugs) {
        if (this.numTravelbugs != numTravelbugs)
            isChanged.set(true);
        this.numTravelbugs = numTravelbugs;
    }

    @Override
    public void setShowOriginalHtmlColor(boolean value) {
        this.setMaskValue(MASK_SHOW_ORIGINAL_HTML_COLOR, value);
    }

    @Override
    public boolean getShowOriginalHtmlColor() {
        return this.getMaskValue(MASK_SHOW_ORIGINAL_HTML_COLOR);
    }

    @Override
    public void dispose() {

    }


    @Override
    public AbstractCache getCopy() {
        return new MutableCache(this);
    }

    @Override
    public void setAttributes(Array<Attributes> attributes) {
        this.attributes = attributes;

        if (attributesNegative == null) {
            attributesNegative = new DLong(0, 0);
        } else {
            attributesNegative.reset();
        }

        if (attributesPositive == null) {
            attributesPositive = new DLong(0, 0);
        } else {
            attributesPositive.reset();
        }


        int n = attributes.size;
        while (n-- > 0) {
            Attributes a = attributes.get(n);
            if (a.isNegative()) {
                attributesNegative.BitOr(Attributes.GetAttributeDlong(a));
            } else {
                attributesPositive.BitOr(Attributes.GetAttributeDlong(a));
            }
        }
    }

    @Override
    public void setLatLon(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public short getBooleanStore() {
        return booleanStore;
    }

    public void reset() {

    }

    private boolean getMaskValue(short mask) {
        return (booleanStore & mask) == mask;
    }

    private void setMaskValue(short mask, boolean value) {
        if (getMaskValue(mask) == value) return;

        if (value) {
            booleanStore |= mask;
        } else {
            booleanStore &= ~mask;
        }

    }

}
