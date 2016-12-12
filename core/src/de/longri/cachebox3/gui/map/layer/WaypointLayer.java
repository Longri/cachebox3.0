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
import com.badlogic.gdx.scenes.scene2d.ui.ScaledSvg;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheTypes;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
        markerSymbolHashMap.clear();
        mOnItemGestureListener=null;
    }

    @Override
    public void CacheListChangedEvent() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (Database.Data.Query) {

                    //clear item list
                    mItemList.clear();

                    //add WayPoint items
                    for (Cache cache : Database.Data.Query) {
                        double lat = cache.Latitude(), lon = cache.Longitude();
                        MarkerItem markerItem = new MarkerItem(lat + "/" + lon, "", new GeoPoint(lat, lon));
                        markerItem.setMarker(getMarkerSymbolByCache(cache));
                        mItemList.add(markerItem);
                    }
                    WaypointLayer.this.populate();
                }
            }
        });
        thread.start();

    }


    private final HashMap<String, MarkerSymbol> markerSymbolHashMap = new HashMap<String, MarkerSymbol>();


    private MarkerSymbol getMarkerSymbolByCache(Cache cache) {
        MarkerSymbol symbol = null;
        String symbolName = getMapIconName(cache);
        symbol = markerSymbolHashMap.get(symbolName);
        if (symbol == null) {
            symbol = getMarkerSymbol(symbolName);
            markerSymbolHashMap.put(symbolName, symbol);
        }
        return symbol;
    }


    private static String getMapIconName(Cache cache) {
        if (cache.ImTheOwner())
            return "star";
        else if (cache.isFound())
            return "mapFound";
        else if ((cache.Type == CacheTypes.Mystery) && cache.CorrectedCoordiantesOrMysterySolved())
            return "mapSolved";
        else if ((cache.Type == CacheTypes.Multi) && cache.HasStartWaypoint())
            return "mapMultiStartP"; // Multi with start point
        else if ((cache.Type == CacheTypes.Mystery) && cache.HasStartWaypoint())
            return "mapMysteryStartP"; // Mystery without Final but with start point
        else
            return "map" + cache.Type.name();

    }

    private static MarkerSymbol getMarkerSymbol(String name) {
        Skin skin = VisUI.getSkin();
        ScaledSvg scaledSvg = skin.get(name, ScaledSvg.class);
        FileHandle fileHandle = Gdx.files.internal(scaledSvg.path);
        Bitmap bitmap = null;
        try {
            bitmap = PlatformConnector.getSvg(fileHandle.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale);
        } catch (IOException e) {
            return null;
        }
        return new MarkerSymbol(bitmap, MarkerSymbol.HotspotPlace.CENTER, true);
    }
}
