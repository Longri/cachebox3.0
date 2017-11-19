/*
 * Copyright (C) 2016 team-cachebox.de
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

import java.util.Arrays;

public class FilterInstances {

    // All Caches 0
    public final static FilterProperties ALL = new FilterProperties();

    // All Caches to find 1
    public final static FilterProperties ACTIVE = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"," + //"
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + setCacheTypes(true) + "\"," + //
            "\"filtername\":\"\"" + //
            "}");

    // Quick Cache 2
    private final static String sQuickCacheTypes() {
        // true,false,false,true,true,-false,-false,-false,-false,-false,-false,true,false
        // Traditional,-Multi,-Mystery,Camera,Earth,-Event,-MegaEvent,-CITO,-Virtual,-Letterbox,-Wherigo,
        //-ReferencePoint,-Wikipedia,-Undefined,-MultiStage,-MultiQuestion, -Trailhead,-ParkingArea,-Final,-Cache,-MyParking, Munzee,-Giga,
        boolean[] mCacheTypes = new boolean[CacheTypes.values().length];
        Arrays.fill(mCacheTypes, false);
        mCacheTypes[CacheTypes.Traditional.ordinal()] = true;
        mCacheTypes[CacheTypes.Camera.ordinal()] = true;
        mCacheTypes[CacheTypes.Earth.ordinal()] = true;
        String tmp = String.valueOf(mCacheTypes[0]);
        for (int i = 1; i < mCacheTypes.length; i++) {
            tmp = tmp + "," + String.valueOf(mCacheTypes[i]);
        }
        return tmp;
    }

    public final static FilterProperties QUICK = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,2.5,1.0,2.5,0.0,4.0,0.0,5.0,0\"," + //
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + sQuickCacheTypes() + "\"," + //
            "\"filtername\":\"\"" + //
            "}");

    // Quick Cache 2
    private final static String sBEGINNERCacheTypes() {
        // true,false,false,true,true,-false,-false,-false,-false,-false,-false,true,false
        // Traditional,-Multi,-Mystery,Camera,Earth,-Event,-MegaEvent,-CITO,-Virtual,-Letterbox,-Wherigo,
        //-ReferencePoint,-Wikipedia,-Undefined,-MultiStage,-MultiQuestion, -Trailhead,-ParkingArea,-Final,-Cache,-MyParking, Munzee,-Giga,
        boolean[] mCacheTypes = new boolean[CacheTypes.values().length];
        Arrays.fill(mCacheTypes, false);
        mCacheTypes[CacheTypes.Traditional.ordinal()] = true;
        String tmp = String.valueOf(mCacheTypes[0]);
        for (int i = 1; i < mCacheTypes.length; i++) {
            tmp = tmp + "," + String.valueOf(mCacheTypes[i]);
        }
        return tmp;
    }

    public final static FilterProperties BEGINNER = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"-1,-1,-1,-1,0,0,0,0,0,1.0,2.0,1.0,2.0,2.0,4.0,0.0,5.0,0\"," + //
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + sBEGINNERCacheTypes() + "\"," + //
            "\"filtername\":\"\"" + //
            "}");

    // Fetch some Travelbugs 3
    public final static FilterProperties WITHTB = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"0,-1,-1,0,1,0,0,0,0,1.0,3.0,1.0,3.0,0.0,4.0,0.0,5.0,0\"," + //
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + sTBsCacheTypes() + "\"," + //
            "\"filtername\":\"\"" + //
            "}");

    // Drop off Travelbugs 4
    private final static String sTBsCacheTypes() {
        boolean[] mCacheTypes = new boolean[CacheTypes.values().length];
        Arrays.fill(mCacheTypes, false);
        mCacheTypes[CacheTypes.Traditional.ordinal()] = true;
        mCacheTypes[CacheTypes.CITO.ordinal()] = true;
        mCacheTypes[CacheTypes.Event.ordinal()] = true;
        mCacheTypes[CacheTypes.Giga.ordinal()] = true;
        mCacheTypes[CacheTypes.Letterbox.ordinal()] = true;
        mCacheTypes[CacheTypes.MegaEvent.ordinal()] = true;
        mCacheTypes[CacheTypes.Multi.ordinal()] = true;
        mCacheTypes[CacheTypes.Mystery.ordinal()] = true;
        mCacheTypes[CacheTypes.Wherigo.ordinal()] = true;
        String tmp = String.valueOf(mCacheTypes[0]);
        for (int i = 1; i < mCacheTypes.length; i++) {
            tmp = tmp + "," + String.valueOf(mCacheTypes[i]);
        }
        return tmp;
    }

    public final static FilterProperties DROPTB = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"0,-1,-1,0,0,0,0,0,0,1.0,3.0,1.0,3.0,2.0,4.0,0.0,5.0,0\"," + //
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + sTBsCacheTypes() + "\"," + //
            "\"filtername\":\"\"" + //
            "}");

    // Highlights 5
    public final static FilterProperties HIGHLIGHTS = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"-1,-1,-1,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,3.5,5.0\"," + //
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + setCacheTypes(true) + "\"," + //
            "\"filtername\":\"\"" + //
            "}");

    // Favoriten
    public final static FilterProperties FAVORITES = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"0,0,0,0,0,1,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"," + //
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + setCacheTypes(true) + "\"," + //
            "\"filtername\":\"\"" + //
            "}");

    // prepare to archive
    public final static FilterProperties TOARCHIVE = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"0,0,-1,-1,0,-1,-1,-1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"," + //
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + setCacheTypes(true) + "\"," + //
            "\"filtername\":\"\"" + //
            "}");

    // Listing Changed
    public final static FilterProperties LISTINGCHANGED = new FilterProperties("{" + //
            "\"gpxfilenameids\":\"\"," + //
            "\"caches\":\"0,0,0,0,0,0,0,1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,0\"," + //
            "\"filtergc\":\"\"," + //
            "\"filterowner\":\"\"," + //
            "\"categories\":\"\"," + //
            "\"attributes\":\"" + setAttributes() + "\"," + //
            "\"types\":\"" + setCacheTypes(true) + "\"," + //
            "\"filtername\":\"\"" + //
            "}");
    public static FilterProperties HISTORY = new FilterProperties(); // == ALL, isHistory wird vor Verwendung gesetzt daher nicht final
    public static int hasCorrectedCoordinates = 0;

    private final static String setCacheTypes(boolean with) {
        String result = "";
        for (int i = 0; i < CacheTypes.values().length; i++) {
            if (i > 0)
                result = result + "," + with;
            else
                result = result + with;
        }
        return result;
    }

    private final static String setAttributes() {
        String result = "0";
        for (int i = 1; i < Attributes.values().length; i++) {
            result = result + ",0";
        }
        return result;
    }

}
