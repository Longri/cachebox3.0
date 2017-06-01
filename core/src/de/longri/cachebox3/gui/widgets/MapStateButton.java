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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DoubleClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.settings.Config;
import org.oscim.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 01.10.16.
 */
public class MapStateButton extends Widget implements Disposable {

    private final static Logger log = LoggerFactory.getLogger(MapStateButton.class);

    public interface StateChangedListener {
        void stateChanged(MapMode mapMode, MapMode lastMapMode, Event event);
    }

    private MapStateButtonStyle style;
    private final DoubleClickListener clickListener;
    private final ActorGestureListener gestureListener;
    private final StateChangedListener stateChangedListener;
    private final int mapStateLength = MapMode.values().length;
    private boolean isLongPressed = false;


    public MapStateButton(StateChangedListener stateChangedListener) {
        this.style = VisUI.getSkin().get("default", MapStateButtonStyle.class);
        if (style.stateCar == null || style.stateFree == null || style.stateLock == null
                || style.stateWaypoint == null || style.stateGps == null) {
            throw new RuntimeException("MapStateButtonStyle drawables can not be NULL");
        }
        clickListener = new DoubleClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                log.debug("button clicked" + event);
                if (isLongPressed) {
                    isLongPressed = false;
                    return;
                }
                int intState = CB.mapMode.ordinal();
                intState++;
                if (intState > mapStateLength - 3) {// last mapMode is Mapstate.Lock. Activated with double click
                    intState = 0;
                }
                setMapMode(MapMode.fromOrdinal(intState), new Event());
            }

            @Override
            public void doubleClicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                log.debug("button double clicked" + event);
                setMapMode(MapMode.LOCK, false, new Event());
            }
        };

        gestureListener = new ActorGestureListener() {
            @Override
            public boolean longPress(Actor actor, float x, float y) {
                log.debug("button long clicked");
                setMapMode(MapMode.CAR, false, new Event());
                isLongPressed = true;
                return true;
            }
        };

        gestureListener.getGestureDetector().setLongPressSeconds((float) (Config.LongClicktime.getValue() / 1000f));


        this.addListener(gestureListener);
        this.addListener(clickListener);
        this.setTouchable(Touchable.enabled);


        this.stateChangedListener = stateChangedListener;
        setSize(getPrefWidth(), getPrefHeight());

    }

    public void setMapMode(MapMode mapMode, Event event) {
        setMapMode(mapMode, false, event);
    }

    public void setMapMode(MapMode mapMode, boolean programmatic, Event event) {
        MapMode lastMode = CB.mapMode;
        CB.mapMode = mapMode;
        log.debug("Set to Mode: {} from last Mode {} / fireEvet:{}", mapMode, lastMode, !programmatic);
        if (!programmatic && this.stateChangedListener != null)
            this.stateChangedListener.stateChanged(mapMode, lastMode, event);
        Gdx.graphics.requestRendering();
    }


    @Override
    public float getPrefWidth() {
        return style.stateFree.getMinWidth();
    }

    @Override
    public float getPrefHeight() {
        return style.stateFree.getMinHeight();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        boolean isPressed = isPressed();

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        switch (CB.mapMode) {
            case FREE:
                style.stateFree.draw(batch, getX(), getY(), getWidth(), getHeight());
                break;
            case GPS:
                style.stateGps.draw(batch, getX(), getY(), getWidth(), getHeight());
                break;
            case WP:
                style.stateWaypoint.draw(batch, getX(), getY(), getWidth(), getHeight());
                break;
            case LOCK:
                style.stateLock.draw(batch, getX(), getY(), getWidth(), getHeight());
                break;
            case CAR:
                style.stateCar.draw(batch, getX(), getY(), getWidth(), getHeight());
                break;
        }

        if (isPressed && !isLongPressed && style.pressedOverdraw != null) {
            style.pressedOverdraw.draw(batch, getX(), getY(), getWidth(), getHeight());
        }

        Stage stage = getStage();
        if (stage != null && stage.getActionsRequestRendering() && isPressed != clickListener.isPressed())
            Gdx.graphics.requestRendering();

    }

    public boolean isPressed() {
        return clickListener.isVisualPressed();
    }

    public void dispose() {
        style = null;
        //remove the listener
        this.removeListener(clickListener);
        this.removeListener(gestureListener);
    }


    public static class MapStateButtonStyle {
        public Drawable stateWaypoint, stateGps, stateFree, stateLock, stateCar, pressedOverdraw;
    }
}
