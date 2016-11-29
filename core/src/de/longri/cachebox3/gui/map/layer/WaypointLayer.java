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
package de.longri.cachebox3.gui.map.layer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Longri on 27.11.16.
 */
public class WaypointLayer extends ItemizedLayer<MarkerItem> implements CacheListChangedEventListener, Disposable {

    private static final MarkerSymbol defaultMarker = null;


    public WaypointLayer(Map map) {
        super(map, new ArrayList<MarkerItem>(), defaultMarker, null);
        mOnItemGestureListener = gestureListener;

        //register as cacheListChanged eventListener
        CacheListChangedEventList.Add(this);

        CacheListChangedEvent();
    }

    private final OnItemGestureListener<MarkerItem> gestureListener = new OnItemGestureListener<MarkerItem>() {
        @Override
        public boolean onItemSingleTapUp(int index, MarkerItem item) {
            return false;
        }

        @Override
        public boolean onItemLongPress(int index, MarkerItem item) {
            return false;
        }
    };

    @Override
    public void dispose() {
        CacheListChangedEventList.Remove(this);

    }

    @Override
    public void CacheListChangedEvent() {

        //clear item list
        mItemList.clear();

        //add WayPoint items
        for (Cache cache : Database.Data.Query) {

            double lat = cache.Latitude(), lon = cache.Longitude();
            MarkerItem markerItem = new MarkerItem(lat + "/" + lon, "", new GeoPoint(lat, lon));
            markerItem.setMarker(getMarkerSymbolByCache(cache));
            mItemList.add(markerItem);
        }
        this.populate();

    }

    private MarkerSymbol getMarkerSymbolByCache(Cache cache) {

        float scale = 0.35f;
        FileHandle fileHandle = Gdx.files.internal("skins/day/svg/cache-icon.svg");

        Bitmap bitmap = null;
        try {
            bitmap = PlatformConnector.getSvg(fileHandle.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scale);
        } catch (IOException e) {
            return null;
        }

        return new MarkerSymbol(bitmap, MarkerSymbol.HotspotPlace.CENTER, true);
    }
}
