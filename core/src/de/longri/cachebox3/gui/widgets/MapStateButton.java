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
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.map.MapMode;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.translation.Translation;
import org.oscim.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 01.10.16.
 */
public class MapStateButton extends SelectBox<MapMode> implements Disposable {

    private final static Logger log = LoggerFactory.getLogger(MapStateButton.class);

    public interface StateChangedListener {
        void stateChanged(MapMode mapMode, MapMode lastMapMode, Event event);
    }

    private final MapStateButtonStyle style;
    private final StateChangedListener stateChangedListener;
    private boolean isLongPressed = false;


    public MapStateButton(StateChangedListener stateChangedListener) {
        this.style = VisUI.getSkin().get("default", MapStateButtonStyle.class);

        this.stateChangedListener = stateChangedListener;
        if (style.stateCar == null || style.stateFree == null || style.stateLock == null
                || style.stateWaypoint == null || style.stateGps == null) {
           return;
        }

        // add values
        Array<MapMode> itemList = new Array<>();
        itemList.add(MapMode.FREE);
        itemList.add(MapMode.GPS);
        itemList.add(MapMode.WP);
        itemList.add(MapMode.LOCK);
        itemList.add(MapMode.CAR);

        this.set(itemList);
        this.setTouchable(Touchable.enabled);
        setSize(getPrefWidth(), getPrefHeight());
        this.setSelectTitle(Translation.get("selectMapMode"));

    }

    @Override
    public Menu getMenu() {
        return super.getMenu();
    }

    @Override
    public void select(MapMode item) {
        super.select(item);
        setMapMode(this.getSelected(), false, new Event());
    }

    public void setMapMode(MapMode mapMode, Event event) {
        setMapMode(mapMode, false, event);
    }

    public void setMapMode(MapMode mapMode, boolean programmatic, Event event) {
        MapMode lastMode = CB.lastMapState.getMapMode();
        log.debug("Set to Mode: {} from last Mode {} / fireEvet:{}", mapMode, lastMode, !programmatic);
        if (!programmatic && this.stateChangedListener != null)
            this.stateChangedListener.stateChanged(mapMode, lastMode, event);
        super.select(mapMode);
        Gdx.graphics.requestRendering();
    }


    @Override
    public float getPrefWidth() {
        if (style == null) return 0;
        return style.stateFree.getMinWidth();
    }

    @Override
    public float getPrefHeight() {
        if (style == null) return 0;
        return style.stateFree.getMinHeight();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        boolean isPressed = isPressed();

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        switch ( this.getSelected()) {
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
    }


    @Override
    public void dispose() {

    }


    public static class MapStateButtonStyle {
        public Drawable stateWaypoint, stateGps, stateFree, stateLock, stateCar, pressedOverdraw;
    }
}
