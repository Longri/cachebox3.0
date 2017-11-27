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

import com.badlogic.gdx.utils.*;
import de.longri.cachebox3.CB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class FilterProperties {
    final static Logger log = LoggerFactory.getLogger(FilterProperties.class);

    private final static String SEPARATOR = ",";
    private final static String GPXSEPARATOR = "^";

    // name for debug
    private String name;

    // Properties can changed from 'General' section
    public final IntProperty NotAvailable = new IntProperty();
    public final IntProperty Archived = new IntProperty();
    public final IntProperty Finds = new IntProperty();
    public final IntProperty Own = new IntProperty();
    public final IntProperty ContainsTravelbugs = new IntProperty();
    public final IntProperty Favorites = new IntProperty();
    public final IntProperty HasUserData = new IntProperty();
    public final IntProperty ListingChanged = new IntProperty();
    public final IntProperty WithManualWaypoint = new IntProperty();
    public final IntProperty hasCorrectedCoordinates = new IntProperty();


    public final IntProperty MinDifficulty = new IntProperty();
    public final IntProperty MaxDifficulty = new IntProperty();
    public final IntProperty MinTerrain = new IntProperty();
    public final IntProperty MaxTerrain = new IntProperty();
    public final IntProperty MinContainerSize = new IntProperty();
    public final IntProperty MaxContainerSize = new IntProperty();
    public final IntProperty MinRating = new IntProperty();
    public final IntProperty MaxRating = new IntProperty();

    public final IntProperty MinFavPoints = new IntProperty();
    public final IntProperty MaxFavPoints = new IntProperty();

    public boolean isHistory;

    final BooleanProperty[] cacheTypes = new BooleanProperty[CacheTypes.values().length];

    final int[] attributes = new int[Attributes.values().length];

    public LongArray GPXFilenameIds;

    public String filterName;
    public String filterGcCode;
    public String filterOwner;

    public LongArray Categories;

    /**
     * creates the FilterProperties with default values.
     * For default nothing is filtered!
     */
    public FilterProperties(String name) {
        this.name = name;
        initCreation();
    }

    /**
     * Copy constructor
     *
     * @param properties
     */
    public FilterProperties(String name, FilterProperties properties) {
        this.name = name;
        initCreation();
        this.set(properties);
    }


    /**
     * creates the FilterProperties from a serialization-String
     * an empty serialization-String filters nothing
     *
     * @param serialization
     */
    public FilterProperties(String name, String serialization) {

        if (serialization.length() == 0) {
            initCreation();
            this.name = (name == null || name.isEmpty()) ? "ALL" : name;
            return;
        }

        try {
            JsonValue json = new JsonReader().parse(serialization);
            this.name = json.getString("name", "?");
            if (this.name.equals("?")) {
                if (name != null && !name.isEmpty()) {
                    this.name = name;
                }
            }

            try {
                isHistory = json.getBoolean("isHistory");
            } catch (Exception e) {
                isHistory = false;
            }
            String caches = json.getString("caches");
            String[] parts = caches.split(SEPARATOR);
            int cnt = 0;
            Finds.set(Integer.parseInt(parts[cnt++]));
            NotAvailable.set(Integer.parseInt(parts[cnt++]));
            Archived.set(Integer.parseInt(parts[cnt++]));
            Own.set(Integer.parseInt(parts[cnt++]));
            ContainsTravelbugs.set(Integer.parseInt(parts[cnt++]));
            Favorites.set(Integer.parseInt(parts[cnt++]));
            HasUserData.set(Integer.parseInt(parts[cnt++]));
            ListingChanged.set(Integer.parseInt(parts[cnt++]));
            WithManualWaypoint.set(Integer.parseInt(parts[cnt++]));

            MinDifficulty.set((int) (Float.parseFloat(parts[cnt++]) * 2));
            MaxDifficulty.set((int) (Float.parseFloat(parts[cnt++]) * 2));
            MinTerrain.set((int) (Float.parseFloat(parts[cnt++]) * 2));
            MaxTerrain.set((int) (Float.parseFloat(parts[cnt++]) * 2));
            MinContainerSize.set((int) (Float.parseFloat(parts[cnt++])));
            MaxContainerSize.set((int) (Float.parseFloat(parts[cnt++])));
            MinRating.set((int) (Float.parseFloat(parts[cnt++]) * 2));
            MaxRating.set((int) (Float.parseFloat(parts[cnt++]) * 2));
            MinFavPoints.set(Integer.parseInt(parts[cnt++]));
            MaxFavPoints.set(Integer.parseInt(parts[cnt++]));

            if (parts.length == 19) {
                this.hasCorrectedCoordinates.set(0);
            } else {
                hasCorrectedCoordinates.set(Integer.parseInt(parts[cnt++]));
            }

            System.arraycopy(parseCacheTypes(json.getString("types")), 0, cacheTypes, 0, cacheTypes.length);

            String attributes = json.getString("attributes");
            parts = attributes.split(SEPARATOR);

            this.attributes[0] = 0; // gibts nicht
            int og = parts.length;
            if (parts.length == this.attributes.length) {
                og = parts.length - 1; // falls doch schon mal mit mehr gespeichert
            }
            for (int i = 0; i < (og); i++)
                this.attributes[i + 1] = Integer.parseInt(parts[i]);
            // aus älteren Versionen
            for (int i = og; i < this.attributes.length - 1; i++)
                this.attributes[i + 1] = 0;

            GPXFilenameIds = new LongArray();
            String gpxfilenames = json.getString("gpxfilenameids");
            parts = gpxfilenames.split(SEPARATOR);
            cnt = 0;
            if (parts.length > cnt) {
                String tempGPX = parts[cnt++];
                String[] partsGPX = tempGPX.split("\\" + GPXSEPARATOR);
                for (int i = 1; i < partsGPX.length; i++) {
                    GPXFilenameIds.add(Long.parseLong(partsGPX[i]));
                }
            }

            filterName = json.getString("filtername");
            filterGcCode = json.getString("filtergc");
            filterOwner = json.getString("filterowner");

            Categories = new LongArray();
            String filtercategories = json.getString("categories");
            if (filtercategories.length() > 0) {
                String[] partsGPX = filtercategories.split("\\" + GPXSEPARATOR);
                for (int i = 1; i < partsGPX.length; i++) {
                    // Log.info(log, "parts[" + i + "]=" + partsGPX[i]);
                    Categories.add(Long.parseLong(partsGPX[i]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initCreation() {
        Finds.set(0);
        NotAvailable.set(0);
        Archived.set(0);
        Own.set(0);
        ContainsTravelbugs.set(0);
        Favorites.set(0);
        ListingChanged.set(0);
        WithManualWaypoint.set(0);
        HasUserData.set(0);

        MinDifficulty.set(2);
        MaxDifficulty.set(10);
        MinTerrain.set(2);
        MaxTerrain.set(10);
        MinContainerSize.set(0);
        MaxContainerSize.set(6);
        MinRating.set(0);
        MaxRating.set(10);
        MinFavPoints.set(-1);
        MaxFavPoints.set(-1);

        this.hasCorrectedCoordinates.set(0);
        isHistory = false;

        int n = cacheTypes.length;
        while (n-- > 0) {
            cacheTypes[n] = new BooleanProperty(true);
        }

        Arrays.fill(attributes, 0);

        GPXFilenameIds = new LongArray();
        filterName = "";
        filterGcCode = "";
        filterOwner = "";

        Categories = new LongArray();
    }

    private BooleanProperty[] parseCacheTypes(String types) {
        String[] parts = types.split(SEPARATOR);
        final BooleanProperty[] result = new BooleanProperty[CacheTypes.values().length];

        for (int i = 0; i < result.length; i++) {
            result[i] = new BooleanProperty(Boolean.parseBoolean(parts[i]));
        }

        return result;
    }

    /**
     * True, wenn FilterProperties eine Filterung nach name, Gc-Code oder Owner enthält!
     *
     * @return
     */
    public boolean isExtendedFilter() {
        if (filterName.length() > 0)
            return true;

        if (filterGcCode.length() > 0)
            return true;

        if (filterOwner.length() > 0)
            return true;

        return false;
    }


    /**
     * Gibt den SQL Where String dieses Filters zurück
     *
     * @param userName Config.settings.GcLogin.getValue()
     * @return
     */
    public String getSqlWhere(String userName) {

        if (isHistory) {
            ArrayList<String> orParts = new ArrayList<String>();
            String[] gcCodes = CB.cacheHistory.split(",");
            for (int i = 0; i < gcCodes.length; i++) {
                String gcCode = gcCodes[i];
                if (gcCode.length() > 0) {
                    if (!orParts.contains(gcCode))
                        orParts.add("GcCode = '" + gcCode + "'");
                }
            }
            return join(" or ", orParts);
        } else {
            userName = userName.replace("'", "''");

            ArrayList<String> andParts = new ArrayList<String>();

            short bitstoreMust = 0;
            short bitstoreMustNot = 0;


            //TODO Bitwise equals [BooleanStore & MASK_FOUND != 0] for true
            //TODO Bitwise equals [BooleanStore & MASK_FOUND == 0] for false
            if (Finds.get() == 1)
                bitstoreMust = ImmutableCache.setMaskValue(ImmutableCache.MASK_FOUND, true, bitstoreMust);//andParts.add("Found=1");
            if (Finds.get() == -1)
                bitstoreMustNot = ImmutableCache.setMaskValue(ImmutableCache.MASK_FOUND, true, bitstoreMustNot);//andParts.add("(Found=0 or Found is null)");

            if (Favorites.get() == 1)
                bitstoreMust = ImmutableCache.setMaskValue(ImmutableCache.MASK_FAVORITE, true, bitstoreMust);
            if (Favorites.get() == -1)
                bitstoreMustNot = ImmutableCache.setMaskValue(ImmutableCache.MASK_FAVORITE, true, bitstoreMustNot);

            if (NotAvailable.get() == -1)
                bitstoreMust = ImmutableCache.setMaskValue(ImmutableCache.MASK_AVAILABLE, true, bitstoreMust);
            if (NotAvailable.get() == 1)
                bitstoreMustNot = ImmutableCache.setMaskValue(ImmutableCache.MASK_AVAILABLE, true, bitstoreMustNot);

            if (Archived.get() == 1)
                bitstoreMust = ImmutableCache.setMaskValue(ImmutableCache.MASK_ARCHIVED, true, bitstoreMust);
            if (Archived.get() == -1)
                bitstoreMustNot = ImmutableCache.setMaskValue(ImmutableCache.MASK_ARCHIVED, true, bitstoreMustNot);

            if (ListingChanged.get() == 1)
                bitstoreMust = ImmutableCache.setMaskValue(ImmutableCache.MASK_LISTING_CHANGED, true, bitstoreMust);
            if (ListingChanged.get() == -1)
                bitstoreMustNot = ImmutableCache.setMaskValue(ImmutableCache.MASK_LISTING_CHANGED, true, bitstoreMustNot);

            if (HasUserData.get() == 1)
                bitstoreMust = ImmutableCache.setMaskValue(ImmutableCache.MASK_HAS_USER_DATA, true, bitstoreMust);
            if (HasUserData.get() == -1)
                bitstoreMustNot = ImmutableCache.setMaskValue(ImmutableCache.MASK_HAS_USER_DATA, true, bitstoreMustNot);

            if (bitstoreMust > 0)
                andParts.add("BooleanStore & " + bitstoreMust + "= " + bitstoreMust);
            if (bitstoreMustNot > 0)
                andParts.add("~BooleanStore & " + bitstoreMustNot + "= " + bitstoreMustNot);


            if (Own.get() == 1)
                andParts.add("(Owner='" + userName + "')");
            if (Own.get() == -1)
                andParts.add("(not Owner='" + userName + "')");

            if (ContainsTravelbugs.get() == 1)
                andParts.add("NumTravelbugs > 0");
            if (ContainsTravelbugs.get() == -1)
                andParts.add("NumTravelbugs = 0");


            if (WithManualWaypoint.get() == 1)
                andParts.add(" ID in (select CacheId FROM Waypoints WHERE UserWaypoint = 1)");
            if (WithManualWaypoint.get() == -1)
                andParts.add(" NOT ID in (select CacheId FROM Waypoints WHERE UserWaypoint = 1)");


            if (MinDifficulty.get() / 2f > 1) andParts.add("Difficulty >= " + String.valueOf(MinDifficulty.get()));
            if (MaxDifficulty.get() / 2f < 5) andParts.add("Difficulty <= " + String.valueOf(MaxDifficulty.get()));
            if (MinTerrain.get() / 2f > 1) andParts.add("Terrain >= " + String.valueOf(MinTerrain.get()));
            if (MaxTerrain.get() / 2f < 5) andParts.add("Terrain <= " + String.valueOf(MaxTerrain.get()));
            if (MinContainerSize.get() > 0) andParts.add("Size >= " + String.valueOf(MinContainerSize.get()));
            if (MaxContainerSize.get() < 6) andParts.add("Size <= " + String.valueOf(MaxContainerSize.get()));
            if (MinRating.get() / 2f > 0)
                andParts.add("Rating >= " + String.valueOf((int) (MinRating.get() / 2f * 100)));
            if (MaxRating.get() / 2f < 5)
                andParts.add("Rating <= " + String.valueOf((int) (MaxRating.get() / 2f * 100)));

            if (MinFavPoints.get() >= 0) andParts.add("FavPoints >= " + String.valueOf(MinFavPoints.get()));
            if (MaxFavPoints.get() >= 0) andParts.add("FavPoints <= " + String.valueOf(MaxFavPoints.get()));


            String csvTypes = "";
            int count = 0;
            for (int i = 0; i < cacheTypes.length; i++) {
                if (cacheTypes[i].get()) {
                    csvTypes += String.valueOf(i) + ",";
                    count++;
                }

            }
            if (count < cacheTypes.length && csvTypes.length() > 0) {
                csvTypes = csvTypes.substring(0, csvTypes.length() - 1);
                andParts.add("Type in (" + csvTypes + ")");
            }

            for (int i = 1; i < attributes.length; i++) {
                if (attributes[i] != 0) {
                    if (i < 62) {
                        long shift = DLong.UL1 << (i);
                        if (attributes[i] == 1)
                            andParts.add("(AttributesPositive & " + shift + ") > 0");
                        else
                            andParts.add("(AttributesNegative &  " + shift + ") > 0");
                    } else {
                        long shift = DLong.UL1 << (i - 61);
                        if (attributes[i] == 1)
                            andParts.add("(AttributesPositiveHigh &  " + shift + ") > 0");
                        else
                            andParts.add("(AttributesNegativeHigh & " + shift + ") > 0");
                    }
                }
            }

            if (GPXFilenameIds.size != 0) {
                String s = "";
                for (long id : GPXFilenameIds.items) {
                    s += String.valueOf(id) + ",";
                }
                // s += "-1";
                if (s.length() > 0) {
                    andParts.add("GPXFilename_Id not in (" + s.substring(0, s.length() - 1) + ")");
                }
            }

            if (!filterName.equals("")) {
                andParts.add("name like '%" + filterName + "%'");
            }
            if (!filterGcCode.equals("")) {
                andParts.add("GcCode like '%" + filterGcCode + "%'");
            }
            if (!filterOwner.equals("")) {
                andParts.add("( PlacedBy like '%" + filterOwner + "%' or Owner like '%" + filterOwner + "%' )");
            }
            return join(" and ", andParts);
        }
    }

    public static String join(String separator, ArrayList<String> array) {
        String retString = "";

        int count = 0;
        for (String tmp : array) {
            retString += tmp;
            count++;
            if (count < array.size())
                retString += separator;
        }
        return retString;
    }

    /**
     * Filter miteinander vergleichen wobei Category Einstellungen ignoriert werden sollen
     *
     * @param filter
     * @return
     */
    public boolean equals(FilterProperties filter) {
        if (!Finds.isEquals(filter.Finds))
            return false;
        if (!NotAvailable.isEquals(filter.NotAvailable))
            return false;
        if (!Archived.isEquals(filter.Archived))
            return false;
        if (!Own.isEquals(filter.Own))
            return false;
        if (!ContainsTravelbugs.isEquals(filter.ContainsTravelbugs))
            return false;
        if (!Favorites.isEquals(filter.Favorites))
            return false;
        if (!ListingChanged.isEquals(filter.ListingChanged))
            return false;
        if (!WithManualWaypoint.isEquals(filter.WithManualWaypoint))
            return false;
        if (!HasUserData.isEquals(filter.HasUserData))
            return false;

        if (MinDifficulty.get() != filter.MinDifficulty.get())
            return false;
        if (MaxDifficulty.get() != filter.MaxDifficulty.get())
            return false;
        if (MinTerrain.get() != filter.MinTerrain.get())
            return false;
        if (MaxTerrain.get() != filter.MaxTerrain.get())
            return false;
        if (MinContainerSize.get() != filter.MinContainerSize.get())
            return false;
        if (MaxContainerSize.get() != filter.MaxContainerSize.get())
            return false;
        if (MinRating.get() != filter.MinRating.get())
            return false;
        if (MaxRating.get() != filter.MaxRating.get())
            return false;
        if (MinFavPoints.get() != filter.MinFavPoints.get())
            return false;
        if (MaxFavPoints.get() != filter.MaxFavPoints.get())
            return false;

        if (!hasCorrectedCoordinates.isEquals(filter.hasCorrectedCoordinates))
            return false;

        for (int i = 0; i < cacheTypes.length; i++) {
            if (filter.cacheTypes.length <= i)
                break;
            if (filter.cacheTypes[i] != this.cacheTypes[i])
                return false; // nicht gleich!!!
        }

        for (int i = 1; i < attributes.length; i++) {
            if (filter.attributes.length <= i)
                break;
            if (filter.attributes[i] != this.attributes[i])
                return false; // nicht gleich!!!
        }

        if (GPXFilenameIds.size != filter.GPXFilenameIds.size)
            return false;
        for (int i = 0, n = GPXFilenameIds.size; i < n; i++) {
            if (!filter.GPXFilenameIds.contains(GPXFilenameIds.items[i]))
                return false;
        }

        if (!filterOwner.equals(filter.filterOwner))
            return false;
        if (!filterGcCode.equals(filter.filterGcCode))
            return false;
        if (!filterName.equals(filter.filterName))
            return false;

        if (isHistory != filter.isHistory)
            return false;

        return true;
    }

    /**
     * @param abstractCache
     * @return
     */
    public boolean passed(AbstractCache abstractCache) {
        if (chkFilterBoolean(this.Finds, abstractCache.isFound()))
            return false;
        if (chkFilterBoolean(this.Own, abstractCache.ImTheOwner()))
            return false;
        if (chkFilterBoolean(this.NotAvailable, !abstractCache.isAvailable()))
            return false;
        if (chkFilterBoolean(this.Archived, abstractCache.isArchived()))
            return false;
        if (chkFilterBoolean(this.ContainsTravelbugs, abstractCache.getNumTravelbugs() > 0))
            return false;
        if (chkFilterBoolean(this.Favorites, abstractCache.isFavorite()))
            return false;
        if (chkFilterBoolean(this.ListingChanged, abstractCache.isListingChanged()))
            return false;
        if (chkFilterBoolean(this.HasUserData, abstractCache.isHasUserData()))
            return false;
        if (chkFilterBoolean(this.hasCorrectedCoordinates, abstractCache.hasCorrectedCoordinates()))
            return false;

        if (this.MinDifficulty.get() / 2f > abstractCache.getDifficulty()) return false;
        if (this.MaxDifficulty.get() / 2f < abstractCache.getDifficulty()) return false;
        if (this.MinTerrain.get() / 2f > abstractCache.getTerrain()) return false;
        if (this.MaxTerrain.get() / 2f < abstractCache.getTerrain()) return false;
        if (this.MinContainerSize.get() > abstractCache.getSize().ordinal()) return false;
        if (this.MaxContainerSize.get() < abstractCache.getSize().ordinal()) return false;
        if (this.MinRating.get() / 2f > abstractCache.getRating()) return false;
        if (this.MaxRating.get() / 2f < abstractCache.getRating()) return false;
        if (this.MinFavPoints.get() >= 0 && this.MinFavPoints.get() > abstractCache.getFavoritePoints()) return false;
        if (this.MaxFavPoints.get() >= 0 && this.MaxFavPoints.get() < abstractCache.getFavoritePoints()) return false;


        if (!this.cacheTypes[abstractCache.getType().ordinal()].get())
            return false;

        return true;
    }

    /**
     * @param propertyValue
     * @param found
     * @return
     */
    private boolean chkFilterBoolean(IntProperty propertyValue, boolean found) {
        // -1 = Cache.{attribute} == False
        //  0 = Cache.{attribute} == False|True
        //  1 = Cache.{attribute} == True

        if (propertyValue.get() != 0) {
            if (propertyValue.get() != (found ? 1 : -1))
                return true;
        }
        return false;
    }

    public FilterProperties copy() {
        return new FilterProperties(this.name, this);
    }

    public void set(FilterProperties properties) {
        this.name = properties.name;
        Finds.set(properties.Finds.get());
        NotAvailable.set(properties.NotAvailable.get());
        Archived.set(properties.Archived.get());
        Own.set(properties.Own.get());
        ContainsTravelbugs.set(properties.ContainsTravelbugs.get());
        Favorites.set(properties.Favorites.get());
        ListingChanged.set(properties.ListingChanged.get());
        WithManualWaypoint.set(properties.WithManualWaypoint.get());
        HasUserData.set(properties.HasUserData.get());

        MinDifficulty.set(properties.MinDifficulty.get());
        MaxDifficulty.set(properties.MaxDifficulty.get());
        MinTerrain.set(properties.MinTerrain.get());
        MaxTerrain.set(properties.MaxTerrain.get());
        MinContainerSize.set(properties.MinContainerSize.get());
        MaxContainerSize.set(properties.MaxContainerSize.get());
        MinRating.set(properties.MinRating.get());
        MaxRating.set(properties.MaxRating.get());

        MinFavPoints.set(properties.MinFavPoints.get());
        MaxFavPoints.set(properties.MaxFavPoints.get());

        this.hasCorrectedCoordinates.set(properties.hasCorrectedCoordinates.get());
        isHistory = properties.isHistory;

        System.arraycopy(properties.cacheTypes, 0, cacheTypes, 0, cacheTypes.length);
        System.arraycopy(properties.attributes, 0, attributes, 0, attributes.length);

        filterName = properties.filterName;
        filterGcCode = properties.filterGcCode;
        filterOwner = properties.filterOwner;

        Categories = new LongArray();
        GPXFilenameIds = new LongArray();
    }

    @Override
    public String toString() {
        return "Filter '" + this.name + "' SQL-WHERE: " + getSqlWhere("UserName");
    }

    /**
     * a String to save in the database
     *
     * @return
     */
    public String getJsonString() {
        String result = "";

        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(stringWriter);
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            json.setWriter(writer);
            json.writeObjectStart();

            // Filter Name
            json.writeValue("name", this.name);

            // add Cache Types
            String tmp = "";
            for (int i = 0; i < cacheTypes.length; i++) {
                if (i > 0)
                    tmp += SEPARATOR;
                tmp += String.valueOf(cacheTypes[i]);
            }
            json.writeValue("types", tmp);

            // add Cache properties
            json.writeValue("caches",
                    String.valueOf(Finds) + SEPARATOR + String.valueOf(NotAvailable) + SEPARATOR
                            + String.valueOf(Archived) + SEPARATOR + String.valueOf(Own) + SEPARATOR
                            + String.valueOf(ContainsTravelbugs) + SEPARATOR + String.valueOf(Favorites)
                            + SEPARATOR + String.valueOf(HasUserData) + SEPARATOR + String.valueOf(ListingChanged)
                            + SEPARATOR + String.valueOf(WithManualWaypoint) + SEPARATOR
                            + String.valueOf(MinDifficulty.get() / 2F) + SEPARATOR
                            + String.valueOf(MaxDifficulty.get() / 2F) + SEPARATOR
                            + String.valueOf(MinTerrain.get() / 2F) + SEPARATOR
                            + String.valueOf(MaxTerrain.get() / 2F) + SEPARATOR
                            + String.valueOf((float) MinContainerSize.get()) + SEPARATOR
                            + String.valueOf((float) MaxContainerSize.get()) + SEPARATOR
                            + String.valueOf(MinRating.get() / 2F) + SEPARATOR
                            + String.valueOf(MaxRating.get() / 2F) + SEPARATOR
                            + String.valueOf(MinFavPoints.get()) + SEPARATOR
                            + String.valueOf(MaxFavPoints.get()) + SEPARATOR
                            + String.valueOf(this.hasCorrectedCoordinates));

            // Filter GCCode
            json.writeValue("filtergc", filterGcCode);

            // GPX Filenames
            tmp = "";
            for (int i = 0; i <= GPXFilenameIds.size - 1; i++) {
                tmp += GPXSEPARATOR + String.valueOf(GPXFilenameIds.get(i));
            }
            json.writeValue("gpxfilenameids", tmp);

            // add Cache Attributes
            tmp = "";
            for (int i = 1; i < attributes.length; i++) {
                if (tmp.length() > 0)
                    tmp += SEPARATOR;
                tmp += String.valueOf(attributes[i]);
            }
            json.writeValue("attributes", tmp);

            // Filter name
            json.writeValue("filtername", filterName);

            // History
            json.writeValue("isHistory", isHistory);

            // Categories
            tmp = "";
            for (int i = 0, n = Categories.size; i < n; i++) {
                tmp += GPXSEPARATOR + Categories.items[i];
            }
            json.writeValue("categories", tmp);

            // Filter Owner
            json.writeValue("filterowner", filterOwner);

            json.writeObjectEnd();
            result = stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("JSON toString", e);
        }
        return result;
    }

    public void setName(String newName) {
        this.name = newName;
    }
}