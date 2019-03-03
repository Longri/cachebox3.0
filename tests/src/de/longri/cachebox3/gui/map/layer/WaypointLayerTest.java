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

import com.badlogic.gdx.backends.lwjgl.JUnitGdxTestApp;
import com.badlogic.gdx.scenes.scene2d.ui.MapWayPointItem;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.map.layer.renderer.WaypointLayerRenderer;
import de.longri.cachebox3.gui.skin.styles.MapWayPointItemStyle;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedWayPointChangedEvent;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oscim.awt.AwtGraphics;
import org.oscim.awt.DesktopRealSvgBitmap;
import org.oscim.backend.CanvasAdapter;
import org.oscim.renderer.atlas.TextureRegion;
import travis.EXCLUDE_FROM_TRAVIS;

import java.util.LinkedHashMap;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 10.02.2017.
 */
class WaypointLayerTest {

    static boolean exclude = EXCLUDE_FROM_TRAVIS.VALUE || EXCLUDE_FROM_TRAVIS.REPAIR;// TODO repair test

    @BeforeAll
    static void beforeAll() {
        if (exclude) return;
        // Initial JUnit GDX App
        new JUnitGdxTestApp("TestHashWriter");
    }

    /**
     * Will check if, after selection changed, the correct Symbol set!
     */
    @Test
    void selectedCacheChanged() {
        if (exclude) return;
        // create test Skin
        AwtGraphics.init();
        MapWayPointItemStyle styleSelectOverlay = new MapWayPointItemStyle();
        styleSelectOverlay.small = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(100, 100, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.small).name = "SmallOverlay";
        styleSelectOverlay.middle = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(200, 200, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.middle).name = "MiddleOverlay";
        styleSelectOverlay.large = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(300, 300, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.large).name = "LargeOverlay";
        MapWayPointItemStyle mapStar = new MapWayPointItemStyle();
        mapStar.small = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(100, 100, 0));
        ((DesktopRealSvgBitmap) mapStar.small).name = "SmallOverlay";
        mapStar.middle = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(200, 200, 0));
        ((DesktopRealSvgBitmap) mapStar.middle).name = "MiddleOverlay";
        mapStar.large = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(300, 300, 0));
        ((DesktopRealSvgBitmap) mapStar.large).name = "LargeOverlay";

        SvgSkin skin = new SvgSkin();
        skin.add("selectOverlay", styleSelectOverlay, MapWayPointItemStyle.class);
        skin.add("disabledOverlay", mapStar, MapWayPointItemStyle.class);
        skin.add("mapStar", mapStar, MapWayPointItemStyle.class);
        Database.Data = new Database(Database.DatabaseType.CacheBox3);
        if (VisUI.isLoaded()) VisUI.dispose();
        VisUI.load(skin);
        LinkedHashMap<Object, TextureRegion> textureRegionMap = MapView.createTextureAtlasRegions();
        WaypointLayer wpLayer = new WaypointLayer(null, null, textureRegionMap);
        MutableCache testCache1 = new MutableCache(0.1, 0, "Cache1", CacheTypes.Traditional, "GC1");
        MutableCache testCache2 = new MutableCache(0.2, 0, "Cache2", CacheTypes.Traditional, "GC2");
        MutableCache testCache3 = new MutableCache(0.3, 0, "ImmutableCache", CacheTypes.Traditional, "GC3");

        Database.Data.Query.add(testCache1);
        Database.Data.Query.add(testCache2);
        Database.Data.Query.add(testCache3);

        WaypointLayerRenderer renderer = (WaypointLayerRenderer) wpLayer.getRenderer();

        assertThat("itemList must empty", wpLayer.mItemList.size == 0);

        EventHandler.fire(new SelectedCacheChangedEvent(null));
        CacheListChangedEventList.Call();

        wait(500);// for CacheListChangedEvent is fired! (Will call in a separate Thread)

        assertThat("itemList must have 3 items", wpLayer.mItemList.size == 3);

        //check if no item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);
            assertThat("regions must have no overlay regions:", regions.length == 1);
        }

        EventHandler.fire(new SelectedCacheChangedEvent(testCache1));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache1.toString(), EventHandler.getSelectedCache().equals(testCache1));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(testCache1)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(testCache1)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
                assertThat("region must size large:", renderItemRegions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }


        EventHandler.fire(new SelectedCacheChangedEvent(testCache2));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache2.toString(), EventHandler.getSelectedCache().equals(testCache2));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(testCache2)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(testCache2)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
                assertThat("region must size large:", renderItemRegions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }

        EventHandler.fire(new SelectedCacheChangedEvent(testCache3));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache3.toString(), EventHandler.getSelectedCache().equals(testCache3));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(testCache3)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(testCache3)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
                assertThat("region must size large:", renderItemRegions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }

    }


    /**
     * Will check if, after selection changed, the correct Symbol set!
     */
    @Test
    void selectedWaypointChanged() {
        if (exclude) return;
        // create test Skin
        AwtGraphics.init();
        MapWayPointItemStyle styleSelectOverlay = new MapWayPointItemStyle();
        styleSelectOverlay.small = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(100, 100, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.small).name = "SmallOverlay";
        styleSelectOverlay.middle = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(200, 200, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.middle).name = "MiddleOverlay";
        styleSelectOverlay.large = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(300, 300, 0));
        ((DesktopRealSvgBitmap) styleSelectOverlay.large).name = "LargeOverlay";
        MapWayPointItemStyle mapStar = new MapWayPointItemStyle();
        mapStar.small = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(100, 100, 0));
        ((DesktopRealSvgBitmap) mapStar.small).name = "SmallOverlay";
        mapStar.middle = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(200, 200, 0));
        ((DesktopRealSvgBitmap) mapStar.middle).name = "MiddleOverlay";
        mapStar.large = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(300, 300, 0));
        ((DesktopRealSvgBitmap) mapStar.large).name = "LargeOverlay";
        MapWayPointItemStyle mapTrailhead = new MapWayPointItemStyle();
        mapTrailhead.small = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(100, 100, 0));
        ((DesktopRealSvgBitmap) mapTrailhead.small).name = "SmallOverlay";
        mapTrailhead.middle = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(200, 200, 0));
        ((DesktopRealSvgBitmap) mapTrailhead.middle).name = "MiddleOverlay";
        mapTrailhead.large = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(300, 300, 0));
        ((DesktopRealSvgBitmap) mapTrailhead.large).name = "LargeOverlay";
        MapWayPointItemStyle mapParkingArea = new MapWayPointItemStyle();
        mapParkingArea.small = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(100, 100, 0));
        ((DesktopRealSvgBitmap) mapParkingArea.small).name = "SmallOverlay";
        mapParkingArea.middle = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(200, 200, 0));
        ((DesktopRealSvgBitmap) mapParkingArea.middle).name = "MiddleOverlay";
        mapParkingArea.large = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(300, 300, 0));
        ((DesktopRealSvgBitmap) mapParkingArea.large).name = "LargeOverlay";
        MapWayPointItemStyle mapMultiQuestion = new MapWayPointItemStyle();
        mapMultiQuestion.small = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(100, 100, 0));
        ((DesktopRealSvgBitmap) mapMultiQuestion.small).name = "SmallOverlay";
        mapMultiQuestion.middle = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(200, 200, 0));
        ((DesktopRealSvgBitmap) mapMultiQuestion.middle).name = "MiddleOverlay";
        mapMultiQuestion.large = new DesktopRealSvgBitmap(CanvasAdapter.newBitmap(300, 300, 0));
        ((DesktopRealSvgBitmap) mapMultiQuestion.large).name = "LargeOverlay";

        MapWayPointItemStyle disabledOverlay = new MapWayPointItemStyle();

        SvgSkin skin = new SvgSkin();
        skin.add("selectOverlay", styleSelectOverlay, MapWayPointItemStyle.class);
        skin.add("disabledOverlay", disabledOverlay, MapWayPointItemStyle.class);
        skin.add("mapStar", mapStar, MapWayPointItemStyle.class);
        skin.add("mapTrailhead", mapTrailhead, MapWayPointItemStyle.class);
        skin.add("mapParkingArea", mapParkingArea, MapWayPointItemStyle.class);
        skin.add("mapMultiQuestion", mapMultiQuestion, MapWayPointItemStyle.class);
        Database.Data = new Database(Database.DatabaseType.CacheBox3);

        if (VisUI.isLoaded()) VisUI.dispose();
        VisUI.load(skin);
        LinkedHashMap<Object, TextureRegion> textureRegionMap = MapView.createTextureAtlasRegions();

        WaypointLayer wpLayer = new WaypointLayer(null, null, textureRegionMap);
        MutableCache testCache1 = new MutableCache(0.1, 0, "Cache1", CacheTypes.Traditional, "GC1");
        MutableCache testCache2 = new MutableCache(0.2, 0, "Cache2", CacheTypes.Traditional, "GC2");
        MutableCache testCache3 = new MutableCache(0.3, 0, "ImmutableCache", CacheTypes.Traditional, "GC3");


        ImmutableWaypoint wp1 = new ImmutableWaypoint("GCwp1", CacheTypes.Trailhead, 0, 0.1, testCache1.getId(), "wp1");
        testCache1.getWaypoints().add(wp1);

        ImmutableWaypoint wp2 = new ImmutableWaypoint("GCwp2", CacheTypes.ParkingArea, 0, 0.2, testCache2.getId(), "wp2");
        testCache2.getWaypoints().add(wp2);

        ImmutableWaypoint wp3 = new ImmutableWaypoint("GCwp3", CacheTypes.MultiQuestion, 0, 0.2, testCache2.getId(), "wp3");
        testCache2.getWaypoints().add(wp3);


        Database.Data.Query.add(testCache1);
        Database.Data.Query.add(testCache2);
        Database.Data.Query.add(testCache3);

        WaypointLayerRenderer renderer = (WaypointLayerRenderer) wpLayer.getRenderer();

        assertThat("itemList must empty", wpLayer.mItemList.size == 0);

        EventHandler.fire(new SelectedCacheChangedEvent(null));
        CacheListChangedEventList.Call();

        wait(500);// for CacheListChangedEvent is fired! (Will call in a separate Thread)

        assertThat("itemList must have 6 items", wpLayer.mItemList.size == 6);

        //check if no item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);
            assertThat("regions must have no overlay regions:", regions.length == 1);
        }

        EventHandler.fire(new SelectedWayPointChangedEvent(wp1));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)

        assertThat("Selected cache must:" + testCache1.toString(), EventHandler.getSelectedCache().equals(testCache1));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(wp1)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(wp1)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }


        EventHandler.fire(new SelectedCacheChangedEvent(testCache3));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache3.toString(), EventHandler.getSelectedCache().equals(testCache3));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(testCache3)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(testCache3)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
                assertThat("region must size large:", renderItemRegions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }

        EventHandler.fire(new SelectedWayPointChangedEvent(wp3));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache2.toString(), EventHandler.getSelectedCache().equals(testCache2));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(wp3)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(wp3)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }

        EventHandler.fire(new SelectedWayPointChangedEvent(wp2));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache2.toString(), EventHandler.getSelectedCache().equals(testCache2));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(wp2)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(wp2)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }

        EventHandler.fire(new SelectedCacheChangedEvent(testCache2));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache2.toString(), EventHandler.getSelectedCache().equals(testCache2));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(testCache2)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(testCache2)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
                assertThat("region must size large:", renderItemRegions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }


        EventHandler.fire(new SelectedCacheChangedEvent(testCache1));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)
        assertThat("Selected cache must:" + testCache1.toString(), EventHandler.getSelectedCache().equals(testCache1));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(testCache1)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(testCache1)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
                assertThat("region must size large:", renderItemRegions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", renderItemRegions.length == 1);
            }
        }

        EventHandler.fire(new SelectedWayPointChangedEvent(wp1));
        wait(500);// for SelectedCacheChangedEvent is fired! (Will call in a separate Thread)

        assertThat("Selected cache must:" + testCache1.toString(), EventHandler.getSelectedCache().equals(testCache1));

        //check if only the selected item has a Overlay
        for (MapWayPointItem item : wpLayer.mItemList) {
            TextureRegion[] regions = item.getMapSymbol(7);

            if (item.dataObject.equals(wp1)) {
                assertThat("regions must have a overlay regions:", regions.length == 2);
                assertThat("region must size large:", regions[1].rect.w == 300);
            } else {
                assertThat("regions must have no overlay regions:", regions.length == 1);
            }
        }
        for (WaypointLayerRenderer.InternalItem item : renderer.mItems) {
            TextureRegion[] renderItemRegions = item.item.getMapSymbol(7);

            if (item.item.dataObject.equals(wp1)) {
                assertThat("regions must have a overlay regions:", renderItemRegions.length == 2);
                assertThat("region must size large:", renderItemRegions[1].rect.w == 300);
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