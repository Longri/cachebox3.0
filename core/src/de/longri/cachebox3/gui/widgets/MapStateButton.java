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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.map.MapState;
import de.longri.cachebox3.settings.Config;

/**
 * Created by Longri on 01.10.16.
 */
public class MapStateButton extends Widget implements Disposable {

    public interface StateChangedListener {
        public void stateChanged(MapState state);
    }

    private MapStateButtonStyle style;
    private MapState state = MapState.FREE;
    private final ClickListener clickListener;
    private final ActorGestureListener gestureListener;
    private final StateChangedListener stateChangedListener;
    private final int mapStateLength = MapState.values().length;
    private boolean isLongPressed = false;


    public MapStateButton(StateChangedListener stateChangedListener) {
        this.style = VisUI.getSkin().get("default", MapStateButtonStyle.class);
        if (style.stateCar == null || style.stateFree == null || style.stateLock == null
                || style.stateWaypoint == null || style.stateGps == null) {
            throw new RuntimeException("MapStateButtonStyle drawables can not be NULL");
        }
        clickListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (isLongPressed) {
                    isLongPressed = false;
                    return;
                }
                int intState = state.ordinal();
                intState++;
                if (intState > mapStateLength - 2) {// last state is Mapstate.Car. Activated with long click
                    intState = 0;
                }
                setState(MapState.fromOrdinal(intState));
            }
        };

        gestureListener = new ActorGestureListener() {
            @Override
            public boolean longPress(Actor actor, float x, float y) {
                setState(MapState.CAR, true);
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

    public void setState(MapState state) {
        setState(state, false);
    }

    private void setState(MapState state, boolean programmatic) {
        this.state = state;
        if (this.stateChangedListener != null) this.stateChangedListener.stateChanged(state);
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
        switch (state) {
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

    public MapState getState() {
        return this.state;
    }

    public void dispose() {
        style = null;
        state = null;

        //remove the listener
        this.removeListener(clickListener);
        this.removeListener(gestureListener);
    }


    public static class MapStateButtonStyle {
        public Drawable stateWaypoint, stateGps, stateFree, stateLock, stateCar, pressedOverdraw;
    }
}
