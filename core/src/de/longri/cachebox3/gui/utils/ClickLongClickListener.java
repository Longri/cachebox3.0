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
package de.longri.cachebox3.gui.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import de.longri.cachebox3.settings.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchDragged;

/**
 * Created by Longri on 19.05.2017.
 */
public abstract class ClickLongClickListener extends ActorGestureListener {

    private static final Logger log = LoggerFactory.getLogger(ClickLongClickListener.class);

    public ClickLongClickListener() {
        super();
        this.getGestureDetector().setLongPressSeconds((Config.LongClicktime.getValue() / 1000f));
    }

    public abstract boolean clicked(InputEvent event, float x, float y);

    public abstract boolean longClicked(Actor actor, float x, float y);

    private final AtomicBoolean moved = new AtomicBoolean(false);
    private final AtomicBoolean down = new AtomicBoolean(false);
    private float touchX, touchY;

    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
        moved.set(false);
        touchX = event.getStageX();
        touchY = event.getStageY();
        down.set(true);
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        down.set(false);
    }

    private boolean canLongPressEventCalled() {
        if (!Gdx.input.isTouched()) return false;
        return !moved.get() && down.get();
    }

    public boolean longPress(Actor actor, float x, float y) {
        return longPress(actor, x, y, false);
    }

    public boolean longPress(Actor actor, float x, float y, boolean force) {
        if (force || canLongPressEventCalled())
            return longClicked(actor, x, y);
        else
            return true;
    }

    public void tap(InputEvent event, float x, float y, int count, int button) {
        if (!moved.get())
            clicked(event, x, y);
    }

    public void fling(InputEvent event, float velocityX, float velocityY, int button) {
        moved.set(true);
    }

    /**
     * The delta is the difference in stage coordinates since the last pan.
     */
    public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
        moved.set(true);
    }

    public boolean handle(Event e) {
        boolean ret = super.handle(e);
        if (!(e instanceof InputEvent)) return false;
        InputEvent event = (InputEvent) e;

        if (event.getType() == touchDragged) {

            float distance = Math.abs(event.getStageX() - touchX) + Math.abs(event.getStageY() - touchY);
//            log.debug("DragDistance {}", distance);
            if (distance > 50) moved.set(true);
        }

        return true;
    }
}
