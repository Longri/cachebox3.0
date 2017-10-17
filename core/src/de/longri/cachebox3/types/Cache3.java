package de.longri.cachebox3.types;

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

    private final static Logger log = LoggerFactory.getLogger(Cache3.class);


    public Cache3(double latitude, double longitude) {
        super(latitude, longitude);
    }

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
    public CharSequence getName() {
        return null;
    }

    @Override
    public void setName(String name) {

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
