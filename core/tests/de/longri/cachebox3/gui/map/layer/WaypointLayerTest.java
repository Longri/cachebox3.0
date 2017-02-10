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
package de.longri.cachebox3.gui.map.layer;

import com.badlogic.gdx.scenes.scene2d.ui.MapWayPointItem;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.map.layer.cluster.ClusterRenderer;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheList;
import de.longri.cachebox3.types.CacheTypes;
import org.junit.jupiter.api.Test;
import org.oscim.awt.AwtGraphics;
import org.oscim.awt.DesktopRealSvgBitmap;
import org.oscim.backend.CanvasAdapter;
import org.oscim.renderer.atlas.TextureRegion;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 10.02.2017.
 */
class WaypointLayerTest {

    /**
     * Will check if, after selection changed, the correct Symbol set!
     */
    @Test
    void selectedCacheChanged() {

        // create test Skin
        AwtGraphics.init();
        MapWayPointItemStyle styleSelectOverlay = new MapWayPointItemStyle();
        styleSelectOverlay.small = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(100, 100, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.small).name = "SmallOverlay";
        styleSelectOverlay.middle = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(200, 200, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.middle).name = "MiddleOverlay";
        styleSelectOverlay.large = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(300, 300, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.large).name = "LargeOverlay";
        MapWayPointItemStyle disabledOverlay = new MapWayPointItemStyle();
        MapWayPointItemStyle mapStarOverlay = new MapWayPointItemStyle();
        SvgSkin skin = new SvgSkin();
        skin.add("selectOverlay", styleSelectOverlay, MapWayPointItemStyle.class);
        skin.add("disabledOverlay", disabledOverlay, MapWayPointItemStyle.class);
        skin.add("mapStar", mapStarOverlay, MapWayPointItemStyle.class);
        Database.Data = new Database(Database.DatabaseType.CacheBox);
        Database.Data.Query = new CacheList();
        VisUI.load(skin);

        WaypointLayer wpLayer = new WaypointLayer(null);
        Cache testCache1 = new Cache(0.1, 0, "Cache1", CacheTypes.Traditional, "GC1");
        Cache testCache2 = new Cache(0.2, 0, "Cache2", CacheTypes.Traditional, "GC2");
        Cache testCache3 = new Cache(0.3, 0, "Cache3", CacheTypes.Traditional, "GC3");

        Database.Data.Query.add(testCache1);
        Database.Data.Query.add(testCache2);
        Database.Data.Query.add(testCache3);

        ClusterRenderer renderer = (ClusterRenderer) wpLayer.getRenderer();

        assertThat("itemList must empty", wpLayer.mItemList.size() == 0);

        CacheListChangedEventList.Call();

        wait(500);// for CacheListChangedEvent is fired! (Will call in a separate Thread)

        assertThat("itemList must have 3 items", wpLayer.mItemList.size() == 3);

        //check if no item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(17);
            assertThat("regions must have no overlay regions:", regions.length == 1);
        }

        CB.setSelectedCache(testCache1);
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache1.toString(), CB.getSelectedCache().equals(testCache1));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(17);

            if (item.dataObject.equals(testCache1)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (ClusterRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(17);

            if (item.item.dataObject.equals(testCache1)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }


        CB.setSelectedCache(testCache2);
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache2.toString(), CB.getSelectedCache().equals(testCache2));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(17);

            if (item.dataObject.equals(testCache2)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (ClusterRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(17);

            if (item.item.dataObject.equals(testCache2)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }

        CB.setSelectedCache(testCache3);
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache3.toString(), CB.getSelectedCache().equals(testCache3));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(17);

            if (item.dataObject.equals(testCache3)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (ClusterRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(17);

            if (item.item.dataObject.equals(testCache3)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }

    }

    private void wait(int length) {
        try {
            Thread.sleep(length);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}