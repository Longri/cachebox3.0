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
package de.longri.cachebox3.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.baseMap.AbstractManagedMapLayer;
import de.longri.cachebox3.gui.map.baseMap.AbstractVectorLayer;
import de.longri.cachebox3.gui.widgets.MapCompass;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings_Map;
import de.longri.cachebox3.utils.DEBUG_SB;
import de.longri.cachebox3.utils.EQUALS;
import de.longri.cachebox3.utils.MathUtils;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.event.Gesture;
import org.oscim.event.MotionEvent;
import org.oscim.layers.GroupLayer;
import org.oscim.layers.tile.TileLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.theme.ThemeFile;
import org.oscim.theme.ThemeLoader;
import org.oscim.theme.VtmThemes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 08.09.2016.
 */
public class CacheboxMapAdapter extends Map implements Map.UpdateListener {

    static {
        ThemeLoader.USE_ATLAS = true;
    }

    private final static Logger log = LoggerFactory.getLogger(CacheboxMapAdapter.class);
    private final MapCompass mapOrientationButton;

    public CacheboxMapAdapter(MapCompass mapOrientationButton) {
        super();
        events.bind(this); //register Update listener
        this.viewport().setMaxTilt(41f);
        this.mapOrientationButton = mapOrientationButton;
    }


    private boolean mRenderWait;
    private boolean mRenderRequest;
    private int width = Gdx.graphics.getWidth(), height = Gdx.graphics.getHeight(), xOffset, yOffset;
    private VectorTileLayer vectorTileLayer;
    private MapPosition lastAnimationEndPosition;
    private final AtomicBoolean block = new AtomicBoolean(false);
    private Runnable waitForAnimationEndRunnable;


    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setMapPosAndSize(float newX, float newY, float newWidth, float newHeight) {
        width = (int) newWidth;
        height = (int) newHeight;

        xOffset = (int) newX;
        yOffset = (int) (Gdx.graphics.getHeight() - newY - newHeight);
    }

    public int getX_Offset() {
        return xOffset;
    }

    public int getY_Offset() {
        return yOffset;
    }

    private final Runnable mRedrawCb = new Runnable() {
        @Override
        public void run() {
            prepareFrame();
            Gdx.graphics.requestRendering();
        }
    };

    @Override
    public void updateMap(boolean forceRender) {
        synchronized (mRedrawCb) {
            if (!mRenderRequest) {
                mRenderRequest = true;
                Gdx.app.postRunnable(mRedrawCb);
            } else {
                mRenderWait = true;
            }
        }
    }


    @Override
    public void render() {
        synchronized (mRedrawCb) {
            mRenderRequest = true;
            if (mClearMap)
                updateMap(false);
            else {
                //   Gdx.graphics.requestRendering();
            }
        }
    }

    @Override
    public boolean post(Runnable runnable) {
        Gdx.app.postRunnable(runnable);
        return true;
    }

    @Override
    public boolean postDelayed(final Runnable action, long delay) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                action.run();
            }
        }, delay / 1000f);
        return true;
    }

    @Override
    public void beginFrame() {
    }

    @Override
    public void doneFrame(boolean animate) {
        synchronized (mRedrawCb) {
            mRenderRequest = false;
            if (animate || mRenderWait) {
                mRenderWait = false;
                updateMap(true);
            }
        }
    }

    @Override
    public void onMapEvent(Event e, MapPosition mapPosition) {
        if (e == Map.ANIM_END) {
            log.trace("Map.ANIM_END     " + DEBUG_SB.toString(mapPosition));
            block.set(false);
            if (waitForAnimationEndRunnable != null) {
                waitForAnimationEndRunnable.run();
                waitForAnimationEndRunnable = null;
            }
        } else if (e == Map.POSITION_EVENT) {
            {// set yOffset at dependency of tilt
                if (mapPosition.getTilt() > 0) {
                    float offset = MathUtils.linearInterpolation
                            (viewport().getMinTilt(), viewport().getMaxTilt(), 0, 0.8f, mapPosition.getTilt());
                    viewport().setMapScreenCenter(offset);

                    float buttonTilt = MathUtils.linearInterpolation
                            (viewport().getMinTilt(), viewport().getMaxTilt(), 0, -60f, mapPosition.getTilt());
                    mapOrientationButton.setTilt(buttonTilt);
                } else {
                    viewport().setMapScreenCenter(0);
                    mapOrientationButton.setTilt(0);
                }
            }
        }
        // mostly handled at MapView
    }

    public boolean handleGesture(Gesture g, MotionEvent e) {
        this.updateMap(true);
        return super.handleGesture(g, e);
    }

    public void setNewBaseMap(final AbstractManagedMapLayer baseMap) {
        if (this.layers().size() > 1) this.layers().remove(1);
        TileLayer tileLayer;

        //remove alt BuildingLabelLayer
        for (int i = 0, n = this.layers().size(); i < n; i++) {
            if (this.layers().get(i) instanceof BuildingLabelLayer) {
                this.layers().remove(i);
                break;
            }
        }

        if (baseMap.isVector()) {

            if (vectorTileLayer == null) {
                vectorTileLayer = (VectorTileLayer) baseMap.getTileLayer(this);
            } else {
                vectorTileLayer.setTileSource(((AbstractVectorLayer) baseMap).getVectorTileSource());
            }
            tileLayer = this.setBaseMap(vectorTileLayer);
            this.setTheme(VtmThemes.OSMARENDER);

            ((AbstractList) this.layers()).add(2, new BuildingLabelLayer(this, vectorTileLayer));

        } else {
            tileLayer = this.setBaseMap(baseMap.getTileLayer(this));
        }

        tileLayer.getManager().update(mMapPosition.setX(mMapPosition.getX() + 0.00001));

        //force reload
        this.updateMap(true);

        CB.postAsync(new Runnable() {
            @Override
            public void run() {
                Settings_Map.CurrentMapLayer.setValue(new String[]{baseMap.name});
                Config.AcceptChanges();
            }
        });
    }


    private void block() {
        // block will release with animation end map event
        block.set(true);
    }

    public boolean isBlocked() {
        return block.get();
    }

    public void animateTo(final MapPosition mapPosition) {
        if (mapPosition == null || EQUALS.is(mapPosition, lastAnimationEndPosition)) return;

        if (isBlocked()) {
            log.trace("animation is blocked!");
            return;
        }
        lastAnimationEndPosition = mapPosition;
        block();
        this.updateMap(false);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                animator().animateTo(500, mapPosition);
                log.trace("animate to: {}", DEBUG_SB.toString(mapPosition));
                updateMap(true);
            }
        });

    }


    public void waitForAnimationEnd(Runnable runnable) {
        if (waitForAnimationEndRunnable != null) return;
        waitForAnimationEndRunnable = runnable;
    }

    public final class BuildingLabelLayer extends GroupLayer {
        public final BuildingLayer buildingLayer;

        public BuildingLabelLayer(Map map, VectorTileLayer vectorTileLayer) {
            super(map);
            this.buildingLayer = new BuildingLayer(map, vectorTileLayer);
            this.layers.add(this.buildingLayer);
            this.layers.add(new LabelLayer(map, vectorTileLayer));
        }
    }

    public void setTheme(ThemeFile themeFile) {

        if (CB.actThemeFile != null && EQUALS.is(CB.actThemeFile, themeFile)) {
            log.debug("set cached Theme");
        } else {
            log.debug("load new Theme");
            CB.actTheme = ThemeLoader.load(themeFile);
            CB.actThemeFile = themeFile;
        }
        setTheme(CB.actTheme, false);
    }
}

