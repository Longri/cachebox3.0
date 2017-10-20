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


import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.CacheDAO;
import de.longri.cachebox3.sqlite.dao.WaypointDAO;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.lists.CB_List;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Cache extends AbstractCache implements Comparable<AbstractCache>, Serializable {
    private static final long serialVersionUID = 1015307624242318838L;
    // ########################################################
    // Boolean Handling
    // one Boolean use up to 4 Bytes
    // Boolean data type represents one bit of information, but its "size" isn't something that's precisely defined. (Oracle Docs)
    //
    // so we use one Short for Store all Boolean and Use a BitMask
    // ########################################################

    // Masks
    private final static short MASK_HAS_HINT = 1 << 0;
    private final static short MASK_CORECTED_COORDS = 1 << 1;
    private final static short MASK_ARCHIVED = 1 << 2;
    private final static short MASK_AVAILABLE = 1 << 3;
    private final static short MASK_FAVORITE = 1 << 4;
    private final static short MASK_FOUND = 1 << 5;
    private final static short MASK_IS_LIVE = 1 << 6;
    private final static short MASK_SOLVER1CHANGED = 1 << 7;
    private final static short MASK_HAS_USER_DATA = 1 << 8;
    private final static short MASK_LISTING_CHANGED = 1 << 9;


    protected static final Charset US_ASCII = Charset.forName("US-ASCII");
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String EMPTY_STRING = "";
    private static String gcLogin = null;

    /**
     * Waypoint Code des Caches
     */
    private byte[] GcCode;
    /**
     * name des Caches
     */
    private byte[] Name;

    private byte[] GcId;

    /**
     * Bin ich der Owner? </br>
     * -1 noch nicht getestet </br>
     * 1 ja </br>
     * 0 nein
     */
    private int myCache = -1;
    private boolean isSearchVisible = true;
    private boolean isDisposed = false;
    /**
     * When Solver1 changes -> this flag must be set. When Solver 2 will be opend and this flag is set -> Solver 2 must reload the content
     * from DB to get the changes from Solver 1
     */
    private boolean solver1Changed = false;
    private short BitFlags = 0;
    /**
     * Stored Difficulty and Terrain<br>
     * <br>
     * First four bits for Difficulty<br>
     * Last four bits for Terrain
     */
    private byte DifficultyTerrain = 0;
    /**
     * Verantwortlicher
     */
    private byte[] Owner;
    private CacheDetail detail = null;
    private long Id;
//    /**
//     * Die Coordinate, an der der Cache liegt.
//     */
//    public Coordinate Pos = new Coordinate(0, 0);

    private float Rating;
    private CacheSizes Size;

    // /**
    // * hat der Cache Clues oder Notizen erfasst
    // */
    // public boolean hasUserData;

    private CacheTypes Type = CacheTypes.Undefined;

    // /**
    // * Das Listing hat sich geaendert!
    // */
    // public boolean listingChanged = false;

    private int NumTravelbugs = 0;

    private int favoritePoints = 0;

    private float cachedDistance = 0;

    private Array<Waypoint> waypoints = null;

	/*
     * Constructors
	 */

    /**
     * Constructor
     */
    public Cache(double lat, double lon, boolean withDetails) {
        super(lat, lon);
        this.setNumTravelbugs(0);
        this.setDifficulty(0);
        this.setTerrain(0);
        this.setSize(CacheSizes.other);
        this.setAvailable(true);
        setWaypoints(new CB_List<Waypoint>());
        if (withDetails) {
            setDetail(new CacheDetail());
        }
    }

    /**
     * Constructor
     */
    public Cache(double Latitude, double Longitude, String Name, CacheTypes type, String GcCode) {
        super(Latitude, Longitude);
        this.setName(Name);
        this.setType(type);
        this.setGcCode(GcCode);
        this.setNumTravelbugs(0);
        this.setDifficulty(0);
        this.setTerrain(0);
        this.setSize(CacheSizes.other);
        this.setAvailable(true);
        setWaypoints(new CB_List<Waypoint>());
    }

    /**
     * Copy constructor
     *
     * @param Latitude
     * @param Longitude
     * @param other
     */
    public Cache(double Latitude, double Longitude, Cache other) {
        super(Latitude, Longitude);
        gcLogin = other.gcLogin;
        GcCode = other.GcCode;
        Name = other.Name;
        GcId = other.GcId;
        myCache = other.myCache;
        isSearchVisible = other.isSearchVisible;
        isDisposed = other.isDisposed;
        solver1Changed = other.solver1Changed;
        BitFlags = other.BitFlags;
        DifficultyTerrain = other.DifficultyTerrain;
        Owner = other.Owner;
        setDetail(other.getDetail());
        setId(other.getId());
        setRating(other.getRating());
        setSize(other.getSize());
        setType(other.getType());
        setNumTravelbugs(other.getNumTravelbugs());
        setFavoritePoints(other.getFavoritePoints());
        setCachedDistance(other.getCachedDistance());
        setWaypoints(other.getWaypoints());
    }

    /**
     * Delete Detail Information to save memory
     */
    public void deleteDetail(boolean showAllWaypoints) {
        if (this.getDetail() == null)
            return;
        this.getDetail().dispose();
        this.setDetail(null);
        // remove all Detail Information from Waypoints
        // remove all Waypoints != Start and Final
        if ((getWaypoints() != null) && (!showAllWaypoints)) {
            for (int i = 0; i < getWaypoints().size; i++) {
                Waypoint wp = getWaypoints().get(i);
                if (wp.isStart() || wp.getType() == CacheTypes.Final) {

                    if (wp.getDetail() != null)
                        wp.getDetail().dispose();
                    wp.setDetail(null);
                } else {
                    if (wp.getDetail() != null) {
                        wp.getDetail().dispose();
                        wp.setDetail(null);
                    }
                    getWaypoints().removeIndex(i);
                    i--;
                }
            }
        }
    }

    public boolean isDetailLoaded() {
        return (getDetail() != null);
    }

    /**
     * Load Detail Information from DB
     */
    public void loadDetail() {
        CacheDAO dao = new CacheDAO();
        dao.readDetail(this);
        // load all Waypoints with full Details
        WaypointDAO wdao = new WaypointDAO();
        Array<Waypoint> wpts = wdao.getWaypointsFromCacheID(getId(), true);
        for (int i = 0; i < wpts.size; i++) {
            Waypoint wp = wpts.get(i);
            boolean found = false;
            for (int j = 0; j < getWaypoints().size; j++) {
                Waypoint wp2 = getWaypoints().get(j);
                if (wp.getGcCode().equals(wp2.getGcCode())) {
                    found = true;
                    wp2.setDetail(wp.getDetail()); // copy Detail Info
                    break;
                }
            }
            if (!found) {
                // Waypoint not in List
                // add Waypoint to List
                getWaypoints().add(wp);
            }
        }
    }

    @Override
    public void deleteDetail(Boolean value) {
        //TODO
    }

    @Override
    public boolean ImTheOwner() {
        String userName = Config.GcLogin.getValue().toLowerCase(Locale.getDefault());
        if (myCache == 0)
            return false;
        if (myCache == 1)
            return true;

        if (gcLogin == null) {
            gcLogin = userName;
        }

        boolean ret = false;

        try {
            ret = this.getOwner().toString().toLowerCase(Locale.getDefault()).equals(gcLogin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        myCache = ret ? 1 : 0;
        return ret;
    }

	/*
     * Getter/Setter
	 */

    @Override
    public boolean CorrectedCoordiantesOrMysterySolved() {
        if (this.hasCorrectedCoordinates())
            return true;

        if (this.getType() != CacheTypes.Mystery)
            return false;

        if (this.getWaypoints() == null || this.getWaypoints().size == 0)
            return false;

        boolean x;
        x = false;

        for (int i = 0, n = getWaypoints().size; i < n; i++) {
            Waypoint wp = getWaypoints().get(i);
            if (wp.getType() == CacheTypes.Final) {
                if (!(wp.latitude == 0 && wp.longitude == 0))
                    x = true;
            }
        }
        ;
        return x;
    }

    @Override
    public boolean HasFinalWaypoint() {
        return GetFinalWaypoint() != null;
    }

    @Override
    public Waypoint GetFinalWaypoint() {
        if (this.getType() != CacheTypes.Mystery)
            return null;
        if (getWaypoints() == null || getWaypoints().size == 0)
            return null;

        for (int i = 0, n = getWaypoints().size; i < n; i++) {
            Waypoint wp = getWaypoints().get(i);
            if (wp.getType() == CacheTypes.Final) {
                // do not activate final waypoint with invalid coordinates
                if (!wp.isValid() || wp.isZero())
                    continue;
                return wp;
            }
        }
        ;

        return null;
    }

    @Override
    public boolean HasStartWaypoint() {
        return GetStartWaypoint() != null;
    }

    @Override
    public Waypoint GetStartWaypoint() {
        if ((this.getType() != CacheTypes.Multi) && (this.getType() != CacheTypes.Mystery))
            return null;

        if (getWaypoints() == null || getWaypoints().size == 0)
            return null;

        for (int i = 0, n = getWaypoints().size; i < n; i++) {
            Waypoint wp = getWaypoints().get(i);
            if ((wp.getType() == CacheTypes.MultiStage) && (wp.isStart())) {
                return wp;
            }
        }
        return null;
    }

//	/**
//	 * Returns a List of Spoiler Ressources
//	 *
//	 * @return ArrayList of String
//	 */
//	public CB_List<ImageEntry> getSpoilerRessources() {
//		if (detail != null) {
//			return detail.getSpoilerRessources(this);
//		} else {
//			return null;
//		}
//	}
//
//	/**
//	 * Set a List of Spoiler Ressources
//	 *
//	 * @param value
//	 *            ArrayList of String
//	 */
//	public void setSpoilerRessources(CB_List<ImageEntry> value) {
//		if (detail != null) {
//			detail.setSpoilerRessources(value);
//		}
//	}

    @Override
    public boolean hasSpoiler() {
        if (getDetail() != null) {
            boolean hasSpoiler = getDetail().hasSpoiler(this);
            return hasSpoiler;
        } else {
            return false;
        }
    }


    @Override
    public float Distance(MathUtils.CalculationType type, boolean useFinal) {
        return Distance(type, useFinal, EventHandler.getMyPosition());
    }

    @Override
    float Distance(MathUtils.CalculationType type, boolean useFinal, Coordinate fromPos) {
        if (isDisposed)
            return 0;

        if (fromPos == null)
            return -1;

        Waypoint waypoint = null;
        if (useFinal)
            waypoint = this.GetFinalWaypoint();
        // Wenn ein Mystery-Cache einen Final-Waypoint hat, soll die
        // Diszanzberechnung vom Final aus gemacht werden
        // If a mystery has a final waypoint, the distance will be calculated to
        // the final not the the cache coordinates
        Coordinate toPos = this;
        if (waypoint != null) {
            toPos = new Coordinate(waypoint.latitude, waypoint.longitude);
            // nur sinnvolles Final, sonst vom Cache
            if (waypoint.latitude == 0 && waypoint.longitude == 0)
                toPos = this;
        }
        float[] dist = new float[4];
        MathUtils.computeDistanceAndBearing(type, fromPos.getLatitude(), fromPos.getLongitude(), toPos.getLatitude(), toPos.getLongitude(), dist);
        setCachedDistance(dist[0]);
        return getCachedDistance();
    }

	/*
     * Overrides
	 */

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Cache))
            return false;
        Cache other = (Cache) obj;

        if (Arrays.equals(this.GcCode, other.GcCode))
            return true;

        return false;
    }

    @Override
    public int compareTo(AbstractCache c2) {
        float dist1 = this.getCachedDistance();
        float dist2 = c2.getCachedDistance();
        return (dist1 < dist2 ? -1 : (dist1 == dist2 ? 0 : 1));
    }

    public void setSearchVisible(boolean value) {
        isSearchVisible = value;
    }

    public boolean isSearchVisible() {
        return isSearchVisible;
    }

    @Override
    protected Waypoint findWaypointByGc(String gc) {
        if (isDisposed)
            return null;
        for (int i = 0, n = getWaypoints().size; i < n; i++) {
            Waypoint wp = getWaypoints().get(i);
            if (wp.getGcCode().equals(gc)) {
                return wp;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Cache:" + getGcCode() + " " + super.toString();
    }

    public void dispose() {
        isDisposed = true;

        if (getDetail() != null)
            getDetail().dispose();
        setDetail(null);

        GcCode = null;
        Name = null;
        setSize(null);
        setType(null);
        Owner = null;

        if (getWaypoints() != null) {
            for (int i = 0, n = getWaypoints().size; i < n; i++) {
                Waypoint entry = getWaypoints().get(i);
                entry.dispose();
            }

            getWaypoints().clear();
            setWaypoints(null);
        }
        Owner = null;

    }

    public void setSolver1Changed(boolean b) {
        this.solver1Changed = b;
    }

    public boolean getSolver1Changed() {
        return solver1Changed;
    }

    @Override
    public CharSequence getGcCode() {
        if (GcCode == null)
            return EMPTY_STRING;
        return new String(GcCode, US_ASCII);
    }

    @Override
    public void setGcCode(String gcCode) {
        if (gcCode == null) {
            GcCode = null;
            return;
        }
        GcCode = gcCode.getBytes(US_ASCII);
    }

    @Override
    public CharSequence getName() {
        if (Name == null)
            return EMPTY_STRING;
        return new String(Name, UTF_8);
    }

    @Override
    public void setName(String name) {
        if (name == null) {
            Name = null;
            return;
        }
        Name = name.getBytes(UTF_8);
    }

    @Override
    public CharSequence getOwner() {
        if (Owner == null)
            return EMPTY_STRING;
        return new String(Owner, UTF_8);
    }

    @Override
    public void setOwner(String owner) {
        if (owner == null) {
            Owner = null;
            return;
        }
        Owner = owner.getBytes(UTF_8);
    }

    @Override
    public CharSequence getGcId() {
        if (GcId == null)
            return EMPTY_STRING;
        return new String(GcId, UTF_8);
    }

    @Override
    public void setGcId(String gcId) {

        if (gcId == null) {
            GcId = null;
            return;
        }
        GcId = gcId.trim().getBytes(UTF_8);
    }

    @Override
    public String getHint() {
        if (getDetail() != null) {
            return getDetail().getHint();
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setHint(String hint) {
        if (getDetail() != null) {
            getDetail().setHint(hint);
        }
    }

    @Override
    public long getGPXFilename_ID() {
        if (getDetail() != null) {
            return getDetail().GPXFilename_ID;
        }
        return 0;
    }

    @Override
    public void setGPXFilename_ID(long gpxFilenameId) {
        if (getDetail() != null) {
            getDetail().GPXFilename_ID = gpxFilenameId;
        }

    }

    @Override
    public boolean hasHint() {
        if (getDetail() != null) {
            return getDetail().getHint().length() > 0;
        } else {
            return false;
        }
    }

    private boolean getMaskValue(short mask) {
        return (BitFlags & mask) == mask;
    }

    private void setMaskValue(short mask, boolean value) {
        if (getMaskValue(mask) == value)
            return;

        if (value) {
            BitFlags |= mask;
        } else {
            BitFlags &= ~mask;
        }

    }

    // Getter and Setter over Mask

    @Override
    public boolean hasCorrectedCoordinates() {
        return this.getMaskValue(MASK_CORECTED_COORDS);
    }

    @Override
    public void setCorrectedCoordinates(boolean correctedCoordinates) {
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
        this.setMaskValue(MASK_FAVORITE, favorite);
    }

    @Override
    public float getDifficulty() {
        return getFloatX_5FromByte((byte) (DifficultyTerrain & 15));
    }

    @Override
    public void setDifficulty(float difficulty) {
        DifficultyTerrain = (byte) (DifficultyTerrain & (byte) 240);// clear Bits
        DifficultyTerrain = (byte) (DifficultyTerrain | getDT_HalfByte(difficulty));
    }

    @Override
    public float getTerrain() {
        return getFloatX_5FromByte((byte) (DifficultyTerrain >>> 4));
    }

    @Override
    public void setTerrain(float terrain) {
        DifficultyTerrain = (byte) (DifficultyTerrain & (byte) 15);// clear Bits
        DifficultyTerrain = (byte) (DifficultyTerrain | getDT_HalfByte(terrain) << 4);
    }

    private byte getDT_HalfByte(float value) {
        if (value == 1f)
            return (byte) 0;
        if (value == 1.5f)
            return (byte) 1;
        if (value == 2f)
            return (byte) 2;
        if (value == 2.5f)
            return (byte) 3;
        if (value == 3f)
            return (byte) 4;
        if (value == 3.5f)
            return (byte) 5;
        if (value == 4f)
            return (byte) 6;
        if (value == 4.5f)
            return (byte) 7;
        return (byte) 8;
    }

    private float getFloatX_5FromByte(byte value) {
        switch (value) {
            case 0:
                return 1f;
            case 1:
                return 1.5f;
            case 2:
                return 2f;
            case 3:
                return 2.5f;
            case 4:
                return 3f;
            case 5:
                return 3.5f;
            case 6:
                return 4f;
            case 7:
                return 4.5f;
        }
        return 5f;
    }

    @Override
    public boolean isFound() {
        return this.getMaskValue(MASK_FOUND);
    }

    @Override
    public void setFound(boolean found) {
        this.setMaskValue(MASK_FOUND, found);
    }

    @Override
    public boolean isLive() {
        return this.getMaskValue(MASK_IS_LIVE);
    }

    @Override
    public void setLive(boolean isLive) {
        this.setMaskValue(MASK_IS_LIVE, isLive);
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
        this.setMaskValue(MASK_LISTING_CHANGED, listingChanged);
    }

    @Override
    public CharSequence getPlacedBy() {
        if (getDetail() != null) {
            return getDetail().PlacedBy;
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setPlacedBy(String value) {
        if (getDetail() != null) {
            getDetail().PlacedBy = value;
        }
    }

    @Override
    public Date getDateHidden() {
        if (getDetail() != null) {
            return getDetail().DateHidden;
        } else {
            return null;
        }
    }

    @Override
    public void setDateHidden(Date date) {
        if (getDetail() != null) {
            getDetail().DateHidden = date;
        }
    }

    public final static byte NOTLIVE = 0;
    public final static byte ISLITE = 1;
    public final static byte NOTLITE = 2;

    @Override
    public byte getApiState() {
        if (getDetail() != null) {
            return getDetail().apiState;
        } else {
            return NOTLIVE;
        }
    }

    @Override
    public void setApiState(byte value) {
        if (getDetail() != null) {
            getDetail().apiState = value;
        }
    }

    @Override
    public int getNoteChecksum() {
        if (getDetail() != null) {
            return getDetail().noteCheckSum;
        } else {
            return 0;
        }
    }

    @Override
    public void setNoteChecksum(int value) {
        if (getDetail() != null) {
            getDetail().noteCheckSum = value;
        }
    }

    @Override
    public String getTmpNote() {
        if (getDetail() != null) {
            return getDetail().tmpNote;
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setTmpNote(String value) {
        if (getDetail() != null) {
            getDetail().tmpNote = value;
        }
    }

    @Override
    public int getSolverChecksum() {
        if (getDetail() != null) {
            return getDetail().solverCheckSum;
        } else {
            return 0;
        }
    }

    @Override
    public void setSolverChecksum(int value) {
        if (getDetail() != null) {
            getDetail().solverCheckSum = value;
        }
    }

    @Override
    public String getTmpSolver() {
        if (getDetail() != null) {
            return getDetail().tmpSolver;
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setTmpSolver(String value) {
        if (getDetail() != null) {
            getDetail().tmpSolver = value;
        }
    }

    @Override
    public String getUrl() {
        if (getDetail() != null) {
            return getDetail().Url;
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setUrl(String value) {
        if (getDetail() != null) {
            getDetail().Url = value;
        }
    }

    @Override
    public String getCountry() {
        if (getDetail() != null) {
            return getDetail().Country;
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setCountry(String value) {
        if (getDetail() != null) {
            getDetail().Country = value;
        }
    }

    @Override
    public String getState() {
        if (getDetail() != null) {
            return getDetail().State;
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setState(String value) {
        if (getDetail() != null) {
            getDetail().State = value;
        }
    }

    @Override
    public Array<Attributes> getAttributes(Database database) {
        if (getDetail() != null) {
            return getDetail().getAttributes(getId());
        } else {
            return null;
        }
    }

    @Override
    public void addAttributeNegative(Attributes attribute) {
        if (getDetail() != null) {
            getDetail().addAttributeNegative(attribute);
        }
    }

    @Override
    public void addAttributePositive(Attributes attribute) {
        if (getDetail() != null) {
            getDetail().addAttributePositive(attribute);
        }
    }

    @Override
    public DLong getAttributesPositive() {
        if (getDetail() != null) {
            return getDetail().getAttributesPositive(getId());
        } else {
            return null;
        }
    }

    @Override
    public DLong getAttributesNegative() {
        if (getDetail() != null) {
            return getDetail().getAttributesNegative(getId());
        } else {
            return null;
        }
    }

    @Override
    public void setAttributesPositive(DLong dLong) {
        if (getDetail() != null) {
            getDetail().setAttributesPositive(dLong);
        }
    }

    @Override
    public void setAttributesNegative(DLong dLong) {
        if (getDetail() != null) {
            getDetail().setAttributesNegative(dLong);
        }
    }

    @Override
    public void setLongDescription(String value) {
        if (getDetail() != null) {
            getDetail().setLongDescription(value);

        }
    }

    @Override
    public String getLongDescription() {
        if (getDetail() != null) {
            if (getDetail().getLongDescription() == null || getDetail().getLongDescription().length() == 0) {
                return CacheDAO.getDescription(this);
            }
            return getDetail().getLongDescription();
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setShortDescription(String value) {
        if (getDetail() != null) {
            getDetail().setShortDescription(value);
        }
    }

    @Override
    public String getShortDescription() {
        if (getDetail() != null) {
            if (getDetail().getShortDescription() == null || getDetail().getShortDescription().length() == 0) {
                return CacheDAO.GetShortDescription(this);
            }
            return getDetail().getShortDescription();
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public void setTourName(String value) {
        if (getDetail() != null) {
            getDetail().TourName = value;
        }
    }

    @Override
    public String getTourName() {
        if (getDetail() != null) {
            return getDetail().TourName;
        } else {
            return EMPTY_STRING;
        }
    }

    @Override
    public boolean isAttributePositiveSet(Attributes attribute) {
        if (getDetail() != null) {
            return getDetail().isAttributePositiveSet(attribute);
        } else {
            return false;
        }
    }

    @Override
    public boolean isAttributeNegativeSet(Attributes attribute) {
        if (getDetail() != null) {
            return getDetail().isAttributeNegativeSet(attribute);
        } else {
            return false;
        }
    }

    public boolean isDisposed() {
        return isDisposed;
    }


    @Override
    public void setFavoritePoints(int value) {
        this.favoritePoints = value;
    }

    @Override
    public int getFaviritPoints() {
        return this.getFavoritePoints();
    }

    @Override
    public int getFavoritePoints() {
        return favoritePoints;
    }

    @Override
    public Array<Waypoint> getWaypoints() {
        return waypoints;
    }

    @Override
    public void setWaypoints(Array<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    @Override
    public CacheDetail getDetail() {
        return detail;
    }

    @Override
    public void setDetail(CacheDetail detail) {
        this.detail = detail;
    }

    @Override
    public long getId() {
        return Id;
    }

    @Override
    public void setId(long id) {
        Id = id;
    }

    @Override
    public float getRating() {
        return Rating;
    }

    @Override
    public void setRating(float rating) {
        Rating = rating;
    }

    @Override
    public CacheSizes getSize() {
        return Size;
    }

    @Override
    public void setSize(CacheSizes size) {
        Size = size;
    }

    @Override
    public CacheTypes getType() {
        return Type;
    }

    @Override
    public void setType(CacheTypes type) {
        Type = type;
    }

    @Override
    public int getNumTravelbugs() {
        return NumTravelbugs;
    }

    @Override
    public void setNumTravelbugs(int numTravelbugs) {
        NumTravelbugs = numTravelbugs;
    }

    @Override
    public float getCachedDistance() {
        return cachedDistance;
    }

    @Override
    public void setCachedDistance(float cachedDistance) {
        this.cachedDistance = cachedDistance;
    }
}