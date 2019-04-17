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

    public final BooleanProperty[] cacheTypes = new BooleanProperty[CacheTypes.values().length];

    public final IntProperty[] attributes = new IntProperty[Attributes.values().length];

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

            //initial attribute properties
            int n = this.attributes.length;
            while (n-- > 0) {
                this.attributes[n] = new IntProperty();
            }

            this.attributes[0].set(0); // don't exist
            int og = parts.length;
            if (parts.length == this.attributes.length) {
                og = parts.length - 1; // falls doch schon mal mit mehr gespeichert
            }
            for (int i = 0; i < (og); i++)
                this.attributes[i + 1].set(Integer.parseInt(parts[i]));


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

        n = attributes.length;
        while (n-- > 0) {
            attributes[n] = new IntProperty();
        }

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

            if (Finds.getInt() == 1)
                bitstoreMust = MutableCache.setMaskValue(MutableCache.MASK_FOUND, true, bitstoreMust);//andParts.add("Found=1");
            if (Finds.getInt() == -1)
                bitstoreMustNot = MutableCache.setMaskValue(MutableCache.MASK_FOUND, true, bitstoreMustNot);//andParts.add("(Found=0 or Found is null)");

            if (Favorites.getInt() == 1)
                bitstoreMust = MutableCache.setMaskValue(MutableCache.MASK_FAVORITE, true, bitstoreMust);
            if (Favorites.getInt() == -1)
                bitstoreMustNot = MutableCache.setMaskValue(MutableCache.MASK_FAVORITE, true, bitstoreMustNot);

            if (NotAvailable.getInt() == -1)
                bitstoreMust = MutableCache.setMaskValue(MutableCache.MASK_AVAILABLE, true, bitstoreMust);
            if (NotAvailable.getInt() == 1)
                bitstoreMustNot = MutableCache.setMaskValue(MutableCache.MASK_AVAILABLE, true, bitstoreMustNot);

            if (Archived.getInt() == 1)
                bitstoreMust = MutableCache.setMaskValue(MutableCache.MASK_ARCHIVED, true, bitstoreMust);
            if (Archived.getInt() == -1)
                bitstoreMustNot = MutableCache.setMaskValue(MutableCache.MASK_ARCHIVED, true, bitstoreMustNot);

            if (ListingChanged.getInt() == 1)
                bitstoreMust = MutableCache.setMaskValue(MutableCache.MASK_LISTING_CHANGED, true, bitstoreMust);
            if (ListingChanged.getInt() == -1)
                bitstoreMustNot = MutableCache.setMaskValue(MutableCache.MASK_LISTING_CHANGED, true, bitstoreMustNot);

            if (HasUserData.getInt() == 1)
                bitstoreMust = MutableCache.setMaskValue(MutableCache.MASK_HAS_USER_DATA, true, bitstoreMust);
            if (HasUserData.getInt() == -1)
                bitstoreMustNot = MutableCache.setMaskValue(MutableCache.MASK_HAS_USER_DATA, true, bitstoreMustNot);

            if (bitstoreMust > 0)
                andParts.add("BooleanStore & " + bitstoreMust + "= " + bitstoreMust);
            if (bitstoreMustNot > 0)
                andParts.add("~BooleanStore & " + bitstoreMustNot + "= " + bitstoreMustNot);


            if (Own.getInt() == 1)
                andParts.add("(Owner='" + userName + "')");
            if (Own.getInt() == -1)
                andParts.add("(not Owner='" + userName + "')");

            if (ContainsTravelbugs.getInt() == 1)
                andParts.add("NumTravelbugs > 0");
            if (ContainsTravelbugs.getInt() == -1)
                andParts.add("NumTravelbugs = 0");


            if (WithManualWaypoint.getInt() == 1)
                andParts.add(" ID in (select CacheId FROM Waypoints WHERE UserWaypoint = 1)");
            if (WithManualWaypoint.getInt() == -1)
                andParts.add(" NOT ID in (select CacheId FROM Waypoints WHERE UserWaypoint = 1)");


            if (MinDifficulty.getInt() / 2f > 1)
                andParts.add("Difficulty >= " + String.valueOf(MinDifficulty.getInt()));
            if (MaxDifficulty.getInt() / 2f < 5)
                andParts.add("Difficulty <= " + String.valueOf(MaxDifficulty.getInt()));
            if (MinTerrain.getInt() / 2f > 1) andParts.add("Terrain >= " + String.valueOf(MinTerrain.getInt()));
            if (MaxTerrain.getInt() / 2f < 5) andParts.add("Terrain <= " + String.valueOf(MaxTerrain.getInt()));
            if (MinContainerSize.getInt() > 0) andParts.add("Size >= " + String.valueOf(MinContainerSize.getInt()));
            if (MaxContainerSize.getInt() < 6) andParts.add("Size <= " + String.valueOf(MaxContainerSize.getInt()));
            if (MinRating.getInt() / 2f > 0)
                andParts.add("Rating >= " + String.valueOf((int) (MinRating.getInt() / 2f * 100)));
            if (MaxRating.getInt() / 2f < 5)
                andParts.add("Rating <= " + String.valueOf((int) (MaxRating.getInt() / 2f * 100)));

            if (MinFavPoints.getInt() >= 0) andParts.add("FavPoints >= " + String.valueOf(MinFavPoints.getInt()));
            if (MaxFavPoints.getInt() >= 0) andParts.add("FavPoints <= " + String.valueOf(MaxFavPoints.getInt()));


            String csvTypes = "";
            int count = 0;
            for (int i = 0; i < cacheTypes.length; i++) {
                BooleanProperty property = cacheTypes[i];
                if (property != null && property.get()) {
                    csvTypes += String.valueOf(i) + ",";
                    count++;
                }

            }
            if (count < cacheTypes.length && csvTypes.length() > 0) {
                csvTypes = csvTypes.substring(0, csvTypes.length() - 1);
                andParts.add("Type in (" + csvTypes + ")");
            }


            boolean mustJoin = false;
            for (int i = 1; i < attributes.length; i++) {
                IntProperty attProperty = attributes[i];

                if (attProperty != null && attProperty.getInt() != 0) {
                    mustJoin = true;
                    if (i < 62) {
                        long shift = DLong.UL1 << (i);
                        if (attProperty.getInt() == 1)
                            andParts.add("(attr.AttributesPositive & " + shift + ") > 0");
                        else
                            andParts.add("(attr.AttributesNegative &  " + shift + ") > 0");
                    } else {
                        long shift = DLong.UL1 << (i - 61);
                        if (attProperty.getInt() == 1)
                            andParts.add("(attr.AttributesPositiveHigh &  " + shift + ") > 0");
                        else
                            andParts.add("(attr.AttributesNegativeHigh & " + shift + ") > 0");
                    }
                }
            }

            if (GPXFilenameIds != null && GPXFilenameIds.size != 0) {
                String s = "";
                for (long id : GPXFilenameIds.items) {
                    s += String.valueOf(id) + ",";
                }
                // s += "-1";
                if (s.length() > 0) {
                    andParts.add("GPXFilename_Id not in (" + s.substring(0, s.length() - 1) + ")");
                }
            }

            if (filterName != null && !filterName.equals("")) {
                andParts.add("name like '%" + filterName + "%'");
            }
            if (filterGcCode != null && !filterGcCode.equals("")) {
                andParts.add("GcCode like '%" + filterGcCode + "%'");
            }
            if (filterOwner != null && !filterOwner.equals("")) {
                andParts.add("( PlacedBy like '%" + filterOwner + "%' or Owner like '%" + filterOwner + "%' )");
            }

            String statement;
            if (mustJoin) {
                statement = "SELECT * FROM CacheCoreInfo core JOIN Attributes attr ON attr.Id = core.Id WHERE " + join(" and ", andParts);
            } else {
                if (andParts.size() == 0) {
                    statement = "SELECT * FROM CacheCoreInfo";
                } else {
                    statement = "SELECT * FROM CacheCoreInfo core WHERE " + join(" and ", andParts);
                }
            }
            return statement;
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

        if (MinDifficulty.getInt() != filter.MinDifficulty.getInt())
            return false;
        if (MaxDifficulty.getInt() != filter.MaxDifficulty.getInt())
            return false;
        if (MinTerrain.getInt() != filter.MinTerrain.getInt())
            return false;
        if (MaxTerrain.getInt() != filter.MaxTerrain.getInt())
            return false;
        if (MinContainerSize.getInt() != filter.MinContainerSize.getInt())
            return false;
        if (MaxContainerSize.getInt() != filter.MaxContainerSize.getInt())
            return false;
        if (MinRating.getInt() != filter.MinRating.getInt())
            return false;
        if (MaxRating.getInt() != filter.MaxRating.getInt())
            return false;
        if (MinFavPoints.getInt() != filter.MinFavPoints.getInt())
            return false;
        if (MaxFavPoints.getInt() != filter.MaxFavPoints.getInt())
            return false;

        if (!hasCorrectedCoordinates.isEquals(filter.hasCorrectedCoordinates))
            return false;

        for (int i = 0; i < cacheTypes.length; i++) {
            if (filter.cacheTypes.length <= i)
                break;

            BooleanProperty otherProperty = filter.cacheTypes[i];
            BooleanProperty thisProperty = this.cacheTypes[i];

            if ((otherProperty == null || thisProperty == null) && (thisProperty != null || otherProperty != null))
                return false;

            if (otherProperty.get() != thisProperty.get())
                return false;
        }

        for (int i = 1; i < attributes.length; i++) {
            if (filter.attributes.length <= i)
                break;
            if (filter.attributes[i].getInt() != this.attributes[i].getInt())
                return false;
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

        if (this.MinDifficulty.getInt() / 2f > abstractCache.getDifficulty()) return false;
        if (this.MaxDifficulty.getInt() / 2f < abstractCache.getDifficulty()) return false;
        if (this.MinTerrain.getInt() / 2f > abstractCache.getTerrain()) return false;
        if (this.MaxTerrain.getInt() / 2f < abstractCache.getTerrain()) return false;
        if (this.MinContainerSize.getInt() > abstractCache.getSize().ordinal()) return false;
        if (this.MaxContainerSize.getInt() < abstractCache.getSize().ordinal()) return false;
        if (this.MinRating.getInt() / 2f > abstractCache.getRating()) return false;
        if (this.MaxRating.getInt() / 2f < abstractCache.getRating()) return false;
        if (this.MinFavPoints.getInt() >= 0 && this.MinFavPoints.getInt() > abstractCache.getFavoritePoints())
            return false;
        if (this.MaxFavPoints.getInt() >= 0 && this.MaxFavPoints.getInt() < abstractCache.getFavoritePoints())
            return false;


        if (!this.cacheTypes[abstractCache.getType().ordinal()].get())
            return false;

        //todo passed Attributes

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

        if (propertyValue.getInt() != 0) {
            if (propertyValue.getInt() != (found ? 1 : -1))
                return true;
        }
        return false;
    }

    public FilterProperties copy() {
        return new FilterProperties(this.name, this);
    }

    public void set(FilterProperties properties) {
        this.name = properties.name;
        Finds.set(properties.Finds.getInt());
        NotAvailable.set(properties.NotAvailable.getInt());
        Archived.set(properties.Archived.getInt());
        Own.set(properties.Own.getInt());
        ContainsTravelbugs.set(properties.ContainsTravelbugs.getInt());
        Favorites.set(properties.Favorites.getInt());
        ListingChanged.set(properties.ListingChanged.getInt());
        WithManualWaypoint.set(properties.WithManualWaypoint.getInt());
        HasUserData.set(properties.HasUserData.getInt());

        MinDifficulty.set(properties.MinDifficulty.getInt());
        MaxDifficulty.set(properties.MaxDifficulty.getInt());
        MinTerrain.set(properties.MinTerrain.getInt());
        MaxTerrain.set(properties.MaxTerrain.getInt());
        MinContainerSize.set(properties.MinContainerSize.getInt());
        MaxContainerSize.set(properties.MaxContainerSize.getInt());
        MinRating.set(properties.MinRating.getInt());
        MaxRating.set(properties.MaxRating.getInt());

        MinFavPoints.set(properties.MinFavPoints.getInt());
        MaxFavPoints.set(properties.MaxFavPoints.getInt());

        this.hasCorrectedCoordinates.set(properties.hasCorrectedCoordinates.getInt());
        isHistory = properties.isHistory;

        int n = cacheTypes.length;
        while (n-- > 0) {
            cacheTypes[n].set(properties.cacheTypes[n].get());
        }

        n = attributes.length;
        while (n-- > 0) {
            attributes[n].set(properties.attributes[n].getInt());
        }

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
                            + String.valueOf(MinDifficulty.getInt() / 2F) + SEPARATOR
                            + String.valueOf(MaxDifficulty.getInt() / 2F) + SEPARATOR
                            + String.valueOf(MinTerrain.getInt() / 2F) + SEPARATOR
                            + String.valueOf(MaxTerrain.getInt() / 2F) + SEPARATOR
                            + String.valueOf((float) MinContainerSize.getInt()) + SEPARATOR
                            + String.valueOf((float) MaxContainerSize.getInt()) + SEPARATOR
                            + String.valueOf(MinRating.getInt() / 2F) + SEPARATOR
                            + String.valueOf(MaxRating.getInt() / 2F) + SEPARATOR
                            + String.valueOf(MinFavPoints.getInt()) + SEPARATOR
                            + String.valueOf(MaxFavPoints.getInt()) + SEPARATOR
                            + String.valueOf(this.hasCorrectedCoordinates));

            // Filter GCCode
            json.writeValue("filtergc", filterGcCode);

            // GPX Filenames
            tmp = "";
            if (GPXFilenameIds != null) {
                for (int i = 0; i <= GPXFilenameIds.size - 1; i++) {
                    tmp += GPXSEPARATOR + String.valueOf(GPXFilenameIds.get(i));
                }
            }
            json.writeValue("gpxfilenameids", tmp);

            // add Cache Attributes
            tmp = "";
            for (int i = 1; i < attributes.length; i++) {
                if (tmp.length() > 0)
                    tmp += SEPARATOR;
                tmp += String.valueOf(attributes[i].getInt());
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